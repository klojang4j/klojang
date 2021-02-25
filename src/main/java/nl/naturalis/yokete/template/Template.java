package nl.naturalis.yokete.template;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import nl.naturalis.common.CollectionMethods;
import nl.naturalis.common.check.Check;
import nl.naturalis.common.collection.IntArrayList;
import nl.naturalis.common.collection.IntList;
import nl.naturalis.common.collection.UnmodifiableIntList;
import static java.util.stream.Collectors.toUnmodifiableSet;
import static nl.naturalis.common.check.CommonChecks.keyIn;

/**
 * A {@code Template} captures the result of parsing a template file. It provides access to the
 * constituent parts of a text template: {@link VariablePart template variables}, {@link
 * TemplatePart nested templates} and {@link TextPart literal text}. {@code Template} instances are
 * immutable, expensive-to-create and heavy-weight objects. They should be cached (for example as a
 * static final field in your controller or resource class) and reused for as long as the web
 * application is up. Creating a new {@code Template} instance for each new request would be very
 * inefficient.
 *
 * @author Ayco Holleman
 */
public class Template {

  /**
   * The name of the root template: &#34;&#64ROOT&#34;. This is the {@code Template} that is
   * explicitly instantiated by calling one of the {@code parse(...)} methods. The templates nested
   * inside it get their name from the contents of the file (e.g. {@code ~%%begin:invoices%} or
   * {@code ~%%include:invoices:/templates/invoice-table.html%}).
   */
  public static final String ROOT_TEMPLATE_NAME = "@ROOT";

  /**
   * Parses the specified source text into a {@code Template} instance. Only use this constructor if
   * the template does not {@code include} other templates (using {@code
   * ~%%include:path/to/other/source/file%}).
   *
   * @param source The source text for the {@code Template}
   * @return a new {@code Template} instance
   * @throws ParseException
   */
  public static Template parse(String source) throws ParseException {
    return parse(null, source);
  }

  /**
   * Parses the specified source text into a {@code Template} instance. The specified class will be
   * used to include other template files by calling {@link Class#getResourceAsStream(String)
   * getResourceAsStream} upon it.
   *
   * @param clazz Any {@code Class} object that provides access to the included tempate files by
   *     calling {@code getResourceAsStream} on it
   * @param source The source text for the {@code Template}
   * @return a new {@code Template} instance
   * @throws ParseException
   */
  public static Template parse(Class<?> clazz, String source) throws ParseException {
    return new Parser(ROOT_TEMPLATE_NAME, clazz, source).parse();
  }

  /**
   * Loads the template file at the specified location by calling {@code clazz.getResourceAsStream,
   * path} and parses its contents into a {@code Template} instance. The specified class will also
   * be used to {@code include} other template files.
   *
   * @param clazz Any {@code Class} object that provides access to the tempate files by calling
   *     {@code getResourceAsStream} on it
   * @param path The location of the template file
   * @return a new {@code Template} instance
   * @throws ParseException
   */
  public static Template parse(Class<?> clazz, Path path) throws ParseException {
    return new Parser(ROOT_TEMPLATE_NAME, clazz, path).parse();
  }

  static Template parse(String tmplName, Class<?> clazz, String source) throws ParseException {
    return new Parser(tmplName, clazz, source).parse();
  }

  static Template parse(String tmplName, Class<?> clazz, Path path) throws ParseException {
    return new Parser(tmplName, clazz, path).parse();
  }

  private final String name;
  private final Path path;
  private final List<Part> parts;
  private final Map<String, IntList> varIndices;
  private final IntList textIndices;
  private final Map<String, Integer> tmplIndices;
  private final int varCount;
  private final Set<String> names; // variable names + template names

