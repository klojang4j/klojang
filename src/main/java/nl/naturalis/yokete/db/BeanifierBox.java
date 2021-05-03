package nl.naturalis.yokete.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import nl.naturalis.common.check.Check;
import static nl.naturalis.common.StringMethods.implode;
import static nl.naturalis.yokete.db.BeanValueTransporter.createTransporters;
import static nl.naturalis.yokete.db.rs.Transporter.getMatchErrors;
import static nl.naturalis.yokete.db.rs.Transporter.isCompatible;

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

  private AtomicReference<BeanValueTransporter<?, ?>[]> pwref = new AtomicReference<>();

  public BeanifierBox(Class<T> beanClass, Supplier<T> beanSupplier) {
    this(beanClass, beanSupplier, UnaryOperator.identity());
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
    BeanValueTransporter<?, ?>[] transporters;
    if ((transporters = pwref.getPlain()) == null) {
      synchronized (this) {
        if (pwref.get() == null) {
          // Ask again. Since we're now the only one in here, if pwref.get()
          // did *not* return null, another thread had slipped in just after
          // our first null check. That's fine. We are done.
          transporters = createTransporters(rs, beanCLass, mapper);
          pwref.set(transporters);
        }
      }
    } else if (verify && !isCompatible(rs, transporters)) {
      List<String> errors = getMatchErrors(rs, transporters);
      throw new ResultSetMismatchException(implode(errors, ". "));
    }
    return new DefaultBeanifier<>(rs, transporters, beanSupplier);
  }
}
