package nl.naturalis.yokete.render;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import nl.naturalis.common.Tuple;
import nl.naturalis.common.check.Check;
import nl.naturalis.common.collection.IntList;
import nl.naturalis.common.io.UnsafeByteArrayOutputStream;
import nl.naturalis.yokete.accessors.BypassAccessor;
import nl.naturalis.yokete.accessors.SelfAccessor;
import nl.naturalis.yokete.template.Part;
import nl.naturalis.yokete.template.Template;
import nl.naturalis.yokete.template.TemplateUtils;
import nl.naturalis.yokete.template.VariablePart;
import static java.util.stream.Collectors.toList;
import static nl.naturalis.common.ArrayMethods.EMPTY_STRING_ARRAY;
import static nl.naturalis.common.CollectionMethods.asUnsafeList;
import static nl.naturalis.common.ObjectMethods.isEmpty;
import static nl.naturalis.common.ObjectMethods.n2e;
import static nl.naturalis.common.check.CommonChecks.*;
import static nl.naturalis.yokete.accessors.BypassAccessor.BYPASS_ACCESSOR;
import static nl.naturalis.yokete.render.Accessor.UNDEFINED;
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

  private final Page page;
  private final RenderState state;

  RenderSession(Page page) {
    this.page = page;
    this.state = new RenderState(page);
  }

  /* METHODS FOR SETTING A SINGLE TEMPLATE VARIABLE */

  /**
   * Sets the specified variable to the specified value. Unless the variable was declared with an
   * inline {@link EscapeType} (e.g. {@code ~%html:fullName%}) no escaping will be applied to the
   * value.
   *
   * @param varName The name of the variable to set
   * @param value The value of the variable
   * @throws RenderException
   */
  public RenderSession set(String varName, Object value) throws RenderException {
    if (value == UNDEFINED) {
      /*
       * Unless the user is manually going through, and accessing the properties of some source data
       * object, specifying UNDEFINED misses the point of that constant, but since we can't know
       * this, we'll have to accept that value and process it as it is meant to be processed (namely:
       * not).
       */
      return this;
    }
    return set(varName, asUnsafeList(value), ESCAPE_NONE);
  }

  /**
   * Sets the specified variable to the specified value using the specified escape type. If the
   * variable was declared with an inline escape type that differs from the specified escape type,
   * the inline escape type will prevail. This allows you to declare variables inside HTML tags
   * (most likely the overwhelming majority) without an inline escape type, while only specifying an
   * inline escape type for variables inside <code>&lt;script&gt;</code> tags (namely "js"). This
   * makes your template easier to read and write.
   *
   * @param varName The name of the variable to set
   * @param value The value of the variable
   * @param escapeType The escape type to use if the variable has no inline escape type
   * @return This {@code RenderSession}
   * @throws RenderException
   */
  public RenderSession set(String varName, Object value, EscapeType escapeType)
      throws RenderException {
    if (value == UNDEFINED) {
      return this;
    }
    return set(varName, asUnsafeList(value), escapeType);
  }

  /**
   * Sets the specified variable to the concatenation of the values within the specified {@code
   * List}. Unless the variable was declared with an inline {@link EscapeType} no escaping will be
   * applied to the value. The values in the {@code List} are first stringified, then escaped, then
   * concatenated. If the {@code List} is empty, the variable will not be rendered at all (that is,
   * an empty string will be inserted at the location of the variable within the template).
   *
   * @param varName The name of the variable to set
   * @param values The string values to concatenate
   * @return This {@code RenderSession}
   * @throws RenderException
   */
  public RenderSession set(String varName, List<?> values) throws RenderException {
    return set(varName, values, ESCAPE_NONE);
  }

  /**
   * Sets the specified variable to the concatenation of the values within the specified {@code
   * List}. The values in the {@code List} are first stringified, then escaped, then concatenated.
   * If the {@code List} is empty, the variable will not be rendered at all (that is, an empty
   * string will be inserted at the location of the variable within the template).
   *
   * @param varName The name of the variable to set
   * @param values The string values to concatenate
   * @param escapeType The escape type to use if the variable did not declare an inline escape type
   * @return This {@code RenderSession}
   * @throws RenderException
   */
  public RenderSession set(String varName, List<?> values, EscapeType escapeType)
      throws RenderException {
    return set(varName, values, escapeType, null, null, null);
  }

  /**
   * Sets the specified variable to the concatenation of the values within the specified {@code
   * List}. Each value will be prefixed with the specified prefix, suffixed with the specified
   * suffix, and separated from the previous one by the specified separator. The values in the
   * {@code List} are first stringified, then escaped, then enriched with prefix, suffix and
   * separator, and then concatenated. If the {@code List} is empty, the variable will not be
   * rendered at all.
   *
   * @param varName The name of the variable to set
   * @param values The string values to concatenate
   * @param escapeType The escape type to use if the variable did not declare an inline escape type
   * @param prefix The prefix to use for each string
   * @param separator The suffix to use for each string
   * @param suffix The separator to use between the stringd
   * @return This {@code RenderSession}
   * @throws RenderException
   */
  public RenderSession set(
      String varName,
      List<?> values,
      EscapeType escapeType,
      String prefix,
      String separator,
      String suffix)
      throws RenderException {
    Check.on(frozenSession(), state.isFrozen()).is(no());
    Check.on(illegalValue("varName", varName), varName).is(notNull());
    Check.on(illegalValue("values", values), values).is(notNull());
    Check.on(illegalValue("escapeType", escapeType), escapeType).is(notNull());
    Check.on(badEscapeType(), escapeType).isNot(sameAs(), NOT_SPECIFIED);
    Template t = page.getTemplate();
    Check.on(noSuchVariable(t, varName), varName).is(in(), t.getVariables());
    Check.on(alreadySet(t, varName), state.isSet(varName)).is(no());
    IntList indices = page.getTemplate().getVarPartIndices().get(varName);
    if (values.isEmpty()) {
      indices.forEach(i -> state.setVar(i, EMPTY_STRING_ARRAY));
    } else {
      String[] strings = page.toString(varName, values);
      indices.forEach(i -> setVar(i, strings, escapeType, prefix, separator, suffix));
    }
    state.done(varName);
    return this;
  }

  private void setVar(
      int partIndex,
      String[] values,
      EscapeType escapeType,
      String prefix,
      String separator,
      String suffix) {
    List<Part> parts = page.getTemplate().getParts();
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
    for (int i = 0; i < values.length; ++i) {
      String s = values[i];
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
        values[i] = s;
      } else if (enrich) {
        if (i == 0) {
          s = prefix + s + suffix;
        } else {
          s = separator + prefix + s + suffix;
        }
        values[i] = s;
      }
    }
    state.setVar(partIndex, values);
  }

  /**
   * Sets the specified variable to the entire output of the specified {@code Renderable}. This
   * allows you to create and populate a template for an HTML snippet once, and then repeatedly (for
   * each render session of the current template) "paste" its output into the current template. See
   * {@link #createRenderable()}.
   *
   * @param varName The template variable to set
   * @param renderable The {@code Renderable}
   * @return This {@code RenderSession}
   * @throws RenderException
   */
  public RenderSession paste(String varName, Renderable renderable) throws RenderException {
    Check.on(frozenSession(), state.isFrozen()).is(no());
    Check.on(illegalValue("varName", varName), varName).is(notNull());
    Check.on(illegalValue("renderable", renderable), renderable).is(notNull());
    Template t = page.getTemplate();
    Check.on(noSuchVariable(t, varName), varName).is(in(), t.getVariables());
    Check.on(alreadySet(t, varName), state.isSet(varName)).is(no());
    IntList indices = page.getTemplate().getVarPartIndices().get(varName);
    indices.forEach(i -> state.setVar(i, renderable));
    return this;
  }

  /* METHODS FOR POPULATING A SINGLE NESTED TEMPLATE */

  /**
   * Populates a <i>nested</i> template. The template is populated with values retrieved from the
   * specified source data. Only variables and (doubly) nested templates whose name is present in
   * the {@code names} argument will be populated. No escaping will be applied to the values
   * retrieved from the data object. See {@link #populate(String, Object, EscapeType, String...)}.
   *
   * @param nestedTemplateName The name of the nested template
   * @param sourceData An object that provides data for all or some of the nested template's
   *     variables and nested templates
   * @param names The names of the variables and doubly-nested templates that you want to be
   *     populated using the specified data object
   */
  public RenderSession populate(String nestedTemplateName, Object sourceData, String... names)
      throws RenderException {
    return populate(nestedTemplateName, sourceData, ESCAPE_NONE, names);
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
   * <h4>Text-only templates</h4>
   *
   * <p>Although the {@code RenderSession} has a {@link #show(String, int) separate method} for
   * dealing with text-only templates, they can also be made to be rendered by this method.
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
  public RenderSession populate(
      String nestedTemplateName, Object sourceData, EscapeType escapeType, String... names)
      throws RenderException {
    Check.on(frozenSession(), state.isFrozen()).is(no());
    Check.on(illegalValue("escapeType", escapeType), escapeType).is(notNull());
    Check.on(badEscapeType(), escapeType).isNot(sameAs(), NOT_SPECIFIED);
    Template t = getNestedTemplate(nestedTemplateName);
    List<?> data = asUnsafeList(sourceData);
    if (t.isTextOnly()) {
      return show(t, data.size());
    }
    Check.on(missingSourceData(t), data).is(neverNull());
    return repeat(t, data, escapeType, names);
  }

  private RenderSession repeat(Template t, List<?> data, EscapeType escapeType, String... names)
      throws RenderException {
    RenderSession[] sessions = state.getOrCreateChildSessions(t, data);
    for (int i = 0; i < sessions.length; ++i) {
      sessions[i].insert(data.get(i), escapeType, names);
    }
    return this;
  }

  /**
   * Causes the specified nested text-only template to be rendered. Equivalent to {@link
   * #show(String, int) show(nestedTemplateName, 1)}.
   *
   * @param nestedTemplateName The name of the nested template. <i>Must</i> be a text-only template,
   *     otherwise a {@code RenderException} is thrown
   * @return This {@code RenderSession}
   * @throws RenderException
   */
  public RenderSession show(String nestedTemplateName) throws RenderException {
    return show(nestedTemplateName, 1);
  }

  /**
   * Causes the specified nested text-only template to be rendered. A text-only template is a
   * template that does not contain any variables or nested templates. One reason you might want to
   * have such a template is that you want to conditionally render it. Another reason could be that
   * you want to reduce clutter in your main template.
   *
   * <p>You could achieve the same by calling {@code populate(nestedTemplateName, null}. However,
   * the {@code show} method bypasses some code that is irrelevant to text-only templates. To
   * disable rendering, specify 0 (zero) for the {@code repeats} argument. Note, however, that by
   * default template variables and nested templates are not rendered, so you could also just not
   * call this method.
   *
   * @param nestedTemplateName The name of the nested template. <i>Must</i> be a text-only template,
   *     otherwise a {@code RenderException} is thrown
   * @param repeats The number of times you want the template to be repeated in the render result
   * @return This {@code RenderSession}
   * @throws RenderException
   */
  public RenderSession show(String nestedTemplateName, int repeats) throws RenderException {
    Check.on(frozenSession(), state.isFrozen()).is(no());
    Check.that(repeats, "repeats").is(gte(), 0);
    Template t = getNestedTemplate(nestedTemplateName);
    Check.on(notTextOnly(t), t.isTextOnly()).is(yes());
    return show(t, repeats);
  }

  /**
   * Causes all of the specified nested text-only templates to be rendered.
   *
   * @param nestedTemplateNames The names of the nested text-only templates you want to be rendered
   * @return This {@code RenderSession}
   * @throws RenderException
   */
  public RenderSession showAll(String... nestedTemplateNames) throws RenderException {
    Check.on(frozenSession(), state.isFrozen()).is(no());
    Check.that(nestedTemplateNames, "nestedTemplateNames").is(neverNull());
    for (String name : nestedTemplateNames) {
      Check.on(noSuchTemplate(page.getTemplate(), name), name).is(validTemplateName());
      Template t = page.getTemplate().getNestedTemplate(name);
      state.getOrCreateTextOnlyChildSessions(t, 1);
    }
    return this;
  }

  private RenderSession show(Template nested, int repeats) throws RenderException {
    state.getOrCreateTextOnlyChildSessions(nested, repeats);
    return this;
  }

  /**
   * Convenience method for populating a nested template that contains exactly one variable and zero
   * doubly-nested templates. See {@link #populate1(String, Object, EscapeType)}.
   *
   * @param nestedTemplateName The name of the nested template. <i>Must</i> contain exactly one
   *     variable
   * @return This {@code RenderSession}
   * @throws RenderException
   */
  public RenderSession populate1(String nestedTemplateName, Object value) throws RenderException {
    return populate1(nestedTemplateName, value, ESCAPE_NONE);
  }

  /**
   * Convenience method for populating a nested template that contains exactly one variable and zero
   * doubly-nested templates. The variable may still occur multiple times within the template
   * though. Contrary to the other {@code populate} methods the {@code value} argument really is the
   * value that is going to be assigned to the one variable within the nested template, rather than
   * a source data object from which values are going to be extracted by the session's {@link
   * Accessor}. This method bypasses the session's {@code Accessor} and uses a {@link SelfAccessor}
   * instead.
   *
   * @param nestedTemplateName The name of the nested template. <i>Must</i> contain exactly one
   *     variable
   * @param value The value to set the template's one and only variable to
   * @param escapeType The escape to use for the variable within the nested template. Will not
   *     override variable's inline escape type, if defined.
   * @return This {@code RenderSession}
   * @throws RenderException
   */
  public RenderSession populate1(String nestedTemplateName, Object value, EscapeType escapeType)
      throws RenderException {
    Check.on(frozenSession(), state.isFrozen()).is(no());
    Template t = getNestedTemplate(nestedTemplateName);
    Check.on(notMonoTemplate(t), t)
        .has(tmpl -> tmpl.getVariables().size(), eq(), 1)
        .has(tmpl -> tmpl.countNestedTemplates(), eq(), 0);
    List<?> values = asUnsafeList(value);
    RenderSession[] sessions = state.getOrCreateChildSessions(t, new SelfAccessor(), values.size());
    for (int i = 0; i < sessions.length; ++i) {
      sessions[i].insert(values.get(i), escapeType);
    }
    return this;
  }

  /**
   * Convenience method for populating a nested template that contains exactly two variables and
   * zero doubly-nested templates. See {@link #populate2(String, List, EscapeType)}.
   *
   * @param nestedTemplateName The name of the nested template. <i>Must</i> contain exactly two
   *     variables
   * @param tuples A list of value pairs
   * @return This {@code RenderSession}
   * @throws RenderException
   */
  public <T, U> RenderSession populate2(String nestedTemplateName, List<Tuple<T, U>> tuples)
      throws RenderException {
    return populate2(nestedTemplateName, tuples, ESCAPE_NONE);
  }

  /**
   * Convenience method for populating a nested template that contains exactly two variables. The
   * template must not containing any doubly-nested templates, but the variables may occur multiple
   * times within the template. The values in the specified {@link Tuple} instances <i>must</i> be
   * in the same order as the encounter order of the two variables within the template. This method
   * could be used, for example, to populate <code>&lt;select&gt;
   * </code> elements with <code>&lt;option&gt;</code> elements and their {@code value} attribute.
   *
   * @param nestedTemplateName The name of the nested template
   * @param tuples A list of value pairs
   * @param escapeType The escape to use for the variables within the nested template. Will not
   *     override the variables' inline escape types, if defined
   * @return This {@code RenderSession}
   * @throws RenderException
   */
  public <T, U> RenderSession populate2(
      String nestedTemplateName, List<Tuple<T, U>> tuples, EscapeType escapeType)
      throws RenderException {
    Check.on(frozenSession(), state.isFrozen()).is(no());
    Check.on(illegalValue("tuples", tuples), tuples).is(neverNull());
    Template t = getNestedTemplate(nestedTemplateName);
    Check.on(notTupleTemplate(t), t)
        .has(tmpl -> tmpl.getVariables().size(), eq(), 2)
        .has(tmpl -> tmpl.countNestedTemplates(), eq(), 0);
    String[] vars = t.getVariables().toArray(new String[2]);
    List<Map<String, Object>> data =
        tuples
            .stream()
            .map(tuple -> Map.of(vars[0], tuple.getLeft(), vars[1], tuple.getRight()))
            .collect(toList());
    RenderSession[] sessions =
        state.getOrCreateChildSessions(t, new MapAccessorInternal(), data.size());
    for (int i = 0; i < sessions.length; ++i) {
      sessions[i].insert(data.get(i), escapeType);
    }
    return this;
  }

  /* METHODS FOR POPULATING WHATEVER IS IN THE PROVIDED OBJECT */

  /**
   * Populates the <i>current</i> template (the template for which this {@code RenderSession} was
   * created). No escaping will be applied to the values extracted from the source data. See {@link
   * #add(Object, EscapeType, String...)}.
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
  public RenderSession insert(Object sourceData, String... names) throws RenderException {
    return insert(sourceData, ESCAPE_NONE, names);
  }

  /**
   * Populates the <i>current</i> template (the template for which this {@code RenderSession} was
   * created). The {@code RenderSession} will attempt to populate all variables and nested template
   * will using the provided source data except for the variables and/or nested templates whose name
   * is in the {@code names} array. This allows you to call this method multiple times with the same
   * source data, but with different escape types for different variables. Note, however, that the
   * source data object is explicitly not required to provide all values for all variables and
   * nested templates (see {@link Accessor#access(Object, String)}). This again allows you to call
   * this method multiple times with <i>different</i> source data, until the template is fully
   * populated.
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
  public RenderSession insert(Object sourceData, EscapeType escapeType, String... names)
      throws RenderException {
    Check.on(frozenSession(), state.isFrozen()).is(no());
    if (sourceData == null) {
      Template t = page.getTemplate();
      Check.on(notTextOnly(t), t.isTextOnly()).is(yes());
      // If we get past this check, the entire template is in fact
      // static HTML. Pretty expensive way to render static HTML,
      // but no reason not to support it.
      return this;
    }
    processVars(sourceData, escapeType, names);
    processTmpls(sourceData, escapeType, names);
    return this;
  }

  @SuppressWarnings("unchecked")
  private <T> void processVars(T data, EscapeType escapeType, String[] names)
      throws RenderException {
    Set<String> varNames;
    if (isEmpty(names)) {
      varNames = page.getTemplate().getVariables();
    } else {
      varNames = new HashSet<>(page.getTemplate().getVariables());
      varNames.retainAll(Set.of(names));
    }
    Accessor<T> acc = (Accessor<T>) page.getAccessor(data);
    for (String varName : varNames) {
      Object value = acc.access(data, varName);
      if (value != UNDEFINED) {
        set(varName, value, escapeType);
      }
    }
  }

  @SuppressWarnings("unchecked")
  private <T> void processTmpls(T data, EscapeType escapeType, String[] names)
      throws RenderException {
    Set<String> tmplNames;
    if (isEmpty(names)) {
      tmplNames = page.getTemplate().getNestedTemplateNames();
    } else {
      tmplNames = new HashSet<>(page.getTemplate().getNestedTemplateNames());
      tmplNames.retainAll(Set.of(names));
    }
    Accessor<T> acc = (Accessor<T>) page.getAccessor(data);
    for (String name : tmplNames) {
      Object nestedData = acc.access(data, name);
      if (nestedData != UNDEFINED) {
        populate(name, nestedData, escapeType, names);
      }
    }
  }

  /* MISCELLANEOUS METHODS */

  /**
   * Creates a render session for the specified nested template. See {@link
   * #createChildSession(String, Accessor)}. This method lets you concicely render a template that
   * is itself not text-only, but contains only templates that are:
   *
   * <p>
   *
   * <pre>
   * session.createChildSession("textOnlyContainer").show("textOnlyTemplate1").show("textOnlyTemplate2");
   * </pre>
   *
   * <p>This is substantially less verbose than:
   *
   * <pre>
   * session.populate(
   *  new MapWriter()
   *    .in("textOnlyContainer")
   *    .write("textOnlyTemplate1", null)
   *    .write("textOnlyTemplate2", null)
   *    .getMap());
   * </pre>
   *
   * <p>(See {@link nl.naturalis.common.path.MapWriter MapWriter}.)
   *
   * @param nestedTemplateName The nested template for which to create the child sessions
   * @return A child session that you can (and should) populate yourself
   * @throws RenderException
   */
  public RenderSession createChildSession(String nestedTemplateName) throws RenderException {
    return createChildSession(nestedTemplateName, BYPASS_ACCESSOR);
  }

  /**
   * Creates a render session for the specified nested template. See {@link
   * #createChildSessions(String, Accessor, int)}.
   *
   * @param nestedTemplateName The nested template for which to create the child sessions
   * @param accessor The {@code Accessor} implementation to be used to extract values from the
   *     source data for the template
   * @return A child session that you can (and should) populate yourself
   * @throws RenderException
   */
  public RenderSession createChildSession(String nestedTemplateName, Accessor<?> accessor)
      throws RenderException {
    Check.on(frozenSession(), state.isFrozen()).is(no());
    Template t = getNestedTemplate(nestedTemplateName);
    return state.createChildSessions(t, accessor, 1)[0];
  }

  /**
   * Creates render sessions for a repeating template within the parent template. Each of the
   * sessions will use a {@link BypassAccessor} to access any source data for the template
   * (effectively meaning you're own your own). See {@link #createChildSessions(String, Accessor,
   * int)}.
   *
   * @param nestedTemplateName The nested template for which to create the child sessions
   * @param repeats The number of times you want the template to repeat itself
   * @return A {@code List} of child sessions that you can (and should) populate yourself
   * @throws RenderException
   */
  public List<RenderSession> createChildSessions(String nestedTemplateName, int repeats)
      throws RenderException {
    return createChildSessions(nestedTemplateName, BYPASS_ACCESSOR, repeats);
  }

  /**
   * Creates render sessions for a repeating template within the parent template. Note that child
   * sessions are automatically and implicitly created when populating a template with a source data
   * object that reflects its structure. This method gives you more fine-grained control over the
   * rendering process, should you need it. Although you could, you should not in principle render
   * the child sessions yourself. They are still attached to the parent session and will be rendered
   * (again) when the parent session is rendered.
   *
   * @param nestedTemplateName The nested template for which to create the child sessions
   * @param accessor The {@code Accessor} implementation to be used to extract values from the
   *     source data for the template
   * @param repeats The number of times you want the template to repeat itself
   * @return A {@code List} of child sessions that you can (and should) populate yourself
   * @throws RenderException
   */
  public List<RenderSession> createChildSessions(
      String nestedTemplateName, Accessor<?> accessor, int repeats) throws RenderException {
    Check.on(frozenSession(), state.isFrozen()).is(no());
    Template t = getNestedTemplate(nestedTemplateName);
    return List.of(state.createChildSessions(t, accessor, repeats));
  }

  /**
   * Returns the child sessions that have been created for the specified nested template. This
   * method throws a {@code RenderException} if no child sessions have been created yet for the
   * specified nested template.
   *
   * @param nestedTemplateName The nested template
   * @return A {@code List} of child sessions
   * @throws RenderException
   */
  public List<RenderSession> getChildSessions(String nestedTemplateName) throws RenderException {
    Check.on(frozenSession(), state.isFrozen()).is(no());
    Template t = getNestedTemplate(nestedTemplateName);
    RenderSession[] sessions = state.getChildSessions(t);
    Check.on(noChildSessionsYet(t), t).is(notNull());
    return List.of(sessions);
  }

  /* RENDER METHODS */

  /**
   * Returns whether or not the template is fully populated. That is, all variables have been set
   * and all nested templates (however deeply nested) have been filled. Note that you may not even
   * <i>want</i> the template to be fully populated. Nested templates whose rendering depended on a
   * condition that turned out to evaluate to {@code false} will not be populated.
   *
   * @return Whether or not the template is fully populated
   */
  public boolean isFullyPopulated() {
    return state.isFullyPopulated();
  }

  /**
   * Returns a {@code Renderable} instance with which you render the current template. See {@link
   * #paste(String, Renderable)}.
   *
   * @return A {@code Renderable} instance with which you render the current template
   */
  public Renderable createRenderable() {
    state.freeze();
    return new Renderer(state);
  }

  /**
   * Writes the populated template to the specified {@code OutputStream}. Shortcut for {@code
   * createRenderable().render(out)}.
   *
   * @param out The output stream to which to write the render result
   * @throws RenderException
   */
  public void render(OutputStream out) {
    createRenderable().render(out);
  }

  /**
   * Appends the populated template to the specified {@code StringBuilder}. Shortcut for {@code
   * createRenderable().render(sb)}.
   *
   * @param sb The {@code StringBuilder} to which to append the render result
   * @throws RenderException
   */
  public void render(StringBuilder sb) {
    createRenderable().render(sb);
  }

  public String render() {
    UnsafeByteArrayOutputStream out = new UnsafeByteArrayOutputStream(1024);
    createRenderable().render(out);
    return new String(out.toByteArray(), 0, out.byteCount(), StandardCharsets.UTF_8);
  }

  @Override
  public String toString() {
    String fqn = TemplateUtils.getFQName(page.getTemplate());
    String clazz0 = getClass().getSimpleName();
    int hash = System.identityHashCode(this);
    return clazz0 + "[" + fqn + "]@" + hash;
  }

  RenderState getState() {
    return state;
  }

  private Template getNestedTemplate(String name) throws RenderException {
    Check.notNull(name, "nestedTemplateName");
    Check.on(noSuchTemplate(page.getTemplate(), name), name).is(validTemplateName());
    return page.getTemplate().getNestedTemplate(name);
  }

  private Predicate<String> validTemplateName() {
    return s -> page.getTemplate().getNestedTemplateNames().contains(s);
  }
}
