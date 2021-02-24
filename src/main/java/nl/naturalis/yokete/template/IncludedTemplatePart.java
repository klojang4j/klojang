package nl.naturalis.yokete.template;

import static nl.naturalis.common.StringMethods.substrAfter;
import static nl.naturalis.common.StringMethods.substrTo;
import static nl.naturalis.yokete.template.Regex.NAME_END;
import static nl.naturalis.yokete.template.Regex.TMPL_START;

public class IncludedTemplatePart extends TemplatePart {

  static String extractName(String path) {
    return substrTo(substrAfter(path, "/", true), '.');
  }

  public IncludedTemplatePart(Template template, int start, int end) {
    super(template, start, end);
  }

  @Override
  public String toString() {
    String basename = extractName(template.getPath().toString());
    StringBuilder sb = new StringBuilder(32).append(TMPL_START).append("include:");
    if (!template.getName().equals(basename)) {
      sb.append(template.getName());
    }
    return sb.append(template.getPath()).append(NAME_END).toString();
  }
}
