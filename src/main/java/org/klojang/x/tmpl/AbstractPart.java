package org.klojang.x.tmpl;

import org.klojang.template.Template;

public abstract class AbstractPart implements Part {

  private final int start;

  private Template parent;

  public AbstractPart(int start) {
    this.start = start;
  }

  @Override
  public int start() {
    return start;
  }

  @Override
  public Template getParentTemplate() {
    return parent;
  }

  public void setParentTemplate(Template parent) {
    this.parent = parent;
  }
}
