package nl.naturalis.yokete.view;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.commons.text.StringEscapeUtils;
import nl.naturalis.common.ExceptionMethods;
import nl.naturalis.common.check.Check;
import nl.naturalis.common.collection.IntList;
import static nl.naturalis.common.CollectionMethods.implode;
import static nl.naturalis.common.CollectionMethods.initializedList;
import static nl.naturalis.common.check.CommonChecks.*;
import static nl.naturalis.common.check.CommonGetters.size;
import static nl.naturalis.yokete.view.EscapeType.ESCAPE_HTML;
import static nl.naturalis.yokete.view.EscapeType.ESCAPE_NONE;
import static nl.naturalis.yokete.view.EscapeType.NOT_SPECIFIED;
import static nl.naturalis.yokete.view.ViewData.ABSENT;
/** @author Ayco Holleman */
public class Renderer {

  private static final String ERR_NOT_STARTED =
      "No active render session. Call Renderer.start() first";

  private final ReentrantLock lock = new ReentrantLock();

  private final Template template;
  private final VariableRenderer varRenderer;

  /**
   * Creates a {@code VariableSubstituter} that uses the {@link #REGEX_VARIABLE default variable
   * pattern} to extract variables from text.
   *
   * @param template
   */
  public Renderer(Template template, VariableRenderer varRenderer) {
    this.template = Check.notNull(template, "template").ok();
    this.varRenderer = Check.notNull(varRenderer, "varRenderer").ok();
  }

  private Thread lockHolder;
  private List<String> variableValues;
  private List<Renderer[]> nestedRenderers;
  private Set<String> unrendered;

  public void start() {
    lock.lock();
    lockHolder = Thread.currentThread();
    List<Part> parts = template.getParts();
    variableValues = initializedList(String.class, parts.size());
    nestedRenderers = initializedList(Renderer[].class, parts.size());
    unrendered = new HashSet<>(template.getVariableNames());
  }

  /* METHODS FOR SETTING A SINGLE TEMPLATE VARIABLE */

  public void setVariable(String varName, Object value) {
    setVariable(varName, value, ESCAPE_NONE);
  }

  public void setVariable(String varName, Object value, EscapeType escapeType) {
    Check.with(IllegalStateException::new, lockHolder)
        .is(notNull(), ERR_NOT_STARTED)
        .is(sameAs(), Thread.currentThread(), ERR_NOT_STARTED);
    Check.notNull(varName, "varName");
    Check.that(escapeType, "escapeType").is(notSameAs(), NOT_SPECIFIED);
    if (value != ABSENT) {
      IntList indices = template.getVariableIndices().getVariableValue(varName);
      if (indices == null) {
        throw new RenderException("No such variable: \"" + varName + "\"");
      }
      indices.forEach(i -> setVar(i, value, escapeType));
      unrendered.remove(varName);
    }
  }

  private void setVar(int partIndex, Object val, EscapeType escapeType) {
    List<Part> parts = template.getParts();
    VariablePart vp = VariablePart.class.cast(parts.get(partIndex));
    String var = vp.getName();
    EscapeType myEscapeType = vp.getEscapeType();
    String s = varRenderer.apply(var, val);
    if (myEscapeType == NOT_SPECIFIED) {
      myEscapeType = escapeType;
    }
    if (myEscapeType == ESCAPE_NONE) {
      variableValues.set(partIndex, s);
    } else if (myEscapeType == ESCAPE_HTML) {
      variableValues.set(partIndex, StringEscapeUtils.escapeHtml4(s));
    } else /* ESCAPE_JS */ {
      variableValues.set(partIndex, StringEscapeUtils.escapeEcmaScript(s));
    }
  }

  /* METHODS FOR POPULATING A SINGLE NESTED TEMPLATE */

  public void populateTemplate(String name, ViewData data) {
    populateTemplate(name, data, ESCAPE_NONE);
  }

  public void populateTemplate(String name, ViewData data, EscapeType escapeType) {
    Check.with(IllegalStateException::new, lockHolder)
        .is(notNull(), ERR_NOT_STARTED)
        .is(sameAs(), Thread.currentThread(), ERR_NOT_STARTED);
    Check.notNull(name, "name");
    Check.that(data, "data").is(notEmpty());
    Check.that(escapeType, "").is(notSameAs(), NOT_SPECIFIED);
    IntList indices = template.getVariableIndices().getVariableValue(name);
    if (indices == null) {
      throw new RenderException("No such nested template: \"" + name + "\"");
    }
    indices.forEach(i -> repeat(i, List.of(data), escapeType, null));
  }

  public void populateTemplate(
      String name, ViewData data, EscapeType escapeType, Set<String> varNames) {
    repeatTemplate(name, List.of(data), escapeType, varNames);
  }

  public void repeatTemplate(
      String name, List<ViewData> data, EscapeType escapeType, Set<String> varNames) {
    Check.with(IllegalStateException::new, lockHolder)
        .is(notNull(), ERR_NOT_STARTED)
        .is(sameAs(), Thread.currentThread(), ERR_NOT_STARTED);
    Check.notNull(name, "name");
    Check.that(data, "data").is(notEmpty());
    Check.that(escapeType, "escapeType").is(notSameAs(), NOT_SPECIFIED);
    Check.that(varNames, "varNames").is(notEmpty());
    IntList indices = template.getVariableIndices().getVariableValue(name);
    if (indices == null) {
      throw new RenderException("No such nested template: \"" + name + "\"");
    }
    indices.forEach(i -> repeat(i, data, escapeType, varNames));
  }

