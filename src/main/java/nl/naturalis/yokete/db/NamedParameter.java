package nl.naturalis.yokete.db;

import java.util.Arrays;

/**
 * Represents a single named parameter within a SQL statement.
 *
 * @author Ayco Holleman
 */
public class NamedParameter {

  private final String name;
  private final int[] indices;

  NamedParameter(String paramName, int[] indices) {
    this.name = paramName;
    this.indices = indices;
  }

  public String getName() {
    return name;
  }

  public int[] getIndices() {
    return Arrays.copyOf(indices, indices.length);
  }

  int[] indices() {
    return indices;
  }
}
