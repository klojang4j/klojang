package nl.naturalis.yokete.render;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import nl.naturalis.common.collection.TypeMap;
import nl.naturalis.common.collection.UnmodifiableTypeMap;
import nl.naturalis.common.function.ThrowingFunction;
/**
 * An {@code ApplicationStringifier} provides application-wide, type-based, stringification
 * capabilities. Unless a template variable has very specific stringification requirements, its
 * values will be stringified based solely on its type. You could, For example, have a {@link
 * Number} stringifier, a {@link LocalDate} stringifier, a {@link File} stringifier, etc. The {@code
 * ApplicationStringifier} lets you specify stringifiers on a per-type basis.
 *
 * <p>The {@code ApplicationStringifier} class is built around a {@link TypeMap}. If a stringifier
 * is requested for a some type, and that type is not in the {@code TypeMap} but one of its super
 * types is, then you get that super type's stringifier. For example, if the {@code TypeMap}
 * contains a {@code Number} stringifier and you request an {@code Integer} stringifier, you get the
 * {@code Number} stringifier. This saves you from having to specify a stringifier for each and
 * every subclass of {@code Number} if they are all stringified in the same way.
 *
 * <p>The {@code ApplicationStringifier} class is not meant to be used directly. It does, in fact,
 * not even have a public interface. Instead, you should configure a (singleton) instance and {@link
 * StringifierConfig#setApplicationStringifier(ApplicationStringifier) register} it with one
 * or more {@link StringifierConfig} instances.
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
 *   <li>Register the stringifier that used date format Y under an arbitrary tag interface (let's
 *       say {@code LocalDateTime2})
 *   <li>Then, when configuring a template-specific {@code Stringifier}, {@link
 *       StringifierConfig#setType(String, String, Class) set} the type of date variables that
 *       must be formatted according to date format Y to {@code LocalDateTime2.class}.
 * </ul>
 *
 * In fact, this particular scenario is so plausible that the {@code ApplicationStringifier} class
 * already contains these tag interfaces.
 *
 * @author Ayco Holleman
 */
public final class ApplicationStringifier {

  /** Can be used to register an alternative stringifier for {@link LocalDate} objects */
  public static interface LocalDate2 {};

  /** Can be used to register an alternative stringifier for {@link LocalDate} objects */
  public static interface LocalDate3 {};

  /** Can be used to register an alternative stringifier for {@link LocalDateTime} objects */
  public static interface LocalDateTime2 {};

  /** Can be used to register an alternative stringifier for {@link LocalDateTime} objects */
  public static interface LocalDateTime3 {};

  /* ++++++++++++++++++++[ BEGIN BUILDER CLASS ]+++++++++++++++++ */

  /**
   * A {@code Buider} class for {@code ApplicationStringifier} instances.
   *
   * @author Ayco Holleman
   */
  public static final class Config {
    private final UnmodifiableTypeMap.Builder<ThrowingFunction<Object, String, RenderException>>
        bldr;

    private Config() {
      this.bldr = UnmodifiableTypeMap.build();
    }

    /**
     * Registers a stringifier for the specified type.
     *
     * @param type The type for which to register the stringifier
     * @param stringifier A function that takes an {@code Object} and returns a {@code String} while
     *     potentially throwing a {@code RenderException} in the process. As with {@link
     *     Stringifier} implementations, the function <b>must</b> be able to handle null values and
     *     it <b>must never</b> return null
     */
    public void register(
        Class<?> type, ThrowingFunction<Object, String, RenderException> stringifier) {
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
   * Returns a {@link Config} object that lets you configure an {@code ApplicationStringifier}
   * instance.
   *
   * @return A {@code Configurator} object that lets you configure an {@code ApplicationStringifier}
   *     instance
   */
  public static Config configure() {
    return new Config();
  }

  private final TypeMap<ThrowingFunction<Object, String, RenderException>> typeMap;

  private ApplicationStringifier(
      TypeMap<ThrowingFunction<Object, String, RenderException>> typeMap) {
    this.typeMap = typeMap;
  }

  boolean canStringify(Class<?> type) {
    return typeMap.containsKey(type);
  }

  Stringifier getStringifier(Class<?> type) {
    // Just ignore template and var name and do your thing on the value (z)
    return (x, y, z) -> typeMap.get(type).apply(z);
  }
}
