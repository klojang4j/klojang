package nl.naturalis.yokete.template;

/**
 * Super class of {@link InlineTemplatePart} and {@link IncludedTemplatePart}. Except that inline
 * templates are defined within the parent template while included templates are defined in an
 * external files, there is no functional difference between them.
 *
 * @author Ayco Holleman
 */
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
