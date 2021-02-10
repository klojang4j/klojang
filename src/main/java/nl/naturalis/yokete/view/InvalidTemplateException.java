package nl.naturalis.yokete.view;

import nl.naturalis.yokete.YoketeException;

public class InvalidTemplateException extends YoketeException {

  static InvalidTemplateException duplicateTemplateName(String name) {
    String fmt = "Duplicate template name: \"%s\"";
    return new InvalidTemplateException(String.format(fmt, name));
  }

  static InvalidTemplateException duplicateVarName(String name) {
    String fmt = "Variable with same name as nested template not allowed: \"%s\"";
    return new InvalidTemplateException(String.format(fmt, name));
  }

  private InvalidTemplateException(String message) {
    super(message);
  }
}
