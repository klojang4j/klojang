package nl.naturalis.yokete.template;

import static nl.naturalis.common.StringMethods.substrAfter;
import static nl.naturalis.common.StringMethods.substrTo;
import static nl.naturalis.yokete.template.Regex.NAME_END;
import static nl.naturalis.yokete.template.Regex.TMPL_START;

public class IncludedTemplatePart extends NestedTemplatePart {

  static String basename(String path) {
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
    return sb.append(template.getPath()).append(NAME_END).toString();
  }
}
