package nl.naturalis.yokete.template;

import java.io.PrintStream;
import java.nio.file.Path;
import java.util.*;
import nl.naturalis.common.StringMethods;
import nl.naturalis.common.Tuple;
import nl.naturalis.common.check.Check;
import nl.naturalis.common.collection.IntArrayList;
import nl.naturalis.common.collection.IntList;
import nl.naturalis.common.collection.UnmodifiableIntList;
import nl.naturalis.yokete.render.Accessor;
import nl.naturalis.yokete.render.RenderSession;
import nl.naturalis.yokete.render.Stringifier;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toUnmodifiableList;
import static nl.naturalis.common.check.CommonChecks.keyIn;

/**
 * A {@code Template} captures the result of parsing a template file. Together with the {@link
 * RenderSession} class this class forms the heart of the Yokete library. It functions as a
 * knowledge repository for a particular template and it used as such by the {@link RenderSession},
 * and by you as you configure your {@link Stringifier stringifiers} and {@link Accessor accessors}.
 *
 * <p>{@code Template} instances are immutable, expensive-to-create and heavy-weight objects. They
 * should be created just once per source file, cached somewhere, and then reused for as long as the
 * application lasts. Creating a new {@code Template} instance for each new request would be very
 * inefficient.
 *
 * @author Ayco Holleman
 */
public class Template {

  /**
   * The name of the root template: "@root". Any {@code Template} that is explicitly instantiated by
   * calling one of the {@code parse(...)} methods gets this name. Templates nested inside this
   * template get their name from the source code (for example: {@code ~%%begin:foo%} or {@code
   * ~%%include:/views/foo.html%} or {@code ~%%include:foo:/views/bar.html%}).
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
  public static Template parseString(String source) throws ParseException {
    return parseString((Class<?>) null, source);
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
  public static Template parseString(Class<?> clazz, String source) throws ParseException {
    return new Parser(ROOT_TEMPLATE_NAME, clazz, source).parse();
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
  public static Template parseResource(Class<?> clazz, String path) throws ParseException {
    return new Parser(ROOT_TEMPLATE_NAME, clazz, Path.of(path)).parse();
  }

  private final String name;
  private final Path path;
  private final List<Part> parts;
  private final Map<String, IntList> varIndices;
  private final IntList textIndices;
  private final Map<String, Integer> tmplIndices;
  private final List<String> names; // names of all vars and nested templates

  private Template parent;

  Template(String name, Path path, List<Part> parts) {
    this.name = name;
    this.path = path;
    this.parts = parts;
    this.varIndices = getVarIndices(parts);
    this.tmplIndices = getTmplIndices(parts);
    this.names = getNames(parts);
    this.textIndices = getTextIndices(parts);
    this.tmplIndices
        .values()
        .stream()
        .map(parts::get)
        .map(NestedTemplatePart.class::cast)
        .map(NestedTemplatePart::getTemplate)
        .forEach(t -> t.parent = this);
  }

  /**
   * Returns the name of this {@code Template}.
   *
   * @return The name of this {@code Template}
   */
  public String getName() {
    return name;
  }

  /**
   * Returns the template inside which this {@code Template} is nested. If this is a root template
   * (it was <i>created from</i> source code rather than <i>defined in</i> source code), this method
   * returns {@code null}.
   *
   * @return The template inside which this {@code Template} is nested
   */
  public Template getParent() {
    return parent;
  }

  /**
   * If the template was created from a file, this method returns its path, else null. In other
   * words, for {@code included} templates this method (by definition) returns a non-null value. For
   * nested templates this method (by definition) returns null. For <i>this</i> {@code Template} the
   * return value depends on which {@code parse} method was used to instantiate the template.
   *
   * @return The file location (if any) of the source code for this {@code Template}
   */
  public Path getPath() {
    return path;
  }

