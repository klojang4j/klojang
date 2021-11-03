package org.klojang.db;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.klojang.x.db.rs.RowChannel;
import nl.naturalis.common.ExceptionMethods;
import nl.naturalis.common.check.Check;
import static nl.naturalis.common.check.CommonChecks.gt;

class DefaultMappifier implements ResultSetMappifier {

  private final ResultSet rs;
  private final RowChannel<?>[] transporters;

  DefaultMappifier(ResultSet rs, RowChannel<?>[] transporters) {
    this.rs = rs;
    this.transporters = transporters;
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
  @Override
  public Optional<Row> mappify() {
    Check.notNull(rs);
    try {
      return Optional.of(RowChannel.toRow(rs, transporters));
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
  @Override
  public List<Row> mappifyAtMost(int limit) {
    Check.notNull(rs, "rs");
    Check.that(limit, "limit").is(gt(), 0);
    List<Row> all = new ArrayList<>(limit);
    int i = 0;
    try {
      do {
        all.add(RowChannel.toRow(rs, transporters));
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
  @Override
  public List<Row> mappifyAll() {
    return mappifyAll(16);
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
  @Override
  public List<Row> mappifyAll(int sizeEstimate) {
    Check.notNull(rs, "rs");
    Check.that(sizeEstimate, "sizeEstimate").is(gt(), 0);
    List<Row> all = new ArrayList<>(sizeEstimate);
    try {
      do {
        all.add(RowChannel.toRow(rs, transporters));
      } while (rs.next());
    } catch (Throwable t) {
      throw ExceptionMethods.uncheck(t);
    }
    return all;
  }
}
