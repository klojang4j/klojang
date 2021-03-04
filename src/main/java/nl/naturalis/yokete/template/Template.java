package nl.naturalis.yokete.template;

import java.io.PrintStream;
import java.nio.file.Path;
import java.util.*;
import nl.naturalis.common.CollectionMethods;
import nl.naturalis.common.Tuple;
import nl.naturalis.common.check.Check;
import nl.naturalis.common.collection.IntArrayList;
import nl.naturalis.common.collection.IntList;
import nl.naturalis.common.collection.UnmodifiableIntList;
import static java.util.stream.Collectors.*;
import static nl.naturalis.common.check.CommonChecks.keyIn;

/**
 * A {@code Template} captures the result of parsing a template file. It provides access to the
 * constituent parts of a text template: {@link VariablePart template variables}, {@link
 * NestedTemplatePart nested templates} and {@link TextPart literal text}. {@code Template}
 * instances are immutable, expensive-to-create and heavy-weight objects. They should be created
 * once per source file and then cached and reused for as long as the web application is up. (For
 * example, you could cache them into a static final field in your controller or resource class.)
 * Creating a new {@code Template} instance for each new request would be very inefficient.
 *
 * @author Ayco Holleman
 */
public class Template {

  /**
   * The name of the root template: &#34;&#64root&#34;. Any {@code Template} that is explicitly
   * instantiated by calling one of the {@code parse(...)} methods has this name.
   */
  public static final String ROOT_TEMPLATE_NAME = "@root";

  /**
   * Parses the specified source code (presumably an HTML page or an HTML snippet) into a {@code
   * Template} instance. Only use this constructor if the template does not {@code include} other
   * templates (using {@code ~%%include:path/to/other/source/file%}). Otherwise a {@code
   * ParseException} is thrown.
   *
   * @param source The source code for the {@code Template}
   * @return a new {@code Template} instance
   * @throws ParseException
   */
  public static Template parse(String source) throws ParseException {
    return parse((Class<?>) null, source);
  }

  /**
   * Parses the specified source code into a {@code Template} instance. The specified class will be
   * used to include other template files by calling {@link Class#getResourceAsStream(String)
   * getResourceAsStream("/path/to/other/source/file")} on it.
   *
   * @param clazz Any {@code Class} object that provides access to the included tempate files by
   *     calling {@code getResourceAsStream} on it
   * @param source The source code for the {@code Template}
   * @return a new {@code Template} instance
   * @throws ParseException
   */
  public static Template parse(Class<?> clazz, String source) throws ParseException {
    return parse(ROOT_TEMPLATE_NAME, clazz, source);
  }

  /**
   * Loads the template file at the specified location by calling {@code
   * clazz.getResourceAsStream(path.toString())} and parses its contents into a {@code Template}
   * instance. The specified class will also be used to {@code include} other template files.
   *
   * @param clazz Any {@code Class} object that provides access to the tempate files by calling
   *     {@code getResourceAsStream} on it
   * @param path The location of the template file
   * @return a new {@code Template} instance
   * @throws ParseException
   */
  public static Template parse(Class<?> clazz, Path path) throws ParseException {
    return parse(ROOT_TEMPLATE_NAME, clazz, path);
  }

  /* Used by the Parser */
  static Template parse(String tmplName, Class<?> clazz, String source) throws ParseException {
    return new Parser(tmplName, clazz, source, new HashSet<>()).parse();
  }

