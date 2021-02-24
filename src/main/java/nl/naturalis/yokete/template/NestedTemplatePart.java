package nl.naturalis.yokete.template;

import static nl.naturalis.yokete.template.Regex.NAME_END;
import static nl.naturalis.yokete.template.Regex.TMPL_START;

public class NestedTemplatePart extends TemplatePart {

  public NestedTemplatePart(Template template, int start, int end) {
    super(template, start, end);
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
