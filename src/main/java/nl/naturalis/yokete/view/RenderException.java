package nl.naturalis.yokete.view;

import nl.naturalis.yokete.YoketeException;
import static java.lang.String.format;

public class RenderException extends YoketeException {

  static final String NO_SUCH_VARIABLE = "No such variable: \"%s\"";

  static final String VARIABLE_ALREADY_SET = "Variable %s has already been set";

  static final String NO_SUCH_TEMPLATE = "No such nested template: \"%s\"";

  static final String TEMPLATE_ALREADY_POPULATED = "Template %s has already been populated";

  static final String TEMPLATE_REPETITION_MISMATCH =
      "Expected %d ViewData objects for template \"%s\". Got %d.";

  static RenderException noSuchVariable(String name) {
    return new RenderException(format(NO_SUCH_VARIABLE, name));
  }

  static RenderException variableAlreadySet(String name) {
    return new RenderException(format(VARIABLE_ALREADY_SET, name));
  }

  static RenderException noSuchTemplate(String name) {
    return new RenderException(format(NO_SUCH_TEMPLATE, name));
  }

  static RenderException templateAlreadyPopulated(String name) {
    return new RenderException(format(TEMPLATE_ALREADY_POPULATED, name));
  }

  static RenderException templateRepetitionMismatch(String name, int expected, int actual) {
    return new RenderException(format(TEMPLATE_REPETITION_MISMATCH, expected, name, actual));
  }

  public RenderException(String message) {
    super(message);
  }

  public RenderException(Throwable cause) {
    super(cause);
  }

  public RenderException(String message, Throwable cause) {
    super(message, cause);
  }
}
