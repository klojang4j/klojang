package org.klojang.db;

import java.sql.ResultSet;
import java.util.List;
import java.util.Optional;
import nl.naturalis.common.Emptyable;

/**
 * Converts the rows in a JDBC {@link ResultSet result sets} into JavaBeans. Contrary to the {@link
 * SQLQuery} class a {@code ResultSetMappifier} is completely agnostic about how you got hold of the
 * {@code ResultSet} and by what means it was created. You cannot instantiate a {@code
 * ResultSetMappifier} directly. You obtain one from a {@link MappifierFactory}. When using a {@code
 * ResultSetMappifier} to iterate over a {@code ResultSet}, do not call {@link ResultSet#next()})
 * yourself. This is done by the {@code ResultSetMappifier}. Just keep calling the {@code
 * #mappify()} until an empty {@code Optional} or {@code List} is returned, or {@link #isEmpty()}
 * returns {@code true}.
 *
 * @author Ayco Holleman
 */
public interface ResultSetMappifier extends Emptyable, Iterable<Row> {

  /**
   * Converts the current row within the specified {@code ResultSet} into a {@link Row} object. If
   * the {@code ResultSet} is empty, or if there are no more rows in the {@code ResultSet}, an empty
   * {@code Optional} is returned.
   *
   * @return A {@code Row} object
   */
  Optional<Row> mappify();

  /**
   * Converts at most {@code limit} rows from the specified {@code ResultSet} into {@link Row}
   * objects. If the {@code ResultSet} is empty, an empty {@code List} is returned.
   *
   * @param limit The maximum number of records to mappify
   * @return A {@code List} of {@code Row} objects or an empty {@code List} if the {@code ResultSet}
   *     contained no (more) rows
   */
  List<Row> mappify(int limit);

  /**
   * Converts all remaining rows in a {@code ResultSet} into {@link Row} objects.
   *
   * @return A {@code List} of {@code Row} objects or an empty {@code List} if the {@code ResultSet}
   *     contained no (more) rows
   */
  List<Row> mappifyAll();

  /**
   * Converts all remaining rows in a {@code ResultSet} into {@link Row} instances.
   *
   * @param sizeEstimate An estimate of the total size of the result set
   * @return A {@code List} of {@code Row} objects or an empty {@code List} if the {@code ResultSet}
   *     contained no (more) rows
   */
  List<Row> mappifyAll(int sizeEstimate);
}
