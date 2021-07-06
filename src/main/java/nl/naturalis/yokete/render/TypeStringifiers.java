package nl.naturalis.yokete.render;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import nl.naturalis.common.check.Check;
import nl.naturalis.common.collection.TypeMap;
import static java.util.Collections.emptyMap;
/**
 * A repository and provider of type-based stringifiers. These stringifiers are not associated with
 * any template or template variable in particular. They stringify values based on their data type.
 * The {@code TypeStringifiers} class is built around a {@link TypeMap}. This means that if a
 * stringifier is requested for some type, and that type is not in the {@code TypeMap}, but one of
 * its super types is, then you get the stringifier associated with the super type. For example, if
 * the {@code TypeMap} contains a {@code Number} stringifier and you request an {@code Integer}
 * stringifier, you get the {@code Number} stringifier. This saves you from having to specify a
 * stringifier for every subclass of {@code Number} if they are all stringified in the same way.
 *
 * <p>The {@code TypeStringifiers} class is not meant to be used directly. It does not even have a
 * public interface. Instead, you should configure an instance of it (most likely a singleton
 * instance) and pass it to {@link
 * TemplateStringifiers#configure(nl.naturalis.yokete.template.Template, TypeStringifiers)
 * TemplateStringifiers.configure}.
 *
 * <h4>Providing alternative stringifiers for a single type</h4>
 *
 * <p>Sometimes you will want to stringify the same type in multiple ways. For example {@link
 * LocalDate} objects may have to formatted differently in different parts of your application. You
 * can do this via the {@link TemplateStringifiers}, whichs lets you specify stringifiers for every
 * single variable within a template, but it can also be done more centrally via the {@code
 * TypeStringifiers}:
 *
 * <p>
 *
 * <ul>
 *   <li>Register the {@link DateTimeFormatter} for the primary date format under type {@code
 *       LocalDate.class}.
 *   <li>Create an arbitrary tag interface (say {@code DateFormat2}) and register the {@link
 *       DateTimeFormatter} for the secondary date format under that type.
 *   <li>Now you can {@link TemplateStringifiers.Builder#setGlobalStringifier(Class,
 *       nl.naturalis.yokete.template.Template, String...) configure} your template variable to use
 *       either stringifier.
 * </ul>
 *
 * In fact, this particular scenario is so plausible that the {@code TypeStringifiers} class
 * already contains these tag interfaces.
 *
 * @author Ayco Holleman
 */
public final class TypeStringifiers {

  /**
   * A {@code TypeStringifiers} without any stringifier. In rare case where all template variables
   * have very specific (i.e. variable-dependent) stringification requirements, you can pass this
   * instance to {@link TemplateStringifiers#configure(nl.naturalis.yokete.template.Template,
   * TypeStringifiers)}
   */
  public static final TypeStringifiers EMPTY = new TypeStringifiers(emptyMap());

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
   * A Builder class for {@code TypeStringifiers} instances.
   *
   * @author Ayco Holleman
   */
  public static final class Builder {
    private final TypeMap<Stringifier> typeMap;

    private Builder() {
      this.typeMap = new TypeMap<>();
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
    public Builder addStringifier(Class<?> type, Stringifier stringifier) {
      Check.notNull(type, "type");
      Check.notNull(stringifier, "stringifier");
      typeMap.put(type, stringifier);
      return this;
    }

    /**
     * Returns a new, immutable {@code TypeStringifiers} instance.
     *
     * @return A new, immutable {@code TypeStringifiers} instance
     */
    public TypeStringifiers freeze() {
      return new TypeStringifiers(typeMap);
    }
  }

  /* +++++++++++++++++++++[ END BUILDER CLASS ]++++++++++++++++++ */

  /**
   * Returns a {@link Builder} object that lets you configure an {@code TypeStringifiers}
   * instance.
   *
   * @return A {@code Configurator} object that lets you configure an {@code TypeStringifiers}
   *     instance
   */
  public static Builder configure() {
    return new Builder();
  }

  private final Map<Class<?>, Stringifier> typeMap;

  private TypeStringifiers(Map<Class<?>, Stringifier> typeMap) {
    this.typeMap = typeMap;
  }

  boolean hasStringifier(Class<?> type) {
    return typeMap.containsKey(type);
  }

  Stringifier getStringifier(Class<?> type) {
    return typeMap.get(type);
  }
}
