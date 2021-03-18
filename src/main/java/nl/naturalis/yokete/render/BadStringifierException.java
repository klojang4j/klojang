package nl.naturalis.yokete.render;

import nl.naturalis.common.ClassMethods;
import nl.naturalis.yokete.template.Template;
import static java.lang.String.format;

/**
 * Subclass of {@code RenderException} dealing with stringification issues.
 *
 * @author Ayco Holleman
 */
public class BadStringifierException extends RenderException {

  /** Thrown if a {@link Stringifier} implementation illegally returns {@code null}. */
  public static BadStringifierException stringifierReturnedNull(Template tmpl, String varName) {
    String fmt = "Bad template stringifier for %s.%s: illegally returned null";
    return new BadStringifierException(format(fmt, tmpl.getName(), varName));
  }

  /**
   * Thrown if the stringifier's {@link Stringifier#toString(Template, String, Object) toString}
   * threw a {@code NullPointerException} (most likely unintentionally).
   */
  public static BadStringifierException stringifierNotNullResistant(Template tmpl, String varName) {
    String fmt = "Bad template stringifier for %s.%s: cannot handle null values";
    return new BadStringifierException(format(fmt, tmpl.getName(), varName));
  }

  /** Thrown if a {@link ApplicationStringifier} implementation illegally returns {@code null}. */
  public static BadStringifierException applicationStringifierReturnedNull(Class<?> type) {
    String fmt = "Bad application stringifier for type %s: illegally returned null";
    String cn = ClassMethods.prettyClassName(type);
    return new BadStringifierException(format(fmt, cn));
  }

  /**
   * Thrown if the application stringifier's {@link ApplicationStringifier.Config#register(Class,
   * nl.naturalis.common.function.ThrowingFunction) stringification function} threw a {@code
   * NullPointerException} (most likely unintentionally).
   */
  public static BadStringifierException applicationStringifierNotNullResistant(Class<?> type) {
    String fmt = "Bad template stringifier for  type %s: cannot handle null values";
    String cn = ClassMethods.prettyClassName(type);
    return new BadStringifierException(format(fmt, cn));
  }

  private BadStringifierException(String message) {
    super(message);
  }
}
