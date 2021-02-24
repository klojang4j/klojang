package nl.naturalis.yokete.template;

import nl.naturalis.common.StringMethods;
import nl.naturalis.yokete.view.EscapeType;
import static nl.naturalis.yokete.template.Regex.*;

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
    return VAR_START + name + NAME_END;
  }

  public String toDebugString() {
    String type = StringMethods.rpad(getClass().getSimpleName(), TYPE_DISPLAY_WIDTH, ' ', " | ");
    String name = StringMethods.rpad(getName(), NAME_DISPLAY_WIDTH, ' ', " | ");
    return new StringBuilder(255).append(type).append(name).append(toString()).toString();
  }
}
