package nl.naturalis.yokete.render;

import java.io.OutputStream;
import java.util.*;
import nl.naturalis.common.check.Check;
import nl.naturalis.common.collection.IntList;
import nl.naturalis.common.collection.TypeMap;
import nl.naturalis.common.collection.UnmodifiableTypeMap;
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
 * A {@code RenderSession} is responsible for populating a template and rendering it. A template can
 * only be rendered once all its variables, and all variables in the templates descending from it,
 * have been set. Populating the template can be done in multiple passes. Once a template is fully
 * populated, the {@code RenderSession} effectively becomes immutable. You can render it as often as
 * you like, but you cannot overwrite its variables.
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

  private static final Object whatever = new Object();

  /*
   * Defines types that are definitely not suitable as data for a template, because they don't have
   * a meaningful, accessible internal structure. There are of course plenty of types that are
   * (very) unlikely candidates as data sources for a template, but these ones we catch out
   * explicitly, as they are likely to be accidentally used by clients. NB we don't care about the
   * values in the TypeMap, only about the keys. We should ideally also have a {@code TypeSet}
   * interface in naturalis-common.
   */
  private static final TypeMap<?> BAD_DATA =
      UnmodifiableTypeMap.build()
          .add(Number.class, whatever)
          .add(CharSequence.class, whatever)
          .add(Object[].class, whatever)
          .add(Collection.class, whatever)
          .freeze();

  private final SessionFactory factory;
  private final RenderState state;

  RenderSession(SessionFactory rsf) {
    this.factory = rsf;
    this.state = new RenderState(rsf);
  }

  /* METHODS FOR SETTING A SINGLE TEMPLATE VARIABLE */

  /**
   * Sets a single variable within the template to the specified value. Unless the variable was
   * declared with an inline {@link EscapeType} (e.g. {@code ~%html:fullName%}) no escaping will be
   * applied to the value.
   *
   * @param name The name of the variable to set
   * @param value The value of the variable
   * @throws RenderException
   */
  public RenderSession setVariable(String name, Object value) throws RenderException {
    return setVariable(name, asList(value), ESCAPE_NONE);
  }

  /**
   * Sets a single variable within the template to the specified value using the specified escape
   * type. If the variable was declared with an inline escape type that differs from the specified
   * escape type, the variable will still be set, but escaped using the inline escape type. This
   * allows you to declare all variables inside HTML tags (most likely the overwhelming majority)
   * without an inline escape type, while only specifying an inline escape type for variables inside
   * <code>&lt;script&gt;</code> tags (namely "js"). This makes your template easier to write and
   * less cluttered.
   *
   * @param name The name of the variable to set
   * @param value The value of the variable
   * @param escapeType The escape type to use if the variable did not declare an inline escape type
   * @return This {@code RenderSession}
   * @throws RenderException
   */
  public RenderSession setVariable(String name, Object value, EscapeType escapeType)
      throws RenderException {
    return setVariable(name, asList(value), escapeType);
  }

  /**
   * Sets the specified variable to the concatenation of the strings within the specified {@code
   * List}. If the {@code List} is empty, the variable will not be rendered at all. No escaping is
   * applied to the strings in the {@code List}.
   *
   * @param name The name of the variable to set
   * @param value The string values to concatenate
   * @return This {@code RenderSession}
   * @throws RenderException
   */
  public RenderSession setVariable(String name, List<?> value) throws RenderException {
    return setVariable(name, value, ESCAPE_NONE);
  }

  /**
   * Sets the specified variable to the concatenation of the strings within the specified {@code
   * List}. If the {@code List} is empty, the variable will not be rendered at all.
   *
   * @param name The name of the variable to set
   * @param value The string values to concatenate
   * @param escapeType The escape type to use if the variable did not declare an inline escape type
   * @return This {@code RenderSession}
   * @throws RenderException
   */
  public RenderSession setVariable(String name, List<?> value, EscapeType escapeType)
      throws RenderException {
    return setVariable(name, value, escapeType, null, null, null);
  }

  /**
   * Sets the specified variable to the concatenation of the strings within the specified {@code
   * List}. Each string will be prefixed with the specified prefix, suffixed with the specified
   * suffix, and separated by the specified separator. If the {@code List} is empty, the variable
   * will not be rendered at all.
   *
   * @param name The name of the variable to set
   * @param value The string values to concatenate
   * @param escapeType The escape type to use if the variable did not declare an inline escape type
   * @param prefix The prefix to use for each string
   * @param separator The suffix to use for each string
   * @param suffix The separator to use between the stringd
   * @return This {@code RenderSession}
   * @throws RenderException
   */
  public RenderSession setVariable(
      String name,
      List<?> value,
      EscapeType escapeType,
      String prefix,
      String separator,
      String suffix)
      throws RenderException {
    Check.notNull(name, "name");
    Check.that(value, "value").is(noneNull());
    Check.notNull(escapeType, "escapeType");
    Template t = factory.getTemplate();
    Check.on(alreadySet(t, name), state.isSet(name)).is(no());
    Check.on(noSuchVariable(t, name), name).is(in(), t.getVars());
    Check.on(badEscapeType(), escapeType).is(notSameAs(), NOT_SPECIFIED);
    IntList indices = factory.getTemplate().getVarPartIndices().get(name);
    if (value.isEmpty()) {
      indices.forEach(i -> state.setVar(i, EMPTY_STRING_ARRAY));
    } else {
      String[] strings = factory.toString(name, value);
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
   * Suppresses the rendering of the specified variable or nested template. The same can be achieved
   * using {@code setVariable(name, Collections.emptyList())} resp. {@code populate(name,
   * Collections.emptyList())}. Note that "just not mentioning" the variable or nested template
   * while in a render session does not have the effect of it not being rendered. Instead, if you
   * call one of the {@code renderSafe} methods, you will get a {@code RenderException}, and if you
   * call one of the {@code render} methods, you will will see the raw variable c.q. template in the
   * output.
   *
   * @param name The name of a variable or nested template
   * @return This {@code RenderSession}
   * @throws RenderException
   */
  public RenderSession dontRender(String name) throws RenderException {
    if (factory.getTemplate().hasVar(name)) {
      setVariable(name, Collections.emptyList());
    } else if (factory.getTemplate().hasNestedTemplate(name)) {
      populate(name, Collections.emptyList());
    } else {
      Check.failOn(invalidName(name));
    }
    return this;
  }

  /**
   * Suppresses the rendering of all variables that have not been set yet, and of all nested
   * templates that have not been populated yet. Nested templates that are partially populated will
   * be rendered, but the variables with them that have not been set yet will not be rendered.
   *
   * @return This {@code RenderSession}
   */
  public RenderSession skipRest() {
    skipRest(this);
    return this;
  }

  private static void skipRest(RenderSession s0) {
    try {
      for (String var : s0.state.getUnsetVars()) {
        s0.setVariable(var, Collections.emptyList());
      }
    } catch (RenderException e) {
      // won't happen because we know we're dealing with valid variable names
    }
    Set<Template> busy = s0.state.getChildSessions().keySet();
    Set<Template> todo = new HashSet<>(s0.factory.getTemplate().getNestedTemplates());
    todo.removeAll(busy);
    try {
      for (Template t : todo) {
        s0.populate(t.getName(), Collections.emptyList());
      }
    } catch (RenderException e) {
    }
    for (Template t : busy) {
      s0.state.getChildSessions(t).forEach(s -> skipRest(s));
    }
  }

  /**
   * Populates the <i>nested</i> template with the specified name with values retrieved from the
   * specified data object. Only variables and (doubly) nested templates whose name is present in
   * the {@code names} argument will be populated. No escaping will be applied to the values
   * retrieved from the data object.
   *
   * @param nestedTemplateName The name of the nested template
   * @param data An object that provides data for all or some of the nested template's variables and
   *     nested templates
   * @param names The names of the variables and doubly-nested templates that you want to be
   *     populated using the specified data object
   */
  public RenderSession populate(String nestedTemplateName, Object data, String... names)
      throws RenderException {
    return populate(nestedTemplateName, data, ESCAPE_NONE, names);
  }

  /**
   * Populates the <i>nested</i> template with the specified name with values retrieved from the
   * specified data object. Only variables and (doubly) nested templates whose name is present in
   * the {@code names} argument will be populated.
   *
   * <h4>Repeating Templates</h4>
   *
   * <p>If the specified object is an array or a {@code Collection}, the template will be repeated
   * for each object in the array or {@code Collection}. This can be used, for example, to generate
   * an HTML table from a template that contains just a single row.
   *
   * <h4>Conditional Rendering</h4>
   *
   * <p>If the specified object is an <i>empty</i> array or a {@code Collection}, the template will
   * not be rendered at all. This allows for conditional rendering (i.e. render the template only if
   * certain conditions are met).
   *
   * <h4>Text-only templates</h4>
   *
   * <p>In rare cases you might want to define a text-only nested template, i.e. a nested template
   * that does not contain any variables or (doubly) nested templates. One reason could be that you
   * want to conditionally render it. For text-only templates the {@code data} argument can be
   * anything you like, including {@code null}. If you want a text-only template to be repeated,
   * specify something like {@code new Object[7]}. If you don't want it to be rendered at all,
   * specify {@code new Object[0]} or an empty list.
   *
   * @param nestedTemplateName The name of the nested template
   * @param data An object that provides data for all or some of the nested template's variables and
   *     nested templates
   * @param escapeType The escape to use for the variables within the nested template
   * @param names The names of the variables and doubly-nested templates that you want to be
   *     populated using the specified data object
   * @return This {@code RenderSession}
   * @throws RenderException
   */
  public RenderSession populate(
      String nestedTemplateName, Object data, EscapeType escapeType, String... names)
      throws RenderException {
    return repeat(nestedTemplateName, asList(data), escapeType, names);
  }

  /**
   * Convenience method for populating a nested template that contains exactly one variable.
   * Ordinarily nested templates are populated with a complex {code Object} and an {@link Accessor}
   * that retrieves variable values from it. With this method, however, you specify the variable's
   * value directly. The value can still be an array or {@code Collection}, causing the template to
   * be repeat itself. See {@link #populate(String, Object, EscapeType, String...)}.
   *
   * @param nestedTemplateName The name of the nested template, which must contain exactly one
   *     variable
   * @param escapeType The escape to use for the variable within the nested template
   * @return This {@code RenderSession}
   * @throws RenderException
   */
  public RenderSession populateMono(String nestedTemplateName, Object value, EscapeType escapeType)
      throws RenderException {
    String name = nestedTemplateName;
    Check.notNull(name, "nestedTemplateName");
    Check.notNull(value, "value");
    Check.notNull(escapeType, "escapeType");
    Check.on(noSuchTemplate(name), name).is(in(), factory.getTemplate().getNestedTemplateNames());
    Check.on(badEscapeType(), escapeType).is(notSameAs(), NOT_SPECIFIED);
    Template t = factory.getTemplate().getNestedTemplate(name);
    Check.on(noMonoTemlate(t), t).has(Template::countVars, eq(), 1);
    List<?> values = asList(value);
    String monoVar = t.getVars().iterator().next();
    List<RenderSession> sessions = state.createChildSessions(t, values.size());
    for (int i = 0; i < values.size(); ++i) {
      String escaped = factory.getStringifier().stringify(t, monoVar, values.get(i));
      sessions.get(i).setVariable(monoVar, escaped, ESCAPE_NONE);
    }
    return this;
  }

  private RenderSession repeat(String name, List<?> data, EscapeType escapeType, String... names)
      throws RenderException {
    Check.notNull(name, "nestedTemplateName");
    Check.that(data, "data").is(noneNull());
    Check.notNull(escapeType, "escapeType");
    Check.on(noSuchTemplate(name), name).is(in(), factory.getTemplate().getNestedTemplateNames());
    Check.on(badEscapeType(), escapeType).is(notSameAs(), NOT_SPECIFIED);
    List<RenderSession> sessions = state.createChildSessions(name, data.size());
    for (int i = 0; i < sessions.size(); ++i) {
      sessions.get(i).fillWith(data.get(i), escapeType, names);
    }
    return this;
  }

  /* METHODS FOR POPULATING WHATEVER IS IN THE PROVIDED ViewData OBJECT */

  /**
   * Populates all variables and nested templates whose name is present in the {@code names}
   * argument with values retrieved from the specified data object. No escaping will be applied to
   * the values retrieved from the data object. See {@link #fillWith(Object, EscapeType,
   * String...)}.
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
  public RenderSession fillWith(Object data, String... names) throws RenderException {
    return fillWith(data, ESCAPE_NONE, names);
  }

  /**
   * Populates all variables and nested templates whose name is present in the {@code names}
   * argument with values retrieved from the specified data object. This allows you to call this
   * method multiple times with the same data object, but with different escape types for different
   * variables.
   *
   * <p>If the {@code names} argument is {@code null} or empty, this method will attempt to populate
   * <i>all</i> all variables and nested templates using the specified data object. Note, however,
   * that the data object is itself explicitly not required to provide all values for all variables
   * and nested templates (see {@link Accessor#access(Object, String)}). This in turn enables you
   * call this method multiple times with different data objects, until the template is fully
   * populated.
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
  public RenderSession fillWith(Object data, EscapeType escapeType, String... names)
      throws RenderException {
    if (data == null) {
      Template t = factory.getTemplate();
      Check.on(nullData(t), t.getNames()).has(size(), eq(), 0);
    }
    Check.on(badData(data), BAD_DATA).is(notContainingKey(), data.getClass());
    processVars(data, escapeType, names);
    processTmpls(data, escapeType, names);
    return this;
  }

  /**
   * Verifies that the template is fully populated. Once your application becomes production-ready,
   * you should either call one of the {@code renderSafe} methods, or make any call to a {@code
   * Render} dependent on whether {@code isRenderable()} returns {@code true}.
   *
   * @return Whether or not the template is fully populated
   */
  public boolean isRenderable() {
    return state.isRenderable();
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
      setVariable(varName, value, escapeType);
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
      populate(name, nestedData, escapeType, names);
    }
  }

  /* RENDER METHODS */

  /**
   * Writes the render result to the specified output stream. If the template is not fully
   * populated, you will see raw variable declations in the output.
   *
   * @param out The output stream to which to write the render result
   */
  public void render(OutputStream out) {
    Check.notNull(out);
    Renderer renderer = new Renderer(state);
    renderer.render(out);
  }

  /**
   * Appends the render result to the specified {@code StringBuilder}. If the template is not fully
   * populated, you will see raw variable declations in the output.
   *
   * @param sb A {@code StringBuilder} to which to append the render result
   */
  public void render(StringBuilder sb) {
    Check.notNull(sb);
    Renderer renderer = new Renderer(state);
    renderer.render(sb);
  }

  /**
   * Returns a {@code StringBuilder} containing the render result. If the template is not fully
   * populated, you will see raw variable declations in the output.
   *
   * @return A {@code StringBuilder} containing the render result
   */
  public StringBuilder render() {
    Renderer renderer = new Renderer(state);
    return renderer.render();
  }

  /**
   * Writes the render result to the specified output stream. If the template is not fully
   * populated, a {@code RenderException} is thrown and the output stream is left untouched.
   *
   * @param out The output stream to which to write the render result
   */
  public void renderSafe(OutputStream out) throws RenderException {
    Check.notNull(out);
    Check.on(notRenderable(state.getUnsetVars()), isRenderable()).is(yes());
    Renderer renderer = new Renderer(state);
    renderer.render(out);
  }

  /**
   * Appends the render result to the specified {@code StringBuilder}. If the template is not fully
   * populated, a {@code RenderException} is thrown and the {@code StringBuilder} is left untouched.
   *
   * @param sb A {@code StringBuilder} to which to append the render result
   */
  public void renderSafe(StringBuilder sb) throws RenderException {
    Check.notNull(sb);
    Check.on(notRenderable(state.getUnsetVars()), isRenderable()).is(yes());
    Renderer renderer = new Renderer(state);
    renderer.render(sb);
  }

  /**
   * Returns a {@code StringBuilder} containing the render result. If the template is not fully
   * populated, a {@code RenderException} is thrown.
   *
   * @return A {@code StringBuilder} containing the render result
   */
  public StringBuilder renderSafe() throws RenderException {
    Check.on(notRenderable(state.getUnsetVars()), isRenderable()).is(yes());
    Renderer renderer = new Renderer(state);
    return renderer.render();
  }

  RenderState getState() {
    return state;
  }
}
