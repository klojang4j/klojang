package org.klojang.x.db.rs;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Map;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import nl.naturalis.common.ExceptionMethods;
import nl.naturalis.common.invoke.Setter;
import nl.naturalis.common.invoke.SetterFactory;

/* Transports a single value from a ResultSet to a bean */
public class RsToBeanTransporter<COLUMN_TYPE, FIELD_TYPE> implements ValueTransporter {

  public static <U> U toBean(
      ResultSet rs, Supplier<U> beanSupplier, RsToBeanTransporter<?, ?>[] setters)
      throws Throwable {
    U bean = beanSupplier.get();
    for (RsToBeanTransporter<?, ?> setter : setters) {
      setter.setValue(rs, bean);
    }
    return bean;
  }

  public static RsToBeanTransporter<?, ?>[] createValueTransporters(
      ResultSet rs, Class<?> beanClass, UnaryOperator<String> nameMapper) {
    Map<String, Setter> setters = SetterFactory.INSTANCE.getSetters(beanClass);
    ExtractorNegotiator negotiator = ExtractorNegotiator.getInstance();
    try {
      ResultSetMetaData rsmd = rs.getMetaData();
      int sz = rsmd.getColumnCount();
      RsToBeanTransporter<?, ?>[] transporters = new RsToBeanTransporter[sz];
      for (int idx = 0; idx < sz; ++idx) {
        int jdbcIdx = idx + 1; // JDBC is one-based
        int sqlType = rsmd.getColumnType(jdbcIdx);
        String label = rsmd.getColumnLabel(jdbcIdx);
        String property = nameMapper.apply(label);
        Setter setter = setters.get(property);
        if (setter != null) {
          Class<?> javaType = setter.getParamType();
          RsExtractor<?, ?> extractor = negotiator.findExtractor(javaType, sqlType);
          transporters[idx] = new RsToBeanTransporter<>(extractor, setter, jdbcIdx, sqlType);
        }
      }
      return transporters;
    } catch (SQLException e) {
      throw ExceptionMethods.uncheck(e);
    }
  }

  private final RsExtractor<COLUMN_TYPE, FIELD_TYPE> extractor;
  private final Setter setter;
  private final int jdbcIdx;
  private final int sqlType;

  private RsToBeanTransporter(
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