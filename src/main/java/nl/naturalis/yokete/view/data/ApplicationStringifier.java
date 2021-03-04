package nl.naturalis.yokete.view.data;

import java.util.Optional;
import nl.naturalis.common.collection.TypeMap;
import nl.naturalis.common.collection.UnmodifiableTypeMap;
import nl.naturalis.yokete.view.Stringifier;
import nl.naturalis.yokete.view.TemplateStringifier.VariableStringifier;
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
public final class ApplicationStringifier {

  public static interface TypeStringifier {
    default VariableStringifier toVariableStringifier() {}

    String stringify(Class<?> type, Object value);
  }

  /* ++++++++++++++++++++[ BEGIN BUILDER CLASS ]+++++++++++++++++ */

  public static final class Builder {
    private final UnmodifiableTypeMap.Builder<TypeStringifier> bldr;

    private Builder() {
      this.bldr = UnmodifiableTypeMap.build();
    }

    public void addStringifier(Class<?> type, TypeStringifier stringifier) {
      bldr.add(type, stringifier);
    }

    public ApplicationStringifier freeze() {
      return new ApplicationStringifier(bldr.freeze());
    }
  }

  /* +++++++++++++++++++++[ END BUILDER CLASS ]++++++++++++++++++ */

  public static Builder configure() {
    return new Builder();
  }

  public static final ApplicationStringifier NONE = configure().freeze();

  private final TypeMap<TypeStringifier> typeMap;

  private ApplicationStringifier(TypeMap<TypeStringifier> typeMap) {
    this.typeMap = typeMap;
  }

  public boolean canStringify(Class<?> type) {
    return typeMap.containsKey(type);
  }

  public Optional<TypeStringifier> getStringifier(Class<?> type) {
    return Optional.ofNullable(typeMap.get(type));
  }
}
