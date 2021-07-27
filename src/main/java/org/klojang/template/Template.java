package org.klojang.template;

import java.util.*;
import org.klojang.render.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import nl.naturalis.common.StringMethods;
import nl.naturalis.common.check.Check;
import nl.naturalis.common.collection.IntArrayList;
import nl.naturalis.common.collection.IntList;
import static java.util.stream.Collectors.toUnmodifiableList;
import static org.klojang.template.TemplateSourceType.STRING;
import static nl.naturalis.common.check.CommonChecks.indexOf;
import static nl.naturalis.common.check.CommonChecks.keyIn;

/**
 * A {@code Template} captures the result of parsing a template file. The {@code Template} class and
 * the {@link RenderSession} class are the two central classes of the Klojang library. {@code
 * Template} instances are unmodifiable, expensive-to-create and heavy-weight objects.
 *
 * @author Ayco Holleman
 */
public class Template {

  @SuppressWarnings("unused")
  private static final Logger LOG = LoggerFactory.getLogger(Template.class);

  /**
   * The name given to the root template: "{root}". Any {@code Template} that is explicitly
   * instantiated by calling one of the {@code parse} methods gets this name. Templates nested
   * inside this template get their name from the source code (for example: {@code ~%%begin:foo%} or
   * {@code ~%%include:/views/foo.html%} or {@code ~%%include:foo:/views/bar.html%}).
   */
  public static final String ROOT_TEMPLATE_NAME = "{root}";

  /**
   * Parses the specified string into a {@code Template} instance. If the string contains any {@code
   * include} declarations (e.g. {@code ~%%include:/path/to/template%}) the path will be interpreted
   * as a file system resource. Templates created from a string are never cached and are never equal
   * to one another, even if they are created from the same string.
   *
   * @param clazz Any {@code Class} object that provides access to the included tempate files by
   *     calling {@code getResourceAsStream} on it
   * @param source The source code for the {@code Template}
   * @return a new {@code Template} instance
   * @throws ParseException
   */
  public static Template fromString(String source) throws ParseException {
    Check.notNull(source, "source");
    return new Parser(ROOT_TEMPLATE_NAME, new TemplateId(), source).parse();
  }

  /**
   * Parses the specified string into a {@code Template} instance. The specified class will be used
   * to include other templates using {@code clazz.getResourceAsStream("/path/to/template")}.
   * Templates created from a string are never cached.
   *
   * @param clazz Any {@code Class} object that provides access to the included tempate files by
   *     calling {@code getResourceAsStream} on it
   * @param source The source code for the {@code Template}
   * @return a {@code Template} instance
   * @throws ParseException
   */
  public static Template fromString(Class<?> clazz, String source) throws ParseException {
    Check.notNull(clazz, "clazz");
    Check.notNull(source, "source");
    return new Parser(ROOT_TEMPLATE_NAME, new TemplateId(clazz), source).parse();
  }

  /**
   * Parses the specified resource into a {@code Template} instance. The resource is read using
   * {@code clazz.getResourceAsStream(path)}. Any included templates will be loaded this way as
   * well. Templates created from a classpath resource are always cached. Thus, calling this method
   * multiple times with the same {@code clazz} and {@code path} arguments will always returns the
   * same {@code Template} instance.
   *
   * @param clazz Any {@code Class} object that provides access to the tempate file by calling
   *     {@code getResourceAsStream} on it
   * @param path The location of the template file
   * @return a {@code Template} instance
   * @throws ParseException
   */
  public static Template fromResource(Class<?> clazz, String path) throws ParseException {
    Check.notNull(clazz, "clazz");
    Check.notNull(path, "path");
    return TemplateCache.INSTANCE.get(ROOT_TEMPLATE_NAME, new TemplateId(clazz, path));
  }

