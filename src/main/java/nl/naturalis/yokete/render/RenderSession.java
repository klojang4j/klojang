package nl.naturalis.yokete.render;

import java.io.PrintStream;
import java.util.*;
import nl.naturalis.common.CollectionMethods;
import nl.naturalis.common.check.Check;
import nl.naturalis.common.collection.TypeMap;
import nl.naturalis.common.collection.UnmodifiableTypeMap;
import nl.naturalis.yokete.template.Part;
import nl.naturalis.yokete.template.Template;
import nl.naturalis.yokete.template.VariablePart;
import static nl.naturalis.common.CollectionMethods.asList;
import static nl.naturalis.common.ObjectMethods.isEmpty;
import static nl.naturalis.common.check.CommonChecks.*;
import static nl.naturalis.common.check.CommonGetters.size;
import static nl.naturalis.yokete.render.EscapeType.ESCAPE_NONE;
import static nl.naturalis.yokete.render.EscapeType.NOT_SPECIFIED;
import static nl.naturalis.yokete.render.RenderException.*;

public class RenderSession {

  private static final Object whatever = new Object();
  /**
   * Defines some types that are definitely not suitable as data for a template, because they don't
   * have a meaningful, accessible internal structure. There are, of course, a lot of types that are
   * (very) unlikely candidates as data sources for a template, but these ones we catch out
   * explicitly. NB we don't care about the values, only about the keys. We should ideally also have
   * a {@code TypeSet} interface in naturalis-common
   */
  private static final TypeMap<?> BAD_DATA =
      UnmodifiableTypeMap.build()
          .add(Number.class, whatever)
          .add(CharSequence.class, whatever)
          .add(Object[].class, whatever)
          .add(Collection.class, whatever)
          .freeze();

  final RenderUnit ru;
  final RenderState state;

  RenderSession(RenderUnit renderUnit) {
    this.ru = renderUnit;
    this.state = new RenderState(renderUnit);
  }

  /* METHODS FOR SETTING A SINGLE TEMPLATE VARIABLE */

  /**
   * Sets a single variable within the template to the specified value. Unless the variable was
   * declared with an inline {@link EscapeType} (e.g. {@code ~%html:fullName%}) no escaping will be
   * applied to the value.
   *
   * @param name
   * @param value
   * @throws RenderException
   */
  public RenderSession setVariable(String name, String value) throws RenderException {
    return setVariable(name, List.of(value), ESCAPE_NONE);
  }

  /**
   * Sets a single variable within the template to the specified value using the specified escape
   * type. The specified escape type will not override the inline escape type of a variable (e.g.
   * {@code ~%html:fullName%}), but it will be used if the variable was declared without an inline
   * escape type ({@code ~%fullName%}).
   *
   * @param name The name of the variable to set
   * @param value The value of the variable
   * @param escapeType The escape type to use when rendering the variable
   * @return This {@code RenderSession}
   * @throws RenderException
   */
  public RenderSession setVariable(String name, String value, EscapeType escapeType)
      throws RenderException {
    return setVariable(name, List.of(value), escapeType);
  }

  /**
   * Sets the specified variable to the concatenation of the strings within the specified {@code
   * List}. If the {@code List} is empty, the variable will not be rendered at all. This is
   * analogous to how to {@code populate} methods work, although it is arguably more useful there.
   *
   * @param name The name of the variable to set
   * @param value The string values to concatenate
   * @param escapeType The escape type to use when rendering the variable
   * @return This {@code RenderSession}
   * @throws RenderException
   */
  public RenderSession setVariable(String name, List<String> value, EscapeType escapeType)
      throws RenderException {
    Check.notNull(name, "name");
    Check.that(value, "value").is(noneNull());
    Check.notNull(escapeType, "escapeType");
    Check.on(alreadySet(name), state.isSet(name)).is(no());
    Check.on(noSuchVariable(name), name).is(in(), ru.getTemplate().getVariableNames());
    Check.on(badEscapeType(), escapeType).is(notSameAs(), NOT_SPECIFIED);
    ru.getTemplate().getVarPartIndices().get(name).forEach(i -> escape(i, value, escapeType));
    state.done(name);
    return this;
  }

  private void escape(int partIndex, List<String> val, EscapeType escapeType) {
    List<Part> parts = ru.getTemplate().getParts();
    VariablePart part = (VariablePart) parts.get(partIndex);
    EscapeType myEscType = part.getEscapeType();
    if (myEscType == NOT_SPECIFIED) {
      myEscType = escapeType;
    }
    List<String> escaped = new ArrayList<>(val.size());
    val.stream().map(myEscType::apply).forEach(escaped::add);
    state.setVar(partIndex, escaped);
  }

  /* METHODS FOR POPULATING A SINGLE NESTED TEMPLATE */

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

