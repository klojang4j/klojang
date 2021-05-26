package nl.naturalis.yokete.db.ps;

import java.sql.PreparedStatement;
import java.util.List;
import nl.naturalis.common.ModulePrivate;
import nl.naturalis.yokete.db.BindInfo;
import nl.naturalis.yokete.db.NamedParameter;

@ModulePrivate
public class BeanBinder<T> {

  private final BeanValueBinder<?, ?>[] transporters;

  public BeanBinder(Class<T> beanClass, List<NamedParameter> params, BindInfo bindInfo) {
    transporters = BeanValueBinder.createBeanValueBinders(beanClass, params, bindInfo);
  }

  public void bindBean(PreparedStatement ps, T bean) throws Throwable {
    BeanValueBinder.bindBean(ps, bean, transporters);
  }
}
