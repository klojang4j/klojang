package org.klojang.template;

import java.util.Set;
import java.util.regex.Pattern;
import org.klojang.KlojangRTException;
import nl.naturalis.common.check.Check;
import static java.util.regex.Pattern.compile;
import static java.util.stream.Collectors.toSet;
import static nl.naturalis.common.check.CommonChecks.blank;
import static nl.naturalis.common.check.CommonChecks.equalTo;

class Regex {

  private static final String SYSPROP_BASE = "org.klojang.template.parser";
  private static final String SYSPROP_VS = SYSPROP_BASE + "varStart";
  private static final String SYSPROP_VE = SYSPROP_BASE + "varEnd";
  private static final String SYSPROP_TS = SYSPROP_BASE + "tmplStart";
  private static final String SYSPROP_TE = SYSPROP_BASE + "tmplEnd";

  private static final String ERR_ILLEGAL_VAL = "Illegal value for system property %s: \"%s\"";
  private static final String ERR_IDENTICAL =
      "Values of varStart and tmplStart must not be identical";

  static final String VAR_START = System.getProperty(SYSPROP_VS, "~%");
  static final String VAR_END = System.getProperty(SYSPROP_VE, "%");
  static final String TMPL_START = System.getProperty(SYSPROP_TS, "~%%");
  static final String TMPL_END = System.getProperty(SYSPROP_TE, "%");

  static {
    checkThat(VAR_START).isNot(blank(), ERR_ILLEGAL_VAL, VAR_START);
    checkThat(VAR_END).isNot(blank(), ERR_ILLEGAL_VAL, VAR_END);
    checkThat(TMPL_START)
        .isNot(blank(), ERR_ILLEGAL_VAL, TMPL_START)
        .isNot(equalTo(), VAR_START, ERR_IDENTICAL);
    checkThat(TMPL_END).isNot(blank(), ERR_ILLEGAL_VAL, TMPL_END);
  }

  private static final String VS = quote(VAR_START);
  private static final String VE = quote(VAR_END);
  private static final String TS = quote(TMPL_START);
  private static final String TE = quote(TMPL_END);

  // Used for group names and template names, *not* for variable names
  private static final String NAME = "([a-zA-Z_]\\w*)";

  static final String VARIABLE = VS + "(" + NAME + ":)?(.+?)" + VE;

  static final String VARIABLE_CMT = "<!--\\s*" + VARIABLE + "\\s*-->";

  static final String INLINE_TMPL = TS + "begin:" + NAME + TE + "(.*?)" + TS + "end:\\1" + TE;

  static final String INLINE_TMPL_CMT = "<!--\\s*" + INLINE_TMPL + "\\s*-->";

  static final String INCLUDED_TMPL = TS + "include:(" + NAME + ":)?(.+?)" + TE;

  static final String INCLUDED_TMPL_CMT = "<!--\\s*" + INCLUDED_TMPL + "\\s*-->";

  /**
   * end-of-template sequence for inline templates. We don't use this regular expression for regular
   * parsing, but we do use it for error reporting ("dangling end-of-template").
   */
  static final String EOT = TS + "end:" + NAME + TE;

  /**
   * By itself not used for regular parsing, but we do use it for error reporting ("ditch block not
   * terminated").
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
    System.out.println("EOT ............: " + REGEX_EOT);
    System.out.println("DITCH_TOKEN: ...: " + REGEX_DITCH_TOKEN);
  }

  private static Check<String, KlojangRTException> checkThat(String sysprop) {
    return Check.on(KlojangRTException::new, sysprop);
  }

  private static String quote(String token) {
    String special = "\\^$.|?*+()[]{}";
    Set<Integer> specialChars = special.codePoints().mapToObj(Integer::valueOf).collect(toSet());
    Set<Integer> tokenChars = token.codePoints().mapToObj(Integer::valueOf).collect(toSet());
    return tokenChars.stream().anyMatch(specialChars::contains) ? Pattern.quote(token) : token;
  }
}
