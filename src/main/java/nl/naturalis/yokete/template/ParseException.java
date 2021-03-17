package nl.naturalis.yokete.template;

import java.nio.file.Path;
import java.util.function.Function;
import nl.naturalis.common.StringMethods;
import nl.naturalis.yokete.YoketeException;
import static nl.naturalis.common.ArrayMethods.prefix;

/**
 * Thrown while parsing the source code for a template.
 *
 * @author Ayco Holleman
 */
public class ParseException extends YoketeException {

  private static final String ERR_BASE = "Error at line %d, char %d: ";

  private static final String EMPTY_VARIABLE_NAME = ERR_BASE + "empty variable name";

  private static final String INVALID_PATH = "File not found: \"%s\"";

  private static final String INVALID_INCLUDE_PATH = ERR_BASE + INVALID_PATH;

  private static final String EMPTY_PATH = ERR_BASE + "Empty include path";

  private static final String EMPTY_TEMPLATE_NAME = ERR_BASE + "empty template name";

  private static final String DUPLICATE_TEMPLATE_NAME = ERR_BASE + "duplicate template name \"%s\"";

  private static final String MISSING_CLASS_OBJECT =
      ERR_BASE + "no Class object provided to call getResourceAsStream(\"%s\") on";

  private static final String DUPLICATE_VAR_NAME =
      ERR_BASE + "variable has same name as nested or included template: \"%s\"";

  private static final String BAD_ESCAPE_TYPE = ERR_BASE + "invalid escape type: \"%s\"";

  private static final String DITCH_BLOCK_NOT_TERMINATED = ERR_BASE + "ditch block not terminated";

  private static final String TEMPLATE_NOT_TERMINATED = ERR_BASE + "template not terminated";

  private static final String DANGLING_END_OF_TMPL = ERR_BASE + "dangling end-of-template";

  private static final String INCLUDE_NOT_TERMINATED = ERR_BASE + "include not terminated";

  static Function<String, ParseException> invalidPath(Path path) {
    return s -> new ParseException(String.format(INVALID_PATH, path));
  }

  static Function<String, ParseException> invalidIncludePath(String src, int pos, String path) {
    return exc1(INVALID_INCLUDE_PATH, src, pos, path);
  }

  static Function<String, ParseException> emptyPath(String src, int pos) {
    return exc1(EMPTY_PATH, src, pos);
  }

  static Function<String, ParseException> emptyTemplateName(String src, int pos) {
    return exc1(EMPTY_TEMPLATE_NAME, src, pos);
  }

  static Function<String, ParseException> duplicateTemplateName(String src, int pos, String name) {
    return exc1(DUPLICATE_TEMPLATE_NAME, src, pos, name);
  }

  static Function<String, ParseException> missingClassObject(
      String src, int pos, String tmplName, String path) {
    return exc1(MISSING_CLASS_OBJECT, src, pos, tmplName, path);
  }

  static Function<String, ParseException> emptyVarName(String src, int pos) {
    return exc1(EMPTY_VARIABLE_NAME, src, pos);
  }

  static Function<String, ParseException> duplicateVarName(String src, int pos, String name) {
    return exc1(DUPLICATE_VAR_NAME, src, pos, name);
  }

  static ParseException badEscapeType(String src, int pos, String name) {
    return exc0(BAD_ESCAPE_TYPE, src, pos, name);
  }

  static Function<String, ParseException> templateNotTerminated(String src, int pos) {
    return exc1(TEMPLATE_NOT_TERMINATED, src, pos);
  }

  static Function<String, ParseException> includeNotTerminated(String src, int pos) {
    return exc1(INCLUDE_NOT_TERMINATED, src, pos);
  }

  static ParseException ditchBlockNotTerminated(String src, int pos) {
    return exc0(DITCH_BLOCK_NOT_TERMINATED, src, pos);
  }

  static ParseException danglingEndOfTemplate(String src, int pos) {
    return exc0(DANGLING_END_OF_TMPL, src, pos);
  }

  private ParseException(String message) {
    super(message);
  }

  private static ParseException exc0(String fmt, String src, int pos, Object... args) {
    int[] x = StringMethods.getLineAndColumn(src, pos);
    return new ParseException(String.format(fmt, prefix(args, x[0] + 1, x[1] + 1)));
  }

  private static Function<String, ParseException> exc1(
      String fmt, String src, int pos, Object... args) {
    return s -> exc0(fmt, src, pos, args);
  }
}
