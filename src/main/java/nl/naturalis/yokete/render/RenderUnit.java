package nl.naturalis.yokete.render;

import nl.naturalis.common.check.Check;
import nl.naturalis.yokete.template.Template;

public final class RenderUnit {

  final Template template;
  final Accessor accessor;
  final TemplateStringifier stringifier;

  public RenderUnit(Template template, Accessor accessor, TemplateStringifier stringifier) {
    this.template = Check.notNull(template).ok();
    this.accessor = Check.notNull(accessor).ok();
    this.stringifier = Check.notNull(stringifier).ok();
  }

  Template getTemplate() {
    return template;
  }

  Accessor getAccessor() {
    return accessor;
  }

  TemplateStringifier getStringifier() {
    return stringifier;
  }
}
