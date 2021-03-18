package nl.naturalis.yokete.render;

import java.io.OutputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import nl.naturalis.common.StringMethods;
import nl.naturalis.common.check.Check;
import nl.naturalis.common.collection.IntList;
import nl.naturalis.yokete.template.Part;
import nl.naturalis.yokete.template.Template;
import nl.naturalis.yokete.template.VariablePart;
import static nl.naturalis.common.ArrayMethods.EMPTY_STRING_ARRAY;
import static nl.naturalis.common.CollectionMethods.asList;
import static nl.naturalis.common.ObjectMethods.isEmpty;
import static nl.naturalis.common.ObjectMethods.n2e;
import static nl.naturalis.common.check.CommonChecks.*;
import static nl.naturalis.common.check.CommonGetters.size;
import static nl.naturalis.yokete.render.EscapeType.ESCAPE_NONE;
import static nl.naturalis.yokete.render.EscapeType.NOT_SPECIFIED;
import static nl.naturalis.yokete.render.RenderException.*;

/**
 * A {@code RenderSession} is responsible for populating a template and rendering it. Populating the
 * template can be done in multiple passes. By default template variables and nested templates are
 * not rendered. As soon as you render the template (by calling of the {@code render} methods), the
 * {@code RenderSession} effectively becomes immutable. You can render the template again, as often
 * as you like, but the values of its variables are fixed.
 *
 * <p>A {@code RenderSession} is a throw-away object that should go out of scope as quickly as
 * possible. It is cheap to instantiate, but can gain a lot of state as the template gets populated.
 * Therefore, make sure it doesn't survive your request method. A possible exception could be
 * templates that render relatively static content, especially if the cost of populating them is
 * high.
 *
 * <h4>Thead Safety</h4>
 *
 * A {@code RenderSession} carries a lot of state across its methods and is therefore in principle
 * not thread-safe. However, as long as different threads populate different parts of the template
 * (e.g. one thread populates the main table and another thread does the rest), they cannot get in
 * each other's way.
 *
 * @author Ayco Holleman
 */
public class RenderSession {

  private final SessionFactory factory;
  private final RenderState state;

  RenderSession(SessionFactory rsf) {
    this.factory = rsf;
    this.state = new RenderState(rsf);
  }

  /* METHODS FOR SETTING A SINGLE TEMPLATE VARIABLE */

  /**
   * Sets the specified variable to the specified value. Unless the variable was declared with an
   * inline {@link EscapeType} (e.g. {@code ~%html:fullName%}) no escaping will be applied to the
   * value.
   *
   * @param name The name of the variable to set
   * @param value The value of the variable
   * @throws RenderException
   */
  public RenderSession set(String name, Object value) throws RenderException {
    return set(name, asList(value), ESCAPE_NONE);
  }

  /**
   * Sets the specified variable to the specified value using the specified escape type. If the
   * variable was declared with an inline escape type that differs from the specified escape type,
   * the inline escape type will prevail. This allows you to declare variables inside HTML tags
   * (most likely the overwhelming majority) without an inline escape type, while only specifying an
   * inline escape type for variables inside <code>&lt;script&gt;</code> tags (namely "js"). This
   * makes your template easier to read and write.
   *
   * @param name The name of the variable to set
   * @param value The value of the variable
   * @param escapeType The escape type to use if the variable has no inline escape type
   * @return This {@code RenderSession}
   * @throws RenderException
   */
  public RenderSession set(String name, Object value, EscapeType escapeType)
      throws RenderException {
    return set(name, asList(value), escapeType);
  }

  /**
   * Sets the specified variable to the concatenation of the values within the specified {@code
   * List}. Unless the variable was declared with an inline {@link EscapeType} no escaping will be
   * applied to the value. The values in the {@code List} are first stringified, then escaped, then
   * concatenated. If the {@code List} is empty, the variable will not be rendered at all (that is,
   * an empty string will be inserted at the location of the variable within the template).
   *
   * @param name The name of the variable to set
   * @param values The string values to concatenate
   * @return This {@code RenderSession}
   * @throws RenderException
   */
  public RenderSession set(String name, List<?> values) throws RenderException {
    return set(name, values, ESCAPE_NONE);
  }

