package nl.naturalis.yokete.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import nl.naturalis.common.ExceptionMethods;
import nl.naturalis.common.check.Check;
import nl.naturalis.yokete.db.ps.BeanBinder;
import static nl.naturalis.common.check.CommonChecks.keyIn;

public abstract class SQLStatement<T extends SQLStatement<T>> implements AutoCloseable {

  final Connection con;
  final SQL sql;
  final List<Object> bindables;

  SQLStatement(Connection con, SQL sql) {
    this.con = con;
    this.sql = sql;
    this.bindables = new ArrayList<>(2);
  }

  @SuppressWarnings("unchecked")
  public T bind(Object bean) {
    Check.notNull(bean, "bean").then(bindables::add);
    return (T) this;
  }

  @SuppressWarnings("unchecked")
  public T bind(Map<String, Object> map) {
    Check.notNull(map, "map").then(bindables::add);
    return (T) this;
  }

  @SuppressWarnings("unchecked")
  public T bind(String param, Object value) {
    Check.notNull(param, "param")
        .is(keyIn(), sql.getParameterMap(), "No such parameter: \"%s\"", param);
    bindables.add(Collections.singletonMap(param, value));
    return (T) this;
  }

  @SuppressWarnings("unchecked")
  <U> void bind(PreparedStatement ps) throws Throwable {
    for (Object obj : bindables) {
      if (obj instanceof Map) {
        sql.getMapBinder().bindMap(ps, (Map<String, Object>) obj);
      } else {
        BeanBinder<U> binder = sql.getBeanBinder((Class<U>) obj.getClass());
        binder.bindBean(ps, (U) obj);
      }
    }
  }

  void close(PreparedStatement ps) {
    if (ps != null) {
      try {
        if (!ps.isClosed()) {
          ps.close();
        }
      } catch (SQLException e) {
        throw ExceptionMethods.uncheck(e);
      }
    }
  }
}
