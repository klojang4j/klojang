package org.klojang.db;

import java.sql.ResultSet;
import java.util.List;
import java.util.Optional;

/**
 * Converts the rows in a JDBC {@link ResultSet result sets} into JavaBeans. Contrary
 * to the {@link SQLQuery} class a {@code ResultSetBeanifier} is completely agnostic
 * about how you got hold of the {@code ResultSet} and by what means it was created.
 * You cannot instantiate a {@code ResultSetBeanifier} directly. You obtain one from
 * a {@link BeanifierFactory}. When using a {@code ResultSetBeanifier} to iterate
 * over a {@code ResultSet}, do not call {@link ResultSet#next()}) yourself. This is
 * done by the {@code ResultSetBeanifier}. Just keep calling {@code #beanify()} until
 * an empty {@code Optional} or {@code List} is returned, or {@link #isEmpty()}
 * returns {@code true}.
 *
 * @param <T> The type of the JavaBeans produced by the {@code
 *     ResultSetBeanifier}
 * @author Ayco Holleman
 */
public interface ResultSetBeanifier<T> extends Iterable<T> {

  /**
   * Converts the current row within the specified {@code ResultSet} into a JavaBean.
   * If the {@code ResultSet} is empty, or if there are no more rows in the {@code
   * ResultSet}, an empty {@code Optional} is returned.
   *
   * @return An {@code Optional} containing the JavaBean or an empty {@code Optional}
   *     if the {@code ResultSet} contained no (more) rows
   */
  Optional<T> beanify();

  /**
   * Converts at most {@code limit} rows from the specified {@code ResultSet} into
   * JavaBeans. If the {@code ResultSet} is empty, an empty {@code List} is
   * returned.
   *
   * @param limit maximum number of rows to extract and convert
   * @return A {@code List} of JavaBeans or an empty {@code List} if the {@code
   *     ResultSet} contained no (more) rows
   */
  List<T> beanify(int limit);

  /**
   * Converts all remaining rows in the specified {@code ResultSet} into JavaBeans.
   *
   * @return A {@code List} of JavaBeans or an empty {@code List} if the {@code
   *     ResultSet} contained no (more) rows
   */
  List<T> beanifyAll();

  /**
   * Converts all remaining rows n the specified {@code ResultSet} into JavaBeans.
   *
   * @param sizeEstimate An estimate of the size of the resulting {@code List}.
   * @return A {@code List} of JavaBeans or an empty {@code List} if the {@code
   *     ResultSet} contained no (more) rows
   */
  List<T> beanifyAll(int sizeEstimate);

  /**
   * Returns {@code true} if the end of the {@code ResultSet} has been reached;
   * {@code false} otherwise.
   *
   * @return Whether the end of the {@code ResultSet} has been reached
   */
  boolean isEmpty();

}
