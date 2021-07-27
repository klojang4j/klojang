package org.klojang.template;

import java.util.Set;
import java.util.regex.Pattern;
import org.klojang.KlojangRTException;
import static java.util.regex.Pattern.compile;
import static java.util.stream.Collectors.*;
import static nl.naturalis.common.ObjectMethods.ifNotNull;

class Regex {

  private static final String SYSPROP_BASE = "org.klojang.";
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
  static final String VARIABLE_CMT = "<!--\\s*(" + VARIABLE + ")\\s*-->";

  /* private */
  static final String NESTED = rgxTemplate(1);

  /* private */
  static final String NESTED_CMT = "<!--\\s*(" + rgxTemplate(2) + ")\\s*-->";

  /* private  - only used for error reporting (dangling end-of-template) */
  static final String TMPL_END = TS + "end:" + NAME + NE;

  /* private */
  static final String INCLUDE = TS + "include:(" + NAME + ":)?" + NAME + NE;

  /* private */
  static final String INCLUDE_CMT = "<!--\\s*(" + INCLUDE + ")\\s*-->";

  /* private  - only used for error reporting (ditch block not terminated) */
  static final String DITCH_TOKEN = "<!--%%-->";

  /* private */
  /*
   * m: multi-line ^ and $ match begin/end of line, not of entire string
   * s: dot also matches newline chars
   * U: un-greedy; used by ditch block to find next rather than last ditch token
   *    following current ditch token
   *
   * Actually, however, the U modifier does not work, at least not as expected.
   * The regex below __does__ the expected and desired thing on regex101.com,
   * but not in Java's regex implementation. Therefore, for now, we'll just use
   * the ditch token itself to programmatically plough through the source and
   * find pairs of consecutive ditch tokens. Eveyrhing inside them (as well as
   * the ditch tokens themselves) is going to be purged from the template.
   */
  static final String DITCH_BLOCK = "(?msU)" + DITCH_TOKEN + ".*" + DITCH_TOKEN;

  // Equivalent to prefixing the regular expression with "(?ms)"
  private static final int MS_MODIFIERS = Pattern.MULTILINE | Pattern.DOTALL;

  static final Pattern REGEX_VARIABLE = compile(VARIABLE);
  static final Pattern REGEX_VARIABLE_CMT = compile(VARIABLE_CMT);
  static final Pattern REGEX_NESTED = compile(NESTED, MS_MODIFIERS);
  static final Pattern REGEX_NESTED_CMT = compile(NESTED_CMT, MS_MODIFIERS);
  static final Pattern REGEX_INCLUDE = compile(INCLUDE);
  static final Pattern REGEX_INCLUDE_CMT = compile(INCLUDE_CMT);
  static final Pattern REGEX_DITCH_BLOCK = compile(DITCH_BLOCK);
  static final Pattern REGEX_DITCH_TOKEN = compile(DITCH_TOKEN);
  // ERROR REPORTING PURPOSES ONLY:
  static final Pattern REGEX_TMPL_END = compile(TMPL_END);

  static void printAll() {
    System.out.println("VARIABLE .......: " + REGEX_VARIABLE);
    System.out.println("VARIABLE_CMT ...: " + REGEX_VARIABLE_CMT);
    System.out.println("NESTED .........: " + REGEX_NESTED);
    System.out.println("NESTED_CMT .....: " + REGEX_NESTED_CMT);
    System.out.println("INCLUDE ........: " + REGEX_INCLUDE);
    System.out.println("INCLUDE_CMT ....: " + REGEX_INCLUDE_CMT);
    System.out.println("DITCH_BLOCK: ...: " + REGEX_DITCH_BLOCK);
    System.out.println("TMPL_END .......: " + REGEX_TMPL_END);
    System.out.println("DITCH_TOKEN: ...: " + REGEX_DITCH_TOKEN);
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

  private static KlojangRTException badSysProp(String name, String val) {
    String fmt = "Illegal value for %s: \"%s\"";
    return new KlojangRTException(String.format(fmt, name, val));
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
