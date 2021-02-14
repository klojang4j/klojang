package nl.naturalis.yokete.view;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import nl.naturalis.common.IOMethods;
import nl.naturalis.common.check.Check;
import nl.naturalis.common.collection.IntArrayList;
import nl.naturalis.common.collection.IntList;
import nl.naturalis.common.collection.UnmodifiableIntList;
import static java.util.stream.Collectors.toUnmodifiableSet;
import static nl.naturalis.common.check.CommonChecks.instanceOf;
import static nl.naturalis.common.check.CommonChecks.keyIn;

/**
 * A {@code Template} captures the result of parsing a template file. It provides access to the
 * constituent parts of a text template: {@link VariablePart template variables}, {@link
 * TemplatePart nested templates} and {@link TextPart literal text}. {@code Template} instances are
 * immutable, expensive-to-create and heavy-weight objects. They should be cached, for example as a
 * static final field in your controller or resource class. Creating a new {@code Template} instance
 * from the same template file would be very inefficient.
 *
 * @author Ayco Holleman
 */
public class Template {

  /**
   * The name of the root template: &#34;&#64ROOT&#34;. This is the {@code Template} that got
   * instantiated directly from file. The templates nested inside it get their name from the
   * contents of the file (e.g. ~%%begin:<b>mainTable</b>%).
   */
  public static final String ROOT_TEMPLATE_NAME = "@ROOT";

  public static Template parse(InputStream in) throws InvalidTemplateException {
    return parse(IOMethods.toString(in));
  }

  public static Template parse(String source) throws InvalidTemplateException {
    List<Part> parts = TemplateParser.INSTANCE.parse(source);
    return new Template(parts);
  }

  static Template parse(String tmplName, String source) throws InvalidTemplateException {
    List<Part> parts = TemplateParser.INSTANCE.parse(source);
    return new Template(tmplName, parts);
  }

  private final String name;
  private final List<Part> parts;
  private final Map<String, IntList> varIndices;
  private final IntList textIndices;
  private final Map<String, Integer> tmplIndices;
  private final int varCount;
  private final int tmplCount;
  private final Set<String> names; // variable names + template names

  private Template(List<Part> parts) {
    this(ROOT_TEMPLATE_NAME, parts);
  }

  private Template(String name, List<Part> parts) {
    this.name = name;
    this.parts = parts;
    this.varIndices = getVarIndices(parts);
    this.varCount = getVarCount(parts);
    this.tmplIndices = getTmplIndices(parts);
    this.tmplCount = getTmplCount(parts);
    this.names = getNames(parts);
    this.textIndices = getTextIndices(parts);
  }

  /**
   * Returns the name of the template.
   *
   * @return The name of the template
   */
  public String getName() {
    return name;
  }

  /**
   * Returns the constituent parts of the template. A template consists of {@link TextPart literal
   * text}, {@link VariablePart template variables} and {@link TemplatePart nested templates}. The
   * returned {@code List} is immutable.
   *
   * @return The constituent parts of the template file
   */
  public List<Part> getParts() {
    return parts;
  }

  public Map<String, IntList> getVarPartIndices() {
    return varIndices;
  }

  public Set<String> getVariableNames() {
    return varIndices.keySet();
  }

  public int countVariables() {
    return varCount;
  }

  public Map<String, Integer> getTemplatePartIndices() {
    return tmplIndices;
  }

  public Set<String> getTemplateNames() {
    return tmplIndices.keySet();
  }

  public int countTemplates() {
    return tmplCount;
  }

  /**
   * Returns a {@code Template} nested inside this {@code Template}, identified by the specified
   * name.
   *
   * @param name The name of the nested template
   * @return A {@code Template} nested inside this {@code Template}
   */
  public Template getTemplate(String name) {
    Check.that(name).is(keyIn(), tmplIndices, "No such template: %s", name);
    int partIndex = tmplIndices.get(name);
    return Check.that(parts.get(partIndex))
        .is(instanceOf(), TemplatePart.class, "%s is not a template", name)
        .ok(TemplatePart.class::cast)
        .getTemplate();
  }

  /**
   * Returns the names of all variables and nested templates within this {@code Template}.
   *
   * @return The names of all variables and nested templates within this {@code Template}
   */
  public Set<String> getAllNames() {
    return names;
  }

  /**
   * Returns an {@link IntList} of all parts that contain literal text.
   *
   * @return An {@link IntList} of all parts that contain literal text
   */
  public IntList getTextPartIndices() {
    return textIndices;
  }

  /*
   * Let's just make it really explicit that nothing is equal to a Template instance except the
   * instance itself.
   */
  @Override
  public boolean equals(Object obj) {
    return this == obj;
  }

  @Override
  public int hashCode() {
    return System.identityHashCode(this);
  }

  private static Map<String, IntList> getVarIndices(List<Part> parts) {
    Map<String, IntList> indices = new HashMap<>();
    for (int i = 0; i < parts.size(); ++i) {
      if (parts.get(i).getClass() == VariablePart.class) {
        String name = VariablePart.class.cast(parts.get(i)).getName();
        indices.computeIfAbsent(name, k -> new IntArrayList(5, 5)).add(i);
      }
    }
    indices.entrySet().forEach(e -> e.setValue(UnmodifiableIntList.copyOf(e.getValue())));
    return Map.copyOf(indices);
  }

  private static int getVarCount(List<Part> parts) {
    return (int) parts.stream().filter(VariablePart.class::isInstance).count();
  }

  private static Map<String, Integer> getTmplIndices(List<Part> parts) {
    Map<String, Integer> indices = new HashMap<>();
    for (int i = 0; i < parts.size(); ++i) {
      if (parts.get(i).getClass() == TemplatePart.class) {
        String name = TemplatePart.class.cast(parts.get(i)).getName();
        indices.put(name, i);
      }
    }
    return Map.copyOf(indices);
  }

  private static int getTmplCount(List<Part> parts) {
    return (int) parts.stream().filter(TemplatePart.class::isInstance).count();
  }

  private static Set<String> getNames(List<Part> parts) {
    return parts
        .stream()
        .filter(NamedPart.class::isInstance)
        .map(NamedPart.class::cast)
        .map(NamedPart::getName)
        .collect(toUnmodifiableSet());
  }

  private static IntList getTextIndices(List<Part> parts) {
    IntArrayList indices = new IntArrayList();
    for (int i = 0; i < parts.size(); ++i) {
      if (parts.get(i).getClass() == TextPart.class) {
        indices.add(i);
      }
    }
    return UnmodifiableIntList.copyOf(indices);
  }
}
