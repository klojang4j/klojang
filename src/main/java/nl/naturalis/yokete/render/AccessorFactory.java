package nl.naturalis.yokete.render;

import nl.naturalis.yokete.template.Template;

/**
 * Produces {@code Accessor} implementations based on the type of the object to be accessed.
 *
 * @author Ayco Holleman
 */
@FunctionalInterface
public interface AccessorFactory {

  /**
   * Returns the {@code Accessor} to be used for reading objects of the specified type.
   *
   * @param type The type of the objects to be accessed
   * @param type The {@code Template} to be populated using these objects
   * @return The {@code Accessor} to be used
   */
  Accessor<?> getAccessor(Class<?> type, Template template);
}
