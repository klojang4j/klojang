package org.klojang.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import org.klojang.x.db.ps.BeanBinder;
import nl.naturalis.common.ExceptionMethods;
import nl.naturalis.common.check.Check;
import static java.util.stream.Collectors.toList;
import static nl.naturalis.common.StringMethods.implode;
import static nl.naturalis.common.check.CommonChecks.keyIn;

public abstract class SQLStatement<T extends SQLStatement<T>> implements AutoCloseable {

  final Connection con;
  final SQL sql;
  final List<Object> bindables;

  private final Set<NamedParameter> bound;

  SQLStatement(Connection con, SQL sql) {
    this.con = con;
    this.sql = sql;
    this.bindables = new ArrayList<>(4);
    this.bound = new HashSet<>(sql.getParameters().size(), 1.0F);
  }

  @SuppressWarnings("unchecked")
  public T bind(Object beanOrMap) {
    Check.notNull(beanOrMap, "beanOrMap").then(bindables::add);
    return (T) this;
  }

  @SuppressWarnings("unchecked")
  public T bind(String param, Object value) {
    Check.notNull(param, "param")
        .is(keyIn(), sql.getParameterMap(), "No such parameter: \"%s\"", param);
    bindables.add(Collections.singletonMap(param, value));
    return (T) this;
  }

  public SQL getSQL() {
    return sql;
  }

  @SuppressWarnings("unchecked")
  <U> void applyBindings(PreparedStatement ps) throws Throwable {
    for (Object obj : bindables) {
      if (obj instanceof Map) {
        Map<String, Object> map = (Map<String, Object>) obj;
        sql.getMapBinder().bindMap(ps, map, bound);
      } else {
        BeanBinder<U> binder = sql.getBeanBinder((Class<U>) obj.getClass());
        binder.bindBean(ps, (U) obj);
        bound.addAll(binder.getBoundParameters());
      }
    }
    if (bound.size() != sql.getParameters().size()) {
      throw notExecutable();
    }
  }

  void close(PreparedStatement ps) {
    try {
      if (ps != null) {
        try {
          ps.close();
        } catch (SQLException e) {
          throw ExceptionMethods.uncheck(e);
        }
      }
    } finally {
      sql.unlock();
    }
  }

  private KJSQLException notExecutable() {
    Set<NamedParameter> params = new HashSet<>(sql.getParameters());
    params.removeAll(bound);
    List<String> unbound = params.stream().map(NamedParameter::getName).collect(toList());
    return new KJSQLException("Some query parameters have not been bound yet: %s", implode(unbound));
  }
}
