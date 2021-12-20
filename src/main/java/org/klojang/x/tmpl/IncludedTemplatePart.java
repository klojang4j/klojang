package org.klojang.x.tmpl;

import org.klojang.template.NestedTemplatePart;
import org.klojang.template.Template;
import static org.klojang.x.tmpl.Regex.TMPL_START;
import static org.klojang.x.tmpl.Regex.VAR_END;
import static nl.naturalis.common.StringMethods.substrAfter;
import static nl.naturalis.common.StringMethods.substrTo;

/**
 * A {@link Part} implementation for representing included templates.
 *
 * @author Ayco Holleman
 */
public class IncludedTemplatePart extends NestedTemplatePart {

  public static String basename(String path) {
    return substrTo(substrAfter(path, "/", true), '.');
  }

  public IncludedTemplatePart(Template template, int start) {
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
