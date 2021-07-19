package nl.naturalis.yokete.template;

abstract class AbstractPart implements Part {

  private final int start;

  private NestedTemplatePart parent;

  public AbstractPart(int start) {
    this.start = start;
  }

  @Override
  public int start() {
    return start;
  }

  @Override
  public NestedTemplatePart getParentPart() {
    return parent;
  }

  void setParentPart(NestedTemplatePart parent) {
    this.parent = parent;
  }
}
