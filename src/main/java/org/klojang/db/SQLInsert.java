package org.klojang.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import nl.naturalis.common.ExceptionMethods;
import nl.naturalis.common.check.Check;
import nl.naturalis.common.invoke.Setter;
import nl.naturalis.common.invoke.SetterFactory;
import static java.sql.Statement.NO_GENERATED_KEYS;
import static java.sql.Statement.RETURN_GENERATED_KEYS;
import static nl.naturalis.common.NumberMethods.convert;
import static nl.naturalis.common.check.CommonChecks.keyIn;
import static nl.naturalis.common.check.CommonChecks.notNull;
import static nl.naturalis.common.check.CommonChecks.number;
import static nl.naturalis.common.invoke.NoSuchPropertyException.noSuchProperty;

public class SQLInsert extends SQLStatement<SQLInsert> {

  @SuppressWarnings("unused")
  private static final Logger LOG = LoggerFactory.getLogger(SQLInsert.class);

  // This will track bindables field in SQLStatement. It will contain the names of the bean
  // properties and/or map keys corresponding to auto-increment columns.
  private final List<String> keys;

  private PreparedStatement ps;

  public SQLInsert(Connection conn, SQL sql) {
    super(conn, sql);
    this.keys = new ArrayList<>(5);
  }

  public SQLInsert bind(Object beanOrMap) {
    super.bind(beanOrMap);
    keys.add(null);
    return this;
  }

  /**
   * Binds the values in the specified JavaBean or {@code Map<String,Object>} to the named
   * parameters within the SQL statement. Bean properties or map keys that do noy correspond to
   * named parameters will be ignored. The effect of passing anything other than a proper JavaBean
   * or a {@code Map<String,Object>} (e.g. a {@code Collection}) is undefined. The {@code
   * idPropertyOrKey} argument must be the name of a bean property or map key corresponding to an
   * auto-increment column. The generated value for that column will be bound back into the bean or
   * {@code Map}. Of course, the bean or {@code Map} needs to be modifiable in that case. If you
   * don't want the auto-increment column to be bound back into the bean or {@code Map}, just call
   * {@link #bind(Object)}.
   *
   * <p>Klojang does not support INSERT statements that generate multiple keys.
   *
   * @param beanOrMap The bean or {@code Map} whose values to bind to the named parameters within
   *     the SQL statement
   * @return This {@code SQLInsert} instance
   */
  public SQLInsert bind(Object beanOrMap, String idPropertyOrKey) {
    super.bind(beanOrMap);
    keys.add(idPropertyOrKey);
    return this;
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  public void execute() {
    boolean mustBindBack = keys.stream().anyMatch(notNull());
    try {
      if (!mustBindBack) {
        exec(false);
      } else {
        exec(true);
        try (ResultSet rs = ps.getGeneratedKeys()) {
          if (!rs.next()) {
            throw new KJSQLException("No keys were generated");
          } else if (rs.getMetaData().getColumnCount() != 1) {
            throw new KJSQLException("Multiple auto-increment keys not supported");
          }
          long id = rs.getLong(1);
          for (int i = 0; i < keys.size(); ++i) {
            String key = keys.get(i);
            if (key != null) {
              Object obj = bindables.get(i);
              if (obj instanceof Map) {
                ((Map) obj).put(key, id);
              } else {
                Map<String, Setter> setters = SetterFactory.INSTANCE.getSetters(obj.getClass());
                Check.on(s -> noSuchProperty(obj, key), key).is(keyIn(), setters);
                Setter setter = setters.get(key);
                Check.that(setter.getParamType()).is(number());
                Number n = convert(id, (Class<? extends Number>) setter.getParamType());
                setter.write(obj, n);
              }
            }
          }
        }
      }
    } catch (Throwable t) {
      throw ExceptionMethods.uncheck(t);
    }
  }

  public long executeAndGetId() {
    try {
      exec(true);
      try (ResultSet rs = ps.getGeneratedKeys()) {
        if (!rs.next()) {
          throw new KJSQLException("No keys were generated");
        } else if (rs.getMetaData().getColumnCount() != 1) {
          throw new KJSQLException("Multiple auto-increment keys not supported");
        }
        return rs.getLong(1);
      }
    } catch (Throwable t) {
      throw ExceptionMethods.uncheck(t);
    }
  }

  @Override
  public void close() {
    close(ps);
  }

  private void exec(boolean returnKeys) throws Throwable {
    int keys = returnKeys ? RETURN_GENERATED_KEYS : NO_GENERATED_KEYS;
    ps = con.prepareStatement(sql.getJdbcSQL(), keys);
    applyBindings(ps);
    ps.executeUpdate();
  }
}
