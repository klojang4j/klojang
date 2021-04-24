package nl.naturalis.yokete.db;

import nl.naturalis.yokete.YoketeRuntimeException;

public class ResultSetReadException extends YoketeRuntimeException {

  public ResultSetReadException(String message) {
    super(message);
  }
}
