package nl.naturalis.yokete.view;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import nl.naturalis.common.check.Check;
import nl.naturalis.common.collection.IntList;
import static java.util.stream.Collectors.joining;
import static nl.naturalis.common.ObjectMethods.ifEmpty;
import static nl.naturalis.common.ObjectMethods.isEmpty;
import static nl.naturalis.common.check.CommonChecks.in;
import static nl.naturalis.common.check.CommonChecks.no;
import static nl.naturalis.common.check.CommonChecks.notSameAs;
import static nl.naturalis.yokete.view.EscapeType.ESCAPE_NONE;
import static nl.naturalis.yokete.view.EscapeType.NOT_SPECIFIED;
import static nl.naturalis.yokete.view.RenderException.*;

public final class RenderSession {

  private final Template tmpl;
  private final RenderState state;

  RenderSession(Template tmpl) {
    this.tmpl = tmpl;
    this.state = new RenderState(tmpl);
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
    return setVar(name, List.of(value), ESCAPE_NONE, false);
  }

  public RenderSession setVariable(String name, String value, boolean allowOverwrite)
      throws RenderException {
    return setVar(name, List.of(value), ESCAPE_NONE, allowOverwrite);
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
    return setVar(name, List.of(value), escapeType, false);
  }

  public RenderSession setVariable(
      String name, String value, EscapeType escapeType, boolean allowOverwrite)
      throws RenderException {
    return setVar(name, List.of(value), escapeType, allowOverwrite);
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
    return setVar(name, value, escapeType, false);
  }

  public RenderSession setVariable(
      String name, List<String> value, EscapeType escapeType, boolean allowOverwrite)
      throws RenderException {
    return setVar(name, value, escapeType, allowOverwrite);
  }

  private RenderSession setVar(
      String name, List<String> value, EscapeType escapeType, boolean allowOverwrite)
      throws RenderException {
    Check.notNull(name, "name");
    Check.notNull(value, "value");
    if (!allowOverwrite) {
      Check.with(s -> alreadySet(name), state.isSet(name)).is(no());
    }
    Check.with(s -> noSuchVariable(name), name).is(in(), tmpl.getVariableNames());
    Check.with(s -> badEscapeType(), escapeType).is(notSameAs(), NOT_SPECIFIED);
    IntList indices = tmpl.getVarPartIndices().get(name);
    indices.forEach(i -> setVar(i, value, escapeType));
    state.done(name);
    return this;
  }

  private void setVar(int partIndex, List<String> val, EscapeType escapeType) {
    List<Part> parts = tmpl.getParts();
    VariablePart part = (VariablePart) parts.get(partIndex);
    EscapeType myEscapeType = part.getEscapeType();
    if (myEscapeType == NOT_SPECIFIED) {
      myEscapeType = escapeType;
    }
    String str = val.stream().map(myEscapeType::apply).collect(joining());
    state.setVar(partIndex, str);
  }

  /* METHODS FOR POPULATING A SINGLE NESTED TEMPLATE */

  public RenderSession populateTemplate(String name, ViewData data) throws RenderException {
    return repeat(name, List.of(data), ESCAPE_NONE, null, false);
  }

  public RenderSession populateTemplate(String name, ViewData data, boolean allowOverwrite)
      throws RenderException {
    return repeat(name, List.of(data), ESCAPE_NONE, null, allowOverwrite);
  }

  public RenderSession populateTemplate(
      String name, ViewData data, EscapeType escapeType, Set<String> names) throws RenderException {
    return repeat(name, List.of(data), escapeType, names, false);
  }

  public RenderSession populateTemplate(
      String name, ViewData data, EscapeType escapeType, Set<String> names, boolean allowOverwrite)
      throws RenderException {
    return repeat(name, List.of(data), escapeType, names, allowOverwrite);
  }

  public RenderSession repeatTemplate(String name, List<ViewData> data) throws RenderException {
    return repeat(name, data, ESCAPE_NONE, null, false);
  }

  public RenderSession repeatTemplate(String name, List<ViewData> data, boolean allowOverwrite)
      throws RenderException {
    return repeat(name, data, ESCAPE_NONE, null, allowOverwrite);
  }

  public RenderSession repeatTemplate(
      String name, List<ViewData> data, EscapeType escapeType, Set<String> names)
      throws RenderException {
    return repeat(name, data, escapeType, names, false);
  }

