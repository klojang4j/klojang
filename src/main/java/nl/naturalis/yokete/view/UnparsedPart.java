package nl.naturalis.yokete.view;

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
}
