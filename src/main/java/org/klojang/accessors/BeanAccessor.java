package org.klojang.accessors;

import org.klojang.render.Accessor;
import org.klojang.render.NameMapper;
import org.klojang.render.RenderException;
import nl.naturalis.common.invoke.BeanReader;

public class BeanAccessor<T> implements Accessor<T> {

  private final BeanReader<T> br;
  private final NameMapper nm;

  public BeanAccessor(Class<T> beanClass, NameMapper nm) {
    this.br = new BeanReader<>(beanClass);
    this.nm = nm;
  }

  @Override
  public Object access(T data, String name) throws RenderException {
    String prop = nm == null ? name : nm.map(name);
    if (br.getIncludedProperties().contains(prop)) {
      return br.read(data, prop);
    }
    return UNDEFINED;
  }
}
