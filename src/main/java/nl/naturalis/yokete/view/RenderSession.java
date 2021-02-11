package nl.naturalis.yokete.view;

import java.util.List;
import java.util.Set;
import nl.naturalis.common.check.Check;
import nl.naturalis.common.collection.IntList;
import static java.util.stream.Collectors.joining;
import static nl.naturalis.common.ObjectMethods.ifEmpty;
import static nl.naturalis.common.check.CommonChecks.in;
import static nl.naturalis.common.check.CommonChecks.no;
import static nl.naturalis.common.check.CommonChecks.notSameAs;
import static nl.naturalis.common.check.CommonChecks.yes;
import static nl.naturalis.yokete.view.EscapeType.ESCAPE_NONE;
import static nl.naturalis.yokete.view.EscapeType.NOT_SPECIFIED;
import static nl.naturalis.yokete.view.RenderException.*;

public class RenderSession {

  boolean active = true;

  private final Template tmpl;
  private final RenderState state;

  RenderSession(Template template) {
    this.tmpl = template;
    this.state = new RenderState(template);
  }

  /* METHODS FOR SETTING A SINGLE TEMPLATE VARIABLE */

  /**
   * Sets a single variable within the template to the specified value.
   *
   * @param varName
   * @param value
   * @throws RenderException
   */
  public void setVariable(String varName, String value) throws RenderException {
    setVariable(varName, value, ESCAPE_NONE);
  }

  /**
   * Sets a single variable within the template to the specified value using the specified escape
   * type.
   *
   * @param varName The name of the variable to set
   * @param value The value of the variable
   * @param escapeType The escape type to use when rendering the variable
   * @return This {@code RenderSession}
   * @throws RenderException
   */
  public RenderSession setVariable(String varName, String value, EscapeType escapeType)
      throws RenderException {
    return setVariable(varName, List.of(value), escapeType);
  }

  /**
   * Sets the specified template variable to the concatenation of the specified list of strings. If
   * you specify an empty list, the variable not be rendered at all (it will be replaced with an
   * empty {@code String}). This enables you to render the variable only under certain conditions.
   *
   * <p><b>Example:</b> with this template:
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
    Check.notNull(value, "value");
    Check.with(s -> noSession(), active).is(yes());
    Check.with(s -> alreadySet(name), state.isSet(name)).is(no());
    Check.with(s -> noSuchVariable(name), name).is(in(), tmpl.getVariableNames());
    Check.with(s -> badEscapeType(), escapeType).is(notSameAs(), NOT_SPECIFIED);
    IntList indices = tmpl.getVarPartIndices().get(name);
    indices.forEach(i -> setVar(i, value, escapeType));
    state.done(name);
    return this;
  }

  private void setVar(int partIndex, List<String> val, EscapeType escapeType) {
    List<Part> parts = tmpl.getParts();
    VariablePart vp = VariablePart.class.cast(parts.get(partIndex));
    EscapeType myEscapeType = vp.getEscapeType();
    if (myEscapeType == NOT_SPECIFIED) {
      myEscapeType = escapeType;
    }
    String str = val.stream().map(myEscapeType::apply).collect(joining());
    state.setVar(partIndex, str);
  }

  /* METHODS FOR POPULATING A SINGLE NESTED TEMPLATE */

  public RenderSession populateTemplate(String name, ViewData data) throws RenderException {
    return populateTemplate(name, data, ESCAPE_NONE, null);
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
    Check.notNull(data, "data");
    Check.with(s -> noSession(), active).is(yes());
    Check.with(s -> alreadyPopulated(name), state.isPopulated(name)).is(no());
    Check.with(s -> noSuchTemplate(name), name).is(in(), tmpl.getTemplateNames());
    Check.with(s -> badEscapeType(), escapeType).is(notSameAs(), NOT_SPECIFIED);
    Template nested = tmpl.getTemplate(name);
    names = ifEmpty(names, nested::getAllNames);
    List<Renderer> renderers = state.getRenderers(nested, name, data.size());
    for (int i = 0; i < renderers.size(); ++i) {
      Renderer renderer = new Renderer(nested);
      renderer.setViewData(data.get(i), escapeType, names);
    }
    state.populated(name);
    return this;
  }
}
