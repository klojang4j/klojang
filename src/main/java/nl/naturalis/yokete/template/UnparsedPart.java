package nl.naturalis.yokete.template;

class UnparsedPart extends AbstractPart {

  private final String text;

  UnparsedPart(String text, int start) {
    super(start);
    this.text = text;
  }

  String text() {
    return text;
  }

  TextPart toTextPart() {
    return new TextPart(text, start());
  }

  @Override
  public String toString() {
    return text;
  }
}
