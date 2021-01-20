package nl.naturalis.yokete.view;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import nl.naturalis.common.collection.IntList;
import static java.util.stream.Collectors.toList;
import static nl.naturalis.common.StringMethods.concat;

/**
 * A {@code Template} parses a template file and breaks it up in parts containing variables and
 * parts containing literal text. It also removes the lines and blocks of text marked as ignorable.
 * You should cache and reuse a {@code Template} instance for a particular template files as
 * instantiating a {@code Template} is expensive.
 *
 * @author Ayco Holleman
 */
public class Template {

  private static final String WHITESPACE = "\\s*";
  private static final String NON_BREAKING_SPACE = "[ \\t]*";
  private static final String WHATEVER = ".*";
  private static final String LINE_START = "^.*";
  private static final String LINE_END = ".*$";

  private static final String PATH_ELEMENT = "[a-zA-Z_][a-zA-Z0-9_]*";
  private static final String INDEX = "\\d+";
  private static final String EITHER = concat(PATH_ELEMENT, "|", INDEX);
  private static final String PATH = concat(EITHER, "(\\.", EITHER, ")*");
  private static final String VARIABLE = concat("~%(", PATH, ")%");

  private static final String HTML_COMMENT_START = Pattern.quote("<!--");
  private static final String HTML_COMMENT_END = Pattern.quote("-->");

  // Variable within an HTML comment. This will make the unprocessed template render nicely in a
  // browser, if that's important.
  private static final String HIDDEN_VARIABLE =
      concat(HTML_COMMENT_START, WHITESPACE, VARIABLE, WHITESPACE, HTML_COMMENT_END);

  private static final String IGNORE_LINE =
      concat(
          LINE_START,
          HTML_COMMENT_START,
          NON_BREAKING_SPACE,
          "~%%ignore%",
          NON_BREAKING_SPACE,
          HTML_COMMENT_END,
          LINE_END);

  private static final String IGNORE_BLOCK =
      concat(
          "(?ms)", // modifiers, equivalent to Pattern.MULTILINE | Pattern.DOTALL
          HTML_COMMENT_START,
          WHITESPACE,
          "~%%beginIgnore%",
          WHATEVER,
          "~%%endIgnore%",
          WHITESPACE,
          HTML_COMMENT_END);

  private static final Pattern REGEX_VARIABLE = Pattern.compile(VARIABLE);
  private static final Pattern REGEX_HIDDEN_VAR = Pattern.compile(HIDDEN_VARIABLE);
  private static final Pattern REGEX_IGNORE_LINE = Pattern.compile(IGNORE_LINE);
  private static final Pattern REGEX_IGNORE_BLOCK = Pattern.compile(IGNORE_BLOCK);

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
    template = removeDesign(template);
    template = unhideVariables(template);
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
    if (varIndices.isEmpty()) {
      throw new RenderException("No variables found in template");
    }
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
  // between "hidden" and "non-hidden" variables, and they can all be processed further in the same
  // way.
  private static String unhideVariables(String template) {
    Matcher matcher = REGEX_HIDDEN_VAR.matcher(template);
    template = matcher.replaceAll(r -> "~%" + r.group(1) + "%");
    return template;
  }

  private static String removeDesign(String template) {
    template = REGEX_IGNORE_LINE.matcher(template).replaceAll("");
    template = REGEX_IGNORE_BLOCK.matcher(template).replaceAll("");
    return template;
  }
}
