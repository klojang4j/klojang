package nl.naturalis.yokete.view;

import java.io.PrintStream;
import java.util.*;
import nl.naturalis.common.check.Check;
import nl.naturalis.yokete.template.Part;
import nl.naturalis.yokete.template.Template;
import nl.naturalis.yokete.template.VariablePart;
import static nl.naturalis.common.ObjectMethods.ifEmpty;
import static nl.naturalis.common.ObjectMethods.isEmpty;
import static nl.naturalis.common.check.CommonChecks.in;
import static nl.naturalis.common.check.CommonChecks.no;
import static nl.naturalis.common.check.CommonChecks.noneNull;
import static nl.naturalis.common.check.CommonChecks.*;
import static nl.naturalis.yokete.view.EscapeType.ESCAPE_NONE;
import static nl.naturalis.yokete.view.EscapeType.NOT_SPECIFIED;
import static nl.naturalis.yokete.view.RenderException.*;

public class RenderSession {

  final Template template;
  final RenderState state;

  RenderSession(Template template) {
    this.template = template;
    this.state = new RenderState(template);
  }

  /* METHODS FOR SETTING A SINGLE TEMPLATE VARIABLE */

  /**
   * Sets a single variable within the tmpl to the specified value.
   *
   * @param name
   * @param value
   * @throws RenderException
   */
  public RenderSession setVariable(String name, String value) throws RenderException {
    return setVariable(name, List.of(value), ESCAPE_NONE);
  }

  /**
   * Sets a single variable within the tmpl to the specified value using the specified escape type.
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
   * Sets the specified tmpl variable to the concatenation of the specified list of strings. If you
   * specify an empty list, the variable not be rendered at all (it will be replaced with an empty
   * {@code String}). This enables you to render the variable only under certain conditions.
   *
   * <p><b>Example:</b> with this tmpl:
   *
   * <p>
   *
   * <pre>
   * &lt;table&gt;
   *  &lt;tr&gt;&lt;td&gt;~%html:greetings%&lt;/td&gt;&lt;/tr&gt;
   * &lt;/table&gt;
   * </pre>
   *
   * <p>and with this input: <code>List.of("Hello", " ", "world", "!")</code>, the rendered result
   * would be:
   *
   * <p>
   *
   * <pre>
   * &lt;table&gt;
   *  &lt;tr&gt;&lt;td&gt;Hello world!&lt;/td&gt;&lt;/tr&gt;
   * &lt;/table&gt;
   * </pre>
   *
   * <p>which is not very useful. It can be useful, however, if the variable stands in for an entire
   * HTML snippet, like for example:
   *
   * <p>
   *
   * <pre>
   * &lt;table&gt;
   *  ~%text:rows%
   * &lt;/table&gt;
   * </pre>
   *
   * <p>Now, with this input:
   *
   * <p>
   *
   * <pre>
   * List.of("&lt;tr&gt;&lt;td&gt;ONE&lt;/td&gt;&lt;/tr&gt;",
   *  "&lt;tr&gt;&lt;td&gt;TWO&lt;/td&gt;&lt;/tr&gt;",
   *  "&lt;tr&gt;&lt;td&gt;THREE&lt;/td&gt;&lt;/tr&gt;");
   * </pre>
   *
   * you would get:
   *
   * <p>
   *
   * <pre>
   * &lt;table&gt;
   *  &lt;tr&gt;&lt;td&gt;ONE&lt;/td&gt;&lt;/tr&gt;
   *  &lt;tr&gt;&lt;td&gt;TWO&lt;/td&gt;&lt;/tr&gt;
   *  &lt;tr&gt;&lt;td&gt;THREE&lt;/td&gt;&lt;/tr&gt;
   * &lt;/table&gt;
   * </pre>
   *
   * @param name
   * @param value
   * @param escapeType
   * @return
   * @throws RenderException
   */
  public RenderSession setVariable(String name, List<String> value, EscapeType escapeType)
      throws RenderException {
    Check.notNull(name, "name");
    Check.that(value, "value").is(noneNull());
    Check.notNull(escapeType, "escapeType");
    Check.on(s -> alreadySet(name), state.isSet(name)).is(no());
    Check.on(s -> noSuchVariable(name), name).is(in(), template.getVariableNames());
    Check.on(s -> badEscapeType(), escapeType).is(notSameAs(), NOT_SPECIFIED);
    template.getVarPartIndices().get(name).forEach(i -> escape(i, value, escapeType));
    state.done(name);
    return this;
  }

