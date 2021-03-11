package nl.naturalis.yokete.render;

import nl.naturalis.yokete.template.Template;

/**
 * Defines how to map the names of template variables and nested templates to bean properties or map
 * keys or whatever name must be used to access their value. When implementing a name mapper for a
 * template, make sure it is also capable of mapping names for <i>all</i> templates descending from
 * it.
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
