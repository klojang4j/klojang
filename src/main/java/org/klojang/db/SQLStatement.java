package org.klojang.db;

import nl.naturalis.common.ExceptionMethods;
import nl.naturalis.common.check.Check;
import org.klojang.x.db.ps.BeanBinder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

import static nl.naturalis.common.CollectionMethods.freezeIntoList;
import static nl.naturalis.common.CollectionMethods.implode;
import static nl.naturalis.common.check.CommonChecks.keyIn;

public abstract class SQLStatement<T extends SQLStatement<T>> implements AutoCloseable {

  final Connection con;
  final SQL sql;
  final List<Object> bindables;

  private final Set<NamedParameter> bound;

  SQLStatement(Connection con, SQL sql) {
    this.con = con;
    this.sql = sql;
    this.bindables = new ArrayList<>(5);
    this.bound = new HashSet<>(sql.getParameters().size());
  }

  /**
   * Binds the values in the specified JavaBean to the named parameters within the SQL statement.
   * Bean properties that do not correspond to named parameters will be ignored. The effect of
   * passing anything other than a proper JavaBean (e.g. scalars like {@code Integer} or
   * multi-valued objects like {@code Employee[]} or {@code ArrayList}) is undefined. The {@code
   * idProperty} argument must be the name of the property that corresponds to the auto-increment
   * column. The generated value for that column will be bound back into the bean. Of course, the
   * bean or {@code Map} needs to be modifiable in that case. If you don't want the auto-increment
   * column to be bound back into the bean or {@code Map}, just call {@link #bind(Object)}.
   *
   * @param bean The bean whose values to bind to the named parameters within the SQL statement
   * @return This {@code SQLInsert} instance
   */
  @SuppressWarnings("unchecked")
  public T bind(Object bean) {
    Check.notNull(bean, "bean").then(bindables::add);
    return (T) this;
  }

  @SuppressWarnings("unchecked")
  public T bind(Map<String, ?> map) {
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

  public SQL getSQL() {
    return sql;
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  <U> void applyBindings(PreparedStatement ps) throws Throwable {
    for (Object obj : bindables) {
      if (obj instanceof Map) {
        Map map = (Map) obj;
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
    List<String> unbound = freezeIntoList(params, NamedParameter::getName);
    String fmt = "Some query parameters have not been bound yet: %s";
    return new KJSQLException(fmt, implode(unbound));
  }
}
