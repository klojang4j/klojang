package nl.naturalis.yokete.render;

import java.util.HashMap;
import java.util.Map;
import nl.naturalis.common.Tuple;
import nl.naturalis.common.check.Check;
import nl.naturalis.common.collection.TypeMap;
import nl.naturalis.yokete.template.Template;
import nl.naturalis.yokete.template.TemplateUtils;
import static java.util.Collections.emptyMap;
import static nl.naturalis.common.ObjectMethods.ifNull;
import static nl.naturalis.common.check.CommonChecks.deepNotEmpty;
import static nl.naturalis.common.check.CommonChecks.in;
import static nl.naturalis.common.check.CommonChecks.keyIn;
import static nl.naturalis.common.check.CommonChecks.yes;
import static nl.naturalis.yokete.template.TemplateUtils.getDeeplyNestedTemplate;

/**
 * Provides {@link Stringifier stringifiers} for template variables. In principle each and every
 * template variable must be associated with a {@code Stringifier}. In practice, however, it is
 * unlikely you will define many variable-specific stringifiers. If a variable's value can be
 * stringified by calling {@code toString()} on it (or to an empty string if null), you don't need
 * to specify a stringifier for it because this is default behaviour. In addition, all variables
 * with the same data type will usually also have to be stringified in the same way. (For example
 * you may want to format all integers according to your country's locale.) These generic,
 * type-based stringifiers can be configured using {@link Builder#setTypeStringifier(String...,
 * Class) Builder.setTypeStringifier}. Only if a template variable has very specific stringification
 * requirements would you register the stringifier using {@link Builder#setStringifier(Stringifier,
 * String...) Builder.setStringifier}.
 *
 * <p>Type-based stringifiers are internally kept in a {@link TypeMap}. This means that if a
 * stringifier is requested for some type, and that type is not in the {@code TypeMap}, but one of
 * its super types is, then you get the stringifier associated with the super type. For example, if
 * the {@code TypeMap} contains a {@code Number} stringifier and you request an {@code Integer}
 * stringifier, you get the {@code Number} stringifier (unless you have also added an {@code
 * Integer} stringifier to the {@code TypeMap}). This saves you from having to specify a stringifier
 * for every subclass of {@code Number} if they are all stringified in the same way.
 *
 * <p>This is how a {@link StringifierFactory} decides which stringifier to hand out for a template
 * variable:
 *
 * <p>
 *
 * <ol>
 *   <li>If a stringifier has been defined for that particular variable in that particular template,
 *       then that is the stringifier that is going to be used.
 *   <li>If a stringifier has been defined for all variables with that particular name (irrespective
 *       of which template they belong to), then that is the stringifier that is going to be used.
 *       See {@link Builder#setNameBasedStringifier(String..., Stringifier)
 *       setNameBasedStringifier}.
 *   <li>If a stringifier has been defined for the data type of that particular variable, then that
 *       is the stringifier that is going to be used.
 *   <li>If you have defined your own default stringifier, then that is the stringifier that is
 *       going to be used.
 *   <li>Otherwise the {@link Stringifier#DEFAULT default stringifier} is going to be used.
 * </ol>
 *
 * @see Page
 * @author Ayco Holleman
 */
public final class StringifierFactory {

  /**
   * A simple, brute-force {@code StringifierFactory} instance that always returns the {@link
   * Stringifier#DEFAULT default stringifier}, whatever the template and whatever the variable.
   * Unlikely to be satisfactory in the end, but handy in the early stages of development.
   */
  public static final StringifierFactory BASIC_STRINGIFIER =
      new StringifierFactory(emptyMap(), emptyMap(), null);

  /**
   * Returns {@link #BASIC_STRINGIFIER}. Can be used as a starting point for building a {@code
   * StringifierFactory} using the {@code withXXX} methods of {@code StringifierFactory} in stead of
   * using a {@link Builder} instance. Note, however, that the {@code withXXX} methods do a lot of
   * back-and-forth copying of internal data structures so are less efficient than using a {@code
   * Builder} instance.
   *
   * @return {@link #BASIC_STRINGIFIER}
   */
  public static StringifierFactory basic() {
    return BASIC_STRINGIFIER;
  }

  /* ++++++++++++++++++++[ BEGIN BUILDER CLASS ]+++++++++++++++++ */

