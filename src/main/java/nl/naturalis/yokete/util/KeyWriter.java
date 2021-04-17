package nl.naturalis.yokete.util;

import java.sql.ResultSet;

class KeyWriter {

  static Row toMap(ResultSet rs, KeyWriter[] writers) throws Throwable {
    Row map = new Row(writers.length);
    for (KeyWriter writer : writers) {
      map.put(writer.mapKey, writer.readColumn(rs));
    }
    return map;
  }

  private final ColumnReader getter;
  private final int jdbcIdx;
  private final int sqlType;
  private final String mapKey;

  KeyWriter(ColumnReader getter, int jdbcIdx, int sqlType, String mapKey) {
    this.getter = getter;
    this.jdbcIdx = jdbcIdx;
    this.sqlType = sqlType;
    this.mapKey = mapKey;
  }

  int getSqlType() {
    return sqlType;
  }

  private Object readColumn(ResultSet rs) throws Throwable {
    if (getter.getClassArgument() == null) {
      return getter.getMethod().invoke(rs, jdbcIdx);
    }
    return getter.getMethod().invoke(rs, jdbcIdx, getter.getClassArgument());
  }
}
