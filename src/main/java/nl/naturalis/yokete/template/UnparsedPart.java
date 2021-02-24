package nl.naturalis.yokete.template;

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
}