  /**
   * Lets you configure a {@code StringifierFactory} instance for a template. If you don't require
   * any template-specific stringifiers, you can also start out using {@link
   * StringifierFactory#basic()}.
   *
   * @author Ayco Holleman
   */
  public static class Builder {

    private static final String NO_SUCH_VARIABLE = "No such variable: \"%s\"";
    private static final String VAR_ASSIGNED = "Stringifier already set for variable \"%s\"";
    private static final String TYPE_ASSIGNED = "Stringifier already set for type \"%s\"";

    private final Map<Tuple<Template, String>, Stringifier> stringifiers = new HashMap<>();

    private final HashMap<Class<?>, Stringifier> typeStringifiers;

    private Stringifier defStringifier;

    private Builder() {
      this.typeStringifiers = new HashMap<>();
    }

    /**
     * Lets you specifiy your own default stringifier, replacing {@link Stringifier#DEFAULT}.
     *
     * @param stringifier The default stringifier to use
     * @return This {@code Builder}
     */
    public Builder setDefaultStringifier(Stringifier stringifier) {
      this.defStringifier = stringifier;
      return this;
    }

    /**
     * Assigns the specified stringifier to the specified variables within the specified template.
     *
     * @param stringifier The stringifier
     * @param template The template containing the variables
     * @param varNames The variables
     * @return This {@code Builder}
     */
    public Builder setStringifier(Stringifier stringifier, Template template, String... varNames) {
      Check.notNull(stringifier, "stringifier");
      Check.notNull(template, "template");
      Check.that(varNames, "varNames").is(deepNotEmpty());
      doRegister(stringifier, template, varNames);
      return this;
    }

    /**
     * Assigns the specified stringifier to the specified variables within the nested template
     * corresponding the the specified fully-qualified name. See also {@link
     * TemplateUtils#getDeeplyNestedTemplate(Template, String)}.
     *
     * @param stringifier The stringifier
     * @param template The template containing the nested template corresponding to the
     *     fully-qualified template name
     * @param fqName The fully-qualified name, relative to the specified template, of the nested
     *     template
     * @param varNames The variables
     * @return This {@code Builder}
     */
    public Builder setStringifier(
        Stringifier stringifier, Template template, String fqName, String... varNames) {
      Check.notNull(stringifier, "stringifier");
      Check.notNull(template, "template");
      Check.that(varNames, "varNames").is(deepNotEmpty());
      doRegister(stringifier, getDeeplyNestedTemplate(template, fqName), varNames);
      return this;
    }

    /**
     * Assigns the specified stringifier to all variables which have the specified name,
     * irrespective of which template they belong to. This may be useful to stringify consistently
     * named variables (like "dateModified" or "price"). Template-specific stringifiers take
     * precedence over name-based stringifiers, while name-based stringifiers take precedence over
     * type-based stringifiers.
     *
     * @param stringifier The stringifier
     * @param varNames The variable names to associate the stringifier with.
     * @return This {@code Builder}
     */
    public Builder setNameBasedStringifier(Stringifier stringifier, String... varNames) {
      Check.notNull(stringifier, "stringifier");
      Check.that(varNames, "varNames").is(deepNotEmpty());
      doRegister(stringifier, null, varNames);
      return this;
    }

    public Builder setTypeStringifier(Stringifier stringifier, Class<?>... types) {
      Check.notNull(stringifier, "stringifier");
      Check.that(types, "types").is(deepNotEmpty());
      for (Class<?> t : types) {
        Check.that(t).isNot(keyIn(), typeStringifiers, TYPE_ASSIGNED, t.getName());
        typeStringifiers.put(t, stringifier);
      }
      return this;
    }

    /**
     * Returns a new, immutable {@code StringifierFactory} instance.
     *
     * @return A new, immutable {@code StringifierFactory} instance
     */
    public StringifierFactory freeze() {
      return new StringifierFactory(stringifiers, typeStringifiers, defStringifier);
    }

