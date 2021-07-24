package org.klojang.template;

/**
 * Captures information about the constituent parts of a text template.
 *
 * @author Ayco Holleman
 */
public interface Part {

  /**
   * The start index of this part within the template.
   *
   * @return The start index of this part within the template.
   */
  int start();

  Template getParentTemplate();
}
