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
   * The value that <i>must</i> be returned if the template variable (or nested template name) could
   * not be mapped to
   */
  public static final Object UNDEFINED = new Object();

  /**
   * Returns the value of the specified property within the specified model object. The term
   * "property" is somewhat misleading here, because the {@code data} argument can be anything a
   * specific {@code Accessor} implementation decides to take care of. It could, for example, also
   * be a {@code Map} and {@code property} would then (most likely) specify a map key.
   *
   * @param data The data to be accessed
   * @param property The name by which to retrieve the desired value from the data
   * @return The value
   * @throws RenderException
   */
  Object access(T data, String property) throws RenderException;
}
