package nl.naturalis.yokete.db.rs;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.UnaryOperator;
import nl.naturalis.common.ExceptionMethods;
import nl.naturalis.common.ModulePrivate;
import nl.naturalis.common.Tuple;
import nl.naturalis.yokete.db.Row;

/* Transports a single value from a ResultSet to a Map<String,Object> */
@ModulePrivate
public class MapValueSetter<COLUMN_TYPE> implements Transporter {

  public static Row toRow(ResultSet rs, MapValueSetter<?>[] transporters) throws Throwable {
    @SuppressWarnings("unchecked")
    Tuple<String, Object>[] tuples = new Tuple[transporters.length];
    for (int i = 0; i < transporters.length; ++i) {
      tuples[i] = transporters[i].transferValue(rs);
    }
    return Row.withColumns(tuples);
  }

  public static Map<String, Object> toMap(ResultSet rs, MapValueSetter<?>[] transporters)
      throws Throwable {
    Map<String, Object> map = new HashMap<>(transporters.length);
    populateMap(rs, map, transporters);
    return map;
  }

  public static void populateMap(
      ResultSet rs, Map<String, Object> map, MapValueSetter<?>[] transporters) throws Throwable {
    for (int i = 0; i < transporters.length; ++i) {
      Tuple<String, Object> tuple = transporters[i].transferValue(rs);
      tuple.insertInto(map);
    }
  }

  public static MapValueSetter<?>[] createTransporters(ResultSet rs, UnaryOperator<String> mapper) {
    RSGetters getters = RSGetters.getInstance();
    try {
      ResultSetMetaData rsmd = rs.getMetaData();
      int sz = rsmd.getColumnCount();
      MapValueSetter<?>[] transporters = new MapValueSetter[sz];
      for (int idx = 0; idx < sz; ++idx) {
        int jdbcIdx = idx + 1; // JDBC is one-based
        int sqlType = rsmd.getColumnType(jdbcIdx);
        RSGetter<?> getter = getters.getReader(sqlType);
        String label = rsmd.getColumnLabel(jdbcIdx);
        String mapKey = mapper.apply(label);
        transporters[idx] = new MapValueSetter<>(getter, jdbcIdx, sqlType, mapKey);
      }
      return transporters;
    } catch (SQLException e) {
      throw ExceptionMethods.uncheck(e);
    }
  }

  private final RSGetter<COLUMN_TYPE> rsGetter;
  private final int jdbcIdx;
  private final int sqlType;
  private final String key;

  private MapValueSetter(RSGetter<COLUMN_TYPE> reader, int jdbcIdx, int sqlType, String key) {
    this.rsGetter = reader;
    this.jdbcIdx = jdbcIdx;
    this.sqlType = sqlType;
    this.key = key;
  }

  @Override
  public int getSqlType() {
    return sqlType;
  }

  private Tuple<String, Object> transferValue(ResultSet rs) throws Throwable {
    return Tuple.of(key, rsGetter.readColumn(rs, jdbcIdx));
  }
}
