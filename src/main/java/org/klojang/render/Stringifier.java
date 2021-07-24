package org.klojang.render;

import org.apache.commons.text.StringEscapeUtils;
import nl.naturalis.common.StringMethods;
import static nl.naturalis.common.ObjectMethods.ifNotNull;

/**
 * Stringifies the values coming back from the data access layer.
 *
 * <p>It is, in principle, not the stringifier's responsibility to apply any form of escaping to the
 * stringified value (e.g. HTML escaping). This is done by the {@link RenderSession} (which uses
 * Apache's {@link StringEscapeUtils}). However, there may be cases where you will want to do this
 * yourself. For example, if you must stringify {@code null} to a non-breaking space (&#38;nbsp;),
 * you are in fact producing an already-escaped value. In that case, you must make sure that the
 * variable does not get escaped again by the {@code RenderSession}. Its inline escape type should
 * be text, or it should be set using {@link EscapeType#ESCAPE_NONE ESCAPE_NONE}).
 *
 * @author Ayco Holleman
 */
@FunctionalInterface
public interface Stringifier {

  /**
   * Stringifies {@code null} to an empty string and any other value by calling {@code toString()}
   * on it. It is the {@code Stringifier} that is used by default for any template variable for
   * which no alternative {@code Stringifier} is {@link StringifierFactory.Builder configured}.
   */
  public static final Stringifier DEFAULT =
      (x) -> ifNotNull(x, Object::toString, StringMethods.EMPTY);

  /**
   * Stringifies the specified value . Stringifier implementations <i>must</i> be able to handle
   * null values and the <i>must never</i> return null.
   *
   * @param value The value to be stringified
   * @return A string represenation of the value
   * @throws RenderException
   */
  String toString(Object value) throws RenderException;

  /**
   * Uses the output of this stringifier as input for the {@code next} stringifier.
   *
   * @param next The stringifier to apply after this stringifier
   * @return A new stringifier that first applies this stringifier and then the {@code next}
   *     stringifier
   */
  default Stringifier andThen(Stringifier next) {
    return x -> next.toString(toString(x));
  }
}
