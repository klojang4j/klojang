package org.klojang.db;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import org.klojang.render.NameMapper;
import org.klojang.render.RenderSession;
import org.klojang.template.PathResolver;
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
 * A container for a single SQL query and a factory for {@link SQLQuery}, {@link SQLInsert} and
 * {@link SQLUpdate} instances. The {@code SQL} class lets you parametrize SQL in two ways:
 *
 * <p>
 *
 * <ol>
 *   <li>Using common (but not JDBC-supported) named parameters for the values in WHERE, HAVING and
 *       LIMIT clauses. Named parameters start with a colon. E.g. {@code :firstName}. Named
 *       parameters are not bound in the {@code SQL} instance itself, but in the {@code SQLQuery},
 *       {@code SQLInsert} or {@link SQLUpdate} instance obtained from it.
 *   <li>Using Klojang template variables for the other parts of a query. Although this basically
 *       lets you parametrize whatever suits you, it is especially meant to parametrize the sort
 *       column in the ORDER BY cluase - a common use case in web applications. Klojang template
 *       variables must be set in the {@code SQL} instance itself.
 * </ol>
 *
 * <p>If the query contains many named parameters and Klojang template variables, and is going to be
 * executed often, you might want to cache the {@code SQL} instance (e.g. as a static final variable
 * in your DAO class).
 *
 * @author Ayco Holleman
 */
public class SQL implements PathResolver {

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

  public static SQLInsertBuilder prepareInsert() {
    return new SQLInsertBuilder();
  }

  /* These maps are unlikely to grow beyond one, maybe two entries */
  private final Map<Class<?>, BeanBinder<?>> beanBinders = new HashMap<>(4);
  private final Map<Class<?>, BeanifierFactory<?>> beanifiers = new HashMap<>(4);

  private final ReentrantLock lock = new ReentrantLock();

  private final SQLNormalizer normalizer;
  private final BindInfo bindInfo;

  private List<Tuple<String, Object>> vars;
  private String jdbcSQL;

  private SQL(SQLNormalizer normalizer, BindInfo bindInfo) {
    this.normalizer = normalizer;
    this.bindInfo = bindInfo;
  }

  public SQL set(String varName, Object value) {
    Check.notNull(varName, "varName");
    Check.that(value).is(notNull(), "Value of %s must not be null", varName);
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

  /**
   * Returns the original, unparsed, user-provided SQL, with all named parameters and Klojand
   * template variables still in it.
   *
   * @return The original, unparsed, user-provided SQL
   */
  public String getOriginalSQL() {
    return normalizer.getUnparsedSQL();
  }

  /**
   * Returns an SQL string in which all named parameters have been replaced with standard JDBC
   * positional parameters ('?'), but with the Klojang template variables still in it.
   *
   * @return An SQL string in which all named parameters have been replaced with standard JDBC
   *     positional parameters
   */
  public String getNormalizedSQL() {
    return normalizer.getNormalizedSQL();
  }

  /**
   * Returns fully JDBC-compliant, executable SQL.
   *
   * @return Fully JDBC-compliant, executable SQL
   */
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
    BeanBinder<T> bb = (BeanBinder<T>) beanBinders.get(beanClass);
    if (bb == null) {
      bb = new BeanBinder<>(beanClass, getParameters(), bindInfo);
      beanBinders.put(beanClass, bb);
    }
    return bb;
  }

  @SuppressWarnings("unchecked")
  <T> BeanifierFactory<T> getBeanifierFactory(Class<T> clazz, NameMapper mapper) {
    BeanifierFactory<?> bf = beanifiers.get(clazz);
    if (bf == null) {
      bf = new BeanifierFactory<>(clazz, mapper);
      beanifiers.put(clazz, bf);
    }
    return (BeanifierFactory<T>) bf;
  }

  @SuppressWarnings("unchecked")
  <T> BeanifierFactory<T> getBeanifierFactory(
      Class<T> clazz, Supplier<T> supplier, NameMapper mapper) {
    BeanifierFactory<?> bf = beanifiers.get(clazz);
    if (bf == null) {
      bf = new BeanifierFactory<>(clazz, supplier, mapper);
      beanifiers.put(clazz, bf);
    }
    return (BeanifierFactory<T>) bf;
  }

  private <T extends SQLStatement<?>> T prepare(
      Connection con, BiFunction<Connection, SQL, T> constructor) {
    Check.on(illegalState(), lock.isHeldByCurrentThread()).is(no(), ERR_LOCKED);
    lock.lock();
    try {
      if (vars != null) {
        LOG.debug("Processing SQL template variables");
        Template template = Template.fromResolver(this, "sql://" + this.hashCode());
        RenderSession session = template.newRenderSession();
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
      throw KJSQLException.wrap(t, this);
    }
  }

  @Override
  public Optional<Boolean> isValidPath(String path) {
    return Optional.of(Boolean.TRUE);
  }

  @Override
  public InputStream resolvePath(String path) throws IOException {
    return new ByteArrayInputStream(getNormalizedSQL().getBytes(StandardCharsets.UTF_8));
  }
}