  /**
   * Sets the specified variable to the concatenation of the values within the specified {@code
   * List}. The values in the {@code List} are first stringified, then escaped, then concatenated.
   * If the {@code List} is empty, the variable will not be rendered at all (that is, an empty
   * string will be inserted at the location of the variable within the template).
   *
   * @param name The name of the variable to set
   * @param values The string values to concatenate
   * @param escapeType The escape type to use if the variable did not declare an inline escape type
   * @return This {@code RenderSession}
   * @throws RenderException
   */
  public RenderSession set(String name, List<?> values, EscapeType escapeType)
      throws RenderException {
    return set(name, values, escapeType, null, null, null);
  }

  /**
   * Sets the specified variable to the concatenation of the values within the specified {@code
   * List}. Each value will be prefixed with the specified prefix, suffixed with the specified
   * suffix, and separated from the previous one by the specified separator. The values in the
   * {@code List} are first stringified, then escaped, then enriched with prefix, suffix and
   * separator, and then concatenated. If the {@code List} is empty, the variable will not be
   * rendered at all.
   *
   * @param name The name of the variable to set
   * @param values The string values to concatenate
   * @param escapeType The escape type to use if the variable did not declare an inline escape type
   * @param prefix The prefix to use for each string
   * @param separator The suffix to use for each string
   * @param suffix The separator to use between the stringd
   * @return This {@code RenderSession}
   * @throws RenderException
   */
  public RenderSession set(
      String name,
      List<?> values,
      EscapeType escapeType,
      String prefix,
      String separator,
      String suffix)
      throws RenderException {
    Check.on(invalidValue("name", name), name).is(notNull());
    Check.on(invalidValue("values", values), values).is(notNull());
    Check.on(invalidValue("escapeType", escapeType), escapeType).is(notNull());
    Template t = factory.getTemplate();
    Check.on(alreadySet(t, name), state.isSet(name)).is(no());
    Check.on(noSuchVariable(t, name), name).is(in(), t.getVars());
    Check.on(badEscapeType(), escapeType).is(notSameAs(), NOT_SPECIFIED);
    IntList indices = factory.getTemplate().getVarPartIndices().get(name);
    if (values.isEmpty()) {
      indices.forEach(i -> state.setVar(i, EMPTY_STRING_ARRAY));
    } else {
      String[] strings = factory.toString(name, values);
      indices.forEach(i -> setVar(i, strings, escapeType, prefix, separator, suffix));
    }
    state.done(name);
    return this;
  }

  private void setVar(
      int partIndex,
      String[] strvals,
      EscapeType escapeType,
      String prefix,
      String separator,
      String suffix) {
    List<Part> parts = factory.getTemplate().getParts();
    VariablePart part = (VariablePart) parts.get(partIndex);
    EscapeType myEscType = part.getEscapeType();
    if (myEscType == NOT_SPECIFIED) {
      myEscType = escapeType;
    }
    prefix = n2e(prefix);
    separator = n2e(separator);
    suffix = n2e(suffix);
    boolean escape = myEscType != ESCAPE_NONE;
    boolean enrich = !prefix.isEmpty() || !separator.isEmpty() || !suffix.isEmpty();
    for (int i = 0; i < strvals.length; ++i) {
      String s = strvals[i];
      if (escape) {
        if (enrich) {
          if (i == 0) {
            s = prefix + myEscType.apply(s) + suffix;
          } else {
            s = separator + prefix + myEscType.apply(s) + suffix;
          }
        } else {
          s = myEscType.apply(s);
        }
        strvals[i] = s;
      } else if (enrich) {
        if (i == 0) {
          s = prefix + s + suffix;
        } else {
          s = separator + prefix + s + suffix;
        }
        strvals[i] = s;
      }
    }
    state.setVar(partIndex, strvals);
  }

  /* METHODS FOR POPULATING A SINGLE NESTED TEMPLATE */

  /**
   * Populates a <i>nested</i> template. The template is populated with values retrieved from the
   * specified source data. Only variables and (doubly) nested templates whose name is present in
   * the {@code names} argument will be populated. No escaping will be applied to the values
   * retrieved from the data object. See {@link #fill(String, Object, EscapeType, String...)}.
   *
   * @param nestedTemplateName The name of the nested template
   * @param sourceData An object that provides data for all or some of the nested template's
   *     variables and nested templates
   * @param names The names of the variables and doubly-nested templates that you want to be
   *     populated using the specified data object
   */
  public RenderSession fill(String nestedTemplateName, Object sourceData, String... names)
      throws RenderException {
    return fill(nestedTemplateName, sourceData, ESCAPE_NONE, names);
  }

