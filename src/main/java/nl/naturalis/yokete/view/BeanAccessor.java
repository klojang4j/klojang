package nl.naturalis.yokete.view;

import java.util.function.UnaryOperator;
import nl.naturalis.common.check.Check;
import nl.naturalis.common.invoke.BeanReader;
import nl.naturalis.common.invoke.NoSuchPropertyException;
import nl.naturalis.yokete.template.Template;

/**
 * An {@code Accessor} providing access to a predefined type of beans. Unless every millisecond
 * counts you should probably opt for the {@link AnyBeanAccessor} instead.
 *
 * @author Ayco Holleman
 * @param <T>
 */
public class BeanAccessor<T> implements Accessor<T> {

  private final BeanReader<T> reader;
  private final UnaryOperator<String> mapper;

  public BeanAccessor(Class<T> beanClass) {
    this(beanClass, x -> x);
  }

  public BeanAccessor(Class<T> beanClass, UnaryOperator<String> nameMapper) {
    Check.notNull(beanClass, "beanClass");
    Check.notNull(nameMapper, "nameMapper");
    this.reader = new BeanReader<>(beanClass);
    this.mapper = nameMapper;
  }

  public BeanAccessor(Class<T> beanClass, Template template, UnaryOperator<String> nameMapper) {
    Check.notNull(beanClass, "beanClass");
    Check.notNull(template, "template");
    Check.notNull(nameMapper, "nameMapper");
    String[] props = template.getAllNames().stream().map(nameMapper::apply).toArray(String[]::new);
    this.reader = new BeanReader<>(beanClass, props);
    this.mapper = nameMapper;
  }

  @Override
  public Object getValue(T from, String varName) throws RenderException {
    try {
      return reader.read(from, mapper.apply(varName));
    } catch (NoSuchPropertyException e) {
      return ABSENT;
    }
  }
}
