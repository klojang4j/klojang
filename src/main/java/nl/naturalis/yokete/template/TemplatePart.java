package nl.naturalis.yokete.template;

public abstract class TemplatePart extends AbstractPart implements NamedPart {

  protected final Template template;

  public TemplatePart(Template template, int start, int end) {
    super(start, end);
    this.template = template;
  }

  @Override
  public String getName() {
    return template.getName();
  }

  public Template getTemplate() {
    return template;
  }

  @Override
  public String toString() {
    return template.toString();
  }
}
