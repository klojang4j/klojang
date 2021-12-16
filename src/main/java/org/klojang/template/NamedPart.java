package org.klojang.template;

/**
 * Extension of the {@link Part} Interface for template parts that can be identified by name
 * (i&#46;e&#46; template variables and nested templates).
 *
 * @author Ayco Holleman
 */
public interface NamedPart extends Part {

  /**
   * The name of the variable or nested template.
   *
   * @return The name of the variable or nested template
   */
  String getName();
}
