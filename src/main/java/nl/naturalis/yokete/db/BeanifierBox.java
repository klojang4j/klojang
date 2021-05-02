package nl.naturalis.yokete.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import nl.naturalis.common.check.Check;
import nl.naturalis.yokete.db.rs.PropertyWriter;
import nl.naturalis.yokete.db.rs.Writer;
import static nl.naturalis.yokete.db.rs.PropertyWriter.createWriters;

/**
 * Creates and holds a {@link DefaultMappifier}
 *
 * @author Ayco Holleman
 * @param <T>
 */
public class BeanifierBox<T> {

  private final Class<T> beanCLass;
  private final Supplier<T> beanSupplier;
  private final UnaryOperator<String> mapper;
  private final boolean verify;

  private AtomicReference<PropertyWriter<?, ?>[]> pwref = new AtomicReference<>();

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
    this.beanCLass = Check.notNull(beanClass, "beanClass").ok();
    this.beanSupplier = Check.notNull(beanSupplier, "beanSupplier").ok();
    this.mapper = Check.notNull(columnToPropertyMapper, "columnToPropertyMapper").ok();
    this.verify = verify;
  }

  public ResultSetBeanifier<T> get(ResultSet rs) throws SQLException {
    if (!rs.next()) {
      return EmptyBeanifier.INSTANCE;
    }
    PropertyWriter<?, ?>[] writers;
    if ((writers = pwref.getPlain()) == null) {
      synchronized (this) {
        if (pwref.get() == null) { // Ask again
          writers = createWriters(rs.getMetaData(), beanCLass, mapper);
          pwref.set(writers);
        }
      }
    } else if (verify) {
      Writer.checkCompatibility(rs, writers);
    }
    return new DefaultBeanifier<>(rs, writers, beanSupplier);
  }
}
