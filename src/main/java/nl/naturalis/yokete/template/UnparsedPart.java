package nl.naturalis.yokete.template;

class UnparsedPart extends AbstractPart {

  private final String text;

  UnparsedPart(String contents, int start, int end) {
    super(start, end);
    this.text = contents;
  }

  String text() {
    return text;
  }

  TextPart toTextPart() {
    return new TextPart(text, start(), end());
  }

  @Override
  public String toString() {
    return text;
  }
}
