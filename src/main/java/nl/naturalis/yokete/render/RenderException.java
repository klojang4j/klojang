package nl.naturalis.yokete.render;

import java.util.Set;
import java.util.function.Function;
import nl.naturalis.yokete.YoketeException;
import nl.naturalis.yokete.template.Template;
import static java.lang.String.format;
import static nl.naturalis.common.ClassMethods.prettyClassName;

public class RenderException extends YoketeException {

  private static final String NO_SUCH_VARIABLE = "No such variable: \"%s\"";

  private static final String NO_SUCH_TEMPLATE = "No such nested template: \"%s\"";

  private static final String INVALID_NAME = "No such variable or nested template: \"%s\"";

  private static final String ALREADY_SET = "Variable %s has already been set";

  private static final String ALREADY_POPULATED = "Template %s has already been populated";

  private static final String REPETITION_MISMATCH =
      "When populating a template in mulitple passes you must always provide the "
          + "same number of ViewData objects. Received %d ViewData objects in first "
          + "round for template \"%s\". Now got %d.";

  private static final String BAD_ESCAPE_TYPE = "NOT_SPECIFIED is not a valid escape type";

  private static final String NOT_RENDERABLE =
      "Not all variables and/or templates populated yet. Variables to do: %s. "
          + "Templates to do: %s";

  private static final String UNEXPECTED_TYPE = "Unexpected or illegal type for variable %s: %s";

  private static final String NESTED_MAP_EXPECTED =
      "Error creating data object for template \"%s\". Expected nested "
          + "Map<String, Object>. Got: %s";

  private static final String NULL_DATA =
      "Data for template %s must not be null because it contains at least one variable or nested template";

  private static final String BAD_DATA = "Cannot use instance of opaque class %s as template data";

  public static Function<String, RenderException> noSuchVariable(String name) {
    return s -> new RenderException(format(NO_SUCH_VARIABLE, name));
  }

  public static Function<String, RenderException> noSuchTemplate(String name) {
    return s -> new RenderException(format(NO_SUCH_TEMPLATE, name));
  }

  public static Function<String, RenderException> invalidName(String name) {
    return s -> new RenderException(format(INVALID_NAME, name));
  }

  public static Function<String, RenderException> alreadySet(String name) {
    return s -> new RenderException(format(ALREADY_SET, name));
  }

  public static Function<String, RenderException> alreadyPopulated(String name) {
    return s -> new RenderException(format(ALREADY_POPULATED, name));
  }

  public static RenderException repetitionMismatch(String name, int expected, int actual) {
    return new RenderException(format(REPETITION_MISMATCH, expected, name, actual));
  }

  public static Function<String, RenderException> badEscapeType() {
    return s -> new RenderException(BAD_ESCAPE_TYPE);
  }

  public static RenderException notRenderable(Set<String> vToDo, Set<String> tToDo) {
    String msg = format(NOT_RENDERABLE, vToDo, tToDo);
    return new RenderException(msg);
  }

  public static RenderException unexpectedType(String var, Object val) {
    String msg = format(UNEXPECTED_TYPE, var, prettyClassName(val));
    return new RenderException(msg);
  }

  public static RenderException nestedMapExpected(String tmplName, Object found) {
    String msg = format(NESTED_MAP_EXPECTED, tmplName, prettyClassName(found));
    return new RenderException(msg);
  }

  public static Function<String, RenderException> nullData(Template t) {
    return s -> new RenderException(format(NULL_DATA, t.getName()));
  }

  public static Function<String, RenderException> badData(Object data) {
    return s -> new RenderException(format(BAD_DATA, prettyClassName(data)));
  }

  RenderException(String message) {
    super(message);
  }
}
