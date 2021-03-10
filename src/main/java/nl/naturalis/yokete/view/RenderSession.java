package nl.naturalis.yokete.view;

import java.io.PrintStream;
import java.util.*;
import nl.naturalis.common.CollectionMethods;
import nl.naturalis.common.check.Check;
import nl.naturalis.yokete.template.Part;
import nl.naturalis.yokete.template.Template;
import nl.naturalis.yokete.template.VariablePart;
import static nl.naturalis.common.CollectionMethods.asList;
import static nl.naturalis.common.ObjectMethods.ifEmpty;
import static nl.naturalis.common.ObjectMethods.isEmpty;
import static nl.naturalis.common.check.CommonChecks.*;
import static nl.naturalis.yokete.view.EscapeType.ESCAPE_NONE;
import static nl.naturalis.yokete.view.EscapeType.NOT_SPECIFIED;
import static nl.naturalis.yokete.view.RenderException.*;

public class RenderSession {

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
   * @return
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
   * specified object. If the specified object is an array or a {@code Collection}, the template
   * will be repeated for each object in the array or {@code Collection}. This can be used, for
   * example, to generate an HTML table from a template that contains just a single row. If the
   * specified object is an empty array or a {@code Collection}, the template will not be rendered
   * at all. This allows for conditional rendering of templates.
   *
   * @param name
   * @param data
   * @return
   * @throws RenderException
   */
  public RenderSession populate(String name, Object data) throws RenderException {
    return repeat(name, asList(data), ESCAPE_NONE, null);
  }

  /**
   * Populates the <i>nested</i> template with the specified name with values retrieved from the
   * specified object. Only variables and (doubly) nested templates whose name is present in the
   * {@code names} argument will be populated.
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
   * not be rendered at all. This allows you to render the template only if certain conditions are
   * met.
   *
   * <h4>Templates Without Variables</h4>
   *
   * <p>In rare cases you might have a nested template that does not contain any variables. In this
   * case the {@code data} argument can be anything you like, including {@code null}. If you want
   * the template to be repeated, specify something like {@code new Object[7]}. If you don't want it
   * to be rendered at all, specify an empty {@code List} or {@code new Object[0]}.
   *
   * @param name
   * @param data
   * @param escapeType
   * @param names
   * @return
   * @throws RenderException
   */
  public RenderSession populate(String name, Object data, EscapeType escapeType, Set<String> names)
      throws RenderException {
    return repeat(name, asList(data), escapeType, names);
  }

  private RenderSession repeat(String name, List<?> data, EscapeType escapeType, Set<String> names)
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
    names = ifEmpty(names, nestedTemplate::getNames);
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
   * @throws RenderException
   */
  public void dontRender(String name) throws RenderException {
    Check.on(invalidName(name), name).is(in(), ru.getTemplate().getNames());
    if (ru.getTemplate().containsVariable(name)) {
      setVariable(name, Collections.emptyList(), ESCAPE_NONE);
    } else {
      populate(name, Collections.emptyList());
    }
  }

  /* METHODS FOR POPULATING WHATEVER IS IN THE PROVIDED ViewData OBJECT */

  public RenderSession setData(Object data) throws RenderException {
    return setData(data, ESCAPE_NONE, null);
  }

  /**
   * Populates all variables and nested templates for which a value c.q. nested object can be found
   * in the specified object.
   *
   * @param data
   * @param escapeType
   * @return
   * @throws RenderException
   */
  public RenderSession setData(Object data, EscapeType escapeType) throws RenderException {
    return setData(data, escapeType, null);
  }

  /**
   * Populatesvariables and nested templates for which a value c.q. nested object can be found in
   * the specified object. Only variables and nested templates whose name is present in the {@code
   * names} argument will be processed. This allows you to call this method multiple times with the
   * same {@code ViewData} object but different escape types. The {@code ViewData} object is itself
   * not required to provide all values for all variables and nested templates. You can call this
   * method multiple times with different {@code ViewData} objects, until all variables and nested
   * templates are populated.
   *
   * @param data A {@code ViewData} instance that provides data for all or some of the template
   *     variables and nested templates.
   * @param escapeType The escape type to use
   * @param names The names of the variable <b>and/or</b> nested templates names that must be
   *     processed
   * @throws RenderException
   */
  public RenderSession setData(Object data, EscapeType escapeType, Set<String> names)
      throws RenderException {
    processVars(data, escapeType, names);
    processTmpls(data, escapeType, names);
    return this;
  }

  private void processVars(Object data, EscapeType escapeType, Set<String> names)
      throws RenderException {
    Set<String> varNames;
    if (isEmpty(names) || names.equals(ru.getTemplate().getNames())) {
      varNames = ru.getTemplate().getVariableNames();
    } else {
      varNames = new HashSet<>(ru.getTemplate().getVariableNames());
      varNames.retainAll(names);
    }
    String tmplName = ru.getTemplate().getName();
    for (String varName : varNames) {
      Object val = ru.getAccessor().getValue(data, varName);
      List<?> vals = CollectionMethods.asList(val);
      List<String> strvals = new ArrayList<>(vals.size());
      for (Object v : vals) {
        String s = ru.getStringifier().stringify(tmplName, varName, v);
        strvals.add(s);
      }
      setVariable(varName, strvals, escapeType);
    }
  }

  private void processTmpls(Object data, EscapeType escapeType, Set<String> names)
      throws RenderException {
    Set<String> tmplNames;
    if (isEmpty(names) || names.equals(ru.getTemplate().getNames())) {
      tmplNames = ru.getTemplate().getNestedTemplateNames();
    } else {
      tmplNames = new HashSet<>(ru.getTemplate().getNestedTemplateNames());
      tmplNames.retainAll(names);
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
