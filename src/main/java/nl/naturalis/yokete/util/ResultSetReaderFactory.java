package nl.naturalis.yokete.util;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Map;
import nl.naturalis.common.check.Check;
import nl.naturalis.common.invoke.Setter;
import nl.naturalis.common.invoke.SetterFactory;
import nl.naturalis.yokete.template.Template;

public class ResultSetReaderFactory {

  private static ResultSetReaderFactory instance;

  public static ResultSetReaderFactory instance() {
    if (instance == null) {
      instance = new ResultSetReaderFactory();
    }
    return instance;
  }

  private final ResultSetGetters rsGetters;

  private ResultSetReaderFactory() {
    rsGetters = ResultSetGetters.getInstance();
  }

  public ResultSetMappifier getMappifier(ResultSet rs) throws SQLException {
    return getMappifier(rs, 0);
  }

  /**
   * Creates a {@code ResultSetMappifier} that will produce {@link Row} instances that will be sized
   * somewhat larger than the number of columns within the {@code ResultSet}. Contrary to the {@code
   * ResultSet} class itself, you are free to add extra entries to the {@code Row} (which really is
   * a <code>HashMap&lt;String,Object&gt;&gt;</code>) after it has been produced. This may come in
   * handy as the {@code Row} object is likely to be used to fill a {@link Template}, and you might
   * want to enrich it first before doing so.
   *
   * @param rs
   * @param extraRowSize
   * @return
   * @throws SQLException
   */
  public ResultSetMappifier getMappifier(ResultSet rs, int extraRowSize) throws SQLException {
    ColumnReader[] infos = createColumnReaders(rs);
    return new ResultSetMappifier(infos, infos.length + extraRowSize);
  }

  public <T> ResultSetBeanifier<T> getBeanifier(Class<T> beanClass, ResultSet rs)
      throws SQLException {
    Check.notNull(beanClass, "beanClass");
    ResultSetMetaData rsmd = Check.notNull(rs).ok().getMetaData();
    Map<String, Setter<?>> setters = SetterFactory.INSTANCE.getSetters(beanClass);
    for (Map.Entry<String, Setter<?>> entry : setters.entrySet()) {
      String prop = entry.getKey();
      Setter<?> setter = entry.getValue();
      int jdbcIdx;
      try {
        jdbcIdx = rs.findColumn(prop);
      } catch (SQLException e) { // No corresponding column
        continue;
      }
      int sqlType = rsmd.getColumnType(jdbcIdx);
      ResultSetGetter getter = rsGetters.getGetter(sqlType);
    }
    return null;
  }

  private ColumnReader[] createColumnReaders(ResultSet rs) throws SQLException {
    ResultSetMetaData rsmd = Check.notNull(rs).ok().getMetaData();
    int sz = rsmd.getColumnCount();
    ColumnReader[] infos = new ColumnReader[sz];
    for (int idx = 0; idx < sz; ++idx) {
      int jdbcIdx = idx + 1; // JDBC is one-based
      int sqlType = rsmd.getColumnType(jdbcIdx);
      ResultSetGetter invoker = rsGetters.getGetter(sqlType);
      String label = rsmd.getColumnLabel(jdbcIdx);
      infos[idx] = new ColumnReader(jdbcIdx, label, sqlType, invoker);
    }
    return infos;
  }
}
