package org.klojang.accessors;

import org.klojang.render.Accessor;
import org.klojang.render.NameMapper;
import org.klojang.render.RenderException;
import nl.naturalis.common.invoke.SaveBeanReader;

/**
 * An {@link Accessor} implementation that can be used to read JavaBean properties. It does not make
 * use of reflection at all and can therefore be used if your application code is inside a Java
 * module, and you are not comfortable with opening up the module to the naturalis-common library.
 *
 * @author Ayco Holleman
 * @param <T> The type of the JavaBean
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