  Template(String name, Path path, List<Part> parts) {
    this.name = name;
    this.path = path;
    this.parts = parts;
    this.varIndices = getVarIndices(parts);
    this.varCount = getVarCount(parts);
    this.tmplIndices = getTmplIndices(parts);
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
   * If the template was created by specifying the location of a template file, this method returns
   * the path, else null. In other words, for {@code included} templates this method returns a
   * non-null value. For nested templates this method returns null. For the root template the return
   * value depends on which {@code parse} method was used to instantiate the template.
   *
   * @return
   */
  public Path getPath() {
    return path;
  }

  /**
   * Returns the constituent parts of the template. A template falls apart in {@link TextPart
   * literal text}, {@link VariablePart template variables}, {@link NestedTemplatePart nested
   * templates} and {@link IncludedTemplatePart included templates}. The returned {@code List} is
   * immutable.
   *
   * @return The constituent parts of the template file
   */
  public List<Part> getParts() {
    return parts;
  }

  /**
   * Tells you which parts in the {@link #getParts() parts list} contain variables. One variable may
   * occur multiple times in the same template. Therefore multiple parts may contain the same
   * variable. Hence the returned {@code Map} maps each variable name to a list of {@code List}
   * indices. The returned {@code Map} is immutable.
   *
   * @return
   */
  public Map<String, IntList> getVarPartIndices() {
    return varIndices;
  }

  /**
   * Returns the names of all variables.
   *
   * @return The names of all variables
   */
  public Set<String> getVariableNames() {
    return varIndices.keySet();
  }

  /**
   * Returns the sum total of all variables in the template. N.B. not the number of <i>unique</i>
   * variable names.
   *
   * @return The sum total of all variables in the template
   */
  public int countVariables() {
    return varCount;
  }

  /**
   * Tells you which parts in the {@link #getParts() parts list} contain templates (nested or
   * included). The returned {@code Map} maps each template name to exactly one {@code List} index
   * (template names must be unique within the parent template). The returned {@code Map} is
   * immutable.
   *
   * @return
   */
  public Map<String, Integer> getTemplatePartIndices() {
    return tmplIndices;
  }

  /**
   * Returns the names of all nested and included templates.
   *
   * @return The names of all nested and included templates
   */
  public Set<String> getTemplateNames() {
    return tmplIndices.keySet();
  }

  public int countTemplates() {
    return tmplIndices.size();
  }

  /**
   * Returns the nested or included template identified by the specified name. This method throws an
   * {@link IllegalArgumentException} if no nested or included template has the specified name.
   *
   * @param name The name of the nested template
   * @return A {@code Template} nested inside this {@code Template}
   */
  public Template getTemplate(String name) {
    Check.that(name).is(keyIn(), tmplIndices, "No such template: \"%s\"", name);
    int partIndex = tmplIndices.get(name);
    return ((TemplatePart) parts.get(partIndex)).getTemplate();
  }

  /**
   * Returns all names found in the template (variable names, nested template names, included
   * template names).
   *
   * @return All names found in the template
   */
  public Set<String> getAllNames() {
    return names;
  }

  /**
   * Tells you which parts in the {@link #getParts() parts list} contain literal text. The returned
   * {@link IntList} contains the {@code List} indexes of those parts. The returned {@code IntList}
   * is immutable.
   *
   * @return An {@link IntList} of all part indexes where literal text can be found
   */
  public IntList getTextPartIndices() {
    return textIndices;
  }

  /*
   * Let's make it really explicit that nothing is equal to a Template instance except the
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

  @Override
  public String toString() {
    return CollectionMethods.implode(parts, "");
  }

  private static Map<String, IntList> getVarIndices(List<Part> parts) {
    Map<String, IntList> indices = new HashMap<>();
    for (int i = 0; i < parts.size(); ++i) {
      if (parts.get(i).getClass() == VariablePart.class) {
        String name = ((VariablePart) parts.get(i)).getName();
        indices.computeIfAbsent(name, k -> new IntArrayList()).add(i);
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
      if (parts.get(i) instanceof TemplatePart) {
        String name = ((TemplatePart) parts.get(i)).getName();
        indices.put(name, i);
      }
    }
    return Map.copyOf(indices);
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