package nl.naturalis.yokete.db;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import nl.naturalis.common.ExceptionMethods;
import nl.naturalis.common.invoke.Setter;
import nl.naturalis.common.invoke.SetterFactory;
import nl.naturalis.yokete.db.rs.Emitter;
import nl.naturalis.yokete.db.rs.EmitterNegotiator;
import nl.naturalis.yokete.db.rs.Transporter;

/**
 * Transports a single value <i>out of</i> a {@code ResultSet} and <i>into</i> a JavaBean.
 *
 * @author Ayco Holleman
 * @param <COLUMN_TYPE>
 * @param <FIELD_TYPE>
 */
class BeanValueTransporter<COLUMN_TYPE, FIELD_TYPE> implements Transporter {

  static <U> U toBean(
      ResultSet rs, Supplier<U> beanSupplier, BeanValueTransporter<?, ?>[] transporters)
      throws Throwable {
    U bean = beanSupplier.get();
    for (BeanValueTransporter<?, ?> t : transporters) {
      t.transferValue(rs, bean);
    }
    return bean;
  }

  static BeanValueTransporter<?, ?>[] createTransporters(
      ResultSet rs, Class<?> beanClass, UnaryOperator<String> nameMapper) {
    Map<String, Setter> setters = SetterFactory.INSTANCE.getSetters(beanClass);
    EmitterNegotiator negotiator = EmitterNegotiator.getInstance();
    try {
      ResultSetMetaData rsmd = rs.getMetaData();
      int sz = rsmd.getColumnCount();
      List<BeanValueTransporter<?, ?>> transporters = new ArrayList<>(sz);
      for (int idx = 0; idx < sz; ++idx) {
        int jdbcIdx = idx + 1; // JDBC is one-based
        int sqlType = rsmd.getColumnType(jdbcIdx);
        String label = rsmd.getColumnLabel(jdbcIdx);
        String property = nameMapper.apply(label);
        Setter setter = setters.get(property);
        if (setter != null) {
          Class<?> javaType = setter.getParamType();
          Emitter<?, ?> synapse = negotiator.getEmitter(javaType, sqlType);
          transporters.add(new BeanValueTransporter<>(synapse, setter, jdbcIdx, sqlType));
        }
      }
      return transporters.toArray(new BeanValueTransporter[transporters.size()]);
    } catch (SQLException e) {
      throw ExceptionMethods.uncheck(e);
    }
  }

  private final Emitter<COLUMN_TYPE, FIELD_TYPE> emitter;
  private final Setter setter;
  private final int jdbcIdx;
  private final int sqlType;

  private BeanValueTransporter(
      Emitter<COLUMN_TYPE, FIELD_TYPE> emitter, Setter setter, int jdbcIdx, int sqlType) {
    this.emitter = emitter;
    this.setter = setter;
    this.jdbcIdx = jdbcIdx;
    this.sqlType = sqlType;
  }

  @Override
  public int getSqlType() {
    return sqlType;
  }

  @SuppressWarnings("unchecked")
  private <U> void transferValue(ResultSet rs, U bean) throws Throwable {
    FIELD_TYPE val = emitter.getValue(rs, jdbcIdx, (Class<FIELD_TYPE>) setter.getParamType());
    setter.getMethod().invoke(bean, val);
  }
}
