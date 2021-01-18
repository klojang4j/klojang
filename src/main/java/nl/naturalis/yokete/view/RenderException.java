package nl.naturalis.yokete.view;

public class RenderException extends RuntimeException {

  public RenderException(String message) {
    super(message);
  }

  public RenderException(String message, Throwable cause) {
    super(message, cause);
  }
}