  private RenderSession repeat(String name, List<?> data, EscapeType escapeType, String... names)
      throws RenderException {
    Check.notNull(name, "name");
    Check.that(data, "data").is(noneNull());
    Check.notNull(escapeType, "escapeType");
    Check.on(alreadyPopulated(name), state.isPopulated(name)).is(no());
    Check.on(noSuchTemplate(name), name).is(in(), ru.getTemplate().getNestedTemplateNames());
    Check.on(badEscapeType(), escapeType).is(notSameAs(), NOT_SPECIFIED);
    Template nestedTemplate = ru.getTemplate().getNestedTemplate(name);
    RenderUnit ru1 =
        new RenderUnit(
            nestedTemplate,
            ru.getAccessor().getAccessorForNestedTemplate(name),
            ru.getStringifier());
    List<RenderSession> session = state.getOrCreateNestedSessions(ru1, data.size());
    for (int i = 0; i < session.size(); ++i) {
      session.get(i).setData(data.get(i), escapeType, names);
    }
    state.populated(name);
    return this;
  }

  /**
   * Shortcut for specifying that you don't want the specified variable or nested template to be
   * rendered.
   *
   * @param name The name of a variable or nested template
   * @return This {@code RenderSession}
   * @throws RenderException
   */
  public RenderSession dontRender(String name) throws RenderException {
    Check.on(invalidName(name), name).is(in(), ru.getTemplate().getNames());
    if (ru.getTemplate().containsVariable(name)) {
      setVariable(name, Collections.emptyList(), ESCAPE_NONE);
    } else if (ru.getTemplate().containsNestedTemplate(name)) {
      populate(name, Collections.emptyList());
    } else {
      Check.fail(invalidName(name));
    }
    return this;
  }

  /* METHODS FOR POPULATING WHATEVER IS IN THE PROVIDED ViewData OBJECT */

  /**
   * Populates all variables and nested templates whose name is present in the {@code names}
   * argument with values retrieved from the specified data object. No escaping will be applied to
   * the values retrieved from the data object. Not specifying any name (or {@code null}) indicates
   * that you want all variables and nested templates to be populated.
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
  public RenderSession setData(Object data, String... names) throws RenderException {
    return setData(data, ESCAPE_NONE, names);
  }

  /**
   * Populates all variables and nested templates whose name is present in the {@code names}
   * argument with values retrieved from the specified data object. This allows you to call this
   * method multiple times with the same data object but different escape types for different
   * variables. (The {@code escapeType} argument is ignored for nested templates.)
   *
   * <p>The data object is itself not required to provide all values for all variables and nested
   * templates. You can call this method multiple times with different data objects, the template is
   * fully populated.
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
  public RenderSession setData(Object data, EscapeType escapeType, String... names)
      throws RenderException {
    if (data == null) {
      Template t = ru.getTemplate();
      Check.on(nullData(t), t.getNames()).has(size(), eq(), 0);
    }
    Check.on(badData(data), BAD_DATA).is(notContainingKey(), data.getClass());
    processVars(data, escapeType, names);
    processTmpls(data, escapeType, names);
    return this;
  }

  private void processVars(Object data, EscapeType escapeType, String[] names)
      throws RenderException {
    Set<String> varNames;
    if (isEmpty(names)) {
      varNames = ru.getTemplate().getVariableNames();
    } else {
      varNames = new HashSet<>(ru.getTemplate().getVariableNames());
      varNames.retainAll(Set.of(names));
    }
    for (String varName : varNames) {
      Object val = ru.getAccessor().getValue(data, varName);
      List<?> vals = CollectionMethods.asList(val);
      List<String> strvals = new ArrayList<>(vals.size());
      for (Object v : vals) {
        String s = ru.getStringifier().stringify(ru.getTemplate(), varName, v);
        strvals.add(s);
      }
      setVariable(varName, strvals, escapeType);
    }
  }

  private void processTmpls(Object data, EscapeType escapeType, String[] names)
      throws RenderException {
    Set<String> tmplNames;
    if (isEmpty(names)) {
      tmplNames = ru.getTemplate().getNestedTemplateNames();
    } else {
      tmplNames = new HashSet<>(ru.getTemplate().getNestedTemplateNames());
      tmplNames.retainAll(Set.of(names));
    }
    for (String name : tmplNames) {
      Object nestedData = ru.getAccessor().getValue(data, name);
      populate(name, nestedData, escapeType, names);
    }
  }

  /* RENDER METHODS */

  void render(PrintStream ps) throws RenderException {
    Check.notNull(ps);
    if (!state.isRenderable()) {
      throw notRenderable(state.getUnsetVariables(), state.getUnpopulatedTemplates());
    }
    Renderer renderer = new Renderer(state);
    renderer.render(ps);
  }

  void render(StringBuilder sb) throws RenderException {
    Check.notNull(sb);
    if (!state.isRenderable()) {
      throw notRenderable(state.getUnsetVariables(), state.getUnpopulatedTemplates());
    }
    Renderer renderer = new Renderer(state);
    renderer.render(sb);
  }

  StringBuilder render() throws RenderException {
    if (!state.isRenderable()) {
      throw notRenderable(state.getUnsetVariables(), state.getUnpopulatedTemplates());
    }
    Renderer renderer = new Renderer(state);
    return renderer.render();
  }
}
