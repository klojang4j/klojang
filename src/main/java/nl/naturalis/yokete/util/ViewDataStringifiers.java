package nl.naturalis.yokete.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.UnaryOperator;
import nl.naturalis.common.check.Check;
import nl.naturalis.yokete.view.InvalidStringifierException;
import static nl.naturalis.yokete.view.InvalidStringifierException.customStringifierMustNotReturnNull;
import static nl.naturalis.yokete.view.InvalidStringifierException.customStringifierNotNullResistant;

public class ViewDataStringifiers {

  private static final String CUSTOM_STRINGIFIER = "Custom stringifier";
  private static final String NULL_STRINGIFIER = "Null stringifier";
  private static final String CATCH_ALL_STRINGIFIER = "Catch-all stringifier";

  private final TypeStringifiers genericStringifiers;
  private final Map<String, Class<?>> varNameToType = new HashMap<>();

  public ViewDataStringifiers(TypeStringifiers typeStringifiers) {
    this.genericStringifiers = typeStringifiers;
  }

  public String stringify(String varName, Object value) throws InvalidStringifierException {
    BiFunction<String, Object, String> cs = getCustomStringifier(varName, value);
    if (cs != null) {
      try {
        String s = cs.apply(varName, value);
        if (s == null) {
          throw customStringifierMustNotReturnNull(CUSTOM_STRINGIFIER);
        }
        return s;
      } catch (NullPointerException e) {
        throw customStringifierNotNullResistant(CUSTOM_STRINGIFIER);
      }
    }
    Class<?> type = varNameToType.get(varName);
    if (value == null) {
      UnaryOperator<String> uo = getNullStringifier(varName);
      if (uo != null) {
        try {
          String s = uo.apply(varName);
          if (s == null) {
            throw customStringifierMustNotReturnNull(NULL_STRINGIFIER);
          }
          return s;
        } catch (NullPointerException e) {
          throw customStringifierNotNullResistant(NULL_STRINGIFIER);
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
      if (s == null) {
        throw customStringifierMustNotReturnNull(CATCH_ALL_STRINGIFIER);
      }
      return s;
    } catch (NullPointerException e) {
      throw customStringifierNotNullResistant(CATCH_ALL_STRINGIFIER);
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
}
