package nl.naturalis.yokete.template;

import nl.naturalis.yokete.render.EscapeType;
import static nl.naturalis.yokete.template.Regex.NAME_END;
import static nl.naturalis.yokete.template.Regex.VAR_START;

/**
 * Captures a substring of the template that declares a template variable. For example: {@code
 * ~%fullName%} or {@code ~%html:fullName%}.
 *
 * @author Ayco Holleman
 */
public class VariablePart extends AbstractPart implements NamedPart {

  private final String name;
  private final EscapeType escapeType;

  public VariablePart(EscapeType escapeType, String name, int start) {
    super(start);
    this.escapeType = escapeType;
    this.name = name;
  }

  /**
   * Returns the inline escape type of the variable.
   *
   * @return The inline escape type of the variable
   */
  public EscapeType getEscapeType() {
    return escapeType;
  }

  /**
   * Returns the name of the variable.
   *
   * @return The name of the variable
   */
  public String getName() {
    return name;
  }

  @Override
  public String toString() {
    return VAR_START + name + NAME_END;
  }
}
