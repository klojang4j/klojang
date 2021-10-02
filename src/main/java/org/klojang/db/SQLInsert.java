package org.klojang.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import nl.naturalis.common.check.Check;
import nl.naturalis.common.invoke.Setter;
import nl.naturalis.common.invoke.SetterFactory;
import static java.sql.Statement.NO_GENERATED_KEYS;
import static java.sql.Statement.RETURN_GENERATED_KEYS;
import static nl.naturalis.common.NumberMethods.convert;
import static nl.naturalis.common.check.CommonChecks.*;
import static nl.naturalis.common.invoke.NoSuchPropertyException.noSuchProperty;

public class SQLInsert extends SQLStatement<SQLInsert> {

  @SuppressWarnings("unused")
  private static final Logger LOG = LoggerFactory.getLogger(SQLInsert.class);

  // This will track the bindables field in SQLStatement. It will contain the names of the bean
  // properties and/or map keys corresponding to auto-increment columns.
  private final List<String> keys;

  private PreparedStatement ps;
  private boolean generateKeys;

  public SQLInsert(Connection conn, SQL sql) {
    super(conn, sql);
    this.keys = new ArrayList<>(5);
  }

  public SQLInsert bind(Map<String, ?> map) {
    super.bind(map);
    keys.add(null);
    return this;
  }

  public SQLInsert bind(Object bean) {
    super.bind(bean);
    keys.add(null);
    return this;
  }

  /**
   * Binds the values in the specified JavaBean to the named parameters within the SQL statement.
   * Bean properties that do not correspond to named parameters will be ignored. The effect of
   * passing anything other than a proper JavaBean (e.g. an {@code InputStream} or a {@code
   * Collection}) is undefined. The {@code idProperty} argument must be the name of the property
   * that corresponds to the auto-increment column. The generated value for that column will be
   * bound back into the bean. Of course, the bean or {@code Map} needs to be modifiable in that
   * case. If you don't want the auto-increment column to be bound back into the bean or {@code
   * Map}, just call {@link #bind(Object)}.
   *
   * <p>Klojang does not support INSERT statements that generate multiple keys or non-number keys.
   *
   * @param bean The bean whose values to bind to the named parameters within the SQL statement
   * @param idProperty The name of the property representing the auto-generated primary key.
   * @return This {@code SQLInsert} instance
   */
  public SQLInsert bind(Object bean, String idProperty) {
    super.bind(bean);
    keys.add(idProperty);
    return this;
  }

  /**
   * Binds the values in the specified {@code Map} to the named parameters within the SQL statement.
   * Map keys that do not correspond to named parameters will be ignored. The {@code idKey} argument
   * must be the name of the map key that corresponds to the auto-increment column. The generated
   * value for that column will be bound back into the {@code Map} under that key. Therefore, make
   * sure the {@code Map} needs to be modifiable. If you don't want the auto-increment column to be
   * bound back into the {@code Map}, just call {@link #bind(Map)}.
   *
   * <p>Klojang does not support INSERT statements that generate multiple keys or non-number keys.
   *
   * @param map The bean whose values to bind to the named parameters within the SQL statement
   * @param idKey The name of the map key representing the auto-generated primary key.
   * @return This {@code SQLInsert} instance
   */
  public SQLInsert bind(Map<String, ?> map, String idKey) {
    super.bind(map);
    keys.add(idKey);
    return this;
  }

  public <U> void insertAll(List<U> beans) {
    Check.on(illegalState(), bindables).is(empty(), "insertAll not allowed on dirty instance");
    try {
      for (U bean : beans) {
        bindables.clear();
        bindables.add(bean);
        exec(false);
      }
    } catch (Throwable t) {
      throw KJSQLException.wrap(t, getSQL());
    } finally {
      reset();
    }
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
      throw KJSQLException.wrap(t, getSQL());
    } finally {
      reset();
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
      throw KJSQLException.wrap(t, getSQL());
    } finally {
      reset();
    }
  }

  @Override
  public void close() {
    close(ps);
  }

  private void exec(boolean generateKeys) throws Throwable {
    if (ps == null) {
      this.generateKeys = generateKeys;
      int i = generateKeys ? RETURN_GENERATED_KEYS : NO_GENERATED_KEYS;
      ps = con.prepareStatement(sql.getJdbcSQL(), i);
    } else if (this.generateKeys != generateKeys) {
      this.generateKeys = generateKeys;
      int i = generateKeys ? RETURN_GENERATED_KEYS : NO_GENERATED_KEYS;
      ps.close();
      ps = con.prepareStatement(sql.getJdbcSQL(), i);
    }
    applyBindings(ps);
    ps.executeUpdate();
  }

  private void reset() {
    bindables.clear();
    keys.clear();
    try {
      ps.clearParameters();
    } catch (SQLException e) {
      throw KJSQLException.wrap(e, getSQL());
    }
  }
}
