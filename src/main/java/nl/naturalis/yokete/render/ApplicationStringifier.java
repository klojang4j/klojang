package nl.naturalis.yokete.render;

import java.time.LocalDateTime;
import nl.naturalis.common.collection.TypeMap;
import nl.naturalis.common.collection.UnmodifiableTypeMap;
import nl.naturalis.yokete.render.TemplateStringifier.VariableStringifier;
/**
 * An {@code ApplicationStringifier} provides application-wide stringification services. Like the
 * {@link TemplateStringifier} its goal is to stringify the values served up by the data layer so
 * the can be inserted into an HTML template. Unlike the {@code TemplateStringifier}, however, the
 * way it stringifies values is not tied to any template variable in particular. It is determined
 * determined solely by the value's type.
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
 * interface. Instead, the (singleton) {@code ApplicationStringifier} is meant to be passed on to
 * {@code TemplateStringifier} instances.
 *
 * <h4>Providing alternative stringifications for a single type</h4>
 *
 * <p>Sometimes you will want to stringify the same type in multiple ways. For example {@link
 * LocalDateTime} objects may have to formatted differently in different parts of your application.
 * This can be achieved without having to write endless amounts of variable-specific stringifiers:
 *
 * <p>
 *
 * <ul>
 *   <li>Register the stringifier that uses date format X under key {@code LocalDateTime.class}
 *   <li>Register the stringifier that used date format Y under some home-grown tag interface (let's
 *       say {@code DateFormat2})
 *   <li>Then, when configuring a {@code TemplateStringifier}, {@link
 *       TemplateStringifier.Builder#setType(String, String, Class) set} the type of date variables
 *       that must be formatted according to date format Y to {@code DateTimeFormat2.class}.
 * </ul>
 *
 * @author Ayco Holleman
 */
public final class ApplicationStringifier {

  /* ++++++++++++++++++++[ BEGIN BUILDER CLASS ]+++++++++++++++++ */

  /**
   * A {@code Buider} class for {@code ApplicationStringifier} instances.
   *
   * @author Ayco Holleman
   */
  public static final class Builder {
    private final UnmodifiableTypeMap.Builder<VariableStringifier> bldr;

    private Builder() {
      this.bldr = UnmodifiableTypeMap.build();
    }

    /**
     * Registers a stringifier for the specified type.
     *
     * @param type The type for which to register the stringifier
     * @param stringifier The stringifier
     */
    public void register(Class<?> type, VariableStringifier stringifier) {
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

  private final TypeMap<VariableStringifier> typeMap;

  private ApplicationStringifier(TypeMap<VariableStringifier> typeMap) {
    this.typeMap = typeMap;
  }

  boolean canStringify(Class<?> type) {
    return typeMap.containsKey(type);
  }

  VariableStringifier getStringifier(Class<?> type) {
    return typeMap.get(type);
  }
}
