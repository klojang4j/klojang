package nl.naturalis.yokete.template;

abstract class AbstractPart implements Part {

  private final int start;

  public AbstractPart(int start) {
    this.start = start;
  }

  /** The start position (string index) of the part. */
  @Override
  public int start() {
    return start;
  }
}
