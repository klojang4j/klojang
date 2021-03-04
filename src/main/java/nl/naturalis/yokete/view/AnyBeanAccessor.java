package nl.naturalis.yokete.view;

import java.util.function.UnaryOperator;
import nl.naturalis.common.check.Check;
import nl.naturalis.common.invoke.AnyBeanReader;
import nl.naturalis.common.invoke.NoSuchPropertyException;

public class AnyBeanAccessor implements Accessor<Object> {

  private final AnyBeanReader reader = new AnyBeanReader();

  private final UnaryOperator<String> mapper;

  public AnyBeanAccessor() {
    this.mapper = x -> x;
  }

  public AnyBeanAccessor(UnaryOperator<String> nameMapper) {
    this.mapper = Check.notNull(nameMapper).ok();
  }

  @Override
  public Object getValue(Object from, String varName) throws RenderException {
    try {
      return reader.read(from, mapper.apply(varName));
    } catch (NoSuchPropertyException e) {
      return ABSENT;
    }
  }
}
