package nl.naturalis.yokete.util;

import java.io.OutputStream;
import java.io.PrintStream;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import nl.naturalis.common.ExceptionMethods;
import nl.naturalis.common.check.Check;
import static nl.naturalis.common.check.CommonChecks.gt;

/**
 * Converts JDBC {@link ResultSet} instances into <code>Map&lt;String, Object&gt;</code> instances
 * or <code>List&lt;Map&lt;String, Object&gt;&gt;</code>, depending on whether you want to mappify
 * just a single row from the {@code ResultSet} or multiple rows. To be more precize: it converts
 * them into instances of {@link Row} or {@link QueryResult}, which are extensions of <code>
 * HashMap&lt;String, Object&gt;</code> and <code>ArrayList&lt;Row&gt;</code>. These subclasses
 * provide some extra methods useful when reading query results but otherwise don't alter their
 * behaviour.
 *
 * <p>You cannot instatiate a {@code ResultSetMappifier} directly. You can obtain an instance from a
 * {@link MappifierFactory}.
 *
 * @author Ayco Holleman
 */
public class ResultSetMappifier {

  private final KeyWriter[] writers;

  ResultSetMappifier(KeyWriter[] writers) {
    this.writers = writers;
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
    if (rsmd.getColumnCount() != writers.length) {
      return false;
    }
    for (int i = 0; i < writers.length; ++i) {
      if (writers[i].getSqlType() != rsmd.getColumnType(i + 1)) {
        return false;
      }
    }
    return true;
  }

  /**
   * If {@link #canMappify(ResultSet) canMappify} returned {@code false}, this method can be used to
   * print the difference between the {@link ResultSetMetaData} that this {@code ResultSetMappifier}
   * was created from and the {@code ResultSetMetaData} of the specified {@code ResultSet}.
   *
   * @param rs
   * @param out
   * @throws SQLException
   */
  public void printResultSetMismatch(ResultSet rs, OutputStream out) throws SQLException {
    Check.notNull(rs);
    Check.notNull(out);
    PrintStream ps = out.getClass() == PrintStream.class ? (PrintStream) out : new PrintStream(out);
    ResultSetMetaData rsmd = rs.getMetaData();
    int mismatches = 0;
    if (rsmd.getColumnCount() != writers.length) {
      ++mismatches;
      String fmt = "Expected column count: %d. Actual column count: %d%n";
      ps.printf(fmt, writers.length, rsmd.getColumnCount());
    }
    int min = Math.min(writers.length, rsmd.getColumnCount());
    for (int i = 0; i < min; ++i) {
      if (writers[i].getSqlType() != rsmd.getColumnType(i + 1)) {
        ++mismatches;
        String expected = SQLTypeNames.getTypeName(writers[i].getSqlType());
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
      return KeyWriter.toMap(rs, writers);
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
  public QueryResult mappifyAtMost(ResultSet rs, int limit) {
    Check.notNull(rs, "rs");
    Check.that(limit, "limit").is(gt(), 0);
    QueryResult all = new QueryResult(limit);
    int i = 0;
    try {
      do {
        all.add(KeyWriter.toMap(rs, writers));
      } while (++i < limit && rs.next());
    } catch (Throwable t) {
      throw ExceptionMethods.uncheck(t);
    }
    return all;
  }

  /**
   * Converts all rows within the specified {@code ResultSet} to <code>
   * Map&lt;String,Object&gt;</code> instances. {@link ResultSet#next()} <b>must</b> have been
   * called first, and it <b>must</b> have returned true. This method will only call {@code
   * ResultSet.next()} <i>after</i> each conversion.
   *
   * @param rs The {@code ResultSet}
   * @return A {@code List} of <code>Map&lt;String,Object&gt;</code> instances
   */
  public QueryResult mappifyAll(ResultSet rs) {
    return mappifyAll(rs, 16);
  }

  /**
   * Converts all rows within the specified {@code ResultSet} to <code>
   * Map&lt;String,Object&gt;</code> instances. {@link ResultSet#next()} <b>must</b> have been
   * called first, and it <b>must</b> have returned true. This method will only call {@code
   * ResultSet.next()} <i>after</i> each conversion.
   *
   * @param rs The {@code ResultSet}
   * @param limit An estimate of the total size of the result set. Will be used to initialize the
   *     returned {@code List}
   * @return A {@code List} of <code>Map&lt;String,Object&gt;</code> instances
   */
  public QueryResult mappifyAll(ResultSet rs, int sizeEstimate) {
    Check.notNull(rs, "rs");
    Check.that(sizeEstimate, "sizeEstimate").is(gt(), 0);
    QueryResult all = new QueryResult(sizeEstimate);
    try {
      while (rs.next()) {
        all.add(KeyWriter.toMap(rs, writers));
      }
    } catch (Throwable t) {
      throw ExceptionMethods.uncheck(t);
    }
    return all;
  }
}
