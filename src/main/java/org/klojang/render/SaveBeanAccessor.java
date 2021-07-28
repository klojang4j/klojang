package org.klojang.render;

import nl.naturalis.common.invoke.SaveBeanReader;

/**
 * An {@link Accessor} implementation that you can use.
 *
 * @author Ayco Holleman
 * @param <T>
 */
public class SaveBeanAccessor<T> implements Accessor<T> {

  private final SaveBeanReader<T> br;
  private final NameMapper nm;

  public SaveBeanAccessor(SaveBeanReader<T> beanReader) {
    this(beanReader, null);
  }

  public SaveBeanAccessor(SaveBeanReader<T> beanReader, NameMapper nameMapper) {
    this.br = beanReader;
    this.nm = nameMapper;
  }

  @Override
  public Object access(T sourceData, String varOrNestedTemplateName) throws RenderException {
    String prop = nm == null ? varOrNestedTemplateName : nm.map(varOrNestedTemplateName);
    if (br.getUsedProperties().contains(prop)) {
      return br.read(sourceData, prop);
    }
    return UNDEFINED;
  }
}
