package nl.naturalis.yokete.view;

import java.time.LocalDateTime;
import nl.naturalis.common.collection.TypeMap;
import nl.naturalis.common.collection.UnmodifiableTypeMap;
import nl.naturalis.yokete.view.TemplateStringifier.VariableStringifier;
/**
 * An {@code ApplicationStringifier} provides application-wide stringification services. Like the
 * {@link TemplateStringifier} its goal is to stringify the values served up by the data layer so
 * the can be inserted into an HTML template. Unlike the {@code TemplateStringifier}, however, its
 * stringification mechanism is not tied to any template variable in particular. Instead, the way it
 * stringifies a value is determined solely by the value's type. An {@code ApplicationStringifier}
 * does not, in fact, stringify any value itself. Rather it is a container of {@link
 * TypeStringifier} instances, which can be retrieved by type (i.e. a {@link Class} object). The
 * {@code TypeStringifier} instances do the actual stringification.
 *
 * <p>The {@code ApplicationStringifier} class is built around a {@link TypeMap}, meaning that if
 * the {@code TemplateStringifier} requests a stringifier for a particular type, and that type is
 * not in the {@code TypeMap} but one of its super types is, then you get that super type's
 * stringifier. For example, if the {@code TypeMap} contains stringifiers for {@link Number} and
 * {@link Byte} and you request the {@code Byte} stringifier you obviously get that one, but if you
 * request an {@link Integer} stringifier, you get the {@code Number} stringifier. This saves you
 * from having to specify a stringifier for each and every {@code Number} class if they are all
 * stringified alike.
 *
 * <p>An {@code ApplicationStringifier} is an immutable object. You should probably create just one
 * instance of it and keep it around for as long as your application lasts. The {@code
 * ApplicationStringifier} is not meant to be used directly. Apart from exposing a {@link Builder}
 * object that lets you configure an instance, it does, in fact, not even <i>have</i> a public
 * interface. Instead, the (singleton) {@code ApplicationStringifier} is meant to be used by {@code
 * TemplateStringifier} instances
 *
 * @author Ayco Holleman
 */
public final class ApplicationStringifier {

  /**
   * Stringifies objects. Unless the {@link TemplateStringifier} contains a specialized {@link
   * VariableStringifier} for a particular template variable, it will request the {@code
   * ApplicationStringifier} to provide a stringifier that can stringify the variable's value based
   * on its type. It passes the value's type to the {@code ApplicationStringifier} and gets back a
   * stringifier suitable for that type.
   *
   * <p>Note, however, that there is no trace of this type-specificity within the {@code
   * TypeStringifier} interface itself. The {@code TypeStringifier} interface is a deliberately
   * non-parametrized type. This allows you to {@link TemplateStringifier.Builder#setType(String,
   * Class) register} two or more stringification variants for values of the same type. For example,
   * if your application uses two date-time formats to stringify {@link LocalDateTime} objects, you
   * could register the stringifier that uses format X under key {@code LocalDateTime.class} while
   * using an arbitrary tag interface (e.g. <code>
   * public interface DateTimeFormat2 { &#47;&#42; nothing here &#42;&#47; }</code>) to register the
   * stringifier that uses format Y.
   *
   * <p>Stringifier implementations <b>must</b> be able to handle null values and the <b>must
   * not</b> return null.
   *
   * @author Ayco Holleman
   */
  @FunctionalInterface
  public static interface TypeStringifier {
    /**
     * Converts this {@code TypeStringifier} to a {@code VariableStringifier}.
     *
     * @return A {@link VariableStringifier}
     */
    default VariableStringifier toVariableStringifier() {
      return (tmpl, var, val) -> stringify(val);
    }

    /**
     * Stringifies the specified value.
     *
     * @param value The value to stringify
     * @return A string represenation of the value
     */
    String stringify(Object value);
  }

  /* ++++++++++++++++++++[ BEGIN BUILDER CLASS ]+++++++++++++++++ */

  /**
   * A {@code Buider} class for {@code ApplicationStringifier} instances.
   *
   * @author Ayco Holleman
   */
  public static final class Builder {
    private final UnmodifiableTypeMap.Builder<TypeStringifier> bldr;

    private Builder() {
      this.bldr = UnmodifiableTypeMap.build();
    }

    /**
     * Registers a stringifier for the specified type.
     *
     * @param type The type for which to register the stringifier
     * @param stringifier The stringifier
     */
    public void addStringifier(Class<?> type, TypeStringifier stringifier) {
      bldr.add(type, stringifier);
    }

    /**
     * Returns a new, immutable {@code ApplicationStringifier} instance.
     *
     * @return A new, immutable {@code ApplicationStringifier} instance
     */
    public ApplicationStringifier freeze() {
      return new ApplicationStringifier(bldr.freeze());
    }
  }

  /* +++++++++++++++++++++[ END BUILDER CLASS ]++++++++++++++++++ */

  /**
   * Returns a {@link Builder} object that lets you configure an {@code ApplicationStringifier}
   * instance.
   *
   * @return A {@code Builder} object that lets you configure an {@code ApplicationStringifier}
   *     instance
   */
  public static Builder configure() {
    return new Builder();
  }

  private final TypeMap<TypeStringifier> typeMap;

  private ApplicationStringifier(TypeMap<TypeStringifier> typeMap) {
    this.typeMap = typeMap;
  }

  boolean canStringify(Class<?> type) {
    return typeMap.containsKey(type);
  }

  TypeStringifier getStringifier(Class<?> type) {
    return typeMap.get(type);
  }
}
