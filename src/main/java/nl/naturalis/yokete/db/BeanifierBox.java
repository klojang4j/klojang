package nl.naturalis.yokete.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import nl.naturalis.common.check.Check;
import nl.naturalis.yokete.db.rs.BeanValueSetter;
import static nl.naturalis.common.StringMethods.implode;
import static nl.naturalis.yokete.db.rs.BeanValueSetter.createSetters;
import static nl.naturalis.yokete.db.rs.ValueTransporter.getMatchErrors;
import static nl.naturalis.yokete.db.rs.ValueTransporter.isCompatible;

/**
 * Contains and supplies a {@link ResultSetBeanifier} for a single SQL query.
 *
 * @author Ayco Holleman
 * @param <T>
 */
public class BeanifierBox<T> {

  private final Class<T> beanClass;
  private final Supplier<T> beanSupplier;
  private final UnaryOperator<String> mapper;
  private final boolean verify;

  private AtomicReference<BeanValueSetter<?, ?>[]> ref = new AtomicReference<>();

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
    this.beanClass = Check.notNull(beanClass, "beanClass").ok();
    this.beanSupplier = Check.notNull(beanSupplier, "beanSupplier").ok();
    this.mapper = Check.notNull(columnToPropertyMapper, "columnToPropertyMapper").ok();
    this.verify = verify;
  }

  public ResultSetBeanifier<T> get(ResultSet rs) throws SQLException {
    if (!rs.next()) {
      return EmptyBeanifier.INSTANCE;
    }
    BeanValueSetter<?, ?>[] setters;
    if ((setters = ref.getPlain()) == null) {
      synchronized (this) {
        if (ref.get() == null) {
          // Ask again. Since we're now the only one in here, if pwref.get()
          // did *not* return null, another thread had slipped in just after
          // our first null check. That's fine. We are done.
          setters = createSetters(rs, beanClass, mapper);
          ref.set(setters);
        }
      }
    } else if (verify && !isCompatible(rs, setters)) {
      List<String> errors = getMatchErrors(rs, setters);
      throw new ResultSetMismatchException(implode(errors, ". "));
    }
    return new DefaultBeanifier<>(rs, setters, beanSupplier);
  }
}
