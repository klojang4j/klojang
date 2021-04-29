package nl.naturalis.yokete.db;

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
import nl.naturalis.yokete.db.read.*;
import static nl.naturalis.yokete.db.read.PropertyWriter.*;

/**
 * Creates and holds a {@link DefaultMappifier}
 *
 * @author Ayco Holleman
 * @param <T>
 */
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
    DefaultBeanifier<T> beanifier = (DefaultBeanifier<T>) ref.getPlain();
    if (beanifier == null) {
      synchronized (this) {
        if (ref.get() == null) { // Ask again, more forcefully
          PropertyWriter<?, ?>[] writers = createWriters(rs.getMetaData(), bc, mapper);
          beanifier = new DefaultBeanifier<>(writers, bs);
          ref.set(beanifier);
        }
      }
    } else if (verify) {
      Writer.checkCompatibility(rs, beanifier.writers);
    }
    return beanifier;
  }
}
