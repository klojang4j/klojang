package nl.naturalis.yokete.util;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import nl.naturalis.common.check.Check;
import nl.naturalis.common.invoke.Setter;
import nl.naturalis.common.invoke.SetterFactory;

public class BeanifierFactory {

  private static BeanifierFactory DFLT_INSTANCE;

  public static BeanifierFactory getInstance() {
    if (DFLT_INSTANCE == null) {
      DFLT_INSTANCE = new BeanifierFactory();
    }
    return DFLT_INSTANCE;
  }

  private BeanifierFactory() {}

  public <T> ResultSetBeanifier<T> getBeanifier(
      ResultSet rs, Class<T> beanClass, Supplier<T> beanSupplier) throws SQLException {
    return getBeanifier(rs, beanClass, beanSupplier, x -> x);
  }

  public <T> ResultSetBeanifier<T> getBeanifier(
      ResultSet rs,
      Class<T> beanClass,
      Supplier<T> beanSupplier,
      UnaryOperator<String> columnToPropertyMapper)
      throws SQLException {
    Check.notNull(rs, "rs");
    Check.notNull(beanClass, "beanClass");
    Check.notNull(beanSupplier, "beanSupplier");
    Check.notNull(columnToPropertyMapper, "columnToPropertyMapper");
    PropertyWriter[] writers = createWriters(beanClass, rs, columnToPropertyMapper);
    return new ResultSetBeanifier<>(beanSupplier, writers);
  }

  private static PropertyWriter[] createWriters(
      Class<?> beanClass, ResultSet rs, UnaryOperator<String> mapper) throws SQLException {
    Map<String, Setter> setters = SetterFactory.INSTANCE.getSetters(beanClass);
    SynapseNegotiator negotiator = SynapseNegotiator.getInstance();
    ResultSetMetaData rsmd = rs.getMetaData();
    int sz = rsmd.getColumnCount();
    List<PropertyWriter> writers = new ArrayList<>(sz);
    for (int idx = 0; idx < sz; ++idx) {
      int jdbcIdx = idx + 1; // JDBC is one-based
      int sqlType = rsmd.getColumnType(jdbcIdx);
      String label = rsmd.getColumnLabel(jdbcIdx);
      String property = mapper.apply(label);
      Setter setter = setters.get(property);
      if (setter != null) {
        Class<?> javaType = setter.getParamType();
        Synapse synapse = negotiator.getSynapse(javaType, sqlType);
        writers.add(new PropertyWriter(synapse, setter, jdbcIdx, sqlType));
      }
    }
    return writers.toArray(new PropertyWriter[writers.size()]);
  }
}
