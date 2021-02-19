package nl.naturalis.yokete.view;

import java.util.Set;
import java.util.regex.Pattern;
import nl.naturalis.yokete.YoketeRuntimeException;
import static java.util.regex.Pattern.compile;
import static java.util.stream.Collectors.*;
import static nl.naturalis.common.ObjectMethods.ifNotNull;

class Regex {

  private static final String SYSPROP_BASE = "nl.naturalis.yokete.";
  private static final String SYSPROP_VS = SYSPROP_BASE + "varStart";
  private static final String SYSPROP_TS = SYSPROP_BASE + "tmplStart";
  private static final String SYSPROP_NE = SYSPROP_BASE + "nameEnd";

  // Identifier start (variables); default: "~%"
  static final String VAR_START = getVarStart();
  // Identifier start (nested templates); default: "~%%"
  static final String TMPL_START = getTmplStart();
  // Identifier end (variables and nested templates); default: "%"
  static final String NAME_END = getIdentifierEnd();

  static {
    if (VAR_START.isBlank()) throw badSysProp(SYSPROP_VS, VAR_START);
    if (TMPL_START.isBlank()) throw badSysProp(SYSPROP_TS, TMPL_START);
    if (NAME_END.isBlank()) throw badSysProp(SYSPROP_NE, NAME_END);
    if (VAR_START.equals(TMPL_START)) throw badSysProp(SYSPROP_TS, TMPL_START);
    if (VAR_START.equals(NAME_END)) throw badSysProp(SYSPROP_NE, NAME_END);
    if (TMPL_START.equals(NAME_END)) throw badSysProp(SYSPROP_NE, NAME_END);
  }

  private static final String SPECIAL_CHARS = "\\^$.|?*+()[]{}";

  private static final String VS = getEscapedVarStart();
  private static final String TS = getEscapedTmplStart();
  private static final String NE = getEscapedIdentifierEnd();

  // A name is any sequence of one or more characters, excluding colon and percentage sign
  private static final String NAME = "([^" + forbiddenChars() + "]+)";

  /* private */
  static final String VARIABLE = VS + "(" + NAME + ":)?" + NAME + NE;

  /* private */
  static final String HIDDEN_VAR = "<!--\\s*(" + VARIABLE + ")\\s*-->";

  /* private */
  static final String TEMPLATE = rgxTemplate(1);

  /* private */
  static final String HIDDEN_TMPL = "<!--\\s*(" + rgxTemplate(2) + ")\\s*-->";

  /* private */
  static final String INCLUDE = TS + "include:(" + NAME + ":)?" + NAME + NE;

  /* private */
  static final String HIDDEN_INCLUDE = "<!--\\s*(" + INCLUDE + ")\\s*-->";

  /* private */
  static final String DITCH_BLOCK = "<!--%%-->.*<!--%%-->";

  // Equivalent to prefixing the regular expression with "(?ms)"
  private static final int MS_MODIFIERS = Pattern.MULTILINE | Pattern.DOTALL;

  static final Pattern REGEX_VARIABLE = compile(VARIABLE);
  static final Pattern REGEX_HIDDEN_VAR = compile(HIDDEN_VAR);
  static final Pattern REGEX_TEMPLATE = compile(TEMPLATE, MS_MODIFIERS);
  static final Pattern REGEX_HIDDEN_TMPL = compile(HIDDEN_TMPL, MS_MODIFIERS);
  static final Pattern REGEX_INCLUDE = compile(INCLUDE);
  static final Pattern REGEX_HIDDEN_INCLUDE = compile(HIDDEN_INCLUDE);
  static final Pattern REGEX_DITCH_BLOCK = compile(DITCH_BLOCK, MS_MODIFIERS);

  static void printAll() {
    System.out.println("VARIABLE ........: " + REGEX_VARIABLE);
    System.out.println("HIDDEN_VAR ......: " + REGEX_HIDDEN_VAR);
    System.out.println("TEMPLATE ........: " + REGEX_TEMPLATE);
    System.out.println("HIDDEN_TMPL .....: " + REGEX_HIDDEN_TMPL);
    System.out.println("IMPORT ..........: " + REGEX_INCLUDE);
    System.out.println("HIDDEN_IMPORT ...: " + REGEX_HIDDEN_INCLUDE);
    System.out.println("DITCH_BLOCK: ....: " + REGEX_DITCH_BLOCK);
  }

  private static final String rgxTemplate(int groupRef) {
    return TS + "begin:" + NAME + NE + "(.*)" + TS + "end:\\" + groupRef + NE;
  }

  private static String getVarStart() {
    return System.getProperty(SYSPROP_VS, "~%");
  }

  private static String getTmplStart() {
    return System.getProperty(SYSPROP_TS, "~%%");
  }

  private static String getIdentifierEnd() {
    return System.getProperty(SYSPROP_NE, "%");
  }

  private static String getEscapedVarStart() {
    return ifNotNull(System.getProperty(SYSPROP_VS), Pattern::quote, "~%");
  }

  private static String getEscapedTmplStart() {
    return ifNotNull(System.getProperty(SYSPROP_TS), Pattern::quote, "~%%");
  }

  private static String getEscapedIdentifierEnd() {
    return ifNotNull(System.getProperty(SYSPROP_TS), Pattern::quote, "%");
  }

  private static YoketeRuntimeException badSysProp(String name, String val) {
    String fmt = "Illegal value for %s: \"%s\"";
    return new YoketeRuntimeException(String.format(fmt, name, val));
  }

  private static String forbiddenChars() {
    Set<Integer> special = SPECIAL_CHARS.codePoints().mapToObj(Integer::valueOf).collect(toSet());
    return (":" + VAR_START + TMPL_START + NAME_END)
        .codePoints()
        .mapToObj(Integer::valueOf)
        .collect(toSet())
        .stream()
        .map(i -> special.contains(i) ? "\\" + Character.toString(i) : Character.toString(i))
        .collect(joining());
  }
}
