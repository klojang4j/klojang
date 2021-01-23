package nl.naturalis.yokete.view;

public class TemplatePart extends AbstractPart {

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
}
