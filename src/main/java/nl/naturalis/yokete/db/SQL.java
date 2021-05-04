package nl.naturalis.yokete.db;

import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import nl.naturalis.yokete.db.ps.BeanBinder;
import nl.naturalis.yokete.db.ps.MapBinder;

public class SQL {

  public static SQL create(String sql) {
    return create(sql, new BindInfo() {});
  }

  public static SQL create(String sql, BindInfo bindInfo) {
    SQLFactory sf = new SQLFactory(sql);
    return new SQL(sf.sql(), sf.params(), sf.paramMap(), bindInfo);
  }

  final Map<Class<?>, BeanBinder<?>> beanBinders = new HashMap<>();

  private final String sql;
  private final List<NamedParameter> params;
  private final Map<String, int[]> paramMap;
  private final BindInfo bindInfo;

  private SQL(
      String sql, List<NamedParameter> params, Map<String, int[]> paramMap, BindInfo bindInfo) {
    this.sql = sql;
    this.params = params;
    this.paramMap = paramMap;
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

  public Map<String, int[]> getParameterMap() {
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