    private void doRegister(Stringifier stringifier, Template t, String... varNames) {
      for (String varName : varNames) {
        Tuple<Template, String> var = Tuple.of(t, varName);
        if (t == null) {
          Check.that(var).isNot(in(), stringifiers.keySet(), VAR_ASSIGNED, varName);
        } else {
          Check.that(varName).has(t::containsVariable, yes(), NO_SUCH_VARIABLE, varName);
          Check.that(var).isNot(in(), stringifiers.keySet(), VAR_ASSIGNED, varName);
        }
        stringifiers.put(var, stringifier);
      }
    }
  }

  /* +++++++++++++++++++++[ END BUILDER CLASS ]++++++++++++++++++ */

  /**
   * Returns a {@code Builder} instance that lets you configure the stringifiers for a template.
   *
   * @param template The template for which to define the stringifiers
   * @param globalStringifiers A {@code TypeStringifiers} instance to retrieve type-based
   *     stringifiers from
   * @return A {@code Builder} instance that lets you configure a {@code StringifierFactory}
   *     instance
   */
  public static Builder configure() {
    return new Builder();
  }

  private final Map<Tuple<Template, String>, Stringifier> stringifiers;
  private final TypeMap<Stringifier> typeStringifiers;
  private final Stringifier defStringifier;

  private StringifierFactory(
      Map<Tuple<Template, String>, Stringifier> stringifiers,
      Map<Class<?>, Stringifier> typeStringifiers,
      Stringifier defStringifier) {
    this.stringifiers = Map.copyOf(stringifiers);
    this.typeStringifiers = new TypeMap<>(typeStringifiers);
    this.defStringifier = defStringifier;
  }

  /**
   * Returns a new {@code StringifierFactory} instance enriched with the specified name-based
   * stringifier. See {@link Builder#setNameBasedStringifier(String..., Stringifier)
   * Builder.setNameBasedStringifier}.
   *
   * @param varName The variable name that the stringifier is associated with
   * @param stringifier The stringifier
   * @return A new {@code StringifierFactory} instance enriched with the specified name-based
   *     stringifier.
   */
  public StringifierFactory withNameBasedStringifier(String varName, Stringifier stringifier) {
    Check.notNull(varName, "varName");
    Check.notNull(stringifier, "stringifier");
    Tuple<Template, String> t = Tuple.of(null, varName);
    Check.that(t).isNot(in(), stringifiers.keySet(), Builder.VAR_ASSIGNED, varName);
    Map<Tuple<Template, String>, Stringifier> stringifiers = new HashMap<>(this.stringifiers);
    stringifiers.put(t, stringifier);
    return new StringifierFactory(stringifiers, typeStringifiers, defStringifier);
  }

  /**
   * Returns a new {@code StringifierFactory} instance enriched with the specified type-based
   * stringifier.
   *
   * @param type The type that the stringifier is associated with
   * @param stringifier The stringifier
   * @return A new {@code StringifierFactory} instance enriched with the specified type-based
   *     stringifier
   */
  public StringifierFactory withTypeStringifier(Class<?> type, Stringifier stringifier) {
    Check.notNull(type, "type");
    Check.notNull(stringifier, "stringifier");
    HashMap<Class<?>, Stringifier> typeStringifiers = new HashMap<>(this.typeStringifiers);
    typeStringifiers.put(type, stringifier);
    return new StringifierFactory(stringifiers, typeStringifiers, defStringifier);
  }

  public StringifierFactory withDefaultStringifiers(Stringifier defaultStringifier) {
    Check.notNull(defaultStringifier, "defaultStringifier");
    return new StringifierFactory(stringifiers, typeStringifiers, defaultStringifier);
  }

  /**
   * Returns the stringifier to be used for the specified template variable.
   *
   * @param template The template containing the variable
   * @param varName The variable
   * @return The stringifier
   */
  Stringifier getStringifier(Template template, String varName, Object value) {
    Check.notNull(template, "template");
    Check.notNull(varName, "varName");
    Tuple<Template, String> key = Tuple.of(template, varName);
    Stringifier stringifier = stringifiers.get(key);
    if (stringifier != null) {
      return stringifier;
    }
    key = Tuple.of(null, varName);
    stringifier = stringifiers.get(key);
    if (stringifier != null) {
      return stringifier;
    }
    if (value != null) {
      stringifier = typeStringifiers.get(value.getClass());
    }
    if (defStringifier != null) {
      return defStringifier;
    }
    return ifNull(stringifier, Stringifier.DEFAULT);
  }
}
