package nl.naturalis.yokete.util;

import java.io.OutputStream;
import java.io.PrintStream;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import nl.naturalis.common.ExceptionMethods;
import nl.naturalis.common.check.Check;
import static nl.naturalis.common.check.CommonChecks.gt;

public class ResultSetMappifier {

  private final ColumnReader[] readers;
  private final int mapSize;

  ResultSetMappifier(ColumnReader[] infos) {
    this(infos, infos.length);
  }

  ResultSetMappifier(ColumnReader[] infos, int mapSize) {
    this.readers = infos;
    this.mapSize = mapSize;
  }

  /**
   * For performance reasons the {@code mappify} methods do not verify whether they can actually
   * mappify the {@code ResultSet} instance that is passed to them. They assume that the {@code
   * ResultSet} has the same metadata (column count and column types) as the {@code ResultSet} that
   * was used to create the mappifier. Strictly speaking, though, they don't know that. If they get
   * passed a {@code ResultSet} with a different layout, all sorts of errors might ensue, including
   * an {@link ArrayIndexOutOfBoundsException}. If you want to be absolutely sure that won't happen,
   * you can call this method first.
   *
   * @param rs
   * @return
   * @throws SQLException
   */
  public boolean canMappify(ResultSet rs) throws SQLException {
    ResultSetMetaData rsmd = rs.getMetaData();
    if (rsmd.getColumnCount() != readers.length) {
      return false;
    }
    for (int i = 0; i < readers.length; ++i) {
      if (readers[i].type != rsmd.getColumnType(i + 1)) {
        return false;
      }
    }
    return true;
  }

  public void printMismatch(ResultSet rs, OutputStream out) throws SQLException {
    Check.notNull(rs);
    Check.notNull(out);
    PrintStream ps = out.getClass() == PrintStream.class ? (PrintStream) out : new PrintStream(out);
    ResultSetMetaData rsmd = rs.getMetaData();
    int mismatches = 0;
    if (rsmd.getColumnCount() != readers.length) {
      ++mismatches;
      String fmt = "Expected column count: %d. Actual column count: %d%n";
      ps.printf(fmt, readers.length, rsmd.getColumnCount());
    }
    int min = Math.min(readers.length, rsmd.getColumnCount());
    for (int i = 0; i < min; ++i) {
      if (readers[i].type != rsmd.getColumnType(i + 1)) {
        ++mismatches;
        String expected = SQLTypeNames.getTypeName(readers[i].type);
        String actual = SQLTypeNames.getTypeName(rsmd.getColumnType(i + 1));
        String fmt = "Colum %3d: expected type: %s; actual type: %s%n";
        ps.printf(fmt, i + 1, expected, actual);
      }
    }
    if (mismatches == 0) {
      ps.println("No mismatch found");
    }
  }

  public Map<String, Object> mappify(ResultSet rs) {
    Check.notNull(rs);
    try {
      return ColumnReader.toMap(rs, readers, mapSize);
    } catch (Throwable t) {
      throw ExceptionMethods.uncheck(t);
    }
  }

  public List<Map<String, Object>> mappify(ResultSet rs, int limit) {
    Check.notNull(rs, "rs");
    Check.that(limit, "limit").is(gt(), 0);
    List<Map<String, Object>> all = new ArrayList<>(limit);
    try {
      for (int i = 0; rs.next() && i < limit; ++i) {
        all.add(ColumnReader.toMap(rs, readers, mapSize));
      }
    } catch (Throwable t) {
      throw ExceptionMethods.uncheck(t);
    }
    return all;
  }

  public List<Map<String, Object>> mappifyAll(ResultSet rs, int expectedSize) {
    Check.notNull(rs, "rs");
    Check.that(expectedSize, "expectedSize").is(gt(), 0);
    List<Map<String, Object>> all = new ArrayList<>(expectedSize);
    try {
      while (rs.next()) {
        all.add(ColumnReader.toMap(rs, readers, mapSize));
      }
    } catch (Throwable t) {
      throw ExceptionMethods.uncheck(t);
    }
    return all;
  }
}
