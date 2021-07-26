package org.klojang.db;

import org.klojang.KJRuntimeException;

public class ResultSetMismatchException extends KJRuntimeException {

  public ResultSetMismatchException(String message) {
    super(message);
  }
}