  /**
   * Populates a <i>nested</i> template. The template is populated with values retrieved from the
   * specified source data. Only variables and (doubly) nested templates whose name is present in
   * the {@code names} argument will be populated.
   *
   * <h4>Repeating Templates</h4>
   *
   * <p>If the specified object is an array or a {@code Collection}, the template will be repeated
   * for each object in the array or {@code Collection}. This can be used, for example, to generate
   * an HTML table from a nested template that contains just a single row.
   *
   * <h4>Conditional Rendering</h4>
   *
   * <p>If the specified object is an empty array or an empty {@code Collection}, the template will
   * not be rendered at all. This allows for conditional rendering (render the template only if
   * certain conditions are met). Note, however, that the same can be achieved more easily by just
   * never calling the {@code fill} method for the template it will not be rendered either. By
   * default neither template variables nor nested templates are rendered.
   *
   * @param nestedTemplateName The name of the nested template
   * @param sourceData An object that provides data for all or some of the nested template's
   *     variables and nested templates
   * @param escapeType The escape to use for the variables within the nested template
   * @param names The names of the variables and doubly-nested templates that you want to be
   *     populated using the specified data object
   * @return This {@code RenderSession}
   * @throws RenderException
   */
  public RenderSession fill(
      String nestedTemplateName, Object sourceData, EscapeType escapeType, String... names)
      throws RenderException {
    return repeat(nestedTemplateName, asList(sourceData), escapeType, names);
  }

  /**
   * Enables rendering of text-only nested templates. Equivalent to {@link #show(String, int)
   * show(nestedTemplateName, 1)}.
   *
   * @param nestedTemplateName The name of the nested template. <b>Must</b> be a text-only template,
   *     otherwise a {@code RenderException} is thrown
   * @return This {@code RenderSession}
   * @throws RenderException
   */
  public RenderSession show(String nestedTemplateName) throws RenderException {
    return show(nestedTemplateName, 1);
  }

  /**
   * Enables/disables rendering of text-only nested templates. In other words: nested templates
   * without any variables or doubly nested templates. One reason you might want to define such a
   * template is that you want to conditionally render it. In principle you could achieve the same
   * by calling {@code fill(nestedTemplateName, new Object[repeats])}, because whatever value you
   * pass is ignored in case of text-only templates. However, this is cleaner and cheaper. To
   * disable rendering, specify 0 (zero) for the {@code repeats} argument, or just don't call this
   * method.
   *
   * @param nestedTemplateName The name of the nested template. <b>Must</b> be a text-only template,
   *     otherwise a {@code RenderException} is thrown
   * @param repeats The number of times you want the template to be repeated in the render result
   * @return This {@code RenderSession}
   * @throws RenderException
   */
  public RenderSession show(String nestedTemplateName, int repeats) throws RenderException {
    String name = nestedTemplateName;
    Check.on(invalidValue("nestedTemplateName", name), name).is(notNull());
    Check.on(invalidValue("repeats", repeats), repeats).is(gte(), 0);
    Check.on(noSuchTemplate(name), name).is(validTemplateName());
    Template t = factory.getTemplate().getNestedTemplate(name);
    Check.on(notTextOnly(t), t.getNames()).has(size(), eq(), 0);
    state.createChildSessions(t, repeats);
    return this;
  }

  /**
   * Convenience method for populating a nested template that contains exactly one variable. See
   * {@link #fillMono(String, Object, EscapeType)}.
   *
   * @param nestedTemplateName The name of the nested template, which <i>must</i> contain exactly
   *     one variable
   * @return This {@code RenderSession}
   * @throws RenderException
   */
  public RenderSession fillMono(String nestedTemplateName, Object value) throws RenderException {
    return fillMono(nestedTemplateName, value, ESCAPE_NONE);
  }

