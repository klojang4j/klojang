package org.klojang;

import static nl.naturalis.common.ObjectMethods.isEmpty;

/**
 * Base class for runtime exceptions emanating from the Yokete package.
 *
 * @author Ayco Holleman
 */
public class KJRuntimeException extends RuntimeException {

  public KJRuntimeException(String message, Object... msgArgs) {
    super(isEmpty(msgArgs) ? message : String.format(message, msgArgs));
  }

  public KJRuntimeException(Throwable cause) {
    super(cause);
  }

  public KJRuntimeException(String message, Throwable cause) {
    super(message, cause);
  }
}
