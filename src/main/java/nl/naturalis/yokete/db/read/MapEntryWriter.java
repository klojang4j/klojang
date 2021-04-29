package nl.naturalis.yokete.db.read;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Map;
import java.util.function.UnaryOperator;
import nl.naturalis.common.ModulePrivate;
import nl.naturalis.yokete.db.Row;
import static java.util.Map.Entry;

@ModulePrivate
public class MapEntryWriter<COLUMN_TYPE> implements Writer {

  public static Row toRow(ResultSet rs, MapEntryWriter<?>[] writers) throws Throwable {
    @SuppressWarnings("unchecked")
    Entry<String, Object>[] entries = new Entry[writers.length];
    for (int i = 0; i < writers.length; ++i) {
      entries[i] = writers[i].transferValue(rs);
    }
    return Row.withData(Map.ofEntries(entries));
  }

  public static MapEntryWriter<?>[] createWriters(ResultSet rs, UnaryOperator<String> mapper)
      throws SQLException {
    ColumnReaders getters = ColumnReaders.getInstance();
    ResultSetMetaData rsmd = rs.getMetaData();
    int sz = rsmd.getColumnCount();
    MapEntryWriter<?>[] writers = new MapEntryWriter[sz];
    for (int idx = 0; idx < sz; ++idx) {
      int jdbcIdx = idx + 1; // JDBC is one-based
      int sqlType = rsmd.getColumnType(jdbcIdx);
      ColumnReader<?> getter = getters.getReader(sqlType);
      String label = rsmd.getColumnLabel(jdbcIdx);
      String mapKey = mapper.apply(label);
      writers[idx] = new MapEntryWriter<>(getter, jdbcIdx, sqlType, mapKey);
    }
    return writers;
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