  /**
   * Returns the constituent parts of this {@code Template}. A template is broken up into parts
   * containing {@link TextPart literal text}, parts containing {@link VariablePart template
   * variables} and parts containing {@link NestedTemplatePart nested templates} (which could either
   * be {@link InlineTemplatePart inline templates} or {@link IncludedTemplatePart included
   * templates}). Apart from the fact that the source code for an inline template resides within the
   * parent template, while the source code for an included template resides in a different file,
   * there is no functional difference between them.
   *
   * <p>The returned {@code List} is immutable.
   *
   * @return The constituent parts of the template file
   */
  public List<Part> getParts() {
    return parts;
  }

  /**
   * Returns the indices of the parts in the {@link #getParts() parts list} that contain variables.
   * The returned {@code Map} maps each variable name to an {@link IntList}, since one variable may
   * occur multiple times in the same template. In other words, you may find the same variable in
   * multiple parts of the parts list. The returned {@code Map} is immutable.
   *
   * @return The list indices of parts that contain variables
   */
  public Map<String, IntList> getVarPartIndices() {
    return varIndices;
  }

  /**
   * Returns the indices of the parts in the {@link #getParts() parts list} that contain nested
   * templates. The returned {@code Map} maps each template name to exactly one {@code List} index,
   * since template names must be unique within the parent template. The returned {@code Map} is
   * immutable.
   *
   * @return The list indices of parts that contain nested templates
   */
  public Map<String, Integer> getTemplatePartIndices() {
    return tmplIndices;
  }

  /**
   * Returns the indices of the parts in the {@link #getParts() parts list} that contain literal
   * text. Each element in the returned {@link IntList} is an index into the parts list. The
   * returned {@code IntList} is immutable.
   *
   * @return The list indices of parts that contain literal text
   */
  public IntList getTextPartIndices() {
    return textIndices;
  }

  /**
   * Returns the names of all variables in this {@code Template}, in order of their first appearance
   * in the template. The returned {@code Set} is immutable.
   *
   * @return The names of all variables in this {@code Template}
   */
  public Set<String> getVars() {
    return varIndices.keySet();
  }

  /**
   * Returns whether or not this {@code Template} contains a variable with the specified name.
   *
   * @param name The name of the variable
   * @return Whether or not this {@code Template} contains a variable with the specified name
   */
  public boolean containsVar(String name) {
    return Check.notNull(name).ok(varIndices::containsKey);
  }

  /**
   * Returns the total number of variables in this {@code Template}. Note that this method does not
   * count the number of <i>unique</i> variable names (which would be {@link #getVars()
   * getVars().size()}).
   *
   * @return The total number of variables in this {@code Template}
   */
  public int countVars() {
    return (int) parts.stream().filter(VariablePart.class::isInstance).count();
  }

  /**
   * Returns, for this {@code Template} and all templates descending from it, the names of their
   * variables. Each tuple in the returned {@code List} contains a {@code Template} instance and a
   * variable name. The returned {@code List} is created on demand and mutable.
   *
   * @return All variable names in this {@code Template} and the templates nested inside it
   */
  public List<Tuple<Template, String>> getVarsPerTemplate() {
    ArrayList<Tuple<Template, String>> tuples = new ArrayList<>(25);
    collectVarsPerTemplate(this, tuples);
    return tuples;
  }

  private static void collectVarsPerTemplate(
      Template t0, ArrayList<Tuple<Template, String>> tuples) {
    t0.getVars().stream().map(s -> Tuple.of(t0, s)).forEach(tuples::add);
    t0.getNestedTemplates().forEach(t -> collectVarsPerTemplate(t, tuples));
  }

  /**
   * Returns all templates nested inside this {@code Template} (non-recursive). The returned {@code
   * List} is created on demand and mutable.
   *
   * @return All templates nested inside this {@code Template}
   */
  public List<Template> getNestedTemplates() {
    return tmplIndices
        .values()
        .stream()
        .map(parts::get)
        .map(NestedTemplatePart.class::cast)
        .map(NestedTemplatePart::getTemplate)
        .collect(toList());
  }

