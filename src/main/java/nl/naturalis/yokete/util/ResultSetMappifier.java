package nl.naturalis.yokete.util;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import nl.naturalis.common.ExceptionMethods;
import nl.naturalis.common.check.Check;
import static nl.naturalis.common.check.CommonChecks.gt;

/**
 * Converts JDBC {@link ResultSet} instances into <code>Map&lt;String, Object&gt;</code> instances
 * or <code>List&lt;Map&lt;String, Object&gt;&gt;</code>, depending on whether you want to mappify
 * just a single row from the {@code ResultSet} or multiple rows. To be more precize: it converts
 * them into instances of {@link Row} or {@link QueryResult}, which are extensions of <code>
 * HashMap&lt;String, Object&gt;</code> and <code>ArrayList&lt;Row&gt;</code> respectively. These
 * subclasses provide some extra methods useful when reading query results but otherwise don't alter
 * their behaviour.
 *
 * <p>You cannot instatiate a {@code ResultSetMappifier} directly. You can obtain an instance from a
 * {@link MappifierBox}.
 *
 * @author Ayco Holleman
 */
public class ResultSetMappifier {

  final KeyWriter[] writers;

  ResultSetMappifier(KeyWriter[] writers) {
    this.writers = writers;
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
  public Optional<Row> mappify(ResultSet rs) {
    Check.notNull(rs);
    try {
      return Optional.of(KeyWriter.toRow(rs, writers));
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
  public List<Row> mappifyAtMost(ResultSet rs, int limit) {
    Check.notNull(rs, "rs");
    Check.that(limit, "limit").is(gt(), 0);
    List<Row> all = new ArrayList<>(limit);
    int i = 0;
    try {
      do {
        all.add(KeyWriter.toRow(rs, writers));
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
  public List<Row> mappifyAll(ResultSet rs) {
    return mappifyAll(rs, 16);
  }

  /**
   * Converts all rows within the specified {@code ResultSet} to <code>
   * Map&lt;String,Object&gt;</code> instances. {@link ResultSet#next()} <b>must</b> have been
   * called first, and it <b>must</b> have returned true. This method will only call {@code
   * ResultSet.next()} <i>after</i> each conversion.
   *
   * @param rs The {@code ResultSet}
   * @param sizeEstimate An estimate of the total size of the result set. Will be used to initialize
   *     the returned {@code List}
   * @return A {@code List} of <code>Map&lt;String,Object&gt;</code> instances
   */
  public List<Row> mappifyAll(ResultSet rs, int sizeEstimate) {
    Check.notNull(rs, "rs");
    Check.that(sizeEstimate, "sizeEstimate").is(gt(), 0);
    List<Row> all = new ArrayList<>(sizeEstimate);
    try {
      while (rs.next()) {
        all.add(KeyWriter.toRow(rs, writers));
      }
    } catch (Throwable t) {
      throw ExceptionMethods.uncheck(t);
    }
    return all;
  }
}
