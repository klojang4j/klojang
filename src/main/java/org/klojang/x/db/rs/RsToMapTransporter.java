package org.klojang.x.db.rs;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import org.klojang.db.Row;
import org.klojang.render.NameMapper;
import nl.naturalis.common.ExceptionMethods;

/* Transports a single value from a ResultSet to a Map<String,Object> */
public class RsToMapTransporter<COLUMN_TYPE> implements ValueTransporter {

  public static Row toRow(ResultSet rs, RsToMapTransporter<?>[] setters) throws Throwable {
    Column[] entries = new Column[setters.length];
    for (int i = 0; i < setters.length; ++i) {
      entries[i] = setters[i].getEntry(rs);
    }
    return Row.withColumns(entries);
  }

  public static RsToMapTransporter<?>[] createValueTransporters(ResultSet rs, NameMapper mapper) {
    RsMethods methods = RsMethods.getInstance();
    try {
      ResultSetMetaData rsmd = rs.getMetaData();
      int sz = rsmd.getColumnCount();
      RsToMapTransporter<?>[] transporters = new RsToMapTransporter[sz];
      for (int idx = 0; idx < sz; ++idx) {
        int jdbcIdx = idx + 1; // JDBC is one-based
        int sqlType = rsmd.getColumnType(jdbcIdx);
        RsMethod<?> method = methods.getMethod(sqlType);
        String label = rsmd.getColumnLabel(jdbcIdx);
        String key = mapper.map(label);
        transporters[idx] = new RsToMapTransporter<>(method, jdbcIdx, sqlType, key);
      }
      return transporters;
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

  private Column getEntry(ResultSet rs) throws Throwable {
    return new Column(key, method.call(rs, jdbcIdx));
  }
}
