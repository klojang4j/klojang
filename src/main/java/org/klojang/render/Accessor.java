package org.klojang.render;

/**
 * Interface for objects implementing a mechanism to access source data for templates. See {@link
 * AccessorFactory} for more information.
 *
 * @author Ayco Holleman
 */
@FunctionalInterface
public interface Accessor<T> {

  /**
   * The value that must be returned if the variable whose value to retrieve is not present in the
   * source data.
   */
  public static final Object UNDEFINED = new Object();

  /**
   * Returns the value of the specified property within the specified source data object. The term
   * "property" is somewhat misleading here, because {@code data} could be anything. It could also
   * be a {@code Map} and {@code property} would then (most likely) specify a map key.
   *
   * @param date The data to be accessed
   * @param property The name by which to retrieve the desired value from the data
   * @return The value
   * @throws RenderException
   */
  Object access(T date, String property) throws RenderException;
}
