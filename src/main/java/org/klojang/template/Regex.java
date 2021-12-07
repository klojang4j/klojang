package org.klojang.template;

import java.util.Set;
import java.util.regex.Pattern;
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

  private static Regex instance;

  static Regex of() throws ParseException {
    if (instance == null) {
      return instance = new Regex();
    }
    return instance;
  }

  // By itself used only for error reporting
  static final String PLACEHOLDER_TAG = "<!--%-->";

  final Pattern variable;
  final Pattern cmtVariable;
  final Pattern beginTag; // error reporting only
  final Pattern endTag; // error reporting only
  final Pattern inlineTemplate;
  final Pattern cmtInlineTemplate;
  final Pattern includedTemplate;
  final Pattern cmtIncludedTemplate;
  final Pattern ditchTag; // error reporting only
  final Pattern ditchBlock;
  final Pattern placeholder;

  private Regex() throws ParseException {

    checkSysProps();

    String vStart = quote(VAR_START);
    String vEnd = quote(VAR_END);
    String tStart = quote(TMPL_START);
    String tEnd = quote(TMPL_END);

    // Used for group name prefixes and template names, *not* for variable names
    String name = "([a-zA-Z_]\\w*)";

    String cmtStart = "<!--\\s*";

    String cmtEnd = "\\s*-->";

    String ptnVariable = vStart + "(" + name + ":)?(.+?)" + vEnd;

    String ptnCmtVariable = cmtStart + ptnVariable + cmtEnd;

    String ptnInlineBegin = tStart + "begin:" + name + tEnd;

    String ptnInlineEnd = tStart + "end:" + name + tEnd;

    String ptnInlineTmpl = ptnInlineBegin + "(.*?)" + tStart + "end:\\1" + tEnd;

    String ptnCmtInlineTmpl =
        cmtStart
            + ptnInlineBegin
            + cmtEnd
            + "(.*?)"
            + cmtStart
            + tStart
            + "end:\\1"
            + tEnd
            + cmtEnd;

    String ptnIncludedTmpl = tStart + "include:(" + name + ":)?(.+?)" + tEnd;

    String ptnCmtIncludedTmpl = cmtStart + ptnIncludedTmpl + cmtEnd;

    String ptnDitchToken = "<!--%%.*?-->";

    String ptnDitchBlock = ptnDitchToken + ".*?" + ptnDitchToken;

    String ptnPlaceholder = PLACEHOLDER_TAG + ".*?" + PLACEHOLDER_TAG;

    // Equivalent to prefixing the regular expression with "(?ms)"
    int msModifiers = Pattern.MULTILINE | Pattern.DOTALL;

    this.variable = compile(ptnVariable);
    this.cmtVariable = compile(ptnCmtVariable);
    this.beginTag = compile(ptnInlineBegin);
    this.endTag = compile(ptnInlineEnd);
    this.inlineTemplate = compile(ptnInlineTmpl, msModifiers);
    this.cmtInlineTemplate = compile(ptnCmtInlineTmpl, msModifiers);
    this.includedTemplate = compile(ptnIncludedTmpl);
    this.cmtIncludedTemplate = compile(ptnCmtIncludedTmpl);
    this.ditchTag = compile(ptnDitchToken);
    this.ditchBlock = compile(ptnDitchBlock, msModifiers);
    this.placeholder = compile(ptnPlaceholder, msModifiers);
  }

  private static void checkSysProps() throws ParseException {
    checkThat(VAR_START).isNot(blank(), ERR_ILLEGAL_VAL, VAR_START);
    checkThat(VAR_END).isNot(blank(), ERR_ILLEGAL_VAL, VAR_END);
    checkThat(TMPL_START)
        .isNot(blank(), ERR_ILLEGAL_VAL, TMPL_START)
        .isNot(equalTo(), VAR_START, ERR_IDENTICAL);
    checkThat(TMPL_END).isNot(blank(), ERR_ILLEGAL_VAL, TMPL_END);
  }

  void printAll() {
    System.out.println("VARIABLE .......: " + variable);
    System.out.println("VARIABLE_CMT ...: " + cmtVariable);
    System.out.println("NESTED .........: " + inlineTemplate);
    System.out.println("NESTED_CMT .....: " + cmtInlineTemplate);
    System.out.println("INCLUDE ........: " + includedTemplate);
    System.out.println("INCLUDE_CMT ....: " + cmtIncludedTemplate);
    System.out.println("DITCH_BLOCK: ...: " + ditchBlock);
    System.out.println("PLACEHOLDER: ...: " + placeholder);
  }

  private static Check<String, ParseException> checkThat(String sysprop) {
    return Check.on(ParseException::new, sysprop);
  }

  private static String quote(String token) {
    String special = "\\^$.|?*+()[]{}";
    Set<Integer> specialChars = special.codePoints().mapToObj(Integer::valueOf).collect(toSet());
    Set<Integer> tokenChars = token.codePoints().mapToObj(Integer::valueOf).collect(toSet());
    return tokenChars.stream().anyMatch(specialChars::contains) ? Pattern.quote(token) : token;
  }
}
