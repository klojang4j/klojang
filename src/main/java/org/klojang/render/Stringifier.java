package org.klojang.render;

import nl.naturalis.common.StringMethods;
import static nl.naturalis.common.ObjectMethods.ifNotNull;

/**
 * Stringifies the values coming back from the data access layer.
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
   * Stringifies the specified value. Stringifier implementations <i>must</i> be able to handle null
   * values and they <i>must never</i> return null.
   *
   * @param value The value to be stringified
   * @return A string representation of the value
   * @throws RenderException
   */
  String toString(Object value) throws RenderException;
}
