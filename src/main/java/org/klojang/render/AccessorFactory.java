package org.klojang.render;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.klojang.accessors.*;
import org.klojang.db.Row;
import org.klojang.template.Template;
import nl.naturalis.common.check.Check;
import nl.naturalis.common.collection.TypeMap;
import nl.naturalis.common.invoke.BeanReader;
import static nl.naturalis.common.ClassMethods.isA;

/**
 * Provides {@link Accessor accessors} capable of extracting values from model objects. For example,
 * if you want to populate a template with a {@code Person} object, the {@link RenderSession} needs
 * to know how to read the {@code Person} properties that correspond to template variables. In most
 * cases the {@code AccessorFactory} defined by the {@link #STANDARD_ACCESSORS} variable is probably
 * all you need, without you actually having to implement any {@code Accessor} yourself.
 *
 * <p>Any {@code AccessorFactory}, including the ones you build yourself and including the {@code
 * STANDARD_ACCESSORS} {@code AccessorFactory} comes with a set of predefined accessors (not exposed
 * via the API) that it hands out to the {@code RenderSession} based on the type of object the
 * {@code RenderSession} wants to access. This happens in the following manner:
 *
 * <p>
 *
 * <ol>
 *   <li>If you have {@link AccessorFactory.Builder#addAccessor(Class, Accessor) registered} your
 *       own {@code Accessor} for that particular type of object, then that is the {@code Accessor}
 *       that is going to be used.
 *   <li>If the object is an {@link Optional}, then, if the {@code Optional} is empty, a {@link
 *       OptionalAccessor} is going to be used. This accessor returns {@link Accessor#UNDEFINED} if
 *       the {@code Optional} is empty. Otherwise another accessor is going to be used, based on the
 *       type of the object within the {@code Optional}. Optionals are typically returned from (for
 *       example) the ubiquitous {@code dao.findById(id)} method and, as for Klojang, it is
 *       perfectly legitimate to {@link RenderSession#insert(Object, String...) insert} them into a
 *       template.
 *   <li>If the object is a {@code Map}, a {@link MapAccessor} is going to be used. (You could
 *       easily create an enhanced version, tailored to your particular needs, yourself. Check the
 *       source code.)
 *   <li>If the object is a {@link Row}, a {@link RowAccessor} is going to be used.
 *   <li>In any other case the object is taken to be a JavaBean and a {@link BeanAccessor} is going
 *       to be used.
 * </ol>
 *
 * <p>Please note that the accessor used to read JavaBean properties makes use of a {@link
 * BeanReader}. This class does not use reflection to read bean properties, but it does use
 * reflection to figure out what the properties are in the first place. Thus, if you use this
 * accessor from within a Java module, you will have to open up the module to the naturalis-common
 * module, which contains the {@code BeanReader} class. If you are not comfortable with this, you
 * can, as an alternative, use the {@link SaveBeanAccessor} class:
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
 * <p>Or you could just write your own {@code Accessor} after all:
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
    private final Map<Class<?>, Map<Template, Accessor<?>>> accs = new HashMap<>();
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
      accs.computeIfAbsent(forType, k -> new HashMap<>()).put(null, accessor);
      return this;
    }

    public <T> Builder addAccessor(
        Class<T> forType, Template template, Accessor<? super T> accessor) {
      accs.computeIfAbsent(forType, k -> new HashMap<>()).put(template, accessor);
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

  private final Map<Class<?>, Map<Template, Accessor<?>>> accs;
  private final NameMapper defMapper;
  private final Map<Template, NameMapper> mappers;

  private AccessorFactory(
      Map<Class<?>, Map<Template, Accessor<?>>> accs,
      NameMapper defMapper,
      Map<Template, NameMapper> mappers) {
    this.accs = accs.isEmpty() ? Collections.emptyMap() : new TypeMap<>(accs);
    this.defMapper = defMapper;
    this.mappers = Map.copyOf(mappers);
  }

  public Accessor<?> getAccessor(Object obj, Template template) {
    Class<?> type = obj.getClass();
    Map<Template, Accessor<?>> m = accs.get(type);
    Accessor<?> acc = null;
    if (m != null) {
      acc = m.get(template);
      if (acc == null) {
        acc = m.get(null);
      }
    }
    if (acc == null) {
      NameMapper nm = mappers.getOrDefault(template, defMapper);
      if (type == Optional.class) {
        return new OptionalAccessor<>(this, template);
      } else if (isA(type, Map.class)) {
        acc = new MapAccessor(nm);
      } else if (isA(type, Row.class)) {
        acc = new RowAccessor(nm);
      } else if (isA(type, Object[].class)) {
        acc = ArrayAccessor.getInstance(template);
      } else {
        acc = new BeanAccessor<>(type, nm);
      }
    }
    return acc;
  }
}