  private void repeat(
      int partIndex, List<ViewData> data, EscapeType escapeType, Set<String> names) {
    List<Part> parts = template.getParts();
    Template nested = TemplatePart.class.cast(parts.get(partIndex)).getTemplate();
    if (names == null) {
      names = nested.getNames();
    }
    Renderer[] renderers = nestedRenderers.get(partIndex);
    if (renderers == null) {
      renderers = new Renderer[data.size()];
      for (int i = 0; i < renderers.length; ++i) {
        renderers[i] = new Renderer(nested, varRenderer);
      }
    } else {
      Check.that(data).has(size(), eq(), renderers.length);
    }
    if (names == null) {
      names = nested.getNames();
    }
    for (int i = 0; i < renderers.length; ++i) {
      Renderer renderer = new Renderer(nested, varRenderer);
      renderer.setViewData(data.get(i), escapeType, names);
    }
  }

  /* +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ */
  /* METHODS FOR SETTING WHATEVER IS PROVIDED BY A ViewData OBJECT */
  /* +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ */

  public void setViewData(ViewData data) {
    setViewData(data, ESCAPE_NONE);
  }

  public void setViewData(ViewData data, EscapeType escapeType) {
    setViewData(data, escapeType, template.getNames());
  }

  /**
   * Populates whatever can be populated from the provided {@code ViewData} object. Only variables
   * <b>and</b> nested templates whose name is present in the {@code varNames} argument will be
   * processed. This allows you to call this method multiple times with the same {@code ViewData}
   * object but different escape types. The {@code ViewData} object is itself not required to
   * provide <i>all</i> values for <i>all</i> variables and nested templates. You can call this
   * method multiple times with different {@code ViewData} objects, until all template variables and
   * nested templates are populated.
   *
   * @param data A {@code ViewData} instance that provides data for all or some of the template
   *     variables and nested templates.
   * @param escapeType The escape type to use
   * @param varNames The names of the variable <b>and/or</b> nested templates names that must be
   *     processed
   */
  public void setViewData(ViewData data, EscapeType escapeType, Set<String> varNames) {
    checkSession();
    Check.notNull(data, "data");
    Check.notNull(varNames, "varNames");
    Check.that(escapeType, "escapeType").is(notSameAs(), NOT_SPECIFIED);
    setViewDataVars(data, escapeType, varNames);
    setViewDataTmpls(data, escapeType, varNames);
  }

  private void setViewDataVars(ViewData data, EscapeType escapeType, Set<String> names) {
    Set<String> varNames;
    if (names == null || names == template.getNames()) {
      varNames = template.getVariableNames();
    } else {
      varNames = new HashSet<>(template.getVariableNames());
      varNames.retainAll(names);
    }
    for (String varName : varNames) {
      setVariable(varName, data.getVariableValue(varName), escapeType);
    }
  }

  private void setViewDataTmpls(ViewData data, EscapeType escapeType, Set<String> names) {
    Set<String> tmplNames;
    if (names == null || names == template.getNames()) {
      tmplNames = template.getNestedTemplateNames();
    } else {
      tmplNames = new HashSet<>(template.getNestedTemplateNames());
      tmplNames.retainAll(names);
    }
    for (String tmplName : tmplNames) {
      Object value = data.getVariableValue(tmplName);
      if (value != null || value != ABSENT) {
        if (!(value instanceof ViewData)) {
          String fmt =
              "Error while populating nested template \"%s\": "
                  + "ViewData must return another ViewData object "
                  + "for nested templates";
          String msg = String.format(fmt, tmplName);
          throw new RenderException(msg);
        }
        populateTemplate(tmplName, (ViewData) value, escapeType, names);
      }
    }
  }

  /* SESSION RENDER METHODS */

  public StringBuilder render() {
    checkSession();
    StringBuilder sb = new StringBuilder(1024);
    List<Part> parts = template.getParts();
    for (int i = 0; i < parts.size(); ++i) {
      Part part = parts.get(i);
      if (part.getClass() == TextPart.class) {
        sb.append(TextPart.class.cast(part).getText());
      } else if (part.getClass() == VariablePart.class) {
        sb.append(variableValues.get(i));
      } else /* TemplatePart */ {

      }
    }
    return sb;
  }

  public void render(StringBuilder sb) {
    checkSession();
    Check.notNull(sb);
    processText();
    variableValues.forEach(sb::append);
  }

  public void render(OutputStream out) {
    checkSession();
    processText();
    try {
      for (int i = 0; i < variableValues.size(); ++i) {
        out.write(variableValues.get(i).getBytes(StandardCharsets.UTF_8));
      }
    } catch (IOException e) {
      throw ExceptionMethods.uncheck(e);
    }
  }

  public void reset() {
    lockHolder = null;
    variableValues = null;
    nestedRenderers = null;
    unrendered = null;
    lock.unlock();
  }

  private void processText() {
    List<Part> parts = template.getParts();
    for (int i = 0; i < parts.size(); ++i) {
      if (parts.get(i).getClass() == TextPart.class) {
        variableValues.set(i, TextPart.class.cast(parts.get(i)).getText());
      }
    }
  }

  private void checkSession() {
    Check.with(IllegalStateException::new, lockHolder)
        .is(sameAs(), Thread.currentThread(), ERR_NOT_STARTED);
  }
}
