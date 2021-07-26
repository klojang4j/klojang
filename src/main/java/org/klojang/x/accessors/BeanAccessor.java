package org.klojang.x.accessors;

import org.klojang.render.Accessor;
import org.klojang.render.NameMapper;
import org.klojang.render.RenderException;
import nl.naturalis.common.invoke.BeanReader;
import nl.naturalis.common.invoke.NoSuchPropertyException;

public class BeanAccessor<T> implements Accessor<T> {

  private final BeanReader<T> reader;
  private final NameMapper nm;

  public BeanAccessor(Class<T> beanClass, NameMapper nm) {
    reader = new BeanReader<>(beanClass);
    this.nm = nm;
  }

  @Override
  public Object access(T data, String name) throws RenderException {
    try {
      return reader.read(data, nm.map(name));
    } catch (NoSuchPropertyException e) {
      return UNDEFINED;
    }
  }
}
