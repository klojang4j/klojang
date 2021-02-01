package nl.naturalis.yokete.view;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import nl.naturalis.common.collection.ImmutableIntList;
import nl.naturalis.common.collection.IntArrayList;
import nl.naturalis.common.collection.IntList;
import static java.util.stream.Collectors.toUnmodifiableSet;

/**
 * A {@code Template} captures the result of parsing a text template. It provides access to the
 * constituent parts of a text template: {@link VariablePart template variables}, {@link
 * TemplatePart nested templates} and literal text. It also takes care of removing comments from the
 * text template.
 *
 * @author Ayco Holleman
 */
public class Template {

  public static Template parse(String source) {
    List<Part> parts = TemplateParser.INSTANCE.parse(source);
    return new Template(parts);
  }

  private final List<Part> parts;
  private final Map<String, IntList> varIndices;
  private final Map<String, IntList> tmplIndices;
  private final int varCount;
  private final int tmplCount;
  private final Set<String> names; // variable + template names
  private final IntList textIndices;

  private Template(List<Part> parts) {
    this.parts = parts;
    this.varIndices = getVarIndices(parts);
    this.varCount = getVarCount(parts);
    this.tmplIndices = getTmplIndices(parts);
    this.tmplCount = getTmplCount(parts);
    this.names = getNames(parts);
    this.textIndices = getTextIndices(parts);
  }

  /**
   * Returns the constituent parts of the template. A template consists of {@link TextPart literal
   * text}, {@link VariablePart template variables} and {@link TemplatePart nested templates}. The
   * returned {@code List} is immutable.
   *
   * @return The constituent parts of the template file
   */
  public List<Part> getParts() {
    return parts;
  }

  public Map<String, IntList> getVariableIndices() {
    return varIndices;
  }

  public Set<String> getVariableNames() {
    return varIndices.keySet();
  }

  public int countVariables() {
    return varCount;
  }

  public Map<String, IntList> getNestedTemplateIndices() {
    return tmplIndices;
  }

  public Set<String> getNestedTemplateNames() {
    return tmplIndices.keySet();
  }

  public int countNestedTemplates() {
    return tmplCount;
  }

  /**
   * Returns the names of all template variables and nested templates.
   *
   * @return The names of all template variables and nested templates found in the template
   */
  public Set<String> getNames() {
    return names;
  }

  /**
   * Returns an immutable {@link IntList} of all parts that contain literal text. instances in the
   * parts list.
   *
   * @return The indices of the parts list that contain nested templates
   */
  public IntList getTextIndices() {
    return textIndices;
  }

  private static Map<String, IntList> getVarIndices(List<Part> parts) {
    Map<String, IntList> indices = new HashMap<>();
    for (int i = 0; i < parts.size(); ++i) {
      if (parts.get(i).getClass() == VariablePart.class) {
        String name = VariablePart.class.cast(parts.get(i)).getName();
        indices.computeIfAbsent(name, k -> new IntArrayList(5, 5)).add(i);
      }
    }
    indices.entrySet().forEach(e -> e.setValue(ImmutableIntList.copyOf(e.getValue())));
    return Map.copyOf(indices);
  }

  private static int getVarCount(List<Part> parts) {
    return (int) parts.stream().filter(VariablePart.class::isInstance).count();
  }

  private static Map<String, IntList> getTmplIndices(List<Part> parts) {
    Map<String, IntList> indices = new HashMap<>();
    for (int i = 0; i < parts.size(); ++i) {
      if (parts.get(i).getClass() == TemplatePart.class) {
        String name = TemplatePart.class.cast(parts.get(i)).getName();
        indices.computeIfAbsent(name, k -> new IntArrayList(5, 5)).add(i);
      }
    }
    indices.entrySet().forEach(e -> e.setValue(ImmutableIntList.copyOf(e.getValue())));
    return Map.copyOf(indices);
  }

  private static int getTmplCount(List<Part> parts) {
    return (int) parts.stream().filter(TemplatePart.class::isInstance).count();
  }

  private static Set<String> getNames(List<Part> parts) {
    return parts
        .stream()
        .filter(NamedPart.class::isInstance)
        .map(NamedPart.class::cast)
        .map(NamedPart::getName)
        .collect(toUnmodifiableSet());
  }

  private static IntList getTextIndices(List<Part> parts) {
    IntArrayList indices = new IntArrayList();
    for (int i = 0; i < parts.size(); ++i) {
      if (parts.get(i).getClass() == TextPart.class) {
        indices.add(i);
      }
    }
    return ImmutableIntList.copyOf(indices);
  }
}
