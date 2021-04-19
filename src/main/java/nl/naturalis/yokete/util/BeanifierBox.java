package nl.naturalis.yokete.util;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import nl.naturalis.common.check.Check;
import nl.naturalis.common.invoke.Setter;
import nl.naturalis.common.invoke.SetterFactory;

public class BeanifierBox<T> {

  private final AtomicReference<ResultSetBeanifier<T>> ref = new AtomicReference<>();

  private final Class<T> bc;
  private final Supplier<T> bs;
  private final UnaryOperator<String> mapper;
  private final boolean verify;

  public BeanifierBox(Class<T> beanClass, Supplier<T> beanSupplier) {
    this(beanClass, beanSupplier, x -> x);
  }

  public BeanifierBox(
      Class<T> beanClass, Supplier<T> beanSupplier, UnaryOperator<String> columnToPropertyMapper) {
    this(beanClass, beanSupplier, columnToPropertyMapper, false);
  }

  public BeanifierBox(
      Class<T> beanClass,
      Supplier<T> beanSupplier,
      UnaryOperator<String> columnToPropertyMapper,
      boolean verify) {
    this.bc = Check.notNull(beanClass, "beanClass").ok();
    this.bs = Check.notNull(beanSupplier, "beanSupplier").ok();
    this.mapper = Check.notNull(columnToPropertyMapper, "columnToPropertyMapper").ok();
    this.verify = verify;
  }

  public ResultSetBeanifier<T> get(ResultSet rs) throws SQLException {
    if (!rs.next()) {
      return EmptyBeanifier.INSTANCE;
    }
    ResultSetBeanifier<T> rsb;
    if ((rsb = ref.get()) == null) {
      synchronized (this) {
        PropertyWriter[] writers = createWriters(rs);
        rsb = new ResultSetBeanifier<>(writers, bs);
        ref.setPlain(rsb);
      }
    } else if (verify) {
      Writer.checkCompatibility(rs, rsb.writers);
    }
    return rsb;
  }

  private PropertyWriter[] createWriters(ResultSet rs) throws SQLException {
    Map<String, Setter> setters = SetterFactory.INSTANCE.getSetters(bc);
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
