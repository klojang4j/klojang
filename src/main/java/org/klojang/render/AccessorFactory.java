package org.klojang.render;

import java.util.HashMap;
import java.util.Map;
import org.klojang.db.Row;
import org.klojang.template.Template;
import org.klojang.x.accessors.*;
import nl.naturalis.common.Tuple;
import nl.naturalis.common.check.Check;
import nl.naturalis.common.invoke.BeanReader;
import static nl.naturalis.common.ClassMethods.isA;

/**
 * Provides {@link Accessor accessors} capable of extracting values from the model objects. For
 * example, if you want to populate a template with a {@code Person} object, the {@link
 * RenderSession} needs to know how to read the {@code Person} properties that correspond to
 * template variables. In most cases the {@code AccessorFactory} defined by the {@link
 * #STANDARD_ACCESSORS} variable is probably all you need, without you actually having to code any
 * {@code Accessor} implementations. It contains predefined accessors for {@code Map<String,Object>}
 * objects, {@link Row} objects and JavaBeans.
 *
 * <p>There is one caveat though. The accessor used to access JavaBeans makes use of the {@link
 * BeanReader} class. This class does not use reflection to read the values of bean properties, but
 * it does use reflection to figure out what the properties are in the first place. Thus, if you use
 * this accessor from within a Java module, you will have to open up the module to the
 * naturalis-common module, which contains the {@code BeanReader} class. If you are not comfortable
 * with this, you can, as an alternative, use the {@link SaveBeanAccessor} class:
 *
 * <blockquote>
 *
 * <pre>{@code
 * SaveBeanReader<Person> personReader = SaveBeanReader
 *   .configure()
 *   .with("id", int.class)
 *   .with("firstName", String.class)
 *   .with("lastName", String.class)
 *   .with("birthDate", LocalDate.class)
 *   .freeze();
 * AccessorFactory af = AccessorFactory
 *   .configure()
 *   .addAccessor(Person.class, new SaveBeanAccesor<>(personReader))
 *   .freeze();
 * }</pre>
 *
 * </blockquote>
 *
 * <p>Although this is only slightly less verbose than writing your your own {@code Accessor} after
 * all:
 *
 * <blockquote>
 *
 * <pre>{@code
 * Accessor<Person> personAccessor =
 *   (person, property) -> {
 *     switch(property) {
 *       case "id" : return person.getId();
 *       case "firstName" : return person.getFirstName();
 *       case "lastName" : return person.getLastName();
 *       case "birthDate" : return person.getBirthDate();
 *       default : return Accessor.UNDEFINED;
 *     }
 *   };
 * AccessorFactory af = AccessorFactory
 *   .configure()
 *   .addAccessor(Person.class, new PersonAccessor())
 *   .freeze();
 * }</pre>
 *
 * </blockquote>
 *
 * <p>Note that in either case the accessors for {@code Map} and {@code Row} objects are still
 * present in the {@code AccessorFactory}.
 *
 * @author Ayco Holleman
 */
public class AccessorFactory {

  /**
   * An {@code AccessorFactory} the should sufficent for most use cases given a one-to-one mapping
   * between template variable names and model object property names.
   */
  public static final AccessorFactory STANDARD_ACCESSORS = configure().freeze();

  /**
   * Returns an {@code AccessorFactory} the should sufficent for most use cases while allowing you
   * to specify a global {@link NameMapper} for mapping template variable names to model object
   * properties.
   *
   * @param nameMapper The {@code NameMapper} to use when accessing model objects
   * @return An {@code AccessorFactory} the should sufficent for most use cases
   */
  public static AccessorFactory standard(NameMapper nameMapper) {
    return configure().setDefaultNameMapper(nameMapper).freeze();
  }

  /* ++++++++++++++++++++[ BEGIN BUILDER CLASS ]+++++++++++++++++ */

  public static class Builder {

    private NameMapper defMapper;
    private final Map<Tuple<Class<?>, Template>, Accessor<?>> accs = new HashMap<>();
    private final Map<Template, NameMapper> mappers = new HashMap<>();

    private Builder() {}

    /**
     * Sets the default {@code NameMapper} to use in order to map template variable names and nested
     * template names to properties within the model objects. If no {@code NameMapper} is specified,
     * a one-to-one relationship is assumed.
     *
     * @param nameMapper
     * @return
     */
    public Builder setDefaultNameMapper(NameMapper nameMapper) {
      defMapper = Check.notNull(nameMapper).ok();
      return this;
    }

    public Builder setNameMapper(Template template, NameMapper nameMapper) {
      Check.notNull(template, "template");
      Check.notNull(nameMapper, "nameMapper");
      mappers.put(template, nameMapper);
      return this;
    }

    public <T> Builder addAccessor(Class<T> forType, Accessor<? extends T> accessor) {
      accs.put(Tuple.of(forType, null), accessor);
      return this;
    }

    public <T> Builder addAccessor(
        Class<T> forType, Template template, Accessor<? super T> accessor) {
      accs.put(Tuple.of(forType, template), accessor);
      return this;
    }

    public AccessorFactory freeze() {
      return new AccessorFactory(accs, defMapper, mappers);
    }
  }

  /* +++++++++++++++++++++[ END BUILDER CLASS ]++++++++++++++++++ */

  public static Builder configure() {
    return new Builder();
  }

  private final Map<Tuple<Class<?>, Template>, Accessor<?>> accs;
  private final NameMapper defMapper;
  private final Map<Template, NameMapper> mappers;

  private AccessorFactory(
      Map<Tuple<Class<?>, Template>, Accessor<?>> accs,
      NameMapper defMapper,
      Map<Template, NameMapper> mappers) {
    this.accs = Map.copyOf(accs);
    this.defMapper = defMapper;
    this.mappers = Map.copyOf(mappers);
  }

  Accessor<?> getAccessor(Class<?> type, Template template) {
    Accessor<?> acc = accs.get(Tuple.of(type, template));
    if (acc == null) {
      NameMapper nm = mappers.getOrDefault(template, defMapper);
      acc = accs.get(Tuple.of(type, null));
      if (acc == null) {
        if (isA(type, Map.class)) {
          acc = new MapAccessor(nm);
        } else if (isA(type, Row.class)) {
          acc = new RowAccessor(nm);
        } else {
          acc = new BeanAccessor<>(type, nm);
        }
      }
    }
    return acc;
  }
}
