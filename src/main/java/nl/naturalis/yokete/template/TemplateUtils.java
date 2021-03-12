package nl.naturalis.yokete.template;

import java.util.ArrayList;
import nl.naturalis.common.check.Check;

public class TemplateUtils {

  private TemplateUtils() {}

  /**
   * Returns the fully-qualified name of the specified name, relative to the specified template.
   *
   * @param template
   * @param name
   * @return
   */
  public static String getFQName(Template template, String name) {
    Check.notNull(template, "template");
    Check.notNull(name, "name");
    int sz = name.length();
    ArrayList<String> chunks = new ArrayList<>(5);
    chunks.add(name);
    for (Template t = template; t != null; t = t.getParent()) {
      chunks.add(t.getName());
      sz += t.getName().length() + 1;
    }
    StringBuilder sb = new StringBuilder(sz);
    for (int i = chunks.size() - 1; i >= 0; --i) {
      if (sb.length() != 0) {
        sb.append('.');
      }
      sb.append(chunks.get(i));
    }
    return sb.toString();
  }
}
