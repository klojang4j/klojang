package nl.naturalis.yokete.render;

import java.util.Set;
import java.util.function.Function;
import nl.naturalis.yokete.YoketeException;
import nl.naturalis.yokete.template.Template;
import nl.naturalis.yokete.template.TemplateUtils;
import static java.lang.String.format;
import static nl.naturalis.common.ClassMethods.prettyClassName;
import static nl.naturalis.common.ClassMethods.prettySimpleClassName;

public class RenderException extends YoketeException {

  private static final String NO_SUCH_VARIABLE = "No such variable: \"%s\"";

  private static final String NO_SUCH_TEMPLATE = "No such nested template: \"%s\"";

  private static final String NO_SUCH_NAME = "No such variable or nested template: \"%s\"";

  private static final String ALREADY_SET = "Variable already set: \"%s\"";

  private static final String REPETITION_MISMATCH =
      "Template \"%s\" has already been partially populated, but with a different amount "
          + "of source data objects. When filling up a template in mulitple passes you must "
          + "always provide the same number of source data objects. Received %d source data "
          + "objects in first round; now got %d";

  private static final String BAD_ESCAPE_TYPE = "NOT_SPECIFIED is not a valid escape type";

  private static final String NOT_RENDERABLE = "Cannot render yet. Not all variables set: %s. ";

  private static final String INACCESSIBLE = "Value of %s (%s) is inaccessible for %s";

  private static final String NULL_DATA =
      "Data for template %s must not be null because it contains at least one variable or nested template";

  private static final String BAD_DATA = "Cannot use instance of opaque class %s as template data";

  private static final String NOT_MONO =
      "populateMono only allowed for single-variable template; \"%s\" contains %d variables and/or nested templates";

  private static final String NO_TEXT_ONLY_TEMPLATE =
      "fillNone only allowed for text-only templates; \"%s\" contains %d variables and/or nested templates";

  private static final String INVALID_VALUE = "Invalid value for %s: %s";

  public static Function<String, RenderException> noSuchVariable(Template t, String var) {
    String fqn = TemplateUtils.getFQName(t, var);
    return s -> new RenderException(format(NO_SUCH_VARIABLE, fqn));
  }

  public static Function<String, RenderException> noSuchTemplate(String name) {
    return s -> new RenderException(format(NO_SUCH_TEMPLATE, name));
  }

  public static Function<String, RenderException> noSuchName(String name) {
    return s -> new RenderException(format(NO_SUCH_NAME, name));
  }

  public static Function<String, RenderException> alreadySet(Template t, String var) {
    String fqn = TemplateUtils.getFQName(t, var);
    return s -> new RenderException(format(ALREADY_SET, fqn));
  }

  public static RenderException repetitionMismatch(String name, int expected, int actual) {
    return new RenderException(format(REPETITION_MISMATCH, expected, name, actual));
  }

  public static Function<String, RenderException> badEscapeType() {
    return s -> new RenderException(BAD_ESCAPE_TYPE);
  }

  public static Function<String, RenderException> notRenderable(Set<String> varsToDo) {
    String msg = format(NOT_RENDERABLE, varsToDo);
    return s -> new RenderException(msg);
  }

  public static RenderException inaccessible(Accessor acc, Template t, String var, Object val) {
    String fqn = TemplateUtils.getFQName(t, var);
    String msg = format(INACCESSIBLE, fqn, prettyClassName(val), prettySimpleClassName(acc));
    return new RenderException(msg);
  }

  public static Function<String, RenderException> nullData(Template t) {
    return s -> new RenderException(format(NULL_DATA, t.getName()));
  }

  public static Function<String, RenderException> badData(Object data) {
    return s -> new RenderException(format(BAD_DATA, prettyClassName(data)));
  }

  public static Function<String, RenderException> notTextOnly(Template t) {
    return s ->
        new RenderException(format(NO_TEXT_ONLY_TEMPLATE, t.getName(), t.getNames().size()));
  }

  public static Function<String, RenderException> notMono(Template t) {
    return s -> new RenderException(format(NOT_MONO, t.getName(), t.getNames().size()));
  }

  public static Function<String, RenderException> invalidValue(String name, Object value) {
    return s -> new RenderException(format(INVALID_VALUE, name, value));
  }

  RenderException(String message) {
    super(message);
  }
}
