package nl.naturalis.yokete.template;

import nl.naturalis.yokete.render.EscapeType;
import static nl.naturalis.yokete.template.Regex.NAME_END;
import static nl.naturalis.yokete.template.Regex.VAR_START;

public class VariablePart extends AbstractPart implements NamedPart {

  private final String name;
  private final EscapeType escapeType;

  public VariablePart(EscapeType escapeType, String name, int start) {
    super(start);
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
}
