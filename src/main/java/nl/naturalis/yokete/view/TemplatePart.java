package nl.naturalis.yokete.view;

public class TemplatePart extends AbstractPart implements NamedPart {

  private final String name;
  private final Template template;

  public TemplatePart(String name, Template template, int start, int end) {
    super(start, end);
    this.name = name;
    this.template = template;
  }

  public String getName() {
    return name;
  }

  public Template getTemplate() {
    return template;
  }

  @Override
  public String toString() {
    return name;
  }
}
