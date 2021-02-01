package nl.naturalis.yokete.view;

public class VariablePart extends AbstractPart implements NamedPart {

  private final String name;
  private final EscapeType escapeType;

  public VariablePart(EscapeType escapeType, String name, int start, int end) {
    super(start, end);
    this.escapeType = escapeType;
    this.name = name;
  }

  public EscapeType getEscapeType() {
    return escapeType;
  }

  public String getName() {
    return name;
  }

  @Override
  public String toString() {
    return name;
  }
}
