package org.klojang.template;

import org.klojang.KlojangException;

/**
 * Thrown if the template source could not be parsed into a {@link Template}.
 *
 * @see ErrorType
 * @author Ayco Holleman
 */
public class ParseException extends KlojangException {

  public ParseException(String message) {
    super(message);
  }
}
