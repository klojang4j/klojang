package nl.naturalis.yokete.db;

import java.sql.PreparedStatement;
import java.util.List;

public class BeanBinder<T> {

  private final BeanValueTransporter<?, ?>[] transporters;

  public BeanBinder(Class<T> beanClass, List<NamedParameter> params, BindInfo bindInfo) {
    transporters = BeanValueTransporter.createTransporters(beanClass, params, bindInfo);
  }

  public void bindBean(PreparedStatement ps, T bean) throws Throwable {
    BeanValueTransporter.bindBean(ps, bean, transporters);
  }
}
