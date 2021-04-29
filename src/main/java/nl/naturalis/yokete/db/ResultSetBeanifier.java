package nl.naturalis.yokete.db;

import java.sql.ResultSet;
import java.util.List;
import java.util.Optional;

/**
 * A {@code ResultSetBeanifier} converts JDBC {@link ResultSet result sets} to JavaBeans. A single
 * {@code ResultSetBeanifier} exactly one SQL query. You cannot instantiate a {@code
 * ResultSetBeanifier} directly. Instead you obtain one from a {@link BeanifierBox}. When using a
 * {@code ResultSetBeanifier} to iterate over a {@code ResultSet} you should not call {@link
 * ResultSet#next()}) yourself. This is done by the {@code ResultSetBeanifier}.
 *
 * @author Ayco Holleman
 * @param <T> The type of the JavaBean
 */
public interface ResultSetBeanifier<T> {

  /**
   * Converts the current row within the specified {@code ResultSet} into a JavaBean. If the {@code
   * ResultSet} is empty, or if there are no more rows in the {@code ResultSet}, an empty {@code
   * ResultSet} is returned.
   *
   * @param rs The {@code ResultSet}
   * @return An {@code Optional} containing the JavaBean or an empty {@code Optional} if the {@code
   *     ResultSet} contained no (more) rows
   */
  Optional<T> beanify(ResultSet rs);

  /**
   * Extracts and converts at most {@code limit} rows from the specified {@code ResultSet} into
   * JavaBeans. If the {@code ResultSet} is empty, or if there are no more rows in the {@code
   * ResultSet}, an empty {@code List} is returned.
   *
   * @param rs The {@code ResultSet}
   * @param limit maximum number of rows to extract and convert
   * @return A {@code List} of JavaBeans or an empty {@code List} if the {@code ResultSet} contained
   *     no (more) rows
   */
  List<T> beanifyAtMost(ResultSet rs, int limit);

  /**
   * First skips {@code from} rows and then extracts and converts at most {@code limit} rows from
   * the specified {@code ResultSet} into a JavaBeans. This method will not throw an exception when
   * attempting to read past the end of the {@code ResultSet}. Instead, it will return an empty
   * {@code List}.
   *
   * @param rs The {@code ResultSet}
   * @param from The number of rows to skip
   * @param limit maximum number of rows to extract and convert
   * @return A {@code List} of JavaBeans or an empty {@code List} if the {@code ResultSet} contained
   *     no (more) rows
   */
  List<T> beanifyAtMost(ResultSet rs, int from, int limit);

  /**
   * Extract and converts all remaining rows within the specified {@code ResultSet} into JavaBeans.
   *
   * @param rs The {@code ResultSet}
   * @return A {@code List} of JavaBeans or an empty {@code List} if the {@code ResultSet} contained
   *     no (more) rows
   */
  List<T> beanifyAll(ResultSet rs);

  /**
   * Extract and converts all remaining rows within the specified {@code ResultSet} into JavaBeans.
   *
   * @param rs The {@code ResultSet}
   * @param sizeEstimate An estimate of the size of the resulting {@code List}. Will be passed on as
   *     the {@code initialCapacity} argument to the {@code ArrayList} constructor.
   * @return A {@code List} of JavaBeans or an empty {@code List} if the {@code ResultSet} contained
   *     no (more) rows
   */
  List<T> beanifyAll(ResultSet rs, int sizeEstimate);
}
