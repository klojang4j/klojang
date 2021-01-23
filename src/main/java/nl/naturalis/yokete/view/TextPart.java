package nl.naturalis.yokete.view;

public class TextPart extends AbstractPart {

  private final String text;

  public TextPart(String text, int start, int end) {
    super(start, end);
    this.text = text;
  }

  public String getText() {
    return text;
  }
}
