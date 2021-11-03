package org.klojang.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import org.klojang.render.NameMapper;
import org.klojang.x.db.rs.BeanChannel;
import org.klojang.x.db.rs.ChannelCache;
import nl.naturalis.common.ExceptionMethods;
import nl.naturalis.common.check.Check;
import static org.klojang.x.db.rs.BeanChannel.createValueTransporters;
import static nl.naturalis.common.StringMethods.implode;

/**
 * A factory for {@link ResultSetBeanifier} instances. You should create a separate {@code
 * BeanifierFactory} instance for each unique SELECT query in your application. You retrieve a
 * {@code ResultSetBeanifier} by passing a {@link ResultSet} to the {@link #getBeanifier(ResultSet)
 * getBeanifier} method. The first time you call this method on a {@code BeanifierFactory}, the
 * result set's metadata is inspected in order to set up and cache the beanification logic.
 * Subsequent calls directly apply this logic. Hence subsequent calls should be done with {@link
 * ResultSet} instances resulting from the same SQL query. (More precisely: subsequent {@code
 * ResultSet} instances passed to {@code getBeanifier} must be <i>compatible</i> with the first one.
 * They must have the same number of columns and their data types must match those of the first
 * {@code ResultSet}.) Since the set-up phase is somewhat expensive, you might want to cache the
 * {@code BeanifierFactory} (e.g. in a private static field).
 *
 * @author Ayco Holleman
 * @param <T>
 */
@SuppressWarnings("rawtypes")
public class BeanifierFactory<T> {

  private final AtomicReference<BeanChannel[]> ref = new AtomicReference<>();

  private final Class<T> beanClass;
  private final Supplier<T> beanSupplier;
  private final NameMapper mapper;
  private final boolean verify;

  public BeanifierFactory(Class<T> beanClass) {
    this(beanClass, () -> newInstance(beanClass), NameMapper.NOOP);
  }

  public BeanifierFactory(Class<T> beanClass, Supplier<T> beanSupplier) {
    this(beanClass, beanSupplier, NameMapper.NOOP);
  }

  public BeanifierFactory(
      Class<T> beanClass, Supplier<T> beanSupplier, NameMapper columnToPropertyMapper) {
    this(beanClass, beanSupplier, columnToPropertyMapper, false);
  }

  public BeanifierFactory(
      Class<T> beanClass,
      Supplier<T> beanSupplier,
      NameMapper columnToPropertyMapper,
      boolean verify) {
    this.beanClass = Check.notNull(beanClass, "beanClass").ok();
    this.beanSupplier = Check.notNull(beanSupplier, "beanSupplier").ok();
    this.mapper = Check.notNull(columnToPropertyMapper, "columnToPropertyMapper").ok();
    this.verify = verify;
  }

  public ResultSetBeanifier<T> getBeanifier(ResultSet rs) throws SQLException {
    if (!rs.next()) {
      return EmptyBeanifier.INSTANCE;
    }
    BeanChannel[] channels;
    if ((channels = ref.getPlain()) == null) {
      synchronized (this) {
        if (ref.get() == null) {
          // Ask again. Since we're now the only one in here, if pwref.get()
          // did *not* return null, another thread had slipped in just after
          // our first null check. That's fine. We are done.
          channels = createValueTransporters(rs, beanClass, mapper);
          ref.set(channels);
        }
      }
    } else if (verify && !ChannelCache.isCompatible(rs, channels)) {
      List<String> errors = ChannelCache.getMatchErrors(rs, channels);
      throw new ResultSetMismatchException(implode(errors, ". "));
    }
    return new DefaultBeanifier<>(rs, channels, beanSupplier);
  }

  private static <U> U newInstance(Class<U> beanClass) {
    try {
      return beanClass.getDeclaredConstructor().newInstance();
    } catch (Exception e) {
      throw ExceptionMethods.uncheck(e);
    }
  }
}
