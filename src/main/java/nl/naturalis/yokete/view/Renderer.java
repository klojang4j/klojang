package nl.naturalis.yokete.view;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.commons.text.StringEscapeUtils;
import nl.naturalis.common.ExceptionMethods;
import nl.naturalis.common.check.Check;
import nl.naturalis.common.collection.IntList;
import static nl.naturalis.common.CollectionMethods.implode;
import static nl.naturalis.common.CollectionMethods.initializedList;
import static nl.naturalis.common.check.CommonChecks.*;
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

  public StringBuilder render(Map<String, Object> data) {
    return render(data, ESCAPE_NONE);
  }

  public StringBuilder render(Map<String, Object> data, EscapeType escapeType) {
    try {
      start();
      // substitute(data, escapeType, data.keySet());
      return render();
    } finally {
      reset();
    }
  }

  public void render(Map<String, Object> data, StringBuilder out) {
    render(data, out, ESCAPE_NONE);
  }

  public void render(Map<String, Object> data, StringBuilder out, EscapeType escapeType) {
    try {
      start();
      // substitute(data, escapeType, data.keySet());
      render(out);
    } finally {
      reset();
    }
  }

  public void repeat(List<Map<String, Object>> data, StringBuilder out) {
    repeat(data, out, ESCAPE_NONE);
  }

  public StringBuilder repeat(List<Map<String, Object>> data) {
    return repeat(data, ESCAPE_NONE);
  }

  public void repeat(List<Map<String, Object>> data, StringBuilder out, EscapeType escapeType) {
    data.stream().forEach(map -> render(map, out, escapeType));
  }

  public StringBuilder repeat(List<Map<String, Object>> data, EscapeType escapeType) {
    StringBuilder out = new StringBuilder(512);
    data.stream().forEach(map -> render(map, out, escapeType));
    return out;
  }

  private Thread lockHolder;
  private List<String> variableValues;
  private List<Renderer> nestedRenderers;
  private Set<String> unrendered;

  public void start() {
    lock.lock();
    lockHolder = Thread.currentThread();
    List<Part> parts = template.getParts();
    variableValues = initializedList(String.class, parts.size());
    nestedRenderers = initializedList(Renderer.class, parts.size());
    unrendered = new HashSet<>(template.getVariableNames());
  }

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
      IntList indices = template.getVariableIndices().get(varName);
      if (indices == null) {
        throw new RenderException("No such variable: \"" + varName + "\"");
      }
      indices.forEach(i -> setVar(i, value, escapeType));
      unrendered.remove(varName);
    }
  }

  public void populate(String templateName, ViewData data) {
    populate(templateName, data, ESCAPE_NONE);
  }

  public void populate(String templateName, ViewData data, EscapeType escapeType) {
    Check.with(IllegalStateException::new, lockHolder)
        .is(notNull(), ERR_NOT_STARTED)
        .is(sameAs(), Thread.currentThread(), ERR_NOT_STARTED);
    Check.notNull(templateName, "tmplName");
    IntList indices = template.getVariableIndices().get(templateName);
    if (indices == null) {
      throw new RenderException("No such nested template: \"" + templateName + "\"");
    }
    indices.forEach(i -> populate(i, data, escapeType, null));
  }

  public void populate(
      String templateName, ViewData data, EscapeType escapeType, Set<String> names) {
    Check.with(IllegalStateException::new, lockHolder)
        .is(notNull(), ERR_NOT_STARTED)
        .is(sameAs(), Thread.currentThread(), ERR_NOT_STARTED);
    Check.notNull(templateName, "tmplName");
    IntList indices = template.getVariableIndices().get(templateName);
    if (indices == null) {
      throw new RenderException("No such nested template: \"" + templateName + "\"");
    }
    indices.forEach(i -> populate(i, data, escapeType, names));
  }

  public void setViewData(ViewData data) {
    setViewData(data, ESCAPE_NONE);
  }

  public void setViewData(ViewData data, EscapeType escapeType) {
    setViewData(data, escapeType, template.getNames());
  }

  /**
   * Populates the template using the specified {@code ViewData} object and {@code EscapeType}. Only
   * template variables and nested templates whose name is present in the {@code names} argument
   * will be processed. This allows you to use the same {@code ViewData} object while applying
   * different escape types for different template variables and/or nested templates. The {@code
   * ViewData} object is not required to provide all values for all templated variables and nested
   * templates. You can call this method multiple times with different {@code ViewData} objects,
   * until all template variables and nested templates are populated.
   *
   * @param data A {@code ViewData} instance that provides data for all or some of the template
   *     variables and nested templates.
   * @param escapeType The escape type to use
   * @param names The set of variable and/or nested templates names restricting which variables
   *     which should be processed
   */
  public void setViewData(ViewData data, EscapeType escapeType, Set<String> names) {
    checkSession();
    Check.notNull(data, "data");
    Check.notNull(names, "names");
    if (names != template.getNames()) {
      Set<String> badNames = Set.copyOf(names);
      badNames.removeAll(template.getNames());
      if (badNames.size() != 0) {
        String fmt = "No such variable%1$s or template%1$s: %2$s";
        String s = badNames.size() == 1 ? "" : "s";
        throw new RenderException(String.format(fmt, s, implode(badNames)));
      }
    }
    setVariables(data, escapeType, names);
    populate(data, escapeType, names);
  }

  private void setVariables(ViewData data, EscapeType escapeType, Set<String> names) {
    Set<String> subset;
    if (names == template.getNames()) {
      subset = template.getVariableNames();
    } else {
      subset = new HashSet<>(template.getVariableNames());
      subset.retainAll(names);
    }
    for (String varName : subset) {
      setVariable(varName, data.get(varName), escapeType);
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

  private void populate(ViewData data, EscapeType escapeType, Set<String> names) {
    Set<String> subset;
    if (names == template.getNames()) {
      subset = template.getNestedTemplateNames();
    } else {
      subset = new HashSet<>(template.getNestedTemplateNames());
      subset.retainAll(names);
    }
    for (String tmplName : subset) {
      Object value = data.get(tmplName);
      if (value != null || value != ABSENT) {
        if (!(value instanceof ViewData)) {
          String fmt =
              "Error while populating nested template \"%s\": "
                  + "ViewData must return another ViewData object "
                  + "for nested templates";
          String msg = String.format(fmt, tmplName);
          throw new RenderException(msg);
        }
        populate((ViewData) value, escapeType, names);
      }
    }
  }

  private void populate(int partIndex, ViewData data, EscapeType escapeType, Set<String> names) {
    List<Part> parts = template.getParts();
    TemplatePart tp = (TemplatePart) parts.get(partIndex);
    Template t = tp.getTemplate();
    Renderer r = nestedRenderers.get(partIndex);
    if (r == null) {
      nestedRenderers.set(partIndex, r = new Renderer(t, varRenderer));
    }
    if (names == null) {
      names = t.getNames();
    }
    r.setViewData(data, escapeType, names);
  }

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
