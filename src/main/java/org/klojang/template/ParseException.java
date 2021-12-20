package org.klojang.template;

import org.klojang.KlojangException;

/**
 * Thrown while parsing the source code for a template.
 *
 * @author Ayco Holleman
 */
public class ParseException extends KlojangException {

  public ParseException(String message) {
    super(message);
  }
}
