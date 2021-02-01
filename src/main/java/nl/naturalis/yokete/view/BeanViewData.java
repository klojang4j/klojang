package nl.naturalis.yokete.view;

import nl.naturalis.common.ExceptionMethods;
import nl.naturalis.common.invoke.BeanReader;

public final class BeanViewData<T> implements ViewData {

  private final BeanReader<T> br;
  private final T bean;

  public BeanViewData(BeanReader<T> beanReader, T bean) {
    this.br = beanReader;
    this.bean = bean;
  }

  @Override
  public Object get(String var) {
    try {
      return br.get(bean, var);
    } catch (Throwable e) {
      throw ExceptionMethods.wrap(e, RenderException::new);
    }
  }
}
