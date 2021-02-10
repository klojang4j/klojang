package nl.naturalis.yokete.view;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.text.StringEscapeUtils;
import nl.naturalis.common.check.Check;
import nl.naturalis.common.collection.IntList;
import static nl.naturalis.yokete.view.EscapeType.ESCAPE_HTML;
import static nl.naturalis.yokete.view.EscapeType.ESCAPE_NONE;
import static nl.naturalis.yokete.view.EscapeType.NOT_SPECIFIED;
import static nl.naturalis.yokete.view.RenderException.*;
import static nl.naturalis.common.check.CommonChecks.*;
import static nl.naturalis.common.check.CommonGetters.size;

public class RenderSession {

  private static final String ERR_NOT_ACTIVE =
      "Session de-activated. You can render the session again, but you cannot modify it any longer";

  private static final String ERR_BAD_ESCAPE_TYPE = "NOT_SPECIFIED is not a valid escape type";

  boolean active = true;

  private final Template template;
  private final RenderState state;

  RenderSession(Template template) {
    this.template = template;
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
   * @throws RenderException
   */
  public void setVariable(String varName, String value, EscapeType escapeType)
      throws RenderException {
    checkActive();
    checkEscapeType(escapeType);
    Check.notNull(varName, "varName");
    Check.notNull(value, "value");
    IntList indices = template.getVarPartIndices().get(varName);
    if (indices == null) {
      throw noSuchVariable(varName);
    }
    if (state.isVariableSet(varName)) {
      throw variableAlreadySet(varName);
    }
    indices.forEach(i -> setVar(i, List.of(value), escapeType));
    state.doneWithVar(varName);
  }

  private void setVar(int partIndex, List<String> val, EscapeType escapeType) {
    List<Part> parts = template.getParts();
    VariablePart vp = VariablePart.class.cast(parts.get(partIndex));
    EscapeType myEscapeType = vp.getEscapeType();
    String str = val.stream().collect(Collectors.joining());
    if (myEscapeType == NOT_SPECIFIED) {
      myEscapeType = escapeType;
    }
    if (myEscapeType == ESCAPE_NONE) {
      state.setVar(partIndex, str);
    } else if (myEscapeType == ESCAPE_HTML) {
      state.setVar(partIndex, StringEscapeUtils.escapeHtml4(str));
    } else /* ESCAPE_JS */ {
      state.setVar(partIndex, StringEscapeUtils.escapeEcmaScript(str));
    }
  }

  /* METHODS FOR POPULATING A SINGLE NESTED TEMPLATE */

  public void repeatTemplate(
      String name, List<ViewData> data, EscapeType escapeType, Set<String> names)
      throws RenderException {
    checkActive();
    checkEscapeType(escapeType);
    Check.notNull(name, "name");
    Check.notNull(data, "data");
    Template nested = template.getNestedTemplate(name);
    if (nested == null) {
      throw noSuchTemplate(name);
    }
    if (names == null || names.isEmpty()) {
      names = nested.getAllNames();
    }
    List<Renderer> renderers = state.getRenderersForTemplate(nested, name, data.size());
    for (int i = 0; i < renderers.size(); ++i) {
      Renderer renderer = new Renderer(nested);
      renderer.setViewData(data.get(i), escapeType, names);
    }
  }

  private void checkActive() {
    Check.with(illegalState(), active).is(yes(), ERR_NOT_ACTIVE);
  }

  private static void checkEscapeType(EscapeType escapeType) {
    Check.that(escapeType).is(notSameAs(), NOT_SPECIFIED, ERR_BAD_ESCAPE_TYPE);
  }
}
