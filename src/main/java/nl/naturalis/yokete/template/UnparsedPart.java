package nl.naturalis.yokete.template;

import nl.naturalis.common.StringMethods;

class UnparsedPart extends AbstractPart {

  private final String contents;

  UnparsedPart(String contents, int start, int end) {
    super(start, end);
    this.contents = contents;
  }

  String getContents() {
    return contents;
  }

  TextPart toTextPart() {
    return new TextPart(contents, start(), end());
  }

  @Override
  public String toString() {
    return contents;
  }

  public String toDebugString() {
    String type = StringMethods.rpad(getClass().getSimpleName(), TYPE_DISPLAY_WIDTH, ' ', " | ");
    String name = StringMethods.rpad("", NAME_DISPLAY_WIDTH, ' ', " | ");
    String src = contents.replaceAll("\\s+", " ").trim();
    return new StringBuilder(255).append(type).append(name).append(src).toString();
  }
}
