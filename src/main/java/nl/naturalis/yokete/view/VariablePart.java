package nl.naturalis.yokete.view;

public class VariablePart implements Part<String> {

  private final String name;

  public VariablePart(String name) {
    this.name = name;
  }

  @Override
  public String getContents() {
    return name;
  }
}
