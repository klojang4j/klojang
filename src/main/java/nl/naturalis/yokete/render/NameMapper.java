package nl.naturalis.yokete.render;

import nl.naturalis.yokete.db.Row;
import nl.naturalis.yokete.template.Template;

/**
 * Maps template variable names and nested template names to the name that must be used to access
 * their value in the source data (e&#46;g&#46; a JavaBean, {@code Map} or {@link Row}). When
 * implementing a name mapper for a template, make sure it is capable of mapping names for
 * <i>all</i> templates descending from it as well. You can optionally provide various {@link
 * Accessor accessors} in the {@code accessor} package with a {@code NameMapper} implementation.
 *
 * @author Ayco Holleman
 */
public interface NameMapper {

  /** The no-op mapper. Maps the variable or nested template name to itself. */
  public static NameMapper NOOP = (x, y) -> y;

  /**
   * Maps the specified name to a name that can be used to access its value.
   *
   * @param template The template containing the variable or nested template
   * @param varOrNestedTemplateName The name of the variable or nested template
   * @return A (new) name that can be used to access the value
   */
  String map(Template template, String varOrNestedTemplateName);
}
