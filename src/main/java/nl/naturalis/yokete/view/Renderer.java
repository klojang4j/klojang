package nl.naturalis.yokete.view;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import org.apache.commons.text.StringEscapeUtils;
import nl.naturalis.common.ExceptionMethods;
import nl.naturalis.common.check.Check;
import nl.naturalis.common.collection.IntList;
import static nl.naturalis.common.CollectionMethods.initializedList;
import static nl.naturalis.common.check.CommonChecks.*;
import static nl.naturalis.common.check.CommonGetters.size;
import static nl.naturalis.yokete.view.EscapeType.ESCAPE_HTML;
import static nl.naturalis.yokete.view.EscapeType.ESCAPE_NONE;
import static nl.naturalis.yokete.view.EscapeType.NOT_SPECIFIED;
import static nl.naturalis.common.check.CommonGetters.*;
import static nl.naturalis.yokete.view.RenderException.*;

/** @author Ayco Holleman */
public class Renderer {

  private static final String ERR_NOT_STARTED =
      "No active render session. Call Renderer.start() first";

  private static final String ERR_NOT_RENDERABLE =
      "Cannot render until all variables and nested templates have been populated";

  private final Template template;

  /**
   * Creates a {@code VariableSubstituter} that uses the {@link #REGEX_VARIABLE default variable
   * pattern} to extract variables from text.
   *
   * @param template
   */
  public Renderer(Template template) {
    this.template = Check.notNull(template, "template").ok();
  }

  private List<String> variableValues;
  private List<Renderer[]> nestedRenderers;
  private Set<String> unrendered;

  public void start() {
    List<Part> parts = template.getParts();
    variableValues = initializedList(String.class, parts.size());
    nestedRenderers = initializedList(Renderer[].class, parts.size());
    unrendered = new HashSet<>(template.getVariableNames());
  }

  /* METHODS FOR POPULATING A SINGLE NESTED TEMPLATE */

  /**
   * Populates a single nested template using the available data in the {@code ViewData} object.
   * Note that the {@code ViewData} object is not required to supply all values for all variables in
   * the template. You can call this method multiple times until all variables are set.
   *
   * @param name
   * @param data
   * @throws RenderException
   */
  public void populateTemplate(String name, ViewData data) throws RenderException {
    populateTemplate(name, data, ESCAPE_NONE);
  }

  /**
   * Populates a single nested template using the available data in the {@code ViewData} object and
   * using the specified escape type. Note that the {@code ViewData} object is not required to
   * supply all values for all variables in the template. You can call this method multiple times
   * until all variables are set.
   *
   * @param name
   * @param data
   * @param escapeType
   * @throws RenderException
   */
  public void populateTemplate(String name, ViewData data, EscapeType escapeType)
      throws RenderException {
    Check.notNull(name, "name");
    Check.that(data, "data").is(notEmpty());
    Check.that(escapeType, "escapeType").is(notSameAs(), NOT_SPECIFIED);
    Template nested = template.getNestedTemplate(name);
    IntList indices = template.getVarPartIndices().get(name);
    if (indices == null) {
      throw noSuchTemplate(name);
    }
    indices.forEach(i -> repeat(i, List.of(data), escapeType, null));
  }

  public void populateTemplate(
      String name, ViewData data, EscapeType escapeType, Set<String> varNames) {
    repeatTemplate(name, List.of(data), escapeType, varNames);
  }

  public void repeatTemplate(
      String name, List<ViewData> data, EscapeType escapeType, Set<String> varNames)
      throws RenderException {
    Check.notNull(name, "name");
    Check.that(data, "data").is(notEmpty());
    Check.that(escapeType, "escapeType").is(notSameAs(), NOT_SPECIFIED);
    Check.that(varNames, "varNames").is(notEmpty());
    IntList indices = template.getVarPartIndices().get(name);
    if (indices == null) {
      throw noSuchTemplate(name);
    }
    indices.forEach(i -> repeat(i, data, escapeType, varNames));
  }

  private void repeat(
      String tmplName, List<ViewData> data, EscapeType escapeType, Set<String> names) {
    List<Part> parts = template.getParts();
    Template nested = template.getNestedTemplate(tmplName);
    if (names == null) {
      names = nested.getAllNames();
    }
    Renderer[] renderers = nestedRenderers.get(partIndex);
    if (renderers == null) {
      renderers = new Renderer[data.size()];
      for (int i = 0; i < renderers.length; ++i) {
        renderers[i] = new Renderer(nested);
      }
    } else {
      Check.that(data).has(size(), eq(), renderers.length);
    }
    if (names == null) {
      names = nested.getAllNames();
    }
    for (int i = 0; i < renderers.length; ++i) {
      Renderer renderer = new Renderer(nested);
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
    setViewData(data, escapeType, template.getAllNames());
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
    Check.notNull(data, "data");
    Check.notNull(varNames, "varNames");
    Check.that(escapeType, "escapeType").is(notSameAs(), NOT_SPECIFIED);
    processVarsInViewData(data, escapeType, varNames);
    processTmplsInViewData(data, escapeType, varNames);
  }

  private void processVarsInViewData(ViewData data, EscapeType escapeType, Set<String> names) {
    Set<String> varNames;
    if (names == null || names == template.getAllNames()) {
      varNames = template.getVariableNames();
    } else {
      varNames = new HashSet<>(template.getVariableNames());
      varNames.retainAll(names);
    }
    for (String varName : varNames) {
      Optional<List<String>> values = data.getValue(template, varName);
      if (values.isPresent()) {}

      data.getValue(template, varName).ifPresent(val -> setVariable(varName, val, escapeType));
    }
  }

  private void processTmplsInViewData(ViewData data, EscapeType escapeType, Set<String> names) {
    Set<String> tmplNames;
    if (names == null || names == template.getAllNames()) {
      tmplNames = template.getNestedTemplateNames();
    } else {
      tmplNames = new HashSet<>(template.getNestedTemplateNames());
      tmplNames.retainAll(names);
    }
    for (String tmplName : tmplNames) {
      data.getNestedViewData(template, tmplName)
          .ifPresent(vd -> populateTemplate(tmplName, vd, escapeType, names));
    }
  }

  /* SESSION RENDER METHODS */

  public StringBuilder render() {
    checkRendereable();
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

  public void render(StringBuilder sb) throws RenderException {
    checkRendereable();
    Check.notNull(sb);
    processText();
    variableValues.forEach(sb::append);
  }

  public void render(OutputStream out) throws RenderException {
    checkRendereable();
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
    variableValues = null;
    nestedRenderers = null;
    unrendered = null;
  }

  private void processText() {
    List<Part> parts = template.getParts();
    for (int i = 0; i < parts.size(); ++i) {
      if (parts.get(i).getClass() == TextPart.class) {
        variableValues.set(i, TextPart.class.cast(parts.get(i)).getText());
      }
    }
  }

  private void checkRendereable() throws RenderException {
    Check.with(RenderException::new, unrendered).has(size(), eq(), 0, ERR_NOT_RENDERABLE);
  }
}