  /* Used by the Parser */
  static Template parse(String tmplName, Class<?> clazz, Path path) throws ParseException {
    return new Parser(tmplName, clazz, path, new HashSet<>()).parse();
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
   * the file path, else null. In other words, for {@code included} templates this method (by
   * definition) returns a non-null value. For nested templates this method (by definition) returns
   * null. For <i>this</i> {@code Template} the return value depends on which {@code parse} method
   * was used to instantiate the template.
   *
   * @return
   */
  public Path getPath() {
    return path;
  }

  /**
   * Returns the constituent parts of the template. A template in broken up into parts containing
   * {@link TextPart literal text}, parts containing {@link VariablePart template variables} and
   * parts containing {@link NestedTemplatePart nested templates} (which could either be {@link
   * InlineTemplatePart inline templates} or {@link IncludedTemplatePart included templates}. Note
   * that apart from the fact that the source code for an inline template resides within the parent
   * template, while the source code for an included template resides in a different file, there is
   * no functional difference at all between them.
   *
   * <p>The returned {@code List} is immutable.
   *
   * @return The constituent parts of the template file
   */
  public List<Part> getParts() {
    return parts;
  }

  /**
   * Returns the indexes of the parts in the {@link #getParts() parts list} that contain variables.
   * The returned {@code Map} maps variable names to an {@link IntList}, since one variable may
   * occur multiple times in the same template.
   *
   * @return The indexes of parts that contain variables
   */
  public Map<String, IntList> getVarPartIndices() {
    return varIndices;
  }

  /**
   * Returns the indexes of the parts in the {@link #getParts() parts list} that contain nested
   * templates. The returned {@code Map} maps each template name to exactly one {@code List} index,
   * since template names must be unique within the parent template. The returned {@code Map} is
   * immutable.
   *
   * @return The indexes of parts that contain nested templates
   */
  public Map<String, Integer> getTemplatePartIndices() {
    return tmplIndices;
  }

  /**
   * Returns the indexes of the parts in the {@link #getParts() parts list} that contain literal
   * text. Each element in the returned {@link IntList} is an index into the parts list.
   *
   * @return The indexes of parts that contain literal text
   */
  public IntList getTextPartIndices() {
    return textIndices;
  }

  /**
   * Returns the names of all variables in this {@code Template}.
   *
   * @return The names of all variables in this {@code Template}
   */
  public Set<String> getVariableNames() {
    return varIndices.keySet();
  }

  /**
   * Returns a depth-first view of all variable names in this {@code Template}, and all templates
   * nested inside it.
   *
   * @return A depth-first view of all variable names in this {@code Template}, and all templates
   *     nested inside it
   */
  public Set<String> getVariableNamesRecursive() {
    ArrayList<String> names = new ArrayList<>(getVariableNames().size() + 25);
    collectVarsRecursive(this, names);
    return new LinkedHashSet<>(names);
  }

  private static void collectVarsRecursive(Template t0, ArrayList<String> names) {
    names.addAll(t0.getVariableNames());
    t0.getTemplates().forEach(t -> collectVarsRecursive(t, names));
  }

  /**
   * Returns a depth-first view of all variable names in this {@code Template} and all templates
   * nested inside it. Each tuple in the returned {@code Set} contains the name of a variable (on
   * the "right-hand" side of the tuple) and the name of the template to which it belongs (on the
   * "left-hand" side of the tuple).
   *
   * @return A depth-first view of all variable names in this {@code Template} and all templates
   *     nested inside it
   */
  public Set<Tuple<String, String>> getVariableNamesPerTemplate() {
    ArrayList<Tuple<String, String>> tuples = new ArrayList<>(getVariableNames().size() + 25);
    collectVarsPerTemplate(this, tuples);
    return new LinkedHashSet<>(tuples);
  }

  private static void collectVarsPerTemplate(Template t0, ArrayList<Tuple<String, String>> tuples) {
    for (String s : t0.getVariableNames()) {
      tuples.add(Tuple.of(t0.getName(), s));
    }
    t0.getTemplates().forEach(t -> collectVarsPerTemplate(t, tuples));
  }

  /**
   * Returns sum total of all variables in this template. Note that this method does <i>not</i>
   * count the number of unique variable names (which would simply be {@link #getVariableNames()
   * getVariableNames().size()}).
   *
   * @return The sum total of all variables in the template
   */
  public int countVariables() {
    return varCount;
  }

  /**
   * Returns all templates nested directly inside this {@code Template}.
   *
   * @return All templates nested directly inside this {@code Template}
   */
  public Set<Template> getTemplates() {
    return tmplIndices
        .values()
        .stream()
        .map(parts::get)
        .map(NestedTemplatePart.class::cast)
        .map(NestedTemplatePart::getTemplate)
        .collect(toCollection(LinkedHashSet::new));
  }

  /**
   * Returns a depth-first view of all templates directly or indirectly nested inside this {@code
   * Template}. This {@code Template} itself is also included in the returned {@code Set} (as the
   * first element).
   *
   * @return
   */
  public Set<Template> getTemplatesRecursive() {
    ArrayList<Template> tmpls = new ArrayList<>(tmplIndices.size() + 10);
    tmpls.add(this);
    collectTmplsRecursive(this, tmpls);
    return new LinkedHashSet<>(tmpls);
  }

  private static void collectTmplsRecursive(Template t0, ArrayList<Template> tmpls) {
    Set<Template> myTmpls = t0.getTemplates();
    tmpls.addAll(myTmpls);
    myTmpls.forEach(t -> collectTmplsRecursive(t, tmpls));
  }

  /**
   * Returns the names of all templates nested inside this {@code Template} (non-recursive).
   *
   * @return The names of all nested templates
   */
  public Set<String> getTemplateNames() {
    return tmplIndices.keySet();
  }

  /**
   * Returns a depth-first view of all templates nested inside this {@code Template}. Note that
   * template names must be globally unique. That is, no two templates descending from the same
   * ancestor template can have the same name, whatever the branch or depth of their ancestry.
   *
   * @return A depth-first view of all templates nested inside this {@code Template}
   */
  public Set<String> getTemplateNamesRecursive() {
    ArrayList<String> names = new ArrayList<>(getTemplateNames().size() + 10);
    collectTmplNamesRecursive(this, names);
    return new LinkedHashSet<>(names);
  }

  private static void collectTmplNamesRecursive(Template t0, ArrayList<String> names) {
    names.addAll(t0.getTemplateNames());
    t0.getTemplates().forEach(t -> collectTmplNamesRecursive(t, names));
  }

  /**
   * Equivalent to {@link #getTemplateNames() getTemplateNames().size()}.
   *
   * @return The number of nested templates
   */
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
    return ((NestedTemplatePart) parts.get(partIndex)).getTemplate();
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

  /**
   * Prints out the constituent parts of this {@code Template}. Can be used to debug your template.
   *
   * @param out The {@code PrintStream} to which to print
   */
  public void printParts(PrintStream out) {
    new PartsPrinter(this).printParts(out);
  }

  /* +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ */
  /* ++++++++++++++++++++++ END OF PUBLIC INTERFACE ++++++++++++++++++++++ */
  /* +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ */

  private static Map<String, IntList> getVarIndices(List<Part> parts) {
    Map<String, IntList> indices = new LinkedHashMap<>();
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
    Map<String, Integer> indices = new LinkedHashMap<>();
    for (int i = 0; i < parts.size(); ++i) {
      if (parts.get(i) instanceof NestedTemplatePart) {
        String name = ((NestedTemplatePart) parts.get(i)).getName();
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
