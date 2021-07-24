package org.klojang.db;

import nl.naturalis.common.collection.IntList;

/**
 * Represents a single named parameter within a SQL statement.
 *
 * @author Ayco Holleman
 */
public class NamedParameter {

  private final String name;
  private final IntList indices;

  NamedParameter(String paramName, IntList indices) {
    this.name = paramName;
    this.indices = indices;
  }

  public String getName() {
    return name;
  }

  public IntList getIndices() {
    return indices;
  }

  @Override
  public int hashCode() {
    int hash = name.hashCode();
    hash = hash * 31 + indices.hashCode();
    return hash;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    } else if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    NamedParameter other = (NamedParameter) obj;
    return name.equals(other.name) && indices.equals(other.indices);
  }

  public String toString() {
    return "{" + name + ": " + indices + "}";
  }
}
