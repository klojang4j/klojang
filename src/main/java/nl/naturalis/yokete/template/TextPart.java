package nl.naturalis.yokete.template;

/**
 * Captures a substring of the template that is text-only. That's simply everything that is not part
 * of a variable declaration, an inline template definition, or an template inclusion declaration.
 *
 * @author Ayco Holleman
 */
public class TextPart extends AbstractPart {

  private final String text;

  TextPart(String text, int start) {
    super(start);
    this.text = text;
  }

  public String getText() {
    return text;
  }

  @Override
  public String toString() {
    return text;
  }
}
