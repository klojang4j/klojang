package nl.naturalis.yokete.util;

import nl.naturalis.common.ExceptionMethods;
import nl.naturalis.common.check.Check;
import nl.naturalis.common.invoke.BeanReader;
import nl.naturalis.yokete.view.RenderException;
import nl.naturalis.yokete.view.ViewData;
import static nl.naturalis.common.check.CommonChecks.*;

public class BeanViewData<T> implements ViewData {

  private final BeanReader<T> br;

  private T bean;

  public BeanViewData(BeanReader<T> beanReader) {
    this.br = beanReader;
  }

  public void setData(T bean) {
    this.bean = bean;
  }

  @Override
  public String getVariableValue(String var) {
    Check.that(bean).is(notNull(), "No data");
    try {
      return br.get(bean, var);
    } catch (Throwable e) {
      throw ExceptionMethods.wrap(e, RenderException::new);
    }
  }
}
