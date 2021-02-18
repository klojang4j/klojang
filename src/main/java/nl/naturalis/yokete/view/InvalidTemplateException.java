package nl.naturalis.yokete.view;

import java.util.function.Function;
import nl.naturalis.yokete.YoketeException;
import static nl.naturalis.common.ClassMethods.prettySimpleClassName;

public class InvalidTemplateException extends YoketeException {

  private static final String EMPTY_VARIABLE_NAME = "Empty variable name near position %d";

  private static final String INVALID_PATH = "Invalid path: \"%s\"";

  private static final String INVALID_IMPORT_PATH =
      "Cannot import template \"%s\" near position %d. Invalid path: \"%s\"";

  private static final String EMPTY_PATH = "Empty path in template import near position %d";

  private static final String EMPTY_TEMPLATE_NAME = "Empty template name near position %d";

  private static final String DUPLICATE_TEMPLATE_NAME =
      "Duplicate template name \"%s\" near position %d";

  private static final String MISSING_CLASS_OBJECT =
      "Cannot import template \"%s\" near position %d. No Class "
          + "object provided to call getResourceAsStream(\"%s\") on";

  private static final String DUPLICATE_VAR_NAME =
      "Variable near position %d has same name as nested or imported template: \"%s\"";

  private static final String BAD_ESCAPE_TYPE =
      "Invalid inline escape type near position %d: \"%s\"";

  static Function<String, InvalidTemplateException> invalidPath(Class<?> clazz, String path) {
    return exc(INVALID_PATH, path, prettySimpleClassName(clazz));
  }

  static Function<String, InvalidTemplateException> invalidImportPath(
      Class<?> clazz, String path, int pos) {
    return exc(INVALID_IMPORT_PATH, pos, path, prettySimpleClassName(clazz));
  }

  static Function<String, InvalidTemplateException> emptyPath(int pos) {
    return exc(EMPTY_PATH, pos);
  }

  static Function<String, InvalidTemplateException> emptyTemplateName(int pos) {
    return exc(EMPTY_TEMPLATE_NAME, pos);
  }

  static Function<String, InvalidTemplateException> duplicateTemplateName(String name, int pos) {
    return exc(DUPLICATE_TEMPLATE_NAME, name, pos);
  }

  static Function<String, InvalidTemplateException> missingClassObject(
      String tmplName, String path, int pos) {
    return exc(MISSING_CLASS_OBJECT, tmplName, pos, path);
  }

  static Function<String, InvalidTemplateException> emptyVarName(int pos) {
    return exc(EMPTY_VARIABLE_NAME, pos);
  }

  static Function<String, InvalidTemplateException> duplicateVarName(String name, int pos) {
    return exc(DUPLICATE_VAR_NAME, pos, name);
  }

  static Function<String, InvalidTemplateException> badEscapeType(String name, int pos) {
    return exc(BAD_ESCAPE_TYPE, pos, name);
  }

  public InvalidTemplateException(String message) {
    super(message);
  }

  private static Function<String, InvalidTemplateException> exc(String fmt, Object... args) {
    return s -> new InvalidTemplateException(String.format(fmt, args));
  }
}
