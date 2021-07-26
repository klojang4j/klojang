package org.klojang.render;

import java.util.HashMap;
import java.util.Map;
import org.klojang.db.Row;
import org.klojang.template.Template;
import org.klojang.x.accessors.*;
import nl.naturalis.common.Tuple;
import nl.naturalis.common.check.Check;
import static nl.naturalis.common.ClassMethods.isA;

public class AccessorFactory {

  public static final AccessorFactory BASIC_ACCESSORS = configure().freeze();

  /* ++++++++++++++++++++[ BEGIN BUILDER CLASS ]+++++++++++++++++ */

  public static class Builder {

    private NameMapper defMapper = NameMapper.NOOP;
    private final Map<Tuple<Class<?>, Template>, Accessor<?>> accs = new HashMap<>();
    private final Map<Template, NameMapper> mappers = new HashMap<>();

    private Builder() {}

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
