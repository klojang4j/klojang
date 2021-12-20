package org.klojang.template;

import org.klojang.x.tmpl.AbstractPart;
import org.klojang.x.tmpl.IncludedTemplatePart;
import org.klojang.x.tmpl.InlineTemplatePart;
import org.klojang.x.tmpl.NamedPart;

/**
 * Base class of {@link InlineTemplatePart} and {@link IncludedTemplatePart}. Except that inline
 * templates are coded within the parent template while included templates reside in a external
 * file, there is no functional difference between them.
 *
 * @author Ayco Holleman
 */
public abstract class NestedTemplatePart extends AbstractPart implements NamedPart {

  protected final Template template;

  protected NestedTemplatePart(Template template, int start) {
    super(start);
    this.template = template;
  }

  @Override
  public String getName() {
    return template.getName();
  }

  /**
   * Returns the {@code Template} instance associated with this part of the parent template.
   *
   * @return The {@code Template} instance associated with this part of the parent template
   */
  public Template getTemplate() {
    return template;
  }

  @Override
  public String toString() {
    return template.toString();
  }

  @Override
  public void setParentTemplate(Template parent) {
    super.setParentTemplate(parent);
    template.parent = parent;
  }
}
