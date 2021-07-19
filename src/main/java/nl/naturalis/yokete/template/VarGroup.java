package nl.naturalis.yokete.template;

import java.util.HashMap;
import nl.naturalis.common.check.Check;

public class VarGroup {

  public static final VarGroup TEXT = new VarGroup("text");
  public static final VarGroup HTML = new VarGroup("html");
  public static final VarGroup JS = new VarGroup("js");

  public static VarGroup withName(String name) {
    Check.notNull(name);
    return groups.computeIfAbsent(name, VarGroup::new);
  }

  private static final HashMap<String, VarGroup> groups = new HashMap<>();

  static {
    groups.put(TEXT.getName(), TEXT);
    groups.put(HTML.getName(), HTML);
    groups.put(JS.getName(), JS);
  }

  private final String name;

  private VarGroup(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  @Override
  public int hashCode() {
    return name.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || (getClass() != obj.getClass())) {
      return false;
    }
    return name.equals(((VarGroup) obj).name);
  }

  @Override
  public String toString() {
    return name;
  }
}
