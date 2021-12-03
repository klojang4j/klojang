package org.klojang.template;

import java.util.Set;
import java.util.regex.Pattern;
import org.klojang.KlojangRTException;
import nl.naturalis.common.check.Check;
import static java.util.regex.Pattern.compile;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toSet;
import static nl.naturalis.common.check.CommonChecks.blank;
import static nl.naturalis.common.check.CommonChecks.equalTo;

class Regex {

  private static final String SYSPROP_BASE = "org.klojang.template";
  private static final String SYSPROP_VS = SYSPROP_BASE + "varStart";
  private static final String SYSPROP_VE = SYSPROP_BASE + "varEnd";
  private static final String SYSPROP_TS = SYSPROP_BASE + "tmplStart";
  private static final String SYSPROP_TE = SYSPROP_BASE + "tmplEnd";

  private static final String ERR_ILLEGAL_VAL = "Illegal value for system property %s: \"%s\"";
  private static final String ERR_IDENTICAL = "Values for %s and %s must not be identical";

  static final String VAR_START = System.getProperty(SYSPROP_VS, "~%");
  static final String VAR_END = System.getProperty(SYSPROP_VE, "%");
  static final String TMPL_START = System.getProperty(SYSPROP_TS, "~%%");
  static final String TMPL_END = System.getProperty(SYSPROP_TE, "%");

  static {
    checkThat(VAR_START).isNot(blank(), ERR_ILLEGAL_VAL, VAR_START);
    checkThat(VAR_END).isNot(blank(), ERR_ILLEGAL_VAL, VAR_END);
    checkThat(TMPL_START)
        .isNot(blank(), ERR_ILLEGAL_VAL, TMPL_START)
        .isNot(equalTo(), VAR_START, ERR_IDENTICAL, SYSPROP_VS, SYSPROP_TS);
    checkThat(TMPL_END).isNot(blank(), ERR_ILLEGAL_VAL, TMPL_END);
  }

  private static final String SPECIAL_CHARS = "\\^$.|?*+()[]{}";

  private static final String VS = Pattern.quote(VAR_START);
  private static final String VE = Pattern.quote(VAR_END);
  private static final String TS = Pattern.quote(TMPL_START);
  private static final String TE = Pattern.quote(TMPL_END);

  // Used for variable group names and template names
  private static final String NAME = "([a-zA-Z_]\\w*)";

  static final String VARIABLE = VS + "(" + NAME + ":)?(.+?)" + VE;

  static final String VARIABLE_CMT = "<!--\\s*(" + VARIABLE + ")\\s*-->";

  static final String INLINE_TMPL = rgxInlineTmpl(1);

  static final String INLINE_TMPL_CMT = "<!--\\s*(" + rgxInlineTmpl(2) + ")\\s*-->";

  static final String INCLUDED_TMPL = TS + "include:(" + NAME + ":)?(.+?)" + TE;

  /**
   * end-of-template sequence for inline templates. We don't use this regular expression for regular
   * parsing, but we do use it for error reporting ("dangling end-of-template").
   */
  static final String EOT = TS + "end:" + NAME + TE;

  static final String INCLUDED_TMPL_CMT = "<!--\\s*(" + INCLUDED_TMPL + ")\\s*-->";

  /**
   * By itself we don't use this regular expression for regular parsing, but we o use it for error
   * reporting ("ditch block not terminated").
   */
  static final String DITCH_TOKEN = "<!--%%-->";

  static final String DITCH_BLOCK = "(?ms)" + DITCH_TOKEN + ".*?" + DITCH_TOKEN;

  // Equivalent to prefixing the regular expression with "(?ms)"
  private static final int MS_MODIFIERS = Pattern.MULTILINE | Pattern.DOTALL;

  static final Pattern REGEX_VARIABLE = compile(VARIABLE);
  static final Pattern REGEX_VARIABLE_CMT = compile(VARIABLE_CMT);
  static final Pattern REGEX_INLINE_TMPL = compile(INLINE_TMPL, MS_MODIFIERS);
  static final Pattern REGEX_INLINE_TMPL_CMT = compile(INLINE_TMPL_CMT, MS_MODIFIERS);
  static final Pattern REGEX_INCLUDED_TMPL = compile(INCLUDED_TMPL);
  static final Pattern REGEX_INCLUDED_TMPL_CMT = compile(INCLUDED_TMPL_CMT);
  static final Pattern REGEX_DITCH_BLOCK = compile(DITCH_BLOCK);
  static final Pattern REGEX_DITCH_TOKEN = compile(DITCH_TOKEN);
  static final Pattern REGEX_EOT = compile(EOT);

  static void printAll() {
    System.out.println("VARIABLE .......: " + REGEX_VARIABLE);
    System.out.println("VARIABLE_CMT ...: " + REGEX_VARIABLE_CMT);
    System.out.println("NESTED .........: " + REGEX_INLINE_TMPL);
    System.out.println("NESTED_CMT .....: " + REGEX_INLINE_TMPL_CMT);
    System.out.println("INCLUDE ........: " + REGEX_INCLUDED_TMPL);
    System.out.println("INCLUDE_CMT ....: " + REGEX_INCLUDED_TMPL_CMT);
    System.out.println("DITCH_BLOCK: ...: " + REGEX_DITCH_BLOCK);
    System.out.println("TMPL_END .......: " + REGEX_EOT);
    System.out.println("DITCH_TOKEN: ...: " + REGEX_DITCH_TOKEN);
  }

  private static final String rgxInlineTmpl(int groupRef) {
    return TS + "begin:" + NAME + TE + "(.*?)" + TS + "end:\\" + groupRef + TE;
  }

  private static Check<String, KlojangRTException> checkThat(String sysprop) {
    return Check.on(KlojangRTException::new, sysprop);
  }

  /**
   * This more severely limits which characters are allowed in a variable name, namely anything as
   * long as it does not contain _any_ of the characters in _any_ of the tokens.
   */
  @SuppressWarnings("unused")
  private static String forbiddenChars() {
    Set<Integer> special = SPECIAL_CHARS.codePoints().mapToObj(Integer::valueOf).collect(toSet());
    return (":" + VAR_START + VAR_END + TMPL_START + TMPL_END)
        .codePoints()
        .mapToObj(Integer::valueOf)
        .collect(toSet())
        .stream()
        .map(i -> special.contains(i) ? "\\" + Character.toString(i) : Character.toString(i))
        .collect(joining());
  }
}
