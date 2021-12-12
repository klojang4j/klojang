package org.klojang.render;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.klojang.SysProp;
import org.klojang.accessors.*;
import org.klojang.db.Row;
import org.klojang.template.Template;
import nl.naturalis.common.check.Check;
import nl.naturalis.common.collection.TypeMap;
import nl.naturalis.common.invoke.BeanReader;
import static nl.naturalis.common.ClassMethods.isA;

/**
 * A registry of {@link Accessor accessors} used by the {@link RenderSession} to extract values from
 * model objects. For example, if you want to populate a template with a {@code Person} object, the
 * {@link RenderSession} needs to know how to read the {@code Person} properties that correspond to
 * template variables. In most cases the {@code AccessorRegistry} defined by the {@link
 * #STANDARD_ACCESSORS} variable is probably all you need, without you actually having to implement
 * any {@code Accessor} yourself.
 *
 * <p>Any {@code AccessorRegistry}, including the ones you build yourself and including the {@code
 * STANDARD_ACCESSORS} {@code AccessorRegistry} comes with a set of predefined accessors (not
 * exposed via the API) that it hands out to the {@code RenderSession} based on the type of object
 * the {@code RenderSession} wants to access. This happens in the following manner:
 *
 * <p>
 *
 * <ol>
 *   <li>If you have {@link AccessorRegistry.Builder#register(Class, Accessor) registered} your own
 *       {@code Accessor} for that particular type of object, then that is the {@code Accessor} that
 *       is going to be used.
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
 * <p>Note that the accessor used to read JavaBean properties makes use of a {@link BeanReader}.
 * This class does not use reflection to read bean properties, but it does use reflection to figure
 * out what the properties are in the first place. Thus, if you use this accessor from within a Java
 * module, you will have to open up the module to the naturalis-common module, which contains the
 * {@code BeanReader} class. If you are not comfortable with this, you could, as an alternative, use
 * the {@link SaveBeanAccessor} class:
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
 * AccessorRegistry aReg = AccessorRegistry
 *   .configure()
 *   .register(Person.class, new SaveBeanAccesor<>(personReader))
 *   .freeze();
 * RenderSession session = template.newRenderSession(aReg);
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
 * AccessorRegistry aReg = AccessorRegistry
 *   .configure()
 *   .register(Person.class, new PersonAccessor())
 *   .freeze();
 * RenderSession session = template.newRenderSession(aReg);
 * }</pre>
 *
 * </blockquote>
 *
 * @see SysProp#USE_BEAN_ACCESSOR
 * @author Ayco Holleman
 */
public class AccessorRegistry {

  /**
   * An {@code AccessorRegistry} the should be sufficent for most use cases. It assumes that the
   * names you use in your templates can be mapped as-is to your model objects.
   */
  public static final AccessorRegistry STANDARD_ACCESSORS = configure().freeze();

  /**
   * Returns an {@code AccessorRegistry} that should be sufficient for most use cases. It allows you
   * to specify one global {@link NameMapper} for mapping the names used in your templates to the
   * names used in your model objects.
   *
   * @param nameMapper The {@code NameMapper} to use when accessing model objects
   * @return An {@code AccessorRegistry} the should sufficent for most use cases
   */
  public static AccessorRegistry standard(NameMapper nameMapper) {
    return configure().setDefaultNameMapper(nameMapper).freeze();
  }

  /* ++++++++++++++++++++[ BEGIN BUILDER CLASS ]+++++++++++++++++ */

  public static class Builder {

    private NameMapper defMapper;
    private final Map<Class<?>, Map<Template, Accessor<?>>> accs = new HashMap<>();
    private final Map<Template, NameMapper> mappers = new HashMap<>();

    private Builder() {}

    /**
     * Sets the default {@code NameMapper} used to map template variable names to JavaBean
     * properties (or {@code Map} keys). If no default {@code NameMapper} is specified, the {@link
     * NameMapper#AS_IS AS_IS} name mapper will be the default name mapper.
     *
     * @param nameMapper The name mapper
     * @return This {@code Builder} instance
     */
    public Builder setDefaultNameMapper(NameMapper nameMapper) {
      defMapper = Check.notNull(nameMapper).ok();
      return this;
    }

    /**
     * Sets the {@code NameMapper} to be used for the specified template.
     *
     * @param template The template for which to used the specified name mapper
     * @param nameMapper The name mapper
     * @return This {@code Builder} instance
     */
    public Builder setNameMapper(Template template, NameMapper nameMapper) {
      Check.notNull(template, "template");
      Check.notNull(nameMapper, "nameMapper");
      mappers.put(template, nameMapper);
      return this;
    }

    /**
     * Sets the {@code Accessor} to be used for objects of the specified type.
     *
     * @param <T> The type of the objects for which to use the {@code Accessor}
     * @param forType The {@code Class} object corresponding to the type
     * @param accessor The {@code Accessor}
     * @return This {@code Builder} instance
     */
    public <T> Builder register(Class<T> forType, Accessor<? extends T> accessor) {
      accs.computeIfAbsent(forType, k -> new HashMap<>()).put(null, accessor);
      return this;
    }

    /**
     * Sets the {@code Accessor} to be used for objects of the specified type, destined for the
     * specified template.
     *
     * @param <T> The type of the objects for which to use the {@code Accessor}
     * @param forType The {@code Class} object corresponding to the type
     * @param template The template for which to use the {@code Accessor}
     * @param accessor The {@code Accessor}
     * @return This {@code Builder} instance
     */
    public <T> Builder register(Class<T> forType, Template template, Accessor<? super T> accessor) {
      accs.computeIfAbsent(forType, k -> new HashMap<>()).put(template, accessor);
      return this;
    }

    /**
     * Returns a
     *
     * @return
     */
    public AccessorRegistry freeze() {
      return new AccessorRegistry(accs, defMapper, mappers);
    }
  }

  /* +++++++++++++++++++++[ END BUILDER CLASS ]++++++++++++++++++ */

  public static Builder configure() {
    return new Builder();
  }

  private final boolean useBeanAccessor = SysProp.USE_BEAN_ACCESSOR.getBoolean();

  private final Map<Class<?>, Map<Template, Accessor<?>>> accs;
  private final NameMapper defMapper;
  private final Map<Template, NameMapper> mappers;

  private AccessorRegistry(
      Map<Class<?>, Map<Template, Accessor<?>>> accs,
      NameMapper defMapper,
      Map<Template, NameMapper> mappers) {
    this.accs = accs.isEmpty() ? Collections.emptyMap() : TypeMap.withValues(accs, 32);
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
      } else if (useBeanAccessor) {
        acc = new BeanAccessor<>(type, nm);
      } else {
        acc = new PathAccessor(nm);
      }
    }
    return acc;
  }
}
