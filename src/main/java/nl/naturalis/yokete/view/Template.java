package nl.naturalis.yokete.view;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import nl.naturalis.common.collection.IntList;
import static nl.naturalis.common.StringMethods.concat;
import static java.util.stream.Collectors.*;

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

  private static final String DESIGN_LINE =
      concat(
          LINE_START,
          HTML_COMMENT_START,
          NON_BREAKING_SPACE,
          "~%%design%",
          NON_BREAKING_SPACE,
          HTML_COMMENT_END,
          LINE_END);

  private static final String DESIGN_BLOCK =
      concat(
          "(?ms)", // modifiers, equivalent to Pattern.MULTILINE | Pattern.DOTALL
          HTML_COMMENT_START,
          WHITESPACE,
          "~%%beginDesign%",
          WHATEVER,
          "~%%endDesign%",
          WHITESPACE,
          HTML_COMMENT_END);

  private static final Pattern REGEX_VARIABLE = Pattern.compile(VARIABLE);
  private static final Pattern REGEX_HIDDEN_VAR = Pattern.compile(HIDDEN_VARIABLE);
  private static final Pattern REGEX_IGNORE_LINE = Pattern.compile(DESIGN_LINE);
  private static final Pattern REGEX_IGNORE_BLOCK = Pattern.compile(DESIGN_BLOCK);

  /*
   * The parts that the template is split into. Some parts will contain just text, other parts will
   * contain the name of a variable
   */
  private final List<String> parts = new ArrayList<>();

  /*
   * Contains the array indices of the elements in the parts List that contain the name of a
   * variable
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

  public List<String> getParts() {
    return new ArrayList<>(parts);
  }

  public IntList getVarIndices() {
    return new IntList(varIndices);
  }

  public int countVariables() {
    return varIndices.size();
  }

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
