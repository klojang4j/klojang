package org.klojang.template;

/**
 * A {@link Part} implementation for representing boilerplate text. That's basically all text in
 * between template variables.
 *
 * @author Ayco Holleman
 */
public class TextPart extends AbstractPart {

  private final String text;

  TextPart(String text, int start) {
    super(start);
    this.text = text;
  }

  /**
   * Returns the text of this part of the template.
   *
   * @return The text of this part of the template
   */
  public String getText() {
    return text;
  }

  @Override
  public String toString() {
    return text;
  }
}
