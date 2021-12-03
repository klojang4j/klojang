package org.klojang.template;

import java.util.Optional;
import org.klojang.x.template.XVarGroup;
import static org.klojang.template.Regex.VAR_END;
import static org.klojang.template.Regex.VAR_START;
import static nl.naturalis.common.ObjectMethods.ifNotNull;

/**
 * Captures a substring of the template that declares a template variable. For example: {@code
 * ~%fullName%} or {@code ~%html:fullName%}.
 *
 * @author Ayco Holleman
 */
public class VariablePart extends AbstractPart implements NamedPart {

  private final VarGroup group;
  private final String name;

  VariablePart(String prefix, String name, int start) {
    super(start);
    this.group = ifNotNull(prefix, XVarGroup::withName);
    this.name = name;
  }

  /**
   * Returns the inline escape type of the variable.
   *
   * @return The inline escape type of the variable
   */
  public Optional<VarGroup> getVarGroup() {
    return Optional.ofNullable(group);
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
    return VAR_START + name + VAR_END;
  }
}
