package org.klojang.db;

import org.klojang.YoketeRuntimeException;

public class ResultSetMismatchException extends YoketeRuntimeException {

  public ResultSetMismatchException(String message) {
    super(message);
  }
}
