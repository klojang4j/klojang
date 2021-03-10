package nl.naturalis.yokete.render;

import nl.naturalis.common.ClassMethods;
import nl.naturalis.yokete.template.Template;
import static java.lang.String.format;

public class BadStringifierException extends RenderException {

  public static BadStringifierException templateStringifierReturnedNull(
      Template tmpl, String varName) {
    String fmt = "Bad template stringifier for %s.%s: illegally returned null";
    return new BadStringifierException(format(fmt, tmpl.getName(), varName));
  }

  public static BadStringifierException templateStringifierNotNullResistant(
      Template tmpl, String varName) {
    String fmt = "Bad template stringifier for %s.%s: cannot handle null values";
    return new BadStringifierException(format(fmt, tmpl.getName(), varName));
  }

  public static BadStringifierException applicationStringifierReturnedNull(Class<?> type) {
    String fmt = "Bad application stringifier for type %s: illegally returned null";
    String cn = ClassMethods.prettyClassName(type);
    return new BadStringifierException(format(fmt, cn));
  }

  public static BadStringifierException applicationStringifierNotNullResistant(Class<?> type) {
    String fmt = "Bad template stringifier for  type %s: cannot handle null values";
    String cn = ClassMethods.prettyClassName(type);
    return new BadStringifierException(format(fmt, cn));
  }

  private BadStringifierException(String message) {
    super(message);
  }
}
