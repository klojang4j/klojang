package nl.naturalis.yokete.db.rs;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import nl.naturalis.common.ModulePrivate;
import nl.naturalis.common.invoke.Setter;
import nl.naturalis.common.invoke.SetterFactory;

@ModulePrivate
public class PropertyWriter<COLUMN_TYPE, TARGET_TYPE> implements Writer {

  public static <U> U toBean(ResultSet rs, Supplier<U> beanSupplier, PropertyWriter<?, ?>[] writers)
      throws Throwable {
    U bean = beanSupplier.get();
    for (PropertyWriter<?, ?> writer : writers) {
      writer.transferValue(rs, bean);
    }
    return bean;
  }

  public static PropertyWriter<?, ?>[] createWriters(
      ResultSetMetaData rsmd, Class<?> beanClass, UnaryOperator<String> nameMapper)
      throws SQLException {
    Map<String, Setter> setters = SetterFactory.INSTANCE.getSetters(beanClass);
    EmitterSelector negotiator = EmitterSelector.getInstance();
    int sz = rsmd.getColumnCount();
    List<PropertyWriter<?, ?>> writers = new ArrayList<>(sz);
    for (int idx = 0; idx < sz; ++idx) {
      int jdbcIdx = idx + 1; // JDBC is one-based
      int sqlType = rsmd.getColumnType(jdbcIdx);
      String label = rsmd.getColumnLabel(jdbcIdx);
      String property = nameMapper.apply(label);
      Setter setter = setters.get(property);
      if (setter != null) {
        Class<?> javaType = setter.getParamType();
        Emitter<?, ?> synapse = negotiator.getProducer(javaType, sqlType);
        writers.add(new PropertyWriter<>(synapse, setter, jdbcIdx, sqlType));
      }
    }
    return writers.toArray(new PropertyWriter[writers.size()]);
  }

  private final Emitter<COLUMN_TYPE, TARGET_TYPE> extractor;
  private final Setter setter;
  private final int jdbcIdx;
  private final int sqlType;

  PropertyWriter(
      Emitter<COLUMN_TYPE, TARGET_TYPE> extractor, Setter setter, int jdbcIdx, int sqlType) {
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
  private <U> void transferValue(ResultSet rs, U bean) throws Throwable {
    TARGET_TYPE val = extractor.getValue(rs, jdbcIdx, (Class<TARGET_TYPE>) setter.getParamType());
    setter.getMethod().invoke(bean, val);
  }
}
