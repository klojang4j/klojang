package nl.naturalis.yokete.view;

import nl.naturalis.yokete.template.Template;

/**
 * Defines how to map the names of template variables and nested templates to bean properties or map
 * keys or whatever name must be used to access their value.
 *
 * @author Ayco Holleman
 */
public interface NameMapper {

  /** The no-op mapper. Maps the variable or nested template name to itself. */
  public static NameMapper NOOP = (x, y) -> y;

  String map(Template template, String varOrNestedTemplateName);
}
