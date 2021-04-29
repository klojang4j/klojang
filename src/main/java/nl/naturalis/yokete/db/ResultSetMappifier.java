package nl.naturalis.yokete.db;

import java.sql.ResultSet;
import java.util.List;
import java.util.Optional;

public interface ResultSetMappifier {

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
  Optional<Row> mappify(ResultSet rs);

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
  List<Row> mappifyAtMost(ResultSet rs, int limit);

  /**
   * Converts all rows within the specified {@code ResultSet} to <code>
   * Map&lt;String,Object&gt;</code> instances. {@link ResultSet#next()} <b>must</b> have been
   * called first, and it <b>must</b> have returned true. This method will only call {@code
   * ResultSet.next()} <i>after</i> each conversion.
   *
   * @param rs The {@code ResultSet}
   * @return A {@code List} of <code>Map&lt;String,Object&gt;</code> instances
   */
  List<Row> mappifyAll(ResultSet rs);

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
  List<Row> mappifyAll(ResultSet rs, int sizeEstimate);
}