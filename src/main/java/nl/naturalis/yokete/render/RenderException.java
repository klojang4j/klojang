package nl.naturalis.yokete.render;

import java.util.function.Function;
import nl.naturalis.yokete.YoketeException;
import nl.naturalis.yokete.template.Template;
import nl.naturalis.yokete.template.TemplateUtils;
import static java.lang.String.format;

/**
 * Thrown from a {@link RenderSession} under various circumstances.
 *
 * @author Ayco Holleman
 */
public class RenderException extends YoketeException {

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

  /** Thrown if you attempt to set a variable more than once. */
  public static Function<String, RenderException> alreadySet(Template t, String var) {
    String fqn = TemplateUtils.getFQName(t, var);
    String fmt = "Variable already set: \"%s\"";
    return s -> new RenderException(format(fmt, fqn));
  }

  /**
   * Thrown during multi-pass {@link RenderSession#populate(String, Object, EscapeType, String...)
   * population} of a nested template if, in the second pass, you don't specify the same number of
   * source data objects as in the first pass. The {@code List} or array of source data objects you
   * specify in the first call to {@code populate} determines how often the template is going to
   * repeat itself. Obviously that fixes it for subsequent calls to {@code populate}.
   */
  public static RenderException repetitionMismatch(
      Template t, RenderSession[] sessions, int repeats) {
    String fmt =
        "Template \"%s\" has already been partially populated, but with a different amount "
            + "of source data objects. When filling up a template in mulitple passes you must "
            + "always provide the same number of source data objects. Received %d source data "
            + "objects in first round; now got %d";
    String fqn = TemplateUtils.getFQName(t);
    return new RenderException(format(fmt, fqn, sessions.length, repeats));
  }

  /** Thrown if you specify {@link EscapeType#NOT_SPECIFIED NOT_SPECIFIED} as the escape type. */
  public static Function<String, RenderException> badEscapeType() {
    return s -> new RenderException("NOT_SPECIFIED is not a valid escape type");
  }

  /** Thrown when attempting to populate a template after it has been rendered. */
  public static Function<String, RenderException> frozenSession() {
    return s -> new RenderException("Session frozen after rendering");
  }

  /**
   * Thrown if you call {@link RenderSession#show(String) RenderSession.show} more than once for
   * text-only template.
   */
  public static RenderException multiPassNotAllowed(Template t) {
    String fqn = TemplateUtils.getFQName(t);
    String fmt =
        "show() can be called at most once per text-only template (template specified: \"%s\")";
    return new RenderException(format(fmt, fqn));
  }

  /**
   * Thrown if you call {@link RenderSession#show(String) RenderSession.show} for a nested template
   * that is not a text-only template.
   */
  public static Function<String, RenderException> notTextOnly(Template t) {
    String fqn = TemplateUtils.getFQName(t);
    String fmt = "Not a text-only template: %s";
    return s -> new RenderException(format(fmt, fqn));
  }

  /**
   * Thrown if you call {@link RenderSession#populateMonoTemplate(String, Object)} for a nested
   * template that does not contain exactly one variable and zero doubly-nested templates.
   */
  public static Function<String, RenderException> notMonoTemplate(Template t) {
    String fqn = TemplateUtils.getFQName(t);
    String fmt = "Not a one-variable template: %s";
    return s -> new RenderException(format(fmt, fqn));
  }

  /**
   * Thrown if you call {@link RenderSession#populateTupleTemplate(String, Object)} for a nested
   * template that does not contain exactly two variables and zero doubly-nested templates.
   */
  public static Function<String, RenderException> notTupleTemplate(Template t) {
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

  public static RenderException noAccessorProvided() {
    String msg =
        "No Accessor provided. When configuring a Page without an Accessor "
            + "you can only call the set methods of the RenderSession class";
    return new RenderException(msg);
  }

  RenderException(String message) {
    super(message);
  }
}
