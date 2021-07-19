package nl.naturalis.yokete.render;

import java.util.Objects;
import nl.naturalis.yokete.template.Template;
import nl.naturalis.yokete.template.VarGroup;

class StringifierId {

  final VarGroup varGroup;
  final Template template;
  final String varName;

  private final int hash;

  public StringifierId(VarGroup varGroup) {
    this(varGroup, null, null);
  }

  public StringifierId(Template template, String varName) {
    this(null, template, varName);
  }

  public StringifierId(String varName) {
    this(null, null, varName);
  }

  private StringifierId(VarGroup varGroup, Template template, String varName) {
    this.varGroup = varGroup;
    this.template = template;
    this.varName = varName;
    this.hash = Objects.hash(template, varGroup, varName);
    ;
  }

  @Override
  public int hashCode() {
    return hash;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    StringifierId other = (StringifierId) obj;
    return Objects.equals(template, other.template)
        && varGroup == other.varGroup
        && Objects.equals(varName, other.varName);
  }
}
