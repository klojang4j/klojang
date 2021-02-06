package nl.naturalis.yokete.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.UnaryOperator;
import nl.naturalis.common.check.Check;
import nl.naturalis.yokete.view.InvalidStringifierException;
import static nl.naturalis.common.check.CommonChecks.notNull;

public class ViewDataStringifiers {

  private static final String ERR0 = "%s illegally returned null";
  private static final String CUSTOM_STRINGIFIER = "custom stringifier";
  private static final String NULL_STRINGIFIER = "null stringifier";
  private static final String CATCH_ALL_STRINGIFIER = "catch-all stringifier";

  private final GenericStringifiers genericStringifiers;
  private final Map<String, Class<?>> varNameToType = new HashMap<>();

  public ViewDataStringifiers(GenericStringifiers typeStringifiers) {
    this.genericStringifiers = typeStringifiers;
  }

  public String stringify(String varName, Object value) {
    BiFunction<String, Object, String> cs = getCustomStringifier(varName, value);
    if (cs != null) {
      try {
        String s = cs.apply(varName, value);
        return checkReturnValue(s).is(notNull(), ERR0, CUSTOM_STRINGIFIER).ok();
      } catch (NullPointerException e) {
        throw notNullResistent(CUSTOM_STRINGIFIER);
      }
    }
    Class<?> type = varNameToType.get(varName);
    if (value == null) {
      UnaryOperator<String> uo = getNullStringifier(varName);
      if (uo != null) {
        try {
          String s = uo.apply(varName);
          return checkReturnValue(s).is(notNull(), ERR0, NULL_STRINGIFIER).ok();
        } catch (NullPointerException e) {
          throw notNullResistent(NULL_STRINGIFIER);
        }
      }
      if (type != null) {
        Optional<String> opt = genericStringifiers.stringify(type, value);
        if (!opt.isEmpty()) return opt.get();
      }
    } else {
      if (type == null) {
        type = value.getClass();
      }
      Optional<String> opt = genericStringifiers.stringify(type, value);
      if (!opt.isEmpty()) return opt.get();
    }
    Stringifier stringifier = getCatchAllStringifier();
    Check.notNull(stringifier, CATCH_ALL_STRINGIFIER);
    try {
      String s = stringifier.stringify(value);
      return checkReturnValue(s).is(notNull(), ERR0, CATCH_ALL_STRINGIFIER).ok();
    } catch (NullPointerException e) {
      throw notNullResistent(CATCH_ALL_STRINGIFIER);
    }
  }

  public void mapVariable(String varName, Class<?> type) {
    varNameToType.put(varName, type);
  }

  @SuppressWarnings("unused")
  protected BiFunction<String, Object, String> getCustomStringifier(String varName, Object value) {
    return null;
  }

  @SuppressWarnings("unused")
  protected UnaryOperator<String> getNullStringifier(String varName) {
    return null;
  }

  protected Stringifier getCatchAllStringifier() {
    return Stringifier.BASIC;
  }

  private static Check<String, InvalidStringifierException> checkReturnValue(String s) {
    return Check.with(InvalidStringifierException::new, s);
  }

  private static InvalidStringifierException notNullResistent(String stringifier) {
    return new InvalidStringifierException(stringifier + " not capable of handling null values");
  }
}