  /**
   * Convenience method for populating a nested template that contains exactly one variable.
   * Ordinarily nested templates are populated with a complex {code Object} and an {@link Accessor}
   * that retrieves values from it. With this method, however, you specify the variable's value
   * directly. The value can still be an array or {@code Collection}, causing the template to repeat
   * itself. See {@link #fill(String, Object, EscapeType, String...)}.
   *
   * @param nestedTemplateName The name of the nested template, which <i>must</i> contain exactly
   *     one variable
   * @param escapeType The escape to use for the variable within the nested template
   * @return This {@code RenderSession}
   * @throws RenderException
   */
  public RenderSession fillMono(String nestedTemplateName, Object value, EscapeType escapeType)
      throws RenderException {
    String name = nestedTemplateName;
    Check.on(invalidValue("nestedTemplateName", name), name).is(notNull());
    Check.on(invalidValue("value", value), value).is(notNull());
    Check.on(invalidValue("escapeType", escapeType), escapeType).is(notNull());
    Check.on(noSuchTemplate(name), name).is(validTemplateName());
    Check.on(badEscapeType(), escapeType).is(notSameAs(), NOT_SPECIFIED);
    Template t = factory.getTemplate().getNestedTemplate(name);
    Check.on(notMono(t), t)
        .has(Template::countVars, eq(), 1)
        .has(Template::countNestedTemplates, eq(), 0);
    List<?> values = asList(value);
    String monoVar = t.getVars().iterator().next();
    List<RenderSession> sessions = state.createChildSessions(t, values.size());
    for (int i = 0; i < values.size(); ++i) {
      String escaped = factory.getStringifier().toString(t, monoVar, values.get(i));
      sessions.get(i).set(monoVar, escaped, ESCAPE_NONE);
    }
    return this;
  }

  private RenderSession repeat(String name, List<?> data, EscapeType escapeType, String... names)
      throws RenderException {
    Check.on(invalidValue("nestedTemplateName", name), name).is(notNull());
    Check.on(invalidValue("escapeType", escapeType), escapeType).is(notNull());
    Check.on(invalidValue("data", data), data).is(noneNull());
    Check.on(noSuchTemplate(name), name).is(validTemplateName());
    Check.on(badEscapeType(), escapeType).is(notSameAs(), NOT_SPECIFIED);
    List<RenderSession> sessions = state.createChildSessions(name, data.size());
    for (int i = 0; i < sessions.size(); ++i) {
      sessions.get(i).populate(data.get(i), escapeType, names);
    }
    return this;
  }

  /* CONVENIENCE METHODS */

  /**
   * Creates a new {@code RenderSession} for the specified nested template. Note that child sessions
   * are automatically created for source data objects that reflect the template's structure. This
   * method lests you "manually" recurse into the template.
   *
   * <p>Unless you have some special purpose for it, you should not call {@code render} or {@code
   * renderSafe} on the child session. Anything you do in the child session will become visible once
   * you render the main session.
   *
   * @param nestedTemplateName The name of a variable or nested template
   * @return A child session of the current session
   * @throws RenderException
   */
  public RenderSession createChildSession(String nestedTemplateName) throws RenderException {
    String name = nestedTemplateName;
    Check.on(invalidValue("nestedTemplateName", name), name).is(notNull());
    Check.on(noSuchTemplate(name), name).is(validTemplateName());
    return state.createChildSessions(name, 1).get(0);
  }

  /* METHODS FOR POPULATING WHATEVER IS IN THE PROVIDED OBJECT */

  /**
   * Populates the <i>entire</i> template, except for variables and nested templates whose name is
   * present in the {@code names} array. The template is populated with values retrieved from the
   * specified source data. No escaping will be applied to the retrieved values. See {@link
   * #populate(Object, EscapeType, String...)}.
   *
   * @param sourceData An object that provides data for all or some of the template variables and
   *     nested templates
   * @param escapeType The escape type to use
   * @param names The names of the variables nested templates names that must be populated. Not
   *     specifying any name (or {@code null}) indicates that you want all variables and nested
   *     templates to be populated.
   * @return This {@code RenderSession}
   * @throws RenderException
   */
  public RenderSession populate(Object sourceData, String... names) throws RenderException {
    return populate(sourceData, ESCAPE_NONE, names);
  }

  /**
   * Populates the <i>entire</i> template, except for variables and nested templates whose name is
   * present in the {@code names} array. This allows you to call this method multiple times with the
   * same source data, but with different escape types for different variables. If the {@code names}
   * array is {@code null} or empty, this method will attempt to populate all variables and nested
   * templates. Note, however, that the source data object is itself explicitly not required to
   * provide all values for all variables (see {@link Accessor#access(Object, String)}). This again
   * allows you to call this method multiple times with <i>different</i> source data, until the
   * template is fully populated.
   *
   * @param data An object that provides data for all or some of the template variables and nested
   *     templates
   * @param escapeType The escape type to use
   * @param names The names of the variables nested templates names that must be populated. Not
   *     specifying any name (or {@code null}) indicates that you want all variables and nested
   *     templates to be populated.
   * @return This {@code RenderSession}
   * @throws RenderException
   */
  public RenderSession populate(Object data, EscapeType escapeType, String... names)
      throws RenderException {
    if (data == null) {
      Check.on(notMono(factory.getTemplate()), factory.getTemplate())
          .has(Template::countVars, eq(), 1)
          .has(Template::countNestedTemplates, eq(), 0);
      /*
       * The entire template is in fact static HTML.
       * Bit wasteful, but why not support it.
       */
      return this;
    }
    processVars(data, escapeType, names);
    processTmpls(data, escapeType, names);
    return this;
  }

