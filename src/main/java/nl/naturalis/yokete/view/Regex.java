package nl.naturalis.yokete.view;

import java.util.regex.Pattern;
import static nl.naturalis.common.StringMethods.concat;
import static java.util.regex.Pattern.compile;

class Regex {

  // Equivalent to prefixing the regular expression with "(?ms)"
  private static final int MS_MODIFIERS = Pattern.MULTILINE | Pattern.DOTALL;

  // GENERIC REGULAR EXPRESSIONS:

  private static final String WHITESPACE = "\\s*";
  private static final String NON_BREAKING_SPACE = "[ \\t]*";
  private static final String DOT = "\\.";
  private static final String WHATEVER = ".*";
  private static final String LINE_START = "^.*";
  private static final String LINE_END = ".*$";
  private static final String HTML_COMMENT_START = Pattern.quote("<!--");
  private static final String HTML_COMMENT_END = Pattern.quote("-->");

  // NAME COMPONENTS:

  private static final String ESCAPE_TYPE = "((text|html|js):)?";
  private static final String NAME = "[a-zA-Z][a-zA-Z0-9_]*";
  private static final String PATH = concat("(", NAME, "(", DOT, NAME, ")*)");

  // ACTUALLY USED TO PARSE TEMPLATES:

  // ~%((text|html|js):)?([a-zA-Z][a-zA-Z0-9_]*(\.[a-zA-Z][a-zA-Z0-9_]*)*)%
  // esc type: group 2
  // var name: group 3
  static final String VARIABLE = concat("~%", ESCAPE_TYPE, PATH, "%");

  static final String HIDDEN_VAR =
      concat(HTML_COMMENT_START, WHITESPACE, "(", VARIABLE, ")", WHITESPACE, HTML_COMMENT_END);

  static final String NESTED_TEMPLATE =
      concat("~%%beginTemplate:(", NAME, ")%(", WHATEVER, ")~%%endTemplate%");

  static final String HIDDEN_NESTED_TMPL =
      concat(
          HTML_COMMENT_START, WHITESPACE, "(", NESTED_TEMPLATE, ")", WHITESPACE, HTML_COMMENT_END);

  static final String COMMENT_LINE =
      concat(
          LINE_START,
          HTML_COMMENT_START,
          NON_BREAKING_SPACE,
          "~%%comment%",
          NON_BREAKING_SPACE,
          HTML_COMMENT_END,
          LINE_END);

  static final String COMMENT_BLOCK =
      concat(
          HTML_COMMENT_START,
          WHITESPACE,
          "~%%beginComment%",
          WHATEVER,
          "~%%endComment%",
          WHITESPACE,
          HTML_COMMENT_END);

  static final Pattern REGEX_VARIABLE = compile(VARIABLE);
  static final Pattern REGEX_HIDDEN_VAR = compile(HIDDEN_VAR);
  static final Pattern REGEX_NESTED_TEMPLATE = compile(NESTED_TEMPLATE, MS_MODIFIERS);
  static final Pattern REGEX_HIDDEN_NESTED_TMPL = compile(HIDDEN_NESTED_TMPL, MS_MODIFIERS);
  static final Pattern REGEX_COMMENT_LINE = compile(COMMENT_LINE);
  static final Pattern REGEX_COMMENT_BLOCK = compile(COMMENT_BLOCK, MS_MODIFIERS);
}
