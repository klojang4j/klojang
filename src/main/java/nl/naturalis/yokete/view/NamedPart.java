package nl.naturalis.yokete.view;

/**
 * Interface for those parts of a template that can be identified by name (i&#46;e&#46; template
 * variables and nested templates).
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
