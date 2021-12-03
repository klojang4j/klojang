package org.klojang.template;

import static org.klojang.template.Regex.VAR_END;
import static org.klojang.template.Regex.TMPL_START;
import static nl.naturalis.common.StringMethods.substrAfter;
import static nl.naturalis.common.StringMethods.substrTo;

/**
 * Captures a substring of the template containing a template inclusion declaration. For example:
 * {@code ~%%include:employees:/views/hr/employees.html%}.
 *
 * @author Ayco Holleman
 */
public class IncludedTemplatePart extends NestedTemplatePart {

  static String basename(String path) {
    return substrTo(substrAfter(path, "/", true), '.');
  }

  IncludedTemplatePart(Template template, int start) {
    super(template, start);
  }

  @Override
  public String toString() {
    String basename = basename(template.getPath().toString());
    StringBuilder sb = new StringBuilder(32).append(TMPL_START).append("include:");
    if (!template.getName().equals(basename)) {
      sb.append(template.getName());
    }
    return sb.append(template.getPath()).append(VAR_END).toString();
  }
}
