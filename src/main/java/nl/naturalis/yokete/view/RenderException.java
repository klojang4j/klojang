package nl.naturalis.yokete.view;

import nl.naturalis.yokete.YoketeException;
import static java.lang.String.format;

public class RenderException extends YoketeException {

  static final String NO_SUCH_VARIABLE = "No such variable: \"%s\"";

  static final String ALREADY_SET = "Variable %s has already been set";

  static final String ALREADY_POPULATED = "Template %s has already been populated";

  static final String NO_SUCH_TEMPLATE = "No such nested template: \"%s\"";

  static final String TEMPLATE_REPETITION_MISMATCH =
      "Expected %d ViewData objects for template \"%s\". Got %d.";

  static final String BAD_ESCAPE_TYPE = "NOT_SPECIFIED is not a valid escape type";

  static final String NO_SESSION =
      "Session de-activated. You can render the session again, but you cannot modify it any longer";

  static RenderException noSuchVariable(String name) {
    return new RenderException(format(NO_SUCH_VARIABLE, name));
  }

  static RenderException noSuchTemplate(String name) {
    return new RenderException(format(NO_SUCH_TEMPLATE, name));
  }

  static RenderException alreadySet(String name) {
    return new RenderException(format(ALREADY_SET, name));
  }

  static RenderException alreadyPopulated(String name) {
    return new RenderException(format(ALREADY_POPULATED, name));
  }

  static RenderException repetitionMismatch(String name, int expected, int actual) {
    return new RenderException(format(TEMPLATE_REPETITION_MISMATCH, expected, name, actual));
  }

  static RenderException badEscapeType() {
    return new RenderException(BAD_ESCAPE_TYPE);
  }

  static RenderException noSession() {
    return new RenderException(NO_SESSION);
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
