package nl.naturalis.yokete.template;

import nl.naturalis.common.StringMethods;

public class IncludedTemplatePart extends TemplatePart {

  public IncludedTemplatePart(Template template, int start, int end) {
    super(template, start, end);
  }

  public String toDebugString() {
    String type = StringMethods.rpad(getClass().getSimpleName(), TYPE_DISPLAY_WIDTH, ' ', " | ");
    String name = StringMethods.rpad(getName(), NAME_DISPLAY_WIDTH, ' ', " | ");
    String src = template.toString().replaceAll("\\s+", " ").trim();
    return new StringBuilder(255).append(type).append(name).append(src).toString();
  }
}
