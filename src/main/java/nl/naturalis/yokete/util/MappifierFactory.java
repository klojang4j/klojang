package nl.naturalis.yokete.util;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.function.UnaryOperator;
import nl.naturalis.common.check.Check;

public class MappifierFactory {

  private static MappifierFactory INSTANCE;

  public static MappifierFactory instance() {
    if (INSTANCE == null) {
      INSTANCE = new MappifierFactory();
    }
    return INSTANCE;
  }

  private MappifierFactory() {}

  /**
   * Creates a {@code ResultSetMappifier} that will produce {@link Row} instances reflecting the
   * {@link ResultSetMetaData} of the specified {@code ResultSet}.
   *
   * @param rs
   * @return
   * @throws SQLException
   */
  public ResultSetMappifier getMappifier(ResultSet rs) throws SQLException {
    return getMappifier(rs, x -> x);
  }

  public ResultSetMappifier getMappifier(ResultSet rs, UnaryOperator<String> columnToKeyMapper)
      throws SQLException {
    Check.notNull(rs, "rs");
    Check.notNull(columnToKeyMapper, "columnToKeyMapper");
    KeyWriter[] writers = createWriters(rs, columnToKeyMapper);
    return new ResultSetMappifier(writers);
  }

  private static KeyWriter[] createWriters(ResultSet rs, UnaryOperator<String> mapper)
      throws SQLException {
    ColumnReaders getters = ColumnReaders.getInstance();
    ResultSetMetaData rsmd = rs.getMetaData();
    int sz = rsmd.getColumnCount();
    KeyWriter[] writers = new KeyWriter[sz];
    for (int idx = 0; idx < sz; ++idx) {
      int jdbcIdx = idx + 1; // JDBC is one-based
      int sqlType = rsmd.getColumnType(jdbcIdx);
      ColumnReader getter = getters.getReader(sqlType);
      String label = rsmd.getColumnLabel(jdbcIdx);
      String mapKey = mapper.apply(label);
      writers[idx] = new KeyWriter(getter, jdbcIdx, sqlType, mapKey);
    }
    return writers;
  }
}
