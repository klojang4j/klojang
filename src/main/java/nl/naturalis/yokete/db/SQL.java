package nl.naturalis.yokete.db;

import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import nl.naturalis.common.collection.IntList;
import nl.naturalis.yokete.db.ps.BeanBinder;
import nl.naturalis.yokete.db.ps.MapBinder;
import static nl.naturalis.common.CollectionMethods.convertValuesAndFreeze;

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

  public static SQL create(String sql) {
    return create(sql, new BindInfo() {});
  }

  public static SQL create(String sql, BindInfo bindInfo) {
    SQLFactory sf = new SQLFactory(sql);
    return new SQL(sql, sf.sql(), sf.params(), sf.paramMap(), bindInfo);
  }

  /* These maps are unlikely to grow beyond one, maybe two entries */
  private final Map<Class<?>, BeanBinder<?>> beanBinders = new HashMap<>(4);
  private final Map<Class<?>, BeanifierBox<?>> beanifierBoxes = new HashMap<>(4);

  private final String sql;
  private final String normalized;
  private final List<NamedParameter> params;
  private final Map<String, IntList> paramMap;
  private final BindInfo bindInfo;

  private MappifierBox mappifierBox;

  private SQL(
      String sql,
      String normalized,
      List<NamedParameter> params,
      Map<String, int[]> paramMap,
      BindInfo bindInfo) {
    this.sql = sql;
    this.normalized = normalized;
    this.params = params;
    this.paramMap = convertValuesAndFreeze(paramMap, IntList::of);
    this.bindInfo = bindInfo;
  }

  public SQLQuery prepareQuery(Connection con) {
    return new SQLQuery(con, this);
  }

  public SQLInsert prepareInsert(Connection con) {
    return new SQLInsert(con, this);
  }

  public SQLUpdate prepareUpdate(Connection con) {
    return new SQLUpdate(con, this);
  }

  /**
   * Returns the SQL query from which this instance was created with any named parameters still left
   * in place.
   *
   * @return
   */
  public String getOriginalSQL() {
    return sql;
  }

  /**
   * Returns a SQL query where all named parameters have been replaced with positional parameters
   * (question marks).
   *
   * @return
   */
  public String getNormalizedSQL() {
    return normalized;
  }

  public List<NamedParameter> getParameters() {
    return params;
  }

  public Map<String, IntList> getParameterMap() {
    return paramMap;
  }

  MapBinder getMapBinder() {
    return new MapBinder(params, bindInfo);
  }

  @SuppressWarnings("unchecked")
  <T> BeanBinder<T> getBeanBinder(Class<T> beanClass) {
    return (BeanBinder<T>)
        beanBinders.computeIfAbsent(beanClass, k -> new BeanBinder<>(k, params, bindInfo));
  }

  @SuppressWarnings("unchecked")
  <T> BeanifierBox<T> getBeanifierBox(
      Class<T> beanClass, Supplier<T> beanSupplier, UnaryOperator<String> columnToPropertyMapper) {
    return (BeanifierBox<T>)
        beanifierBoxes.computeIfAbsent(
            beanClass, k -> new BeanifierBox<>(beanClass, beanSupplier, columnToPropertyMapper));
  }

  MappifierBox getMappifierBox(UnaryOperator<String> columnToKeyMapper) {
    if (mappifierBox == null) {
      mappifierBox = new MappifierBox(columnToKeyMapper);
    }
    return mappifierBox;
  }

  @Override
  public String toString() {
    return sql;
  }
}
