package nl.naturalis.yokete.render;

import java.util.List;
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

  private static final String NOT_MONO =
      "populateMono only allowed for single-variable template; \"%s\" contains %d variables and/or nested templates";

  private static final String NO_TEXT_ONLY =
      "fillNone only allowed for text-only templates; \"%s\" contains %d variables and/or nested templates";

  private static final String INVALID_VALUE = "Invalid value for \"%s\": %s";

  /** Thrown when specifying a non-existent variable name. */
  public static Function<String, RenderException> noSuchVariable(Template t, String var) {
    String fqn = TemplateUtils.getFQName(t, var);
    return s -> new RenderException(format(NO_SUCH_VARIABLE, fqn));
  }

  /** Thrown when specifying a non-existent template name. */
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

  public static RenderException repetitionMismatch(
      Template t, List<RenderSession> sessions, int repeats) {
    String fqn = TemplateUtils.getFQName(t);
    return new RenderException(format(REPETITION_MISMATCH, fqn, sessions.size(), repeats));
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

  public static Function<String, RenderException> notTextOnly(Template t) {
    return s -> new RenderException(format(NO_TEXT_ONLY, t.getName(), t.getNames().size()));
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
