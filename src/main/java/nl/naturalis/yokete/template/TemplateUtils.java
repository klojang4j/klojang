package nl.naturalis.yokete.template;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import nl.naturalis.common.Tuple;
import nl.naturalis.common.check.Check;

/**
 * Utility class extending extending the functionality of the {@link Template} class.
 *
 * @author Ayco Holleman
 */
public class TemplateUtils {

  private TemplateUtils() {}

  /**
   * Returns the fully-qualified name of the specified name, relative to the root template. If the
   * template <i>is</i> the root template, the {@link Template#ROOT_TEMPLATE_NAME} is returned.
   *
   * @param template
   * @return
   */
  public static String getFQName(Template template) {
    Check.notNull(template);
    if (template.getParent() == null) {
      return template.getName();
    }
    int sz = 0;
    ArrayList<String> chunks = new ArrayList<>(5);
    for (Template t = template; t != null && t.getParent() != null; t = t.getParent()) {
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
    for (Template t = template; t != null && t.getParent() != null; t = t.getParent()) {
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

  /**
   * Returns the names of all variables and nested templates within the specified template and all
   * templates descending from it. The returned {@code Set} is created on demand and modifiable.
   *
   * @param template The {@code Template} to extract the names from
   * @return The names of all variables and nested templates within the specified template and all
   *     templates descending from it
   */
  public static Set<String> getAllNames(Template template) {
    Set<String> names = new HashSet<>();
    collectNames(template, names);
    return names;
  }

  private static void collectNames(Template template, Set<String> names) {
    names.addAll(template.getNames());
    for (Template t : template.getNestedTemplates()) {
      collectNames(t, names);
    }
  }

  /**
   * Returns a {@code List} containing the specified {@code Template} and all templates descending
   * from it. The specified {@code Template} will be the first element of the {@code List}. The
   * {@code List} is created on demand and modifiable.
   *
   * @return A {@code List} containing the {@code Template} and all templates descending from it
   */
  public static List<Template> getNestedTemplatesRecursive(Template template) {
    ArrayList<Template> tmpls = new ArrayList<>(20);
    tmpls.add(template);
    collectTemplates(template, tmpls);
    return tmpls;
  }

  private static void collectTemplates(Template t0, ArrayList<Template> tmpls) {
    List<Template> myTmpls = t0.getNestedTemplates();
    tmpls.addAll(myTmpls);
    myTmpls.forEach(t -> collectTemplates(t, tmpls));
  }

  /**
   * Returns, for this {@code Template} and all templates descending from it, the names of their
   * variables. Each tuple in the returned {@code List} contains a {@code Template} instance and a
   * variable name. The returned {@code List} is created on demand and modifiable.
   *
   * @return All variable names in this {@code Template} and the templates nested inside it
   */
  public static List<Tuple<Template, String>> getVarsPerTemplate(Template template) {
    ArrayList<Tuple<Template, String>> tuples = new ArrayList<>(25);
    collectVarsPerTemplate(template, tuples);
    return tuples;
  }

  private static void collectVarsPerTemplate(
      Template t0, ArrayList<Tuple<Template, String>> tuples) {
    t0.getVariables().stream().map(s -> Tuple.of(t0, s)).forEach(tuples::add);
    t0.getNestedTemplates().forEach(t -> collectVarsPerTemplate(t, tuples));
  }

  /**
   * Prints out the constituent parts of the specified {@code Template}. Can be used for debugging
   * purposes.
   *
   * @param template The {@code Template} whose parts to print
   * @param out The {@code PrintStream} to which to print
   */
  public static void printParts(Template template, PrintStream out) {
    new PartsPrinter(template).printParts(out);
  }
}
