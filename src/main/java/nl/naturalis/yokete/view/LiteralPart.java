package nl.naturalis.yokete.view;

public class LiteralPart implements Part<String> {

  private final String contents;

  public LiteralPart(String contents) {
    this.contents = contents;
  }

  @Override
  public String getContents() {
    return contents;
  }
}
