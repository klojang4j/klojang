package nl.naturalis.yokete.view;

abstract class AbstractPart implements Part {

  private final int start;
  private final int end;

  public AbstractPart(int start, int end) {
    this.start = start;
    this.end = end;
  }

  @Override
  public int start() {
    return start;
  }

  @Override
  public int end() {
    return end;
  }
}
