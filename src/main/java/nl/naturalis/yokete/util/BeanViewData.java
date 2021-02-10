package nl.naturalis.yokete.util;

import java.util.Optional;
import nl.naturalis.common.check.Check;
import nl.naturalis.common.invoke.BeanReader;
import nl.naturalis.common.invoke.NoSuchPropertyException;
import nl.naturalis.yokete.view.Template;
import nl.naturalis.yokete.view.ViewData;
import static nl.naturalis.common.check.CommonChecks.notNull;
import static nl.naturalis.common.ObjectMethods.*;

public class BeanViewData extends AbstractViewData {

  private final BeanReader br;

  private Object bean;

  public BeanViewData(BeanReader beanReader, ViewDataStringifiers stringifiers) {
    super(stringifiers);
    this.br = beanReader;
  }

  public BeanViewData with(Object bean) {
    this.bean = bean;
    return this;
  }

  @Override
  protected Optional<?> getRawValue(Template template, String name) {
    Check.that(bean).is(notNull(), "No data");
    try {
      return ifNotNull(br.get(bean, name), Optional::of, NULL);
    } catch (NoSuchPropertyException e) {
      return Optional.empty();
    }
  }

  @Override
  protected ViewData createViewData(Template template, String tmplName, Object bean) {
    BeanReader reader = new BeanReader(bean.getClass());
    return new BeanViewData(reader, stringifiers).with(bean);
  }
}
