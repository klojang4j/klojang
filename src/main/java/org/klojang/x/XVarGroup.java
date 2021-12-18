package org.klojang.x;

import java.util.HashMap;
import org.klojang.template.VarGroup;

public class XVarGroup implements VarGroup {

  public static final HashMap<String, VarGroup> VAR_GROUPS = new HashMap<>();

  public static VarGroup get(String name) {
    return VAR_GROUPS.get(name);
  }

  public static XVarGroup withName(String name) {
    return (XVarGroup) VAR_GROUPS.computeIfAbsent(name, XVarGroup::new);
  }

  private final String name;

  private XVarGroup(String name) {
    this.name = name;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public int hashCode() {
    return System.identityHashCode(this);
  }

  @Override
  public boolean equals(Object obj) {
    return this == obj;
  }

  @Override
  public String toString() {
    return name;
  }
}
