package nl.naturalis.yokete.render;

import org.apache.commons.text.StringEscapeUtils;
import nl.naturalis.common.check.Check;
import nl.naturalis.yokete.template.Template;
import static nl.naturalis.common.ObjectMethods.ifNotNull;

/**
 * Stringifies values for a particular template. A {@link RenderSession} will call the stringifier's
 * {@link #stringify(Template, String, Object) stringify} method to stringify values obtained (via
 * an {@link Accessor} object) from the data access layer. A {@code Stringifier} must be capable of
 * stringifying values for <i>all</i> variables in the {@link Template#ROOT_TEMPLATE_NAME root
 * template} and <i>all</i> templates nested inside it. Although the {@code stringify} method of
 * this interface takes a {@link Template} instance as it first argument, the {@code RenderSession}
 * will only use it to specify either the root template or one of the templates descending from it.
 * In other words, you need one {@code Stringifier} per root template.
 *
 * <p>You can implement your own {@code Stringifier} or you can use a {@link
 * StringifierConfigurator} to configure a {@code Stringifier} instance. Using a {@code
 * StringifierConfigurator} is probably easier and it should be capable of meeting any
 * stringification requirements you may have.
 *
 * <p>It is, in principle, not the stringifier's responsibility to apply some form of escaping to
 * the stringified value (e.g. HTML escaping). This is done by the {@link RenderSession} (which uses
 * Apache's {@link StringEscapeUtils}). However, there may be cases where you will want to do this
 * yourself. For example, if you must stringify {@code null} to a non-breaking space (&#38;nbsp;),
 * you are in fact producing an already-escaped value. In that case, make sure the variable does not
 * get escaped again by the {@code RenderSession} (its inline escape type should be text, or it
 * should be set using escape type {@link EscapeType#ESCAPE_NONE ESCAPE_NONE}).
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
   * Returns a {@link StringifierConfigurator} that lets you configure a {@link Stringifier}
   * instance.
   *
   * @param template The template for which to configure the {@code Stringifier}
   * @return A {@link StringifierConfigurator} object that lets you configure a {@link Stringifier}
   *     instance
   */
  static StringifierConfigurator configure(Template template) {
    return new StringifierConfigurator(Check.notNull(template).ok());
  }

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
