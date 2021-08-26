package org.klojang.render;

import java.util.function.Function;
import org.klojang.KlojangException;
import org.klojang.template.Template;
import org.klojang.template.TemplateUtils;
import org.klojang.template.VarGroup;
import static java.lang.String.format;
import static org.klojang.x.Messages.ERR_NO_SUCH_TEMPLATE;
import static org.klojang.x.Messages.ERR_NO_SUCH_VARIABLE;

/**
 * Thrown from a {@link RenderSession} under various circumstances.
 *
 * @author Ayco Holleman
 */
public class RenderException extends KlojangException {

  /**
   * Thrown when specifying a {@link RenderSession#set(String, Object, VarGroup) default group} for
   * which no stringifier has been {@link
   * StringifierFactory.Builder#addGroupStringifier(Stringifier, String...) defined}.
   */
  public static RenderException noStringifierForGroup(VarGroup vg) {
    String fmt = "No stringifier associated with variable group \"%s\"";
    return new RenderException(format(fmt, vg));
  }

  /** Thrown when specifying a non-existent variable name. */
  public static Function<String, RenderException> noSuchVariable(Template t, String var) {
    String fqn = TemplateUtils.getFQName(t, var);
    return s -> new RenderException(format(ERR_NO_SUCH_VARIABLE, fqn));
  }

  /** Thrown when specifying a non-existent template name. */
  public static Function<String, RenderException> noSuchTemplate(Template t, String name) {
    String fqn = TemplateUtils.getFQName(t, name);
    return s -> new RenderException(format(ERR_NO_SUCH_TEMPLATE, fqn));
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
        "Template \"%s\" has already been partially populated, but with a different number "
            + "of source data objects. When populating a template in mulitple passes you must "
            + "always provide the same number of source data objects. Received %d source data "
            + "objects in first round. Now got %d";
    String fqn = TemplateUtils.getFQName(t);
    return new RenderException(format(fmt, fqn, sessions.length, repeats));
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
        "show() can be called only once per text-only template (template specified: \"%s\")";
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
   * Thrown if you call {@link RenderSession#populateWithValue(String, Object)} for a nested
   * template that does not contain exactly one variable and zero doubly-nested templates.
   */
  public static Function<String, RenderException> notMonoTemplate(Template t) {
    String fqn = TemplateUtils.getFQName(t);
    String fmt = "Not a one-variable template: %s";
    return s -> new RenderException(format(fmt, fqn));
  }

  /**
   * Thrown if you call {@link RenderSession#populateWithTuple(String, Object)} for a nested
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

  public RenderException(String message) {
    super(message);
  }
}
