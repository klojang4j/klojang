package nl.naturalis.yokete.render;

import nl.naturalis.yokete.template.Template;
import static nl.naturalis.common.ObjectMethods.*;

/**
 * Stringifies values for a particular template variable. You can implement your own {@code
 * Stringifier}, or you can use a {@link StringifierFactory} to configure a concrete {@code
 * Stringifier} instance.
 *
 * <p>It is, in principle, <i>not</i> the stringifier's responsibility to apply some form of
 * escaping to the stringified value (e.g. HTML escaping). This is done by the {@link
 * RenderSession}. However, there may be cases where you will want to do this yourself. For example,
 * if you must stringify {@code null} to a non-breaking space (&#38;nbsp;), you are in fact
 * producing an already-escaped value. In that case, make sure the variable does not get escaped
 * again by the {@code RenderSession} (its inline escape type should be text, or it shhould be set
 * using escape type {@link EscapeType#ESCAPE_NONE ESCAPE_NONE}).
 *
 * @author Ayco Holleman
 */
@FunctionalInterface
public interface Stringifier {

  /**
   * A simple, brute-force {@code TemplateStringifier} that stringifies {@code null} to an empty
   * string and otherwise calls {@code toString()} on the value. Unlikely to be satisfactory in the
   * end, but useful in the early stages of development.
   */
  public static final Stringifier SIMPLETON = (x, y, z) -> ifNotNull(z, Object::toString, "");

  /**
   * Stringifies the specified value for the specified variable in the specified template.
   * Stringifier implementations <b>must</b> be able to handle null values and the <b>must never</b>
   * return null.
   *
   * @param template The template containing the variable for which to stringify the specified value
   * @param varName The variable for which to stringify the specified value
   * @param value The value to be stringified
   * @return A string represenation of the value
   * @throws RenderException
   */
  String stringify(Template template, String varName, Object value) throws RenderException;
}