  private void escape(int partIndex, List<String> val, EscapeType escapeType) {
    List<Part> parts = template.getParts();
    VariablePart part = (VariablePart) parts.get(partIndex);
    EscapeType myEscapeType = part.getEscapeType();
    if (myEscapeType == NOT_SPECIFIED) {
      myEscapeType = escapeType;
    }
    List<String> escaped = new ArrayList<>(val.size());
    val.stream().map(myEscapeType::apply).forEach(escaped::add);
    state.setVar(partIndex, escaped);
  }

  /* METHODS FOR POPULATING A SINGLE NESTED TEMPLATE */

  public RenderSession populateTemplate(String name, ViewData data) throws RenderException {
    return repeatTemplate(name, List.of(data), ESCAPE_NONE, null);
  }

  public RenderSession populateTemplate(
      String name, ViewData data, EscapeType escapeType, Set<String> names) throws RenderException {
    return repeatTemplate(name, List.of(data), escapeType, names);
  }

  public RenderSession repeatTemplate(String name, List<ViewData> data) throws RenderException {
    return repeatTemplate(name, data, ESCAPE_NONE, null);
  }

  public RenderSession repeatTemplate(
      String name, List<ViewData> data, EscapeType escapeType, Set<String> names)
      throws RenderException {
    Check.notNull(name, "name");
    Check.that(data, "data").is(noneNull());
    Check.notNull(escapeType, "escapeType");
    Check.on(s -> alreadyPopulated(name), state.isPopulated(name)).is(no());
    Check.on(s -> noSuchTemplate(name), name).is(in(), template.getTemplateNames());
    Check.on(s -> badEscapeType(), escapeType).is(notSameAs(), NOT_SPECIFIED);
    Template nested = template.getTemplate(name);
    names = ifEmpty(names, nested::getAllNames);
    List<RenderSession> session = state.createOrGetSessions(nested, name, data.size());
    for (int i = 0; i < session.size(); ++i) {
      session.get(i).setViewData(data.get(i), escapeType, names);
    }
    state.populated(name);
    return this;
  }

  /* METHODS FOR POPULATING WHATEVER IS IN THE PROVIDED ViewData OBJECT */

  public RenderSession setViewData(ViewData data) throws RenderException {
    return setViewData(data, ESCAPE_NONE, null);
  }

  public RenderSession setViewData(ViewData data, EscapeType escapeType) throws RenderException {
    return setViewData(data, escapeType, null);
  }

  /**
   * Populates variables and nested templates that can be populated using the provided {@code
   * ViewData} object. Only variables and nested templates whose name is present in the {@code
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
  public RenderSession setViewData(ViewData data, EscapeType escapeType, Set<String> names)
      throws RenderException {
    processVarsInViewData(data, escapeType, names);
    processTmplsInViewData(data, escapeType, names);
    return this;
  }

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

  private void processVarsInViewData(ViewData data, EscapeType escapeType, Set<String> names)
      throws RenderException {
    Set<String> varNames;
    if (isEmpty(names) || names.equals(template.getAllNames())) {
      varNames = template.getVariableNames();
    } else {
      varNames = new HashSet<>(template.getVariableNames());
      varNames.retainAll(names);
    }
    for (String name : varNames) {
      Optional<List<String>> value = data.getValue(template, name);
      if (value.isPresent()) {
        setVariable(name, value.get(), escapeType);
      }
    }
  }

  private void processTmplsInViewData(ViewData data, EscapeType escapeType, Set<String> names)
      throws RenderException {
    Set<String> tmplNames;
    if (isEmpty(names) || names.equals(template.getAllNames())) {
      tmplNames = template.getTemplateNames();
    } else {
      tmplNames = new HashSet<>(template.getTemplateNames());
      tmplNames.retainAll(names);
    }
    for (String name : tmplNames) {
      Optional<List<ViewData>> nested = data.getNestedViewData(template, name);
      if (nested.isPresent()) {
        repeatTemplate(name, nested.get(), escapeType, names);
      }
    }
  }
}
