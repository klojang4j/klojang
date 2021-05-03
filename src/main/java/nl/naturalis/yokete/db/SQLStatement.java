package nl.naturalis.yokete.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import nl.naturalis.common.ExceptionMethods;
import nl.naturalis.common.check.Check;
import nl.naturalis.yokete.db.ps.BeanBinder;
import static java.util.stream.Collectors.toSet;
import static nl.naturalis.common.check.CommonChecks.in;

abstract class SQLStatement implements AutoCloseable {

  protected final Connection con;
  protected final SQL sql;
  protected final List<Object> bindables;

  private Set<String> params;

  SQLStatement(Connection con, SQL sql) {
    this.con = con;
    this.sql = sql;
    this.bindables = new ArrayList<>(2);
  }

  public SQLStatement bind(Object bean) {
    Check.notNull(bean).then(bindables::add);
    return this;
  }

  public SQLStatement bind(Map<String, Object> map) {
    Check.notNull(map).then(bindables::add);
    return this;
  }

  public SQLStatement bind(String param, Object value) {
    if (params == null) {
      params = sql.getParameters().stream().map(p -> p.getName()).collect(toSet());
    }
    Check.notNull(param, "param").is(in(), params, "No such parameter: \"%s\"", param);
    bindables.add(Collections.singletonMap(param, value));
    return this;
  }

  @SuppressWarnings("unchecked")
  protected <T> void bind(PreparedStatement ps) throws Throwable {
    for (Object obj : bindables) {
      if (obj instanceof Map) {
        sql.getMapBinder().bindMap(ps, (Map<String, Object>) obj);
      } else {
        BeanBinder<T> binder = sql.getBeanBinder((Class<T>) obj.getClass());
        binder.bindBean(ps, (T) obj);
      }
    }
  }

  protected void close(PreparedStatement ps) {
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
