package org.klojang.template;

import static org.klojang.template.Regex.NAME_END;
import static org.klojang.template.Regex.TMPL_START;

/**
 * Captures a substring of the template containing an inline template definition. For example:
 *
 * <p>
 *
 * <pre>
 * ~%%begin:hello%
 *  &lt;p&gt;Some great stuff here, ~%firstName%!&lt;/p&gt;
 * ~%%end:hello%
 * </pre>
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
        .append(NAME_END)
        .append(template.toString())
        .append(TMPL_START)
        .append("end:")
        .append(template.getName())
        .append(NAME_END)
        .toString();
  }
}
