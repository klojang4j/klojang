package nl.naturalis.yokete.util;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.UnaryOperator;
import nl.naturalis.common.check.Check;

public class MappifierBox {

  private final AtomicReference<ResultSetMappifier> ref = new AtomicReference<>();

  private final UnaryOperator<String> mapper;
  private final boolean verify;

  public MappifierBox() {
    this(x -> x);
  }

  public MappifierBox(UnaryOperator<String> columToKeyMapper) {
    this(columToKeyMapper, false);
  }

  public MappifierBox(UnaryOperator<String> columToKeyMapper, boolean verify) {
    this.mapper = Check.notNull(columToKeyMapper).ok();
    this.verify = verify;
  }

  public ResultSetMappifier get(ResultSet rs) throws SQLException {
    if (!rs.next()) {
      return EmptyMappifier.INSTANCE;
    }
    ResultSetMappifier rsm;
    if ((rsm = ref.get()) == null) {
      synchronized (this) {
        rsm = new ResultSetMappifier(createWriters(rs, mapper));
        ref.setPlain(rsm);
      }
    } else if (verify) {
      Writer.checkCompatibility(rs, rsm.writers);
    }
    return rsm;
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
