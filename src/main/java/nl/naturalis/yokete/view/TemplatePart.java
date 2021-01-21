package nl.naturalis.yokete.view;

public class TemplatePart implements Part<Template> {

  private final String name;
  private final Template template;

  public TemplatePart(String name, Template template) {
    this.name = name;
    this.template = template;
  }

  @Override
  public Template getContents() {
    return template;
  }

  public String getName() {
    return name;
  }
}
