package nl.naturalis.yokete.view;

import nl.naturalis.yokete.YoketeException;

public class InvalidTemplateException extends YoketeException {

  public InvalidTemplateException(String message) {
    super(message);
  }

  public InvalidTemplateException(Throwable cause) {
    super(cause);
  }

  public InvalidTemplateException(String message, Throwable cause) {
    super(message, cause);
  }
}
