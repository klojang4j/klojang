package nl.naturalis.yokete.template;

public class TextPart extends AbstractPart {

  private final String text;

  public TextPart(String text, int start, int end) {
    super(start, end);
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
