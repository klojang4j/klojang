package org.klojang.db.rs;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.UnaryOperator;
import org.klojang.db.Row;
import nl.naturalis.common.ExceptionMethods;
import nl.naturalis.common.ModulePrivate;
import nl.naturalis.common.Tuple;

/**
 * Transports a single column-value pair from the {@code ResultSet} to a <code>
 * Map&lt;String,Object&gt;</code>.
 *
 * @author Ayco Holleman
 * @param <COLUMN_TYPE>
 */
@ModulePrivate
public class RsToMapTransporter<COLUMN_TYPE> implements ValueTransporter {

  @SuppressWarnings("unchecked")
  public static Row toRow(ResultSet rs, RsToMapTransporter<?>[] setters) throws Throwable {
    Tuple<String, Object>[] entries = new Tuple[setters.length];
    for (int i = 0; i < setters.length; ++i) {
      entries[i] = setters[i].getEntry(rs);
    }
    return Row.withColumns(entries);
  }

  public static Map<String, Object> toMap(ResultSet rs, RsToMapTransporter<?>[] setters)
      throws Throwable {
    Map<String, Object> map = new HashMap<>(setters.length);
    populateMap(rs, map, setters);
    return map;
  }

  public static void populateMap(ResultSet rs, Map<String, Object> map, RsToMapTransporter<?>[] setters)
      throws Throwable {
    for (int i = 0; i < setters.length; ++i) {
      Tuple<String, Object> tuple = setters[i].getEntry(rs);
      tuple.insertInto(map);
    }
  }

  public static RsToMapTransporter<?>[] createMapValueSetters(
      ResultSet rs, UnaryOperator<String> mapper) {
    RsMethodInventory methods = RsMethodInventory.getInstance();
    try {
      ResultSetMetaData rsmd = rs.getMetaData();
      int sz = rsmd.getColumnCount();
      RsToMapTransporter<?>[] setters = new RsToMapTransporter[sz];
      for (int idx = 0; idx < sz; ++idx) {
        int jdbcIdx = idx + 1; // JDBC is one-based
        int sqlType = rsmd.getColumnType(jdbcIdx);
        RsMethod<?> method = methods.getMethod(sqlType);
        String label = rsmd.getColumnLabel(jdbcIdx);
        String key = mapper.apply(label);
        setters[idx] = new RsToMapTransporter<>(method, jdbcIdx, sqlType, key);
      }
      return setters;
    } catch (SQLException e) {
      throw ExceptionMethods.uncheck(e);
    }
  }

  private final RsMethod<COLUMN_TYPE> method;
  private final int jdbcIdx;
  private final int sqlType;
  private final String key;

  private RsToMapTransporter(RsMethod<COLUMN_TYPE> method, int jdbcIdx, int sqlType, String key) {
    this.method = method;
    this.jdbcIdx = jdbcIdx;
    this.sqlType = sqlType;
    this.key = key;
  }

  @Override
  public int getSqlType() {
    return sqlType;
  }

  private Tuple<String, Object> getEntry(ResultSet rs) throws Throwable {
    return Tuple.of(key, method.call(rs, jdbcIdx));
  }
}