  public RenderSession repeatTemplate(
      String name,
      List<ViewData> data,
      EscapeType escapeType,
      Set<String> names,
      boolean allowOverwrite)
      throws RenderException {
    return repeat(name, data, escapeType, names, allowOverwrite);
  }

  private RenderSession repeat(
      String name,
      List<ViewData> data,
      EscapeType escapeType,
      Set<String> names,
      boolean allowOverwrite)
      throws RenderException {
    Check.notNull(name, "name");
    Check.notNull(data, "data");
    if (!allowOverwrite) {
      Check.with(s -> alreadyPopulated(name), state.isPopulated(name)).is(no());
    }
    Check.with(s -> noSuchTemplate(name), name).is(in(), tmpl.getTemplateNames());
    Check.with(s -> badEscapeType(), escapeType).is(notSameAs(), NOT_SPECIFIED);
    Template nested = tmpl.getTemplate(name);
    names = ifEmpty(names, nested::getAllNames);
    List<RenderSession> session = state.getSessions(nested, name, data.size());
    for (int i = 0; i < session.size(); ++i) {
      session.get(i).setViewData(data.get(i), escapeType, names, allowOverwrite);
    }
    state.populated(name);
    return this;
  }

  /* METHODS FOR POPULATING WHATEVER IS IN THE PROVIDED ViewData OBJECT */

  public void setViewData(ViewData data) throws RenderException {
    processVarsInViewData(data, ESCAPE_NONE, null, false);
    processTmplsInViewData(data, ESCAPE_NONE, null, false);
  }

  public void setViewData(ViewData data, boolean allowOverwrite) throws RenderException {
    processVarsInViewData(data, ESCAPE_NONE, null, allowOverwrite);
    processTmplsInViewData(data, ESCAPE_NONE, null, allowOverwrite);
  }

  public void setViewData(ViewData data, EscapeType escapeType) throws RenderException {
    processVarsInViewData(data, escapeType, null, false);
    processTmplsInViewData(data, escapeType, null, false);
  }

  public void setViewData(ViewData data, EscapeType escapeType, boolean allowOverwrite)
      throws RenderException {
    processVarsInViewData(data, escapeType, null, allowOverwrite);
    processTmplsInViewData(data, escapeType, null, allowOverwrite);
  }

  /**
   * Populates all variables and nested templates that can be populated using the provided {@code
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
  public void setViewData(ViewData data, EscapeType escapeType, Set<String> names)
      throws RenderException {
    processVarsInViewData(data, escapeType, names, false);
    processTmplsInViewData(data, escapeType, names, false);
  }

  public void setViewData(
      ViewData data, EscapeType escapeType, Set<String> names, boolean allowOverwrite)
      throws RenderException {
    processVarsInViewData(data, escapeType, names, allowOverwrite);
    processTmplsInViewData(data, escapeType, names, allowOverwrite);
  }

  private void processVarsInViewData(
      ViewData data, EscapeType escapeType, Set<String> names, boolean allowOverwrite)
      throws RenderException {
    Set<String> varNames;
    if (isEmpty(names) || names.equals(tmpl.getAllNames())) {
      varNames = tmpl.getVariableNames();
    } else {
      varNames = new HashSet<>(tmpl.getVariableNames());
      varNames.retainAll(names);
    }
    for (String name : varNames) {
      Optional<List<String>> value = data.getValue(tmpl, name);
      if (value.isPresent()) {
        setVar(name, value.get(), escapeType, allowOverwrite);
      }
    }
  }

  private void processTmplsInViewData(
      ViewData data, EscapeType escapeType, Set<String> names, boolean allowOverwrite)
      throws RenderException {
    Set<String> tmplNames;
    if (isEmpty(names) || names.equals(tmpl.getAllNames())) {
      tmplNames = tmpl.getTemplateNames();
    } else {
      tmplNames = new HashSet<>(tmpl.getTemplateNames());
      tmplNames.retainAll(names);
    }
    for (String name : tmplNames) {
      Optional<List<ViewData>> nested = data.getNestedViewData(tmpl, name);
      if (nested.isPresent()) {
        repeat(name, nested.get(), escapeType, names, allowOverwrite);
      }
    }
  }
}
