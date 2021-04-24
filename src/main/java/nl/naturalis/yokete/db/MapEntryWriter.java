package nl.naturalis.yokete.db;

import java.sql.ResultSet;
import java.util.Map;
import static java.util.Map.Entry;

class MapEntryWriter<COLUMN_TYPE> implements Writer {

  static Row toRow(ResultSet rs, MapEntryWriter<?>[] writers) throws Throwable {
    @SuppressWarnings("unchecked")
    Entry<String, Object>[] entries = new Entry[writers.length];
    for (int i = 0; i < writers.length; ++i) {
      entries[i] = writers[i].transferValue(rs);
    }
    return new Row(Map.ofEntries(entries));
  }

  private final ColumnReader<COLUMN_TYPE> reader;
  private final int jdbcIdx;
  private final int sqlType;
  private final String key;

  MapEntryWriter(ColumnReader<COLUMN_TYPE> reader, int jdbcIdx, int sqlType, String key) {
    this.reader = reader;
    this.jdbcIdx = jdbcIdx;
    this.sqlType = sqlType;
    this.key = key;
  }

  @Override
  public int getSqlType() {
    return sqlType;
  }

  private Entry<String, Object> transferValue(ResultSet rs) throws Throwable {
    Object val = reader.readColumn(rs, jdbcIdx);
    return Map.entry(key, val);
  }
}
