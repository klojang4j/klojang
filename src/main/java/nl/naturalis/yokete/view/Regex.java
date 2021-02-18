package nl.naturalis.yokete.view;

import java.util.regex.Pattern;
import static java.util.regex.Pattern.compile;
import static nl.naturalis.common.ObjectMethods.*;

class Regex {

  // Equivalent to prefixing the regular expression with "(?ms)"
  private static final int MS_MODIFIERS = Pattern.MULTILINE | Pattern.DOTALL;

  // A name is any sequence of one or more characters, excluding colon, tilde and percentage sign
  private static final String NAME = "([^%:~]+)";
  private static final String VS = getVarStart();
  private static final String TS = getTmplStart();
  private static final String NE = getNameEnd();

  // escape type: group 2; var name: group 3
  static final String VARIABLE = VS + "(" + NAME + ":)?" + NAME + NE;

  static final String HIDDEN_VAR = "<!--\\s*(" + VARIABLE + ")\\s*-->";

  static final String TEMPLATE = rgxTemplate(1);

  static final String HIDDEN_TMPL = "<!--\\s*(" + rgxTemplate(2) + ")\\s*-->";

  static final String IMPORT = TS + "import:(" + NAME + ":)?" + NAME + NE;

  static final String HIDDEN_IMPORT = "<!--\\s*(" + IMPORT + ")\\s*-->";

  // Everything in a template file that is inside a pair of <!--%%--> tokens is completely ignored
  // when rendering the template
  static final String DITCH_BLOCK = "<!--%%-->.*<!--%%-->";

  static final Pattern REGEX_VARIABLE = compile(VARIABLE);
  static final Pattern REGEX_HIDDEN_VAR = compile(HIDDEN_VAR);
  static final Pattern REGEX_TEMPLATE = compile(TEMPLATE, MS_MODIFIERS);
  static final Pattern REGEX_IMPORT = compile(IMPORT);
  static final Pattern REGEX_HIDDEN_IMPORT = compile(HIDDEN_IMPORT);
  static final Pattern REGEX_HIDDEN_TMPL = compile(HIDDEN_TMPL, MS_MODIFIERS);
  static final Pattern REGEX_DITCH_BLOCK = compile(DITCH_BLOCK, MS_MODIFIERS);

  private static final String rgxTemplate(int groupRef) {
    return TS + "begin:" + NAME + NE + "(.*)" + TS + "end:\\" + groupRef + NE;
  }

  private static String getVarStart() {
    return ifNotNull(System.getProperty("nl.naturalis.yokete.varStart"), Pattern::quote, "~%");
  }

  private static String getTmplStart() {
    return ifNotNull(System.getProperty("nl.naturalis.yokete.tmplStart"), Pattern::quote, "~%%");
  }

  private static String getNameEnd() {
    return ifNotNull(System.getProperty("nl.naturalis.yokete.nameEnd"), Pattern::quote, "%");
  }

  static void printAll() {
    System.out.println("VARIABLE ........: " + REGEX_VARIABLE);
    System.out.println("HIDDEN_VAR ......: " + REGEX_HIDDEN_VAR);
    System.out.println("TEMPLATE ........: " + REGEX_TEMPLATE);
    System.out.println("HIDDEN_TMPL .....: " + REGEX_HIDDEN_TMPL);
    System.out.println("IMPORT ..........: " + REGEX_IMPORT);
    System.out.println("HIDDEN_IMPORT ...: " + REGEX_HIDDEN_IMPORT);
    System.out.println("DITCH_BLOCK: ....: " + REGEX_DITCH_BLOCK);
  }
}
