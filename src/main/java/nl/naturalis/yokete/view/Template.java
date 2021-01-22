package nl.naturalis.yokete.view;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import nl.naturalis.common.collection.IntList;
import static java.util.stream.Collectors.toList;
import static nl.naturalis.yokete.view.Regex.*;

/**
 * A {@code Template} parses a template file and breaks it up in parts containing variables and
 * parts containing literal text. It also removes the lines and blocks of text marked as ignorable.
 * You should cache and reuse a {@code Template} instance for a particular template files as
 * instantiating a {@code Template} is expensive.
 *
 * @author Ayco Holleman
 */
public class Template {

  /*
   * The parts that the template is split into. Some parts will contain literal text, other parts will
   * contain the name of a variable
   */
  private final List<String> parts = new ArrayList<>();

  /*
   * Contains the array indices of the elements in the parts List that contain the name of a
   * variable.
   */
  private final IntList varIndices = new IntList();

  public Template(String template) {
    template = removeComments(template);
    template = unhideVariables(template);
    template = unhideTemplates(template);
    Matcher matcher = REGEX_VARIABLE.matcher(template);
    int partCounter = 0;
    int offset = 0;
    while (matcher.find()) {
      int start = matcher.start();
      if (start > offset) {
        parts.add(template.substring(offset, start));
        partCounter++;
      }
      parts.add(matcher.group(1));
      varIndices.add(partCounter++);
      offset = matcher.end();
    }
    if (offset < template.length()) {
      parts.add(template.substring(offset));
    }
  }

  private LinkedList<Part<?>> extractSubTemplates(String template) {
    LinkedList<Part<?>> parts = new LinkedList<>();
    Matcher matcher = REGEX_NESTED_TEMPLATE.matcher(template);
    int offset = 0;
    while (matcher.find()) {
      int start = matcher.start();
      if (start > offset) {
        String parseLater = template.substring(offset, start);
        parts.add(new LiteralPart(parseLater));
      }
      String name = matcher.group(1);
      String src = matcher.group(2);
      parts.add(new TemplatePart(name, new Template(src)));
      offset = matcher.end();
    }
    if (offset < template.length()) {
      String parseLater = template.substring(offset);
      parts.add(new LiteralPart(parseLater));
    }
    return parts;
  }

  private LinkedList<Part<?>> extractVariables(LinkedList<Part<?>> parseResult) {
    for (int i = 0; i < parseResult.size(); ++i) {
      Part<?> p = parseResult.get(i);
      if (p.getClass() == LiteralPart.class) {}
    }

    return parseResult;
  }

  private List<Part<?>> extractVariables(String template) {
    List<Part<?>> parts = new ArrayList<>();
    Matcher matcher = REGEX_VARIABLE.matcher(template);
    int offset = 0;
    while (matcher.find()) {
      int start = matcher.start();
      if (start > offset) {
        String text = template.substring(offset, start);
        parts.add(new LiteralPart(text));
      }
      String varName = matcher.group(1);
      parts.add(new VariablePart(varName));
      offset = matcher.end();
    }
    if (offset < template.length()) {
      String text = template.substring(offset);
      parts.add(new LiteralPart(text));
    }
    return parts;
  }

  /**
   * Returns the parts that the template is split into. Some parts will contain literal text, other
   * parts will contain the name of a variable.
   *
   * @return The constuent parts of the template file
   */
  public List<String> getParts() {
    return new ArrayList<>(parts);
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
    return varIndices.stream().mapToObj(parts::get).collect(toList());
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
