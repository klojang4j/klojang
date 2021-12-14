package org.klojang.db;

import java.sql.ResultSet;

/**
 * Thrown when the values in a {@link ResultSet} could not be transported to a JavaBean.
 *
 * @author Ayco Holleman
 */
public class ResultSetReadException extends KJSQLException {

  public ResultSetReadException(String message) {
    super(message);
  }
}
