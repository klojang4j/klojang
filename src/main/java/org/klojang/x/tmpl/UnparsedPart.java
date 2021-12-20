package org.klojang.x.tmpl;

public class UnparsedPart extends AbstractPart {

  private final String text;

  public UnparsedPart(String text, int start) {
    super(start);
    this.text = text;
  }

  public String text() {
    return text;
  }

  @Override
  public String toString() {
    return text;
  }
}
