package nl.naturalis.yokete.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import nl.naturalis.common.ExceptionMethods;
import nl.naturalis.yokete.db.ps.BeanBinder;

public class SQL {

  public static SQL create(String sql) {
    return create(sql, new BindInfo() {});
  }

  public static SQL create(String sql, BindInfo bindInfo) {
    SQLFactory sf = new SQLFactory(sql);
    return new SQL(sf.getNormalizedSQL(), sf.getParams(), bindInfo);
  }

  final Map<Class<?>, BeanBinder<?>> beanBinders = new HashMap<>();

  private final String sql;
  private final List<NamedParameter> params;
  private final BindInfo bindInfo;

  private SQL(String sql, List<NamedParameter> params, BindInfo bindInfo) {
    this.sql = sql;
    this.params = params;
    this.bindInfo = bindInfo;
  }

  BeanBinder<?> getBeanBinder(Class<?> beanClass) {
    return beanBinders.computeIfAbsent(beanClass, k -> new BeanBinder<>(k, params, bindInfo));
  }
}
