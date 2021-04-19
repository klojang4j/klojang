package nl.naturalis.yokete.util;

import java.sql.ResultSet;
import java.util.Map;
import static java.util.Map.Entry;

class KeyWriter implements Writer {

  static Row toRow(ResultSet rs, KeyWriter[] writers) throws Throwable {
    @SuppressWarnings("unchecked")
    Entry<String, Object>[] entries = new Entry[writers.length];
    for (int i = 0; i < writers.length; ++i) {
      entries[i] = Map.entry(writers[i].mapKey, writers[i].readColumn(rs));
    }
    return new Row(Map.ofEntries(entries));
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

  @Override
  public int getSqlType() {
    return sqlType;
  }

  private Object readColumn(ResultSet rs) throws Throwable {
    Object v;
    if (getter.getClassArgument() == null) {
      v = getter.getMethod().invoke(rs, jdbcIdx);
    } else {
      v = getter.getMethod().invoke(rs, jdbcIdx, getter.getClassArgument());
    }
    return rs.wasNull() ? null : v;
  }
}
