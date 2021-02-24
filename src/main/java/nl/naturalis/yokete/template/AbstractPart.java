package nl.naturalis.yokete.template;

abstract class AbstractPart implements Part {

  private final int start;
  private final int end;

  public AbstractPart(int start, int end) {
    this.start = start;
    this.end = end;
  }

  /** The start position (string index) of the part. */
  @Override
  public int start() {
    return start;
  }

  /** The end position (exclusive) of the part. */
  @Override
  public int end() {
    return end;
  }
}
