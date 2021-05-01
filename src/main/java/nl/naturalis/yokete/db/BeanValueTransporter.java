package nl.naturalis.yokete.db;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import nl.naturalis.common.invoke.Getter;
import nl.naturalis.common.invoke.GetterFactory;
import nl.naturalis.yokete.db.ps.ReceiverSelector;
import nl.naturalis.yokete.db.ps.Receiver;

class BeanValueTransporter<FIELD_TYPE, PARAM_TYPE> {

  public static <T> void bindBean(
      PreparedStatement ps, T bean, BeanValueTransporter<?, ?>[] transporters) throws Throwable {
    for (BeanValueTransporter<?, ?> transporter : transporters) {
      transporter.transportValue(ps, bean);
    }
  }

  public static BeanValueTransporter<?, ?>[] createTransporters(
      Class<?> beanClass, List<NamedParameter> params, BindConfig cfg) {
    ReceiverSelector negotiator = ReceiverSelector.getInstance();
    Map<String, Getter> getters = GetterFactory.INSTANCE.getGetters(beanClass, true);
    List<BeanValueTransporter<?, ?>> vts = new ArrayList<>(params.size());
    for (NamedParameter param : params) {
      Getter getter = getters.get(param.getName());
      if (getter == null) {
        continue;
      }
      Class<?> fieldType = getter.getReturnType();
      Integer sqlType = cfg.getSQLType(param.getName(), fieldType);
      Receiver<?, ?> persister;
      if (sqlType == null) {
        persister = negotiator.getDefaultReceiver(fieldType);
      } else {
        persister = negotiator.getReceiver(fieldType, sqlType);
      }
      vts.add(new BeanValueTransporter<>(getter, persister, param));
    }
    return vts.toArray(BeanValueTransporter[]::new);
  }

  private final Getter getter;
  private final Receiver<FIELD_TYPE, PARAM_TYPE> persister;
  private final NamedParameter param;

  BeanValueTransporter(
      Getter getter, Receiver<FIELD_TYPE, PARAM_TYPE> persister, NamedParameter param) {
    this.getter = getter;
    this.persister = persister;
    this.param = param;
  }

  private <T> void transportValue(PreparedStatement ps, T bean) throws Throwable {
    FIELD_TYPE beanValue = (FIELD_TYPE) getter.getMethod().invoke(bean);
    PARAM_TYPE paramValue = persister.getParamValue(beanValue);
    for (int paramIndex : param.indices()) {
      persister.bind(ps, paramIndex, paramValue);
    }
  }
}
