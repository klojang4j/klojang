package nl.naturalis.yokete.template;

import nl.naturalis.common.StringMethods;

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

  public String toDebugString() {
    String type = StringMethods.rpad(getClass().getSimpleName(), TYPE_DISPLAY_WIDTH, ' ', " | ");
    String name = StringMethods.rpad("", NAME_DISPLAY_WIDTH, ' ', " | ");
    String txt = text.replaceAll("\\s+", " ").trim();
    return new StringBuilder(255).append(type).append(name).append(txt).toString();
  }
}
