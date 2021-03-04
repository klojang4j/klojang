package nl.naturalis.yokete.view;

import java.util.function.Function;
import nl.naturalis.common.ClassMethods;
import static java.lang.String.format;

public class InvalidStringifierException extends RenderException {

  private static final String ERR_BASE = "Invalid Stringifier for type %s: ";

  private static final String TS_NOT_NULL_RESISTANT =
      ERR_BASE + "not capable of handling null values";
  private static final String TS_MUST_NOT_RETURN_NULL = ERR_BASE + "illegally returned null";

  private static final String CS_NOT_NULL_RESISTANT = "%s not capable of handling null values";
  private static final String MUST_NOT_RETURN_NULL = "%s illegally returned null";

  public static InvalidStringifierException typeStringifierNotNullResistant(Class<?> type) {
    return new InvalidStringifierException(String.format(TS_NOT_NULL_RESISTANT, type));
  }

  public static InvalidStringifierException stringifierNotNullResistant(String name) {
    return new InvalidStringifierException(String.format(CS_NOT_NULL_RESISTANT, name));
  }

  public static InvalidStringifierException typeStringifierMustNotReturnNull(Object toStringify) {
    String className = ClassMethods.prettyClassName(toStringify);
    return new InvalidStringifierException(String.format(TS_MUST_NOT_RETURN_NULL, className));
  }

  public static Function<String, InvalidStringifierException> stringifierReturnedNull(String name) {
    return s -> new InvalidStringifierException(format(MUST_NOT_RETURN_NULL, name));
  }

  public InvalidStringifierException(String message) {
    super(message);
  }
}