  /**
   * Returns the names of all templates nested inside this {@code Template} (non-recursive). The
   * returned {@code Set} is immutable.
   *
   * @return The names of all nested templates
   */
  public Set<String> getNestedTemplateNames() {
    return tmplIndices.keySet();
  }

  /**
   * Returns whether or not this {@code Template} contains a nested template with the specified
   * name.
   *
   * @param name The name of the nested template
   * @return Whether or not this {@code Template} contains a nested template with the specified name
   */
  public boolean containsNestedTemplate(String name) {
    return Check.notNull(name).ok(varIndices::containsKey);
  }

  /**
   * Returns the number of templates nested inside this {@code Template} (non-recursive).
   *
   * @return The number of nested templates
   */
  public int countNestedTemplates() {
    return tmplIndices.size();
  }

  /**
   * Returns the nested template identified by the specified name. This method throws an {@link
   * IllegalArgumentException} if no nested template has the specified name.
   *
   * @param name The name of a nested template
   * @return The {@code Template} with the specified name
   */
  public Template getNestedTemplate(String name) {
    Check.notNull(name).is(keyIn(), tmplIndices, "No such template: \"%s\"", name);
    int partIndex = tmplIndices.get(name);
    return ((NestedTemplatePart) parts.get(partIndex)).getTemplate();
  }

  /**
   * Returns this {@code Template} and all templates descending from it. The returned {@code List}
   * is created on demand and mutable.
   *
   * @return This {@code Template} and all templates descending from it
   */
  public List<Template> getNestedTemplatesRecursive() {
    ArrayList<Template> tmpls = new ArrayList<>(20);
    tmpls.add(this);
    collectTmplsRecursive(this, tmpls);
    return tmpls;
  }

  private static void collectTmplsRecursive(Template t0, ArrayList<Template> tmpls) {
    List<Template> myTmpls = t0.getNestedTemplates();
    tmpls.addAll(myTmpls);
    myTmpls.forEach(t -> collectTmplsRecursive(t, tmpls));
  }
  /**
   * Returns the names of all variables and nested templates in this {@code Template}
   * (non-recursive). The returned {@code List} is immutable.
   *
   * @return The names of all variables and nested templates in this {@code Template}
   */
  public List<String> getNames() {
    return names;
  }

  /**
   * Returns whether or not this is a text-only template. In other words: whether or not this is a
   * template without any variables or nested templates.
   *
   * @return
   */
  public boolean isTextOnly() {
    return names.isEmpty();
  }

  /**
   * The {@code Template} class explicitly overrides the {@code equals()} method such that the only
   * object equal to a {@code Template} instance is the instance itself. Therefore you should be
   * wary of creating two {@code Template} instances from the same source file.
   */
  @Override
  public boolean equals(Object obj) {
    return this == obj;
  }

  /** Returns {@code System.identityHashCode(this)}. */
  @Override
  public int hashCode() {
    return System.identityHashCode(this);
  }

  /**
   * More or less re-assembles to source code from the constituent parts of the {@code Template}.
   * Note, however, that ditch block are ditched early on in the parsing process and there is no
   * trace of them left in the resulting {@code Template} instance.
   */
  @Override
  public String toString() {
    return StringMethods.implode(parts, "");
  }

  /**
   * Prints out the constituent parts of this {@code Template}. Can be useful when debugging your
   * template.
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
    return Collections.unmodifiableMap(indices);
  }

  private static Map<String, Integer> getTmplIndices(List<Part> parts) {
    Map<String, Integer> indices = new LinkedHashMap<>();
    for (int i = 0; i < parts.size(); ++i) {
      if (parts.get(i) instanceof NestedTemplatePart) {
        String name = ((NestedTemplatePart) parts.get(i)).getName();
        indices.put(name, i);
      }
    }
    return Collections.unmodifiableMap(indices);
  }

  private static List<String> getNames(List<Part> parts) {
    return parts
        .stream()
        .filter(NamedPart.class::isInstance)
        .map(NamedPart.class::cast)
        .map(NamedPart::getName)
        .collect(toUnmodifiableList());
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
