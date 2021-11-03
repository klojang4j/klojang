package org.klojang.x.db.rs;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import org.klojang.render.NameMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import nl.naturalis.common.ExceptionMethods;
import nl.naturalis.common.invoke.Setter;
import nl.naturalis.common.invoke.SetterFactory;

/* Transports a single value from a ResultSet to a bean */
public class BeanChannel<COLUMN_TYPE, FIELD_TYPE> implements Channel<Object> {

  private static final Logger LOG = LoggerFactory.getLogger(BeanChannel.class);

  @SuppressWarnings("rawtypes")
  public static <U> U toBean(ResultSet rs, Supplier<U> beanSupplier, BeanChannel[] channels)
      throws Throwable {
    U bean = beanSupplier.get();
    for (BeanChannel channel : channels) {
      channel.send(rs, bean);
    }
    return bean;
  }

  @SuppressWarnings("rawtypes")
  public static BeanChannel[] createValueTransporters(
      ResultSet rs, Class<?> beanClass, NameMapper nameMapper) {
    Map<String, Setter> setters = SetterFactory.INSTANCE.getSetters(beanClass);
    ExtractorNegotiator negotiator = ExtractorNegotiator.getInstance();
    try {
      ResultSetMetaData rsmd = rs.getMetaData();
      int sz = rsmd.getColumnCount();
      List<BeanChannel<?, ?>> transporters = new ArrayList<>(sz);
      for (int idx = 0; idx < sz; ++idx) {
        int jdbcIdx = idx + 1; // JDBC is one-based
        int sqlType = rsmd.getColumnType(jdbcIdx);
        String label = rsmd.getColumnLabel(jdbcIdx);
        String property = nameMapper.map(label);
        Setter setter = setters.get(property);
        if (setter == null) {
          LOG.warn(
              "Column {} cannot be mapped to a property of {}", label, beanClass.getSimpleName());
          continue;
        }
        Class<?> javaType = setter.getParamType();
        RsExtractor<?, ?> extractor = negotiator.findExtractor(javaType, sqlType);
        transporters.add(new BeanChannel<>(extractor, setter, jdbcIdx, sqlType));
      }
      return transporters.toArray(new BeanChannel[transporters.size()]);
    } catch (SQLException e) {
      throw ExceptionMethods.uncheck(e);
    }
  }

  private final RsExtractor<COLUMN_TYPE, FIELD_TYPE> extractor;
  private final Setter setter;
  private final int jdbcIdx;
  private final int sqlType;

  private BeanChannel(
      RsExtractor<COLUMN_TYPE, FIELD_TYPE> extractor, Setter setter, int jdbcIdx, int sqlType) {
    this.extractor = extractor;
    this.setter = setter;
    this.jdbcIdx = jdbcIdx;
    this.sqlType = sqlType;
  }

  @Override
  @SuppressWarnings("unchecked")
  public void send(ResultSet rs, Object bean) throws Throwable {
    FIELD_TYPE val = extractor.getValue(rs, jdbcIdx, (Class<FIELD_TYPE>) setter.getParamType());
    setter.write(bean, val);
  }

  @Override
  public int getSqlType() {
    return sqlType;
  }
}
