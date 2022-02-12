package org.klojang.template;

import static nl.naturalis.common.StringMethods.substrAfter;
import static nl.naturalis.common.StringMethods.substrBefore;
import static org.klojang.x.tmpl.Regex.TMPL_START;
import static org.klojang.x.tmpl.Regex.VAR_END;

/**
 * A {@link Part} implementation for representing included templates.
 *
 * @author Ayco Holleman
 */
class IncludedTemplatePart extends NestedTemplatePart {

  static String basename(String path) {
    return substrBefore(substrAfter(path, "/", -1), ".", -1);
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
