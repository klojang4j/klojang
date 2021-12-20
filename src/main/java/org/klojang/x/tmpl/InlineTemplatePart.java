package org.klojang.x.tmpl;

import static org.klojang.x.tmpl.Regex.VAR_END;
import org.klojang.template.NestedTemplatePart;
import org.klojang.template.Template;
import static org.klojang.x.tmpl.Regex.TMPL_START;

/**
 * A {@link Part} implementation for representing inline templates.
 *
 * @author Ayco Holleman
 */
public class InlineTemplatePart extends NestedTemplatePart {

  public InlineTemplatePart(Template template, int start) {
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
