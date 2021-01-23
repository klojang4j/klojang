package nl.naturalis.yokete.view;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import nl.naturalis.common.collection.IntList;
import static java.util.stream.Collectors.toList;
import static nl.naturalis.yokete.view.Regex.*;

/**
 * A {@code Template} captures the result of parsing a text template. It provides access to the
 * constituent parts of a text template: template variables, nested templates and literal text. It
 * also takes care of removing comments from the text template.
 *
 * @author Ayco Holleman
 */
public class Template {

  private final List<Part> parts;
  private final IntList tmplIndices;
  private final IntList varIndices;

  public Template(String template) {
    template = removeComments(template);
    template = unhideVariables(template);
    template = unhideTemplates(template);
    LinkedList<Part> parts = new LinkedList<>();
    parseNestedTemplates(template, parts);
    parseVariables(parts);
    this.parts = new ArrayList<>(parts);
    this.tmplIndices = getTmplIndices(parts);
    this.varIndices = getVarIndices(parts);
  }

  /**
   * Returns the parts that the template is split into. Some parts will contain literal text, other
   * parts will contain the name of a variable.
   *
   * @return The constuent parts of the template file
   */
  public List<Part> getParts() {
    return new ArrayList<>(parts);
  }

  /**
   * Returns the indices of the parts list that contain nested templates.
   *
   * @return The indices of the parts list that contain nested templates
   */
  public IntList getTemplateIndices() {
    return new IntList(tmplIndices);
  }

  /**
   * Returns the indices of the parts list that contain variables.
   *
   * @return The indices of the parts list that contain variables
   */
  public IntList getVarIndices() {
    return new IntList(varIndices);
  }

  /**
   * The returns the number of variables found in the template.
   *
   * @return The number of variables found in the template
   */
  public int countVariables() {
    return varIndices.size();
  }

  /**
   * Returns all variables found in the template. Note that variables may occur multiple times in a
   * template, so the returned list does not necessarily contain unique variable names.
   *
   * @return All variables found in the template
   */
  public List<String> getVariables() {
    return varIndices
        .stream()
        .mapToObj(parts::get)
        .map(VariablePart.class::cast)
        .map(VariablePart::getName)
        .collect(toList());
  }

  private static void parseNestedTemplates(String template, LinkedList<Part> parts) {
    Matcher matcher = REGEX_NESTED_TEMPLATE.matcher(template);
    int end = 0; // end index of the previous nested template
    while (matcher.find()) {
      int start = matcher.start();
      if (start > end) {
        String parseLater = template.substring(end, start);
        parts.add(new UnparsedPart(parseLater, end, start));
      }
      String name = matcher.group(1);
      String src = matcher.group(2);
      end = matcher.end();
      parts.add(new TemplatePart(name, new Template(src), start, end));
    }
    if (end < template.length()) {
      String parseLater = template.substring(end);
      parts.add(new UnparsedPart(parseLater, end, template.length()));
    }
  }

  private static IntList getTmplIndices(List<Part> parts) {
    IntList indices = new IntList();
    for (int i = 0; i < parts.size(); ++i) {
      if (parts.get(i).getClass() == TemplatePart.class) {
        indices.add(i);
      }
    }
    return indices;
  }

  private static IntList getVarIndices(List<Part> parts) {
    IntList indices = new IntList();
    for (int i = 0; i < parts.size(); ++i) {
      if (parts.get(i).getClass() == VariablePart.class) {
        indices.add(i);
      }
    }
    return indices;
  }

  private static void parseVariables(LinkedList<Part> parseResult) {
    for (int i = 0; i < parseResult.size(); ++i) {
      Part p = parseResult.get(i);
      if (p.getClass() == UnparsedPart.class) {
        List<Part> parts = parseVariables((UnparsedPart) p);
        parseResult.remove(i);
        parseResult.addAll(i, parts);
      }
    }
  }

  private static List<Part> parseVariables(UnparsedPart unparsed) {
    String contents = unparsed.getContents();
    int offset = unparsed.start();
    List<Part> parts = new ArrayList<>();
    Matcher matcher = REGEX_VARIABLE.matcher(contents);
    int end = 0;
    while (matcher.find()) {
      int start = matcher.start();
      if (start > end) {
        String text = contents.substring(end, start);
        parts.add(new TextPart(text, end + offset, start + offset));
      }
      EscapeType et = EscapeType.parse(matcher.group(2));
      String name = matcher.group(3);
      end = matcher.end();
      parts.add(new VariablePart(et, name, start + offset, end + offset));
    }
    if (end < contents.length()) {
      String text = contents.substring(end);
      parts.add(new TextPart(text, end + offset, contents.length() + offset));
    }
    return parts;
  }

  // Replaces "<!-- ~%myVar% -->" with "~%myVar%", after which there is no longer any difference
  // between "hidden" and "visible" variables, and they can all be processed further in the same
  // way.
  private static String unhideVariables(String template) {
    Matcher matcher = REGEX_HIDDEN_VAR.matcher(template);
    template = matcher.replaceAll(r -> r.group(1));
    return template;
  }

  private static String unhideTemplates(String template) {
    Matcher matcher = REGEX_HIDDEN_NESTED_TMPL.matcher(template);
    template = matcher.replaceAll(r -> r.group(1));
    return template;
  }

  private static String removeComments(String template) {
    template = REGEX_COMMENT_LINE.matcher(template).replaceAll("");
    template = REGEX_COMMENT_BLOCK.matcher(template).replaceAll("");
    return template;
  }
}
