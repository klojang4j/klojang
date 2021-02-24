package nl.naturalis.yokete.template;

/**
 * Captures information about the constituent parts of a text template.
 *
 * @author Ayco Holleman
 */
public interface Part {

  static final int TYPE_DISPLAY_WIDTH = 20;
  static final int NAME_DISPLAY_WIDTH = 20;

  /**
   * The start index of this part within the template.
   *
   * @return The start index of this part within the template.
   */
  int start();

  /**
   * The end index of this part within the template.
   *
   * @return The end index of this part within the template
   */
  int end();

  /**
   * Returns a detailed description of the part
   *
   * @return A detailed description of the part
   */
  String toDebugString();
}
