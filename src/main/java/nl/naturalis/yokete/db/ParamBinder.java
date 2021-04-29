package nl.naturalis.yokete.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import nl.naturalis.common.check.Check;
import nl.naturalis.common.invoke.BeanReader;
import nl.naturalis.common.invoke.Getter;
import nl.naturalis.common.invoke.GetterFactory;
import nl.naturalis.yokete.db.write.ValueAbsorber;
import nl.naturalis.yokete.db.write.ValueAbsorberNegotiator;

public class ParamBinder<T> {

  public abstract static class BindInfo {

    @SuppressWarnings("unused")
    public Integer getSQLType(String fieldName, Class<?> fieldType) {
      return null;
    }
  }

  private static class ValueTransferUnit {
    ValueAbsorber<?, ?> dendrite;
    Getter getter;
  }

  private final StatementInfo si;
  private final BeanReader<T> br;
  private final BindInfo bi;

  public ParamBinder(String sql, Class<T> beanClass) {
    this(sql, beanClass, new BindInfo() {});
  }

  public ParamBinder(String sql, Class<T> beanClass, BindInfo bindInfo) {
    si = Check.notNull(sql).ok(ParamExtractor::parseSQL);
    br = new BeanReader<>(beanClass, si.getParamNames().toArray(String[]::new));
    bi = bindInfo;
    Map<String, Getter> getters = GetterFactory.INSTANCE.getGetters(beanClass, true);
    Map<String, ValueAbsorber<?, ?>> dendrites = new HashMap<>(si.getParams().size());
    ValueAbsorberNegotiator negotiator = ValueAbsorberNegotiator.getInstance();
    for (Map.Entry<String, List<Integer>> e : si.getParams().entrySet()) {
      String fieldName = e.getKey();
      Class<?> fieldType = getters.get(fieldName).getReturnType();
      Integer sqlType = bi.getSQLType(fieldName, fieldType);
      ValueAbsorber<?, ?> dendrite;
      if (sqlType == null) {
        dendrite = negotiator.getDefaultDendrite(fieldType);
      } else {
        dendrite = negotiator.getDendrite(fieldType, sqlType);
      }
    }
  }

  public PreparedStatement createAndBind(Connection con, T bean) throws SQLException {
    ValueAbsorberNegotiator negotiator = ValueAbsorberNegotiator.getInstance();
    PreparedStatement ps = con.prepareStatement(si.getSQL());
    for (Map.Entry<String, List<Integer>> e : si.getParams().entrySet()) {
      String fieldName = e.getKey();
      Class<?> fieldType = br.getUsedGetters().get(fieldName).getReturnType();
      Integer sqlType = bi.getSQLType(fieldName, fieldType);
      ValueAbsorber<?, ?> dendrite;
      if (sqlType == null) {
        dendrite = negotiator.getDefaultDendrite(fieldType);
      } else {
        dendrite = negotiator.getDendrite(fieldType, sqlType);
      }

      Object val = br.read(bean, e.getKey());
    }
    return ps;
  }
}
