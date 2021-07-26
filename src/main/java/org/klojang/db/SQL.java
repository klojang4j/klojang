package org.klojang.db;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import org.klojang.render.Page;
import org.klojang.render.RenderSession;
import org.klojang.template.Template;
import org.klojang.x.db.ps.BeanBinder;
import org.klojang.x.db.ps.MapBinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import nl.naturalis.common.Tuple;
import nl.naturalis.common.check.Check;
import nl.naturalis.common.collection.IntList;
import static nl.naturalis.common.ObjectMethods.ifNull;
import static nl.naturalis.common.check.CommonChecks.illegalState;
import static nl.naturalis.common.check.CommonChecks.no;
import static nl.naturalis.common.check.CommonChecks.notNull;

/**
 * A container for a single SQL query. The SQL query is assumed to be parametrized using named
 * parameters. This class functions as a factory for {@link SQLQuery}, {@link SQLInsert} and {@link
 * SQLUpdate} instances. If the query contains a lot of parameters and it is going to be executed
 * often, storing the {@code SQL} instance into a static final variable (e.g. in your DAO class) may
 * improve performance.
 *
 * @author Ayco Holleman
 */
public class SQL {

  private static final Logger LOG = LoggerFactory.getLogger(SQL.class);

  private static final String ERR_LOCKED =
      "An SQLQuery, SQLInsert or SQLUpdate is still active. "
          + "Did you forget to call close() on it?";
  private static final String ERR_NO_JDBC_SQL =
      "No valid JDBC SQL has been generated yet. "
          + "Call prepareQuery/prepareInsert/prepareUpdate first";

  public static SQL create(String sql) {
    return create(sql, new BindInfo() {});
  }

  public static SQL create(String sql, BindInfo bindInfo) {
    return new SQL(new SQLNormalizer(sql), bindInfo);
  }

  /* These maps are unlikely to grow beyond one, maybe two entries */
  private final Map<Class<?>, BeanBinder<?>> beanBinders = new HashMap<>(4);
  private final Map<Class<?>, BeanifierBox<?>> beanifierBoxes = new HashMap<>(4);

  private final ReentrantLock lock = new ReentrantLock();

  private final SQLNormalizer normalizer;
  private final BindInfo bindInfo;

  private Page page;
  private List<Tuple<String, Object>> vars;
  private String jdbcSQL;

  private SQL(SQLNormalizer normalizer, BindInfo bindInfo) {
    this.normalizer = normalizer;
    this.bindInfo = bindInfo;
  }

  public SQL set(String varName, Object value) {
    Check.notNull(varName, "varName");
    Check.notNull(value, "value");
    if (vars == null) {
      vars = new ArrayList<>();
    }
    vars.add(Tuple.of(varName, value));
    return this;
  }

  public SQL setSortColumn(Object sortColumn) {
    return set("sortColumn", sortColumn);
  }

  public SQL setSortOrder(Object sortOrder) {
    return set("sortOrder", sortOrder);
  }

  public SQL setOrderBy(Object sortColumn, Object sortOrder) {
    return setSortColumn(sortColumn).setSortOrder(sortOrder);
  }

  public SQLQuery prepareQuery(Connection con) {
    return prepare(con, SQLQuery::new);
  }

  public SQLInsert prepareInsert(Connection con) {
    return prepare(con, SQLInsert::new);
  }

  public SQLUpdate prepareUpdate(Connection con) {
    return prepare(con, SQLUpdate::new);
  }

  public String getOriginalSQL() {
    return normalizer.getUnparsedSQL();
  }

  public String getNormalizedSQL() {
    return normalizer.getNormalizedSQL();
  }

  public String getJdbcSQL() {
    return Check.that(jdbcSQL).is(notNull(), ERR_NO_JDBC_SQL).ok();
  }

  public List<NamedParameter> getParameters() {
    return normalizer.getNamedParameters();
  }

  public Map<String, IntList> getParameterMap() {
    return normalizer.getParameterMap();
  }

  @Override
  public String toString() {
    return ifNull(jdbcSQL, getNormalizedSQL());
  }

  void unlock() {
    vars = null;
    jdbcSQL = null;
    lock.unlock();
  }

  MapBinder getMapBinder() {
    return new MapBinder(getParameters(), bindInfo);
  }

  @SuppressWarnings("unchecked")
  <T> BeanBinder<T> getBeanBinder(Class<T> beanClass) {
    return (BeanBinder<T>)
        beanBinders.computeIfAbsent(beanClass, k -> new BeanBinder<>(k, getParameters(), bindInfo));
  }

  @SuppressWarnings("unchecked")
  <T> BeanifierBox<T> getBeanifierBox(
      Class<T> beanClass, Supplier<T> beanSupplier, UnaryOperator<String> columnToPropertyMapper) {
    return (BeanifierBox<T>)
        beanifierBoxes.computeIfAbsent(
            beanClass, k -> new BeanifierBox<>(beanClass, beanSupplier, columnToPropertyMapper));
  }

  private <T extends SQLStatement<?>> T prepare(
      Connection con, BiFunction<Connection, SQL, T> constructor) {
    Check.on(illegalState(), lock.isHeldByCurrentThread()).is(no(), ERR_LOCKED);
    lock.lock();
    try {
      if (vars != null) {
        LOG.debug("Processing SQL template variables");
        if (page == null) {
          page = Page.configure(Template.parseString(getNormalizedSQL()));
        }
        RenderSession session = page.newRenderSession();
        for (Tuple<String, Object> var : vars) {
          LOG.debug("** Variable \"{}\": {}", var.getLeft(), var.getRight());
          session.set(var.getLeft(), var.getRight());
        }
        jdbcSQL = session.render();
      } else {
        jdbcSQL = getNormalizedSQL();
      }
      return constructor.apply(con, this);
    } catch (Throwable t) {
      unlock();
      throw new KJSQLException(t);
    }
  }
}
