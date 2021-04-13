package nl.naturalis.yokete.util;

import java.io.OutputStream;
import java.io.PrintStream;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import nl.naturalis.common.ExceptionMethods;
import nl.naturalis.common.check.Check;
import static nl.naturalis.common.check.CommonChecks.gt;

/** @author Ayco Holleman */
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

  public void printResultSetMismatch(ResultSet rs, OutputStream out) throws SQLException {
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

  /**
   * Converts the current record within the specified {@code ResultSet} to a <code>
   * Map&lt;String,Object&gt;</code> with keys corresponding to column labels and values
   * corresponding to column values. {@link ResultSet#next()} <b>must</b> have been called first,
   * and it <b>must</b> have returned true. This method does not call {@link ResultSet#next()}
   * either before or after the conversion.
   *
   * @param rs The {@code ResultSet}
   * @return A {@code Map} containing the values of the current record within the specified {@code
   *     ResultSet}.
   */
  public Row mappify(ResultSet rs) {
    Check.notNull(rs);
    try {
      return ColumnReader.toMap(rs, readers, mapSize);
    } catch (Throwable t) {
      throw ExceptionMethods.uncheck(t);
    }
  }

  /**
   * Converts at most {@code limit} records within the specified {@code ResultSet} to <code>
   * Map&lt;String,Object&gt;</code> instances. {@link ResultSet#next()} <b>must</b> have been
   * called first, and it <b>must</b> have returned true. This method will only call {@code
   * ResultSet.next()} <i>after</i> each conversion.
   *
   * @param rs The {@code ResultSet}
   * @param limit The maximum number of records to mappify
   * @return A {@code List} of <code>Map&lt;String,Object&gt;</code> instances
   */
  public QueryResult mappify(ResultSet rs, int limit) {
    Check.notNull(rs, "rs");
    Check.that(limit, "limit").is(gt(), 0);
    QueryResult all = new QueryResult(limit);
    int i = 0;
    try {
      do {
        all.add(ColumnReader.toMap(rs, readers, mapSize));
      } while (++i < limit && rs.next());
    } catch (Throwable t) {
      throw ExceptionMethods.uncheck(t);
    }
    return all;
  }

  /**
   * Converts allrecords within the specified {@code ResultSet} to <code>
   * Map&lt;String,Object&gt;</code> instances. {@link ResultSet#next()} <b>must</b> have been
   * called first, and it <b>must</b> have returned true. This method will only call {@code
   * ResultSet.next()} <i>after</i> each conversion.
   *
   * @param rs The {@code ResultSet}
   * @param limit The maximum number of records to mappify
   * @return A {@code List} of <code>Map&lt;String,Object&gt;</code> instances
   */
  public QueryResult mappifyAll(ResultSet rs, int expectedSize) {
    Check.notNull(rs, "rs");
    Check.that(expectedSize, "expectedSize").is(gt(), 0);
    QueryResult all = new QueryResult(expectedSize);
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
