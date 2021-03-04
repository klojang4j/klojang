package nl.naturalis.yokete.template;

public abstract class NestedTemplatePart extends AbstractPart implements NamedPart {

  protected final Template template;

  public NestedTemplatePart(Template template, int start) {
    super(start);
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
