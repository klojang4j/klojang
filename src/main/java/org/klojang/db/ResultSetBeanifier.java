package org.klojang.db;

import java.sql.ResultSet;
import java.util.List;
import java.util.Optional;
import nl.naturalis.common.Emptyable;

/**
 * Converts JDBC {@link ResultSet result sets} to JavaBeans. Contrary to {@link SQLQuery} a {@code
 * ResultSetBeanifier} is agnostic about how you obtained the {@code ResultSet} and does not help
 * you with it. You cannot instantiate a {@code ResultSetBeanifier} directly. You obtain one from a
 * {@link BeanifierFactory}. When using a {@code ResultSetBeanifier} to iterate over a {@code
 * ResultSet} you should not call {@link ResultSet#next()}) yourself. This is done by the {@code
 * ResultSetBeanifier}.
 *
 * @author Ayco Holleman
 * @param <T> The type of the JavaBean
 */
public interface ResultSetBeanifier<T> extends AutoCloseable, Emptyable {

  /**
   * Converts the current row within the specified {@code ResultSet} into a JavaBean. If the {@code
   * ResultSet} is empty, or if there are no more rows in the {@code ResultSet}, an empty {@code
   * ResultSet} is returned.
   *
   * @param rs The {@code ResultSet}
   * @return An {@code Optional} containing the JavaBean or an empty {@code Optional} if the {@code
   *     ResultSet} contained no (more) rows
   */
  Optional<T> beanify();

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
  List<T> beanifyAtMost(int limit);

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
  List<T> beanifyAtMost(int from, int limit);

  /**
   * Extract and converts all remaining rows within the specified {@code ResultSet} into JavaBeans.
   *
   * @param rs The {@code ResultSet}
   * @return A {@code List} of JavaBeans or an empty {@code List} if the {@code ResultSet} contained
   *     no (more) rows
   */
  List<T> beanifyAll();

  /**
   * Extract and converts all remaining rows within the specified {@code ResultSet} into JavaBeans.
   *
   * @param rs The {@code ResultSet}
   * @param sizeEstimate An estimate of the size of the resulting {@code List}. Will be passed on as
   *     the {@code initialCapacity} argument to the {@code ArrayList} constructor.
   * @return A {@code List} of JavaBeans or an empty {@code List} if the {@code ResultSet} contained
   *     no (more) rows
   */
  List<T> beanifyAll(int sizeEstimate);

  /**
   * Overrides {@link AutoCloseable}'s {@code close} method, making it <i>not</i> throw any checked
   * exception.
   */
  void close();
}
