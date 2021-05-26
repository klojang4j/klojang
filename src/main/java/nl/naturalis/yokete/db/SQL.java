package nl.naturalis.yokete.db;

import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import nl.naturalis.common.collection.IntList;
import nl.naturalis.yokete.db.ps.BeanBinder;
import nl.naturalis.yokete.db.ps.MapBinder;
import static nl.naturalis.common.CollectionMethods.convertValuesAndFreeze;

/**
 * A container of a single SQL query with named parameters. This class functions as a factory for
 * {@link SQLQuery}, {@link SQLInsert} and {@link SQLUpdate} instances.
 *
 * @author Ayco Holleman
 */
public class SQL {

  public static SQL create(String sql) {
    return create(sql, new BindInfo() {});
  }

  public static SQL create(String sql, BindInfo bindInfo) {
    SQLFactory sf = new SQLFactory(sql);
    return new SQL(sf.sql(), sf.params(), sf.paramMap(), bindInfo);
  }

  private final Map<Class<?>, BeanBinder<?>> beanBinders = new HashMap<>();

  private final String sql;
  private final List<NamedParameter> params;
  private final Map<String, IntList> paramMap;
  private final BindInfo bindInfo;

  private SQL(
      String sql, List<NamedParameter> params, Map<String, int[]> paramMap, BindInfo bindInfo) {
    this.sql = sql;
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
   * Returns an SQL string where all named parameters have been replaced with positional parameters
   * (question marks).
   *
   * @return
   */
  public String getNormalizedSQL() {
    return sql;
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
}
