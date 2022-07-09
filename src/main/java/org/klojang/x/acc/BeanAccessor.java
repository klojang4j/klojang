package org.klojang.x.acc;

import org.klojang.template.Accessor;
import org.klojang.template.NameMapper;
import org.klojang.template.RenderException;
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
    if (br.getReadableProperties().contains(prop)) {
      return br.read(data, prop);
    }
    return UNDEFINED;
  }

}
