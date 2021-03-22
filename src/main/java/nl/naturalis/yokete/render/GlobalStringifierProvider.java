package nl.naturalis.yokete.render;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import nl.naturalis.common.collection.TypeMap;
import nl.naturalis.common.collection.UnmodifiableTypeMap;
import static java.util.Collections.*;
/**
 * Provides type-based stringfiers. In most cases the way a value must be stringified doesn't really
 * depend on any template variable in particular but rather on the data type of the variable. In
 * other words, unless a template variable has very specific stringification requirements, its
 * values will be stringified based solely on its type. You could, for example, have a {@link
 * Number} stringifier, a {@link LocalDate} stringifier, a {@link File} stringifier, etc. The {@code
 * GlobalStringifierProvider} lets you configure stringifiers on a per-type basis.
 *
 * <p>The {@code GlobalStringifierProvider} class is built around a {@link TypeMap}. If a
 * stringifier is requested for a some type, and that type is not in the {@code TypeMap} but one of
 * its super types is, then you get that super type's stringifier. For example, if the {@code
 * TypeMap} contains a {@code Number} stringifier and you request an {@code Integer} stringifier,
 * you get the {@code Number} stringifier. This saves you from having to specify a stringifier for
 * every subclass of {@code Number} if they are all stringified in the same way.
 *
 * <p>The {@code GlobalStringifierProvider} class is not meant to be used directly. It does, in
 * fact, not even have a public interface. Instead, you should configure an instance of it (most
 * likely a singleton instance) and pass it to {@link
 * StringifierProvider#configure(nl.naturalis.yokete.template.Template, GlobalStringifierProvider)
 * StringifierProvider.configure}.
 *
 * <h4>Providing alternative stringifiers for a single type</h4>
 *
 * <p>Sometimes you will want to stringify the same type in multiple ways. For example {@link
 * LocalDate} objects may have to formatted differently in different parts of your application. You
 * can do this via the {@link StringifierProvider}, whichs lets you specify stringifiers for every
 * single variable within a template, but it can also be done more centrally via the {@code
 * GlobalStringifierProvider}:
 *
 * <p>
 *
 * <ul>
 *   <li>Register the {@link DateTimeFormatter} for the primary date format under type {@code
 *       LocalDate.class}.
 *   <li>Create an arbitrary tag interface (say {@code DateFormat2}) and register the {@link
 *       DateTimeFormatter} for the secondary date format under that type.
 *   <li>Now you can {@link StringifierProvider.Builder#lookupGlobal(Class,
 *       nl.naturalis.yokete.template.Template, String...) configure} your template variable to use
 *       either stringifier.
 * </ul>
 *
 * In fact, this particular scenario is so plausible that the {@code GlobalStringifierProvider}
 * class already contains these tag interfaces.
 *
 * @author Ayco Holleman
 */
public final class GlobalStringifierProvider {

  /**
   * A {@code GlobalStringifierProvider} without any stringifier. In rare case where all template
   * variables have very specific (i.e. variable-dependent) stringification requirements, you can
   * pass this instance to {@link
   * StringifierProvider#configure(nl.naturalis.yokete.template.Template,
   * GlobalStringifierProvider)}
   */
  public static final GlobalStringifierProvider EMPTY = new GlobalStringifierProvider(emptyMap());

  /** Can be used to register an alternative stringifier for {@link LocalDate} objects */
  public static interface DateFormat2 {};

  /** Can be used to register an alternative stringifier for {@link LocalDate} objects */
  public static interface DateFormat3 {};

  /** Can be used to register an alternative stringifier for {@link LocalDateTime} objects */
  public static interface DateTimeFormat2 {};

  /** Can be used to register an alternative stringifier for {@link LocalDateTime} objects */
  public static interface DateTimeFormat3 {};

  /* ++++++++++++++++++++[ BEGIN BUILDER CLASS ]+++++++++++++++++ */

  /**
   * A {@code Buider} class for {@code GlobalStringifierProvider} instances.
   *
   * @author Ayco Holleman
   */
  public static final class Builder {
    private final UnmodifiableTypeMap.Builder<Stringifier> typeMapBuilder;

    private Builder() {
      this.typeMapBuilder = UnmodifiableTypeMap.build();
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
    public void register(Class<?> type, Stringifier stringifier) {
      typeMapBuilder.add(type, stringifier);
    }

    /**
     * Returns a new, immutable {@code GlobalStringifierProvider} instance.
     *
     * @return A new, immutable {@code GlobalStringifierProvider} instance
     */
    public GlobalStringifierProvider freeze() {
      return new GlobalStringifierProvider(typeMapBuilder.freeze());
    }
  }

  /* +++++++++++++++++++++[ END BUILDER CLASS ]++++++++++++++++++ */

  /**
   * Returns a {@link Builder} object that lets you configure an {@code GlobalStringifierProvider}
   * instance.
   *
   * @return A {@code Configurator} object that lets you configure an {@code
   *     GlobalStringifierProvider} instance
   */
  public static Builder configure() {
    return new Builder();
  }

  private final Map<Class<?>, Stringifier> typeMap;

  private GlobalStringifierProvider(Map<Class<?>, Stringifier> typeMap) {
    this.typeMap = typeMap;
  }

  boolean hasStringifier(Class<?> type) {
    return typeMap.containsKey(type);
  }

  Stringifier getStringifier(Class<?> type) {
    return typeMap.get(type);
  }
}
