package org.klojang.template;

import static nl.naturalis.common.StringMethods.concat;

public class InvalidPathException extends ParseException {

  InvalidPathException(String path) {
    super(concat("Invalid path: \"", path, '"'));
  }
}
