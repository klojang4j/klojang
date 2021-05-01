package nl.naturalis.yokete.db;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import nl.naturalis.common.invoke.Getter;
import nl.naturalis.common.invoke.GetterFactory;
import nl.naturalis.yokete.db.write.PersisterNegotiator;
import nl.naturalis.yokete.db.write.ValuePersister;

class BeanValueTransporter<FIELD_TYPE, PARAM_TYPE> {

  public static <T> void bindBean(
      PreparedStatement ps, T bean, BeanValueTransporter<?, ?>[] transporters) throws Throwable {
    for (BeanValueTransporter<?, ?> transporter : transporters) {
      transporter.transportValue(ps, bean);
    }
  }

  public static BeanValueTransporter<?, ?>[] createTransporters(
      Class<?> beanClass, List<NamedParameter> params, BindConfig cfg) {
    PersisterNegotiator negotiator = PersisterNegotiator.getInstance();
    Map<String, Getter> getters = GetterFactory.INSTANCE.getGetters(beanClass, true);
    List<BeanValueTransporter<?, ?>> vts = new ArrayList<>(params.size());
    for (NamedParameter param : params) {
      Getter getter = getters.get(param.getName());
      if (getter == null) {
        continue;
      }
      Class<?> fieldType = getter.getReturnType();
      Integer sqlType = cfg.getSQLType(param.getName(), fieldType);
      ValuePersister<?, ?> persister;
      if (sqlType == null) {
        persister = negotiator.getDefaultPersister(fieldType);
      } else {
        persister = negotiator.getPersister(fieldType, sqlType);
      }
      vts.add(new BeanValueTransporter<>(getter, persister, param));
    }
    return vts.toArray(BeanValueTransporter[]::new);
  }

  private final Getter getter;
  private final ValuePersister<FIELD_TYPE, PARAM_TYPE> persister;
  private final NamedParameter param;

  BeanValueTransporter(
      Getter getter, ValuePersister<FIELD_TYPE, PARAM_TYPE> persister, NamedParameter param) {
    this.getter = getter;
    this.persister = persister;
    this.param = param;
  }

  private <T> void transportValue(PreparedStatement ps, T bean) throws Throwable {
    FIELD_TYPE beanValue = (FIELD_TYPE) getter.getMethod().invoke(bean);
    PARAM_TYPE paramValue = persister.getParamValue(beanValue);
    for (int paramIndex : param.indices()) {
      persister.bindValue(ps, paramIndex, paramValue);
    }
  }
}