  private void processVars(Object data, EscapeType escapeType, String[] names)
      throws RenderException {
    Set<String> varNames;
    if (isEmpty(names)) {
      varNames = factory.getTemplate().getVars();
    } else {
      varNames = new HashSet<>(factory.getTemplate().getVars());
      varNames.retainAll(Set.of(names));
    }
    for (String varName : varNames) {
      Object value = factory.getAccessor().access(data, varName);
      set(varName, value, escapeType);
    }
  }

  private void processTmpls(Object data, EscapeType escapeType, String[] names)
      throws RenderException {
    Set<String> tmplNames;
    if (isEmpty(names)) {
      tmplNames = factory.getTemplate().getNestedTemplateNames();
    } else {
      tmplNames = new HashSet<>(factory.getTemplate().getNestedTemplateNames());
      tmplNames.retainAll(Set.of(names));
    }
    for (String name : tmplNames) {
      Object nestedData = factory.getAccessor().access(data, name);
      fill(name, nestedData, escapeType, names);
    }
  }

  /* RENDER METHODS */

  /**
   * Verifies that is fully populated. If this is not the case, calling {@link
   * #renderSafe(OutputStream) renderSafe} will throw a {@link RenderException}.
   *
   * @return Whether or not the template is fully populated
   */
  public boolean isReady() {
    return state.isReady();
  }

  /**
   * Writes the render result to the specified output stream.
   *
   * @param out The output stream to which to write the render result
   * @throws RenderException
   */
  public void render(OutputStream out) {
    Check.notNull(out);
    saturate();
    Renderer renderer = new Renderer(state);
    renderer.render(out);
  }

  /**
   * Appends the render result to the specified {@code StringBuilder}.
   *
   * @param sb The {@code StringBuilder} to which to append the render result
   * @throws RenderException
   */
  public void render(StringBuilder sb) throws RenderException {
    Check.notNull(sb);
    saturate();
    Renderer renderer = new Renderer(state);
    renderer.render(sb);
  }

  /**
   * Writes the render result to the specified output stream. If the template is not fully
   * populated, a {@code RenderException} is thrown and the output stream is left untouched.
   *
   * @param out The output stream to which to write the render result
   */
  public void renderSafe(OutputStream out) throws RenderException {
    Check.on(invalidValue("out", out), out).is(notNull());
    Check.on(notRenderable(state.getUnsetVars()), isReady()).is(yes());
    Renderer renderer = new Renderer(state);
    renderer.render(out);
  }

  /**
   * Appends the render result to the specified {@code StringBuilder}. If the template is not fully
   * populated, a {@code RenderException} is thrown and the {@code StringBuilder} is left untouched.
   *
   * @param sb The {@code StringBuilder} to which to append the render result
   */
  public void renderSafe(StringBuilder sb) throws RenderException {
    Check.on(invalidValue("sb", sb), sb).is(notNull());
    Check.on(notRenderable(state.getUnsetVars()), isReady()).is(yes());
    Renderer renderer = new Renderer(state);
    renderer.render(sb);
  }

  RenderState getState() {
    return state;
  }

  private Predicate<String> validTemplateName() {
    return s -> factory.getTemplate().getNestedTemplateNames().contains(s);
  }

  /*
   * Explicitly sets all remaining unset variables to an empty string in order to saturate the
   * template. A template can be rendered again and again but the values of its variables are fixed
   * forever.
   */
  private void saturate() {
    saturate0(this);
  }

  private static void saturate0(RenderSession s0) {
    try {
      for (String var : s0.state.getUnsetVars()) {
        s0.set(var, StringMethods.EMPTY);
      }
    } catch (RenderException e) {
    }
    Set<Template> inProgress = s0.state.getChildSessions().keySet();
    Set<Template> todo = new HashSet<>(s0.factory.getTemplate().getNestedTemplates());
    todo.removeAll(inProgress);
    try {
      for (Template t : todo) {
        s0.fill(t.getName(), Collections.emptyList());
      }
    } catch (RenderException e) {
    }
    for (Template t : inProgress) {
      s0.state.getChildSessions(t).forEach(RenderSession::saturate0);
    }
  }
}
