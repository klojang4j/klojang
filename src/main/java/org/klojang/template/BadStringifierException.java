package org.klojang.template;

import static java.lang.String.format;
import static org.klojang.template.TemplateUtils.*;

/**
 * Subclass of {@code RenderException} dealing with stringification issues.
 *
 * @author Ayco Holleman
 */
public class BadStringifierException extends RenderException {

  /** Thrown if a {@link Stringifier} implementation illegally returns {@code null}. */
  public static BadStringifierException stringifierReturnedNull(Template tmpl, String varName) {
    String fmt = "Bad stringifier for %s: illegally returned null";
    return new BadStringifierException(format(fmt, getFQName(tmpl, varName)));
  }

  /**
   * Thrown if a stringifier's {@link Stringifier#toString(Object) toString} method threw a {@code
   * NullPointerException}.
   */
  public static BadStringifierException stringifierNotNullResistant(Template tmpl, String varName) {
    String fmt = "Bad stringifier for %s: cannot handle null values";
    return new BadStringifierException(format(fmt, getFQName(tmpl, varName)));
  }

  private BadStringifierException(String message) {
    super(message);
  }
}
