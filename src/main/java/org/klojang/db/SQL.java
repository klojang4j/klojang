package org.klojang.db;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import org.klojang.template.NameMapper;
import org.klojang.template.RenderSession;
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
 * A factory for {@link SQLQuery}, {@link SQLInsert} and {@link SQLUpdate} instances. An {@code SQL}
 * instance represents a single SQL statement that cannot be changed. The {statement can be
 * parametrized in two ways:
 *
 * <p>
 *
 * <ol>
 *   <li>Using named parameters for values in WHERE, HAVING and LIMIT clauses. Named parameters
 *       start with a colon. For example: {@code :firstName}. Named parameters are not bound in the
 *       {@code SQL} instance itself, but in the {@code SQLQuery}, {@code SQLInsert} or {@code
 *       SQLUpdate} instance obtained from it.
 *   <li>Using Klojang template variables for the other parts of a query. Although this basically
 *       lets you parametrize whatever makes you happy, it is especially meant to parametrize the
 *       sort column in the ORDER BY cluase - a common use case in web applications. Klojang
 *       template variables must be set in the {@code SQL} instance itself.
 * </ol>
 *
 * <p>In other words, the SQL fed into an instance of this class might look like this:
 *
 * <blockquote>
 *
 * <pre>{@code
 * SELECT *
 *   FROM EMPLOYEE
 *  WHERE FIRST_NAME = :firstName
 *    AND LAST_NAME = :lastName
 *  ORDER BY ~%sortColumn%
 * }</pre>
 *
 * </blockquote>
 *
 * <p>You would then set the {@code age} and {@code salary} variables in the {@code SQL} instance,
 * request a {@code SQLQuery} from it, and then bind the {@code firstName} and {@code lastName}
 * parameters in the {@code SQLQuery} instance:
 *
 * <p>
 *
 * <blockquote>
 *
 * <pre>{@code
 * SQL sql = SQL.create(theAboveSQL);
 * sql.set("sortColumn", "BIRTH_DATE");
 * SQLQuery query = sql.prepareQuery(conn);
 * List<Employee> employees =
 * query
 *  .bind("firstName", "John")
 *  .bind("lastName", "Smith")
 *  .getBeanifier(Employee.class)
 *  .beanifyAll();
 * }</pre>
 *
 * </blockquote>
 *
 * <p>If the SQL statement contains many named parameters and Klojang template variables, and is
 * going to be executed often, you might want to cache the {@code SQL} instance (e.g. as a static
 * final variable in your DAO class).
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

  /**
   * Creates an {@code SQL} instances from the specified SQL statement.
   *
   * @param sql The SQL
   * @return an {@code SQL} instance
   */
  public static SQL create(String sql) {
    return create(sql, new BindInfo() {});
  }

  public static SQL create(String sql, BindInfo bindInfo) {
    return new SQL(new SQLNormalizer(sql), bindInfo);
  }

  public static SQLInsertBuilder prepareInsert() {
    return new SQLInsertBuilder();
  }

  private final ReentrantLock lock = new ReentrantLock();

  /* These maps are unlikely to grow beyond one, maybe two entries */
  private final Map<Class<?>, BeanBinder<?>> beanBinders = new HashMap<>(4);
  private final Map<Tuple<Class<?>, NameMapper>, BeanifierFactory<?>> beanifiers = new HashMap<>(4);
  private final Map<NameMapper, MappifierFactory> mappifiers = new HashMap<>(4);

  private final SQLNormalizer normalizer;
  private final BindInfo bindInfo;

  private Template template;
  private List<Tuple<String, Object>> vars;
  private String jdbcSQL;

  private SQL(SQLNormalizer normalizer, BindInfo bindInfo) {
    this.normalizer = normalizer;
    this.bindInfo = bindInfo;
  }

  /**
   * Sets the value of a Klojang template variable within the SQL.
   *
   * @param varName The name of the variable
   * @param value The value to give it
   * @return This {@code SQL} instance
   */
  public SQL set(String varName, Object value) {
    Check.notNull(varName, "varName");
    Check.that(value).is(notNull(), "Value of %s must not be null", varName);
    if (vars == null) {
      vars = new ArrayList<>();
    }
    vars.add(Tuple.of(varName, value));
    return this;
  }

  /**
   * If you decide to go along and parametrize the sort column using a variable named {@code
   * ~%sortColumn%}, this method lets you set the value for that variable.
   *
   * @param sortColumn The column on which to sort
   * @return This {@code SQL} instance
   */
  public SQL setSortColumn(Object sortColumn) {
    return set("sortColumn", sortColumn);
  }

  /**
   * If you decide to go along and parametrize the sort order using a variable named {@code
   * ~%sortOrder%}, this method lets you set the value for that variable. Calling {@code toString()}
   * on the argument must yield "ASC", "DESC" or an empty string. The argument may also be a {@code
   * Boolean} with {@code false} being translated into "ASC" and {@code true} into "DESC".
   *
   * @param sortOrder The sort order
   * @return This {@code SQL} instance
   */
  public SQL setSortOrder(Object sortOrder) {
    return (sortOrder instanceof Boolean)
        ? setDescending((Boolean) sortOrder)
        : set("sortOrder", sortOrder);
  }

  /**
   * Sets the value of the {@code ~%sortOrder%} variable to "DESC" if the argument equals {@code
   * true} and to "ASC" if the argument equals {@code false}. This presumes (and requires) that you
   * have that variable in the SQL statement.
   *
   * @param isDescending Whether to sort in descending order
   * @return This {@code SQL} instance
   */
  public SQL setDescending(boolean isDescending) {
    return set("sortOrder", isDescending ? "DESC" : "ASC");
  }

  /**
   * Sets the values of the values of the {@code ~%sortColumn%} and {@code ~%sortOrder%} variables.
   * This presumes (and requires) that you have those variables in the SQL statement.
   *
   * @param sortColumn The column on which to sort
   * @param sortOrder The sort order
   * @return This {@code SQL} instance
   */
  public SQL setOrderBy(Object sortColumn, Object sortOrder) {
    return setSortColumn(sortColumn).setSortOrder(sortOrder);
  }

  /**
   * Sets the values of the values of the {@code ~%sortColumn%} and {@code ~%sortOrder%} variables.
   * This presumes (and requires) that you have those variables in the SQL statement.
   *
   * @param sortColumn
   * @param isDescending
   * @return This {@code SQL} instance
   */
  public SQL setOrderBy(Object sortColumn, boolean isDescending) {
    return setSortColumn(sortColumn).setDescending(isDescending);
  }

  /**
   * Produces a {@link SQLQuery} instance from the SQL passed in through one of the {@link
   * #create(String) create} methods. Calling this method for SQL that is not a SELECT statement has
   * undefined consequences.
   *
   * @param con The database connection to use when executing the statement
   * @return
   */
  public SQLQuery prepareQuery(Connection con) {
    return prepare(con, SQLQuery::new);
  }

  /**
   * Produces a {@link SQLInsert} instance from the SQL passed in through one of the {@link
   * #create(String) create} methods. Calling this method for SQL that is not an INSERT statement
   * has undefined consequences.
   *
   * @param con The database connection to use when executing the statement
   * @return
   */
  public SQLInsert prepareInsert(Connection con) {
    return prepare(con, SQLInsert::new);
  }

  /**
   * Produces a {@link SQLInsert} instance from the SQL passed in through one of the {@link
   * #create(String) create} methods. Calling this method for SQL that is not an UPDATE OR DELETE
   * statement has undefined consequences.
   *
   * @param con The database connection to use when executing the statement
   * @return
   */
  public SQLUpdate prepareUpdate(Connection con) {
    return prepare(con, SQLUpdate::new);
  }

  /**
   * Returns the original, unparsed SQL, with all named parameters and Klojang template variables
   * still in it.
   *
   * @return The original, unparsed SQL
   */
  public String getUnparsedSQL() {
    return normalizer.getUnparsedSQL();
  }

  /**
   * Returns a SQL string in which all named parameters have been replaced with positional
   * parameters (i&#46;e&#46; a question mark), but with the Klojang template variables still in it.
   *
   * @return A SQL string in which all named parameters have been replaced with positional
   *     parameters
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

  /**
   * Returns the named parameters that were extracted from the SQL passed in through the {@link
   * #create(String) create} methods.
   *
   * @return The named parameters that were extracted from the SQL
   */
  public List<NamedParameter> getParameters() {
    return normalizer.getNamedParameters();
  }

  /**
   * Returns a map that specifies for each named parameter at which positions it is found within the
   * SQL.
   *
   * @return A map that specifies for each named parameter at which positions it is found within the
   *     SQL
   */
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
    Tuple<Class<?>, NameMapper> key = Tuple.of(clazz, mapper);
    BeanifierFactory<T> bf = (BeanifierFactory<T>) beanifiers.get(key);
    if (bf == null) {
      beanifiers.put(key, bf = new BeanifierFactory<>(clazz, mapper));
    }
    return bf;
  }

  @SuppressWarnings("unchecked")
  <T> BeanifierFactory<T> getBeanifierFactory(
      Class<T> clazz, Supplier<T> supplier, NameMapper mapper) {
    Tuple<Class<?>, NameMapper> key = Tuple.of(clazz, mapper);
    BeanifierFactory<T> bf = (BeanifierFactory<T>) beanifiers.get(key);
    if (bf == null) {
      beanifiers.put(key, bf = new BeanifierFactory<>(clazz, supplier, mapper));
    }
    return bf;
  }

  MappifierFactory getMappifierFactory(NameMapper mapper) {
    return mappifiers.computeIfAbsent(mapper, MappifierFactory::new);
  }

  private <T extends SQLStatement<?>> T prepare(
      Connection con, BiFunction<Connection, SQL, T> constructor) {
    Check.on(illegalState(), lock.isHeldByCurrentThread()).is(no(), ERR_LOCKED);
    lock.lock();
    try {
      if (vars != null) {
        LOG.debug("Processing SQL template variables");
        if (template == null) {
          template = Template.fromString(getNormalizedSQL());
        }
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
}
