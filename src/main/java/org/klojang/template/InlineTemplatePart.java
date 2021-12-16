package org.klojang.template;

import static org.klojang.template.Regex.VAR_END;
import static org.klojang.template.Regex.TMPL_START;

/**
 * A {@link Part} implementation for representing inline templates.
 *
 * @author Ayco Holleman
 */
public class InlineTemplatePart extends NestedTemplatePart {

  InlineTemplatePart(Template template, int start) {
    super(template, start);
  }

  public String toString() {
    return new StringBuilder(100)
        .append(TMPL_START)
        .append("begin:")
        .append(template.getName())
        .append(VAR_END)
        .append(template.toString())
        .append(TMPL_START)
        .append("end:")
        .append(template.getName())
        .append(VAR_END)
        .toString();
  }
}
