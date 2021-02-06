package nl.naturalis.yokete.util;

import java.util.Optional;
import nl.naturalis.common.check.Check;
import nl.naturalis.common.collection.TypeMap;
import nl.naturalis.common.collection.UnmodifiableTypeMap;
import nl.naturalis.yokete.view.InvalidStringifierException;
import static nl.naturalis.common.ClassMethods.getClassName;
import static nl.naturalis.common.check.CommonChecks.notNull;

public final class GenericStringifiers {

  /* ++++++++++++++++++++[ BEGIN BUILDER CLASS ]+++++++++++++++++ */

  public static final class Builder {
    private final UnmodifiableTypeMap.Builder<Stringifier> bldr;

    private Builder() {
      this.bldr = UnmodifiableTypeMap.build();
    }

    public void add(Class<?> type, Stringifier stringifier) {
      bldr.add(type, stringifier);
    }

    public GenericStringifiers freeze() {
      return new GenericStringifiers(bldr.freeze());
    }
  }

  /* +++++++++++++++++++++[ END BUILDER CLASS ]++++++++++++++++++ */

  public static Builder configure() {
    return new Builder();
  }

  public static final GenericStringifiers NONE = configure().freeze();

  private static final String ERR0 = "Invalid Stringifier for type %s: ";
  private static final String ERR1 = ERR0 + "not capable of handling null values";
  private static final String ERR2 = ERR0 + "illegally returned null";

  private final TypeMap<Stringifier> typeMap;

  private GenericStringifiers(TypeMap<Stringifier> typeMap) {
    this.typeMap = typeMap;
  }

  public Optional<Stringifier> getStringifier(Class<?> type) {
    return Optional.ofNullable(typeMap.get(type));
  }

  public Optional<String> stringify(Object value) {
    Check.that(value).is(notNull(), "Cannot stringify null without knowing type");
    Stringifier stringifier = typeMap.get(value.getClass());
    if (stringifier != null) {
      String s = stringifier.stringify(value);
      checkReturnValue(s).is(notNull(), ERR2, getClassName(value));
      return Optional.of(s);
    }
    return Optional.empty();
  }

  public Optional<String> stringify(Class<?> type, Object value) {
    Check.notNull(type, "type");
    Stringifier stringifier = typeMap.get(type);
    if (stringifier != null) {
      try {
        String s = stringifier.stringify(value);
        checkReturnValue(s).is(notNull(), ERR2, getClassName(value));
        return Optional.of(s);
      } catch (NullPointerException e) {
        throw new InvalidStringifierException(String.format(ERR1, getClassName(value)));
      }
    }
    return Optional.empty();
  }

  private static Check<String, InvalidStringifierException> checkReturnValue(String s) {
    return Check.with(InvalidStringifierException::new, s);
  }
}
