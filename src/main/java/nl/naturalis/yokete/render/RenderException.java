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

/**
 * Thrown from a {@link RenderSession} under various circumstances.
 *
 * @author Ayco Holleman
 */
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

  private static final String NULL_ACCESSOR =
      "Accessor.getAccessorForTemplate() returned " + "null for nested template \"%s\"";

  private static final String NOT_MONO =
      "populateMono() can only called for single-variable template; \"%s\" contains %d "
          + "variables and/or nested templates";

  private static final String NO_TEXT_ONLY =
      "show() can only called for text-only templates; \"%s\" contains %d variables and/or "
          + "nested templates";

  private static final String MULTI_PASS_NOT_ALLOWED =
      "show() can be called at most once per text-only template (template specified: \"%s\")";

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

  /** Thrown when specifying a non-existent variable and/or template name. */
  public static Function<String, RenderException> noSuchName(String name) {
    return s -> new RenderException(format(NO_SUCH_NAME, name));
  }

  /** Thrown if you attempt to set a variable more than once. */
  public static Function<String, RenderException> alreadySet(Template t, String var) {
    String fqn = TemplateUtils.getFQName(t, var);
    return s -> new RenderException(format(ALREADY_SET, fqn));
  }

  /**
   * Thrown during a multi-pass {@link RenderSession#fill(String, Object, EscapeType, String...)
   * fill} of a nested template if, in the second pass, you don't specify the same number of source
   * data objects as in the first pass. The number of source data objects you specify in the first
   * call to {@code fill} determines how often the template is going to repeat itself. Obviously
   * that fixes it for subsequent calls to {@code fill}.
   */
  public static RenderException repetitionMismatch(
      Template t, List<RenderSession> sessions, int repeats) {
    String fqn = TemplateUtils.getFQName(t);
    return new RenderException(format(REPETITION_MISMATCH, fqn, sessions.size(), repeats));
  }

  /** Thrown if you specify {@link EscapeType#NOT_SPECIFIED NOT_SPECIFIED} as the escape type. */
  public static Function<String, RenderException> badEscapeType() {
    return s -> new RenderException(BAD_ESCAPE_TYPE);
  }

  /**
   * Thrown by {@link RenderSession#renderSafe(java.io.OutputStream)} if not all variables have been
   * explicitly set.
   */
  public static Function<String, RenderException> notReady(Set<String> varsToDo) {
    String msg = format(NOT_RENDERABLE, varsToDo);
    return s -> new RenderException(msg);
  }

  /** Thrown if an {@code Accessor} cannot access the request value. */
  public static RenderException inaccessible(Accessor acc, Template t, String var, Object val) {
    String fqn = TemplateUtils.getFQName(t, var);
    String msg = format(INACCESSIBLE, fqn, prettyClassName(val), prettySimpleClassName(acc));
    return new RenderException(msg);
  }

  /** Thrown if {@link Accessor#getAccessorForTemplate(Template, Object)} returned null */
  public static Function<String, RenderException> nullAccessor(Template t) {
    String fqn = TemplateUtils.getFQName(t);
    String msg = format(NULL_ACCESSOR, fqn);
    return s -> new RenderException(msg);
  }

  /**
   * Thrown if you call {@link RenderSession#show(String) RenderSession.show} for a nested template
   * that is not a text-only template.
   */
  public static Function<String, RenderException> notTextOnly(Template t) {
    String fqn = TemplateUtils.getFQName(t);
    return s -> new RenderException(format(NO_TEXT_ONLY, fqn, t.getNames().size()));
  }

  /**
   * Thrown if you call {@link RenderSession#show(String) RenderSession.show} more than once for
   * text-only template.
   */
  public static RenderException multiPassNotAllowed(Template t) {
    String fqn = TemplateUtils.getFQName(t);
    return new RenderException(format(MULTI_PASS_NOT_ALLOWED, fqn));
  }

  /**
   * Thrown if you call {@link RenderSession#fillMono(String, Object) RenderSession.fillMono} for a
   * nested template that does not contain exactly one variable (and zero doubly-nested templates).
   */
  public static Function<String, RenderException> notMono(Template t) {
    return s -> new RenderException(format(NOT_MONO, t.getName(), t.getNames().size()));
  }

  /** Generic error condition. */
  public static Function<String, RenderException> invalidValue(String name, Object value) {
    return s -> new RenderException(format(INVALID_VALUE, name, value));
  }

  RenderException(String message) {
    super(message);
  }
}