  /**
   * Parses the specified file into a {@code Template} instance. Templates created from file are
   * always cached. Thus, calling this method multiple times with the same {@code path} argument
   * will always returns the same {@code Template} instance.
   *
   * @param path The path of the file to be parsed
   * @return a {@code Template} instance
   * @throws ParseException
   */
  public static Template fromFile(String path) throws ParseException {
    Check.notNull(path, "path");
    return TemplateCache.INSTANCE.get(ROOT_TEMPLATE_NAME, new TemplateId(path));
  }

  private final String name;
  private final TemplateId id;
  private final List<Part> parts;
  private final Map<String, IntList> varIndices;
  private final IntList textIndices;
  private final Map<String, Integer> tmplIndices;
  /** All variable names and nested template together */
  private final List<String> names;

  Template parent;

  Template(String name, TemplateId id, List<Part> parts) {
    parts.forEach(p -> ((AbstractPart) p).setParentTemplate(this));
    this.name = name;
    this.id = id.sourceType() == STRING ? null : id;
    this.parts = parts;
    this.varIndices = getVarIndices(parts);
    this.tmplIndices = getTmplIndices(parts);
    this.names = getNames(parts);
    this.textIndices = getTextIndices(parts);
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
   * If the template was created from a file or classpath resource, this method returns its path,
   * else null. In other words, for {@code included} templates this method (by definition) returns a
   * non-null value. For inline templates this method (by definition) returns null. For <i>this</i>
   * {@code Template} the return value depends on which of the static factory methods was used to
   * instantiate the template.
   *
   * @return The file location (if any) of the source code for this {@code Template}
   */
  public String getPath() {
    return id.path();
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
   * <p>The returned {@code List} is unmodifiable.
   *
   * @return The constituent parts of the template file
   */
  public List<Part> getParts() {
    return parts;
  }

  /**
   * Returns the template part at the specified index.
   *
   * @param <T> The type of the {@code Part}
   * @param index The {@code List} index of the {@code Part}
   * @return The template part at the specified index
   */
  @SuppressWarnings("unchecked")
  public <T extends Part> T getPart(int index) {
    return (T) Check.that(index).is(indexOf(), parts).ok(parts::get);
  }

  /**
   * Returns the indices of the parts in the {@link #getParts() parts list} that contain variables.
   * The returned {@code Map} maps each variable name to an {@link IntList}, since one variable may
   * occur multiple times in the same template. In other words, you may find the same variable in
   * multiple parts of the parts list. The returned {@code Map} is unmodifiable.
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
   * unmodifiable.
   *
   * @return The list indices of parts that contain nested templates
   */
  public Map<String, Integer> getTemplatePartIndices() {
    return tmplIndices;
  }

  /**
   * Returns the indices of the parts in the {@link #getParts() parts list} that contain literal
   * text. Each element in the returned {@link IntList} is an index into the parts list. The
   * returned {@code IntList} is unmodifiable.
   *
   * @return The list indices of parts that contain literal text
   */
  public IntList getTextPartIndices() {
    return textIndices;
  }

  /**
   * Returns the names of all variables in this {@code Template} (non-recursive), in order of their
   * first appearance in the template. The returned {@code Set} is unmodifiable.
   *
   * @return The names of all variables in this {@code Template}
   */
  public Set<String> getVariables() {
    return varIndices.keySet();
  }

  /**
   * Returns whether or not this {@code Template} contains a variable with the specified name.
   *
   * @param name The name of the variable
   * @return Whether or not this {@code Template} contains a variable with the specified name
   */
  public boolean containsVariable(String name) {
    return Check.notNull(name).ok(varIndices::containsKey);
  }

  /**
   * Returns the total number of variables in this {@code Template}. Note that this method does not
   * count the number of <i>unique</i> variable names (which would be {@link #getVariables()
   * getVars().size()}).
   *
   * @return The total number of variables in this {@code Template}
   */
  public int countVariables() {
    return (int) parts.stream().filter(VariablePart.class::isInstance).count();
  }

  private List<Template> nestedTemplates;

  /**
   * Returns all templates nested inside this {@code Template} (non-recursive). The returned {@code
   * List} is unmodifiable.
   *
   * @return All templates nested inside this {@code Template}
   */
  public List<Template> getNestedTemplates() {
    if (nestedTemplates == null) {
      return nestedTemplates =
          tmplIndices
              .values()
              .stream()
              .map(parts::get)
              .map(NestedTemplatePart.class::cast)
              .map(NestedTemplatePart::getTemplate)
              .collect(toUnmodifiableList());
    }
    return nestedTemplates;
  }

  /**
   * Returns the names of all templates nested inside this {@code Template} (non-recursive). The
   * returned {@code Set} is unmodifiable.
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
   * Returns the names of all variables and all nested templates within this {@code Template}
   * (non-recursive). The returned {@code List} is unmodifiable.
   *
   * @return The names of all variables and nested templates in this {@code Template}
   */
  public List<String> getNames() {
    return names;
  }

  /**
   * Returns whether or not this is a text-only template. In other words, whether this is a template
   * without variables or nested templates.
   *
   * @return
   */
  public boolean isTextOnly() {
    return names.isEmpty();
  }

  /**
   * Returns a {@code RenderSession} that will only use the {@link
   * AccessorFactory#STANDARD_ACCESSORS predefined accessors} to extract values from source data
   * objects, and only the {@link StringifierFactory predefined stringifiers} to stringify those
   * values.
   *
   * @return A new {@code RenderSession}
   */
  public RenderSession newRenderSession() {
    return new SessionConfig(this).newRenderSession();
  }

  /**
   * Returns a {@code RenderSession} that will use the {@link AccessorFactory#STANDARD_ACCESSORS
   * predefined accessors} to extract values from source data objects, and the specified {@code
   * StringifierFactory} to get hold of stringifiers for those values.
   *
   * @param stringifiers The {@code StringifierFactory} used to supply the {@code RenderSession}
   *     with {@link Stringifier stringifiers}
   * @return A new {@code RenderSession}
   */
  public RenderSession newRenderSession(StringifierFactory stringifiers) {
    Check.notNull(stringifiers, "stringifiers");
    return new SessionConfig(this, stringifiers).newRenderSession();
  }

  /**
   * Returns a {@code RenderSession} that will only use the specified {@code AccessorFactory} to
   * extract values from source data, and the {@link StringifierFactory predefined stringifiers} to
   * stringify those values.
   *
   * @param accessors The {@code AccessorFactory} used to supply the {@code RenderSession} with
   *     {@link Accessor accessors}
   * @return A new {@code RenderSession}
   */
  public RenderSession newRenderSession(AccessorFactory accessors) {
    Check.notNull(accessors, "accessors");
    return new SessionConfig(this, accessors).newRenderSession();
  }

  /**
   * Returns a {@code RenderSession} that will only use the specified {@code AccessorFactory} to
   * extract values from source data, and the specified {@code StringifierFactory} to get hold of
   * stringifiers for those values.
   *
   * @param accessors The {@code AccessorFactory} used to supply the {@code RenderSession} with
   *     {@link Accessor accessors}
   * @param stringifiers The {@code StringifierFactory} used to supply the {@code RenderSession}
   *     with {@link Stringifier stringifiers}
   * @return A new {@code RenderSession}
   */
  public RenderSession newRenderSession(
      AccessorFactory accessors, StringifierFactory stringifiers) {
    Check.notNull(accessors, "accessors");
    Check.notNull(stringifiers, "stringifiers");
    return new SessionConfig(this, accessors, stringifiers).newRenderSession();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    } else if (id == null || obj == null || getClass() != obj.getClass()) {
      return false;
    }
    return id.equals(((Template) obj).id);
  }

  @Override
  public int hashCode() {
    return id == null ? System.identityHashCode(this) : id.hashCode();
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
    indices.entrySet().forEach(e -> e.setValue(IntList.copyOf(e.getValue())));
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
    return IntList.copyOf(indices);
  }
}