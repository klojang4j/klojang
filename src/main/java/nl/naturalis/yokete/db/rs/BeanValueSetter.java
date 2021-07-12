package nl.naturalis.yokete.db.rs;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import nl.naturalis.common.ExceptionMethods;
import nl.naturalis.common.ModulePrivate;
import nl.naturalis.common.invoke.Setter;
import nl.naturalis.common.invoke.SetterFactory;

/* Transports a single value from a ResultSet to a bean */
@ModulePrivate
public class BeanValueSetter<COLUMN_TYPE, FIELD_TYPE> implements ValueTransporter {

  public static <U> U toBean(
      ResultSet rs, Supplier<U> beanSupplier, BeanValueSetter<?, ?>[] setters) throws Throwable {
    U bean = beanSupplier.get();
    for (BeanValueSetter<?, ?> setter : setters) {
      setter.setValue(rs, bean);
    }
    return bean;
  }

  public static BeanValueSetter<?, ?>[] createSetters(
      ResultSet rs, Class<?> beanClass, UnaryOperator<String> nameMapper) {
    Map<String, Setter> setters = SetterFactory.INSTANCE.getSetters(beanClass);
    ExtractorNegotiator negotiator = ExtractorNegotiator.getInstance();
    try {
      ResultSetMetaData rsmd = rs.getMetaData();
      int sz = rsmd.getColumnCount();
      List<BeanValueSetter<?, ?>> transporters = new ArrayList<>(sz);
      for (int idx = 0; idx < sz; ++idx) {
        int jdbcIdx = idx + 1; // JDBC is one-based
        int sqlType = rsmd.getColumnType(jdbcIdx);
        String label = rsmd.getColumnLabel(jdbcIdx);
        String property = nameMapper.apply(label);
        Setter setter = setters.get(property);
        if (setter != null) {
          Class<?> javaType = setter.getParamType();
          RsExtractor<?, ?> extractor = negotiator.findExtractor(javaType, sqlType);
          transporters.add(new BeanValueSetter<>(extractor, setter, jdbcIdx, sqlType));
        }
      }
      return transporters.toArray(new BeanValueSetter[transporters.size()]);
    } catch (SQLException e) {
      throw ExceptionMethods.uncheck(e);
    }
  }

  private final RsExtractor<COLUMN_TYPE, FIELD_TYPE> extractor;
  private final Setter setter;
  private final int jdbcIdx;
  private final int sqlType;

  private BeanValueSetter(
      RsExtractor<COLUMN_TYPE, FIELD_TYPE> extractor, Setter setter, int jdbcIdx, int sqlType) {
    this.extractor = extractor;
    this.setter = setter;
    this.jdbcIdx = jdbcIdx;
    this.sqlType = sqlType;
  }

  @Override
  public int getSqlType() {
    return sqlType;
  }

  @SuppressWarnings("unchecked")
  private <U> void setValue(ResultSet rs, U bean) throws Throwable {
    FIELD_TYPE val = extractor.getValue(rs, jdbcIdx, (Class<FIELD_TYPE>) setter.getParamType());
    setter.getMethod().invoke(bean, val);
  }
}
