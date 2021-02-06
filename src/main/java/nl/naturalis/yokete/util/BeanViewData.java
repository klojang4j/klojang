package nl.naturalis.yokete.util;

import nl.naturalis.common.ExceptionMethods;
import nl.naturalis.common.check.Check;
import nl.naturalis.common.invoke.BeanReader;
import nl.naturalis.yokete.view.RenderException;
import static nl.naturalis.common.check.CommonChecks.notNull;

public class BeanViewData<T> extends AbstractViewData {

  private final BeanReader<T> br;

  private T bean;

  public BeanViewData(BeanReader<T> beanReader, ViewDataStringifiers stringifiers) {
    super(stringifiers);
    this.br = beanReader;
  }

  public void setData(T bean) {
    this.bean = bean;
  }

  @Override
  protected Object getRawValue(String var) {
    Check.that(bean).is(notNull(), "No data");
    try {
      return br.get(bean, var);
    } catch (Throwable e) {
      throw ExceptionMethods.wrap(e, RenderException::new);
    }
  }
}
