package nl.naturalis.yokete.util;

import java.util.Optional;
import nl.naturalis.common.check.Check;
import nl.naturalis.common.collection.TypeMap;
import nl.naturalis.common.collection.UnmodifiableTypeMap;
import nl.naturalis.yokete.view.InvalidStringifierException;
import static nl.naturalis.common.check.CommonChecks.notNull;
import static nl.naturalis.yokete.view.InvalidStringifierException.typeStringifierMustNotReturnNull;
import static nl.naturalis.yokete.view.InvalidStringifierException.typeStringifierNotNullResistant;
/**
 * A class that allows you to configure and retrieve type-specific stringifiers. In other words the
 * stringifiers defined here are not tied to any model object in particular. You can configure a
 * specific {@link Stringifier} for a specific class, but you can also configure a single
 * stringifier for a set of classes that share a base class or interface. If you associate a {@code
 * Stringifier} with a base class or interface, all classes that extend or implement it will be
 * stringified using that {@code Stringifier}.
 *
 * @author Ayco Holleman
 */
public final class TypeStringifiers {

  /* ++++++++++++++++++++[ BEGIN BUILDER CLASS ]+++++++++++++++++ */

  public static final class Builder {
    private final UnmodifiableTypeMap.Builder<Stringifier> bldr;

    private Builder() {
      this.bldr = UnmodifiableTypeMap.build();
    }

    public void add(Class<?> type, Stringifier stringifier) {
      bldr.add(type, stringifier);
    }

    public TypeStringifiers freeze() {
      return new TypeStringifiers(bldr.freeze());
    }
  }

  /* +++++++++++++++++++++[ END BUILDER CLASS ]++++++++++++++++++ */

  public static Builder configure() {
    return new Builder();
  }

  public static final TypeStringifiers NONE = configure().freeze();

  private final TypeMap<Stringifier> typeMap;

  private TypeStringifiers(TypeMap<Stringifier> typeMap) {
    this.typeMap = typeMap;
  }

  public Optional<Stringifier> getStringifier(Class<?> type) {
    return Optional.ofNullable(typeMap.get(type));
  }

  public Optional<String> stringify(Object value) throws InvalidStringifierException {
    Check.that(value).is(notNull(), "Cannot stringify null without knowing type");
    Stringifier stringifier = typeMap.get(value.getClass());
    if (stringifier != null) {
      String s = stringifier.stringify(value);
      if (s == null) {
        throw typeStringifierMustNotReturnNull(value);
      }
      return Optional.of(s);
    }
    return Optional.empty();
  }

  public Optional<String> stringify(Class<?> type, Object value)
      throws InvalidStringifierException {
    Check.notNull(type, "type");
    Stringifier stringifier = typeMap.get(type);
    if (stringifier != null) {
      try {
        String s = stringifier.stringify(value);
        if (s == null) {
          throw typeStringifierMustNotReturnNull(value);
        }
        return Optional.of(s);
      } catch (NullPointerException e) {
        throw typeStringifierNotNullResistant(type);
      }
    }
    return Optional.empty();
  }
}
