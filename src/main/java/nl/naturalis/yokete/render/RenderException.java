package nl.naturalis.yokete.render;

import java.util.List;
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

  private static final String NO_SUCH_NAME = "No such variable or nested template: \"%s\"";

  private static final String REPETITION_MISMATCH =
      "Template \"%s\" has already been partially populated, but with a different amount "
          + "of source data objects. When filling up a template in mulitple passes you must "
          + "always provide the same number of source data objects. Received %d source data "
          + "objects in first round; now got %d";

  private static final String NOT_READY = "Cannot render yet. Not all variables set: %s. ";

  private static final String INACCESSIBLE = "Value of %s (%s) is inaccessible for %s";

  private static final String NULL_ACCESSOR =
      "Accessor.getAccessorForTemplate() returned " + "null for nested template \"%s\"";

  private static final String MULTI_PASS_NOT_ALLOWED =
      "show() can be called at most once per text-only template (template specified: \"%s\")";

  /** Thrown when specifying a non-existent variable name. */
  public static Function<String, RenderException> noSuchVariable(Template t, String var) {
    String fqn = TemplateUtils.getFQName(t, var);
    String fmt = "No such variable: \"%s\"";
    return s -> new RenderException(format(fmt, fqn));
  }

  /** Thrown when specifying a non-existent template name. */
  public static Function<String, RenderException> noSuchTemplate(Template t, String name) {
    String fqn = TemplateUtils.getFQName(t, name);
    String fmt = "No such template: \"%s\"";
    return s -> new RenderException(format(fmt, fqn));
  }

  /** Thrown when specifying a non-existent variable and/or template name. */
  public static Function<String, RenderException> noSuchName(String name) {
    return s -> new RenderException(format(NO_SUCH_NAME, name));
  }

  /** Thrown if you attempt to set a variable more than once. */
  public static Function<String, RenderException> alreadySet(Template t, String var) {
    String fqn = TemplateUtils.getFQName(t, var);
    String fmt = "Variable already set: \"%s\"";
    return s -> new RenderException(format(fmt, fqn));
  }

  /**
   * Thrown during a multi-pass {@link RenderSession#fill(String, Object, EscapeType, String...)
   * fill} of a nested template if, in the second pass, you don't specify the same number of source
   * data objects as in the first pass. The number of source data objects you specify in the first
   * call to {@code fill} determines how often the template is going to repeat itself. Obviously
   * that fixes it for subsequent calls to {@code fill}.
   */
  public static RenderException repetitionMismatch(
      Template t, RenderSession[] sessions, int repeats) {
    String fqn = TemplateUtils.getFQName(t);
    return new RenderException(format(REPETITION_MISMATCH, fqn, sessions.length, repeats));
  }

  /** Thrown if you specify {@link EscapeType#NOT_SPECIFIED NOT_SPECIFIED} as the escape type. */
  public static Function<String, RenderException> badEscapeType() {
    return s -> new RenderException("NOT_SPECIFIED is not a valid escape type");
  }

  /**
   * Thrown by {@link RenderSession#renderSafe(java.io.OutputStream)} if not all variables have been
   * explicitly set.
   */
  public static Function<String, RenderException> notReady(List<String> varsToDo) {
    return s -> new RenderException(format(NOT_READY, varsToDo));
  }

  /** Thrown when attempting to populate a template after it has been rendered. */
  public static Function<String, RenderException> frozenSession() {
    return s -> new RenderException("Session frozen after rendering");
  }

  /** Thrown if an {@code Accessor} cannot access the request value. */
  public static RenderException inaccessible(Accessor<?> acc, Template t, String var, Object val) {
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
  public static Function<String, RenderException> noTextOnly(Template t) {
    String fqn = TemplateUtils.getFQName(t);
    String fmt = "Not a text-only template: %s";
    return s -> new RenderException(format(fmt, fqn));
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
   * Thrown if you call {@link RenderSession#fillMonoTemplate(String, Object) RenderSession.fillOne}
   * for a nested template that does not contain exactly one variable and zero doubly-nested
   * templates.
   */
  public static Function<String, RenderException> noMonoTemplate(Template t) {
    String fqn = TemplateUtils.getFQName(t);
    String fmt = "Not a one-variable template: %s";
    return s -> new RenderException(format(fmt, fqn));
  }

  /**
   * Thrown if you call {@link RenderSession#fillTupleTemplate(String, Object)
   * RenderSession.fillTwo} for a nested template that does not contain exactly two variables and
   * zero doubly-nested templates.
   */
  public static Function<String, RenderException> noTupleTemplate(Template t) {
    String fqn = TemplateUtils.getFQName(t);
    String fmt = "Not a two-variable template: %s";
    return s -> new RenderException(format(fmt, fqn));
  }

  /**
   * Thrown when the source data object for a template that contains one or more variables and or
   * nested templates is null.
   */
  public static Function<String, RenderException> missingSourceData(Template t) {
    String fqn = TemplateUtils.getFQName(t);
    String fmt = "Source data must not be null for non-text-only template %s";
    return s -> new RenderException(format(fmt, fqn));
  }

  /**
   * Thrown if you attempt to {@link RenderSession#createChildSessions(String, Accessor, int)
   * create} a child session for the specified template, but one or more child sessions have already
   * been created for it.
   */
  public static Function<String, RenderException> childSessionsAlreadyCreated(Template t) {
    String fqn = TemplateUtils.getFQName(t);
    String fmt = "Child sessions already created for template %s";
    return s -> new RenderException(format(fmt, fqn));
  }

  /**
   * Thrown if you {@link RenderSession#getChildSessions(String) request} the child sessions created
   * for the specified template, but no child sessions have been created yet.
   */
  public static Function<String, RenderException> noChildSessionsYet(Template t) {
    String fqn = TemplateUtils.getFQName(t);
    String fmt = "No child sessions yet for template %s";
    return s -> new RenderException(format(fmt, fqn));
  }

  /** Generic error condition, usually akin to an {@link IllegalArgumentException}. */
  public static Function<String, RenderException> illegalValue(String name, Object value) {
    String fmt = "Illegal value for \"%s\": %s";
    return s -> new RenderException(format(fmt, name, value));
  }

  RenderException(String message) {
    super(message);
  }
}
