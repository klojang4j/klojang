package nl.naturalis.yokete.db.rs;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Map;
import java.util.function.UnaryOperator;
import nl.naturalis.common.ModulePrivate;
import nl.naturalis.yokete.db.Row;
import static java.util.Map.Entry;

/**
 * Transports a single value <i>out of</i> a {@code ResultSet} and <i>into</i> a <code>
 * Map&lt;String,Object&gt;</code> instance.
 *
 * @author Ayco Holleman
 * @param <COLUMN_TYPE>
 */
@ModulePrivate
public class MapValueTransporter<COLUMN_TYPE> implements Transporter {

  public static Row toRow(ResultSet rs, MapValueTransporter<?>[] transporters) throws Throwable {
    @SuppressWarnings("unchecked")
    Entry<String, Object>[] entries = new Entry[transporters.length];
    for (int i = 0; i < transporters.length; ++i) {
      entries[i] = transporters[i].transferValue(rs);
    }
    return Row.withData(Map.ofEntries(entries));
  }

  public static MapValueTransporter<?>[] createTransporters(ResultSet rs, UnaryOperator<String> mapper)
      throws SQLException {
    RSGetters getters = RSGetters.getInstance();
    ResultSetMetaData rsmd = rs.getMetaData();
    int sz = rsmd.getColumnCount();
    MapValueTransporter<?>[] transporters = new MapValueTransporter[sz];
    for (int idx = 0; idx < sz; ++idx) {
      int jdbcIdx = idx + 1; // JDBC is one-based
      int sqlType = rsmd.getColumnType(jdbcIdx);
      RSGetter<?> getter = getters.getReader(sqlType);
      String label = rsmd.getColumnLabel(jdbcIdx);
      String mapKey = mapper.apply(label);
      transporters[idx] = new MapValueTransporter<>(getter, jdbcIdx, sqlType, mapKey);
    }
    return transporters;
  }

  private final RSGetter<COLUMN_TYPE> rsGetter;
  private final int jdbcIdx;
  private final int sqlType;
  private final String key;

  private MapValueTransporter(RSGetter<COLUMN_TYPE> reader, int jdbcIdx, int sqlType, String key) {
    this.rsGetter = reader;
    this.jdbcIdx = jdbcIdx;
    this.sqlType = sqlType;
    this.key = key;
  }

  @Override
  public int getSqlType() {
    return sqlType;
  }

  private Entry<String, Object> transferValue(ResultSet rs) throws Throwable {
    Object val = rsGetter.readColumn(rs, jdbcIdx);
    return Map.entry(key, val);
  }
}
