package org.klojang.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import org.klojang.template.NameMapper;
import org.klojang.x.db.rs.BeanChannel;
import nl.naturalis.common.ExceptionMethods;
import nl.naturalis.common.check.Check;
import static org.klojang.x.db.rs.BeanChannel.createChannels;

/**
 * A factory for {@link ResultSetBeanifier} instances producing JavaBeans of type {@code <T>}. The
 * {@link ResultSet result sets} passed to a {@code BeanifierFactory} in return for a beanifier
 * instance cannot just be any arbitrary {@code ResultSet}; they must all be created from the same
 * SQL query. The very first {@code ResultSet} passed to a {@code BeanifierFactory} is used to
 * create and cache the objects needed to convert the {@code ResultSet} into a JavaBean. Subsequent
 * calls to {@link #getBeanifier(ResultSet)} will use these objects, too. Hence, all result sets
 * passed to {@code getBeanifier} must be <i>compatible</i> with the first one: they must have at
 * least as many columns and the column types must match those of the first result set. Column names
 * do in fact no longer matter. The column-to-property mapping is set up and fixed after the first
 * call to {@code getBeanifier}.
 *
 * @author Ayco Holleman
 * @param <T> The type of JavaBeans that the {@code BeanifierFactory} will be catering for
 */
@SuppressWarnings("rawtypes")
public class BeanifierFactory<T> {

  private final AtomicReference<BeanChannel[]> ref = new AtomicReference<>();

  private final Class<T> beanClass;
  private final Supplier<T> beanSupplier;
  private final NameMapper mapper;

  /**
   * Creates a new {@code BeanifierFactory}. Column names will be mapped {@link NameMapper#AS_IS
   * as-is} to property names. The JavaBeans will be created through {@code
   * beanClass.getDeclaredConstructor().newInstance()}.
   *
   * @param beanClass The class of the JavaBeans that the {@code BeanifierFactory} will be catering
   *     for
   */
  public BeanifierFactory(Class<T> beanClass) {
    this(beanClass, () -> newInstance(beanClass), NameMapper.AS_IS);
  }

  /**
   * Creates a new {@code BeanifierFactory}. Column names will be mapped {@link NameMapper#AS_IS
   * as-is} to property names.
   *
   * @param beanClass The class of the JavaBeans that the {@code BeanifierFactory} will be catering
   *     for
   * @param beanSupplier The supplier of the JavaBeans
   */
  public BeanifierFactory(Class<T> beanClass, Supplier<T> beanSupplier) {
    this(beanClass, beanSupplier, NameMapper.AS_IS);
  }

  /**
   * Creates a new {@code BeanifierFactory}. The JavaBeans will be created through {@code
   * beanClass.getDeclaredConstructor().newInstance()}.
   *
   * @param beanClass The class of the JavaBeans that the {@code BeanifierFactory} will be catering
   *     for
   * @param columnToPropertyMapper A {@code NameMapper} mapping column names to property names
   */
  public BeanifierFactory(Class<T> beanClass, NameMapper columnToPropertyMapper) {
    this(beanClass, () -> newInstance(beanClass), columnToPropertyMapper);
  }

  /**
   * Creates a new {@code BeanifierFactory}.
   *
   * @param beanClass The class of the JavaBean that the {@code BeanifierFactory} will be catering
   *     for
   * @param beanSupplier The supplier of the JavaBeans
   * @param columnToPropertyMapper A {@code NameMapper} mapping column names to property names
   */
  public BeanifierFactory(
      Class<T> beanClass, Supplier<T> beanSupplier, NameMapper columnToPropertyMapper) {
    this.beanClass = Check.notNull(beanClass, "beanClass").ok();
    this.beanSupplier = Check.notNull(beanSupplier, "beanSupplier").ok();
    this.mapper = Check.notNull(columnToPropertyMapper, "columnToPropertyMapper").ok();
  }

  /**
   * Returns a {@code ResultSetBeanifier} that will convert the rows in the specified {@code
   * ResultSet} into JavaBeans of type {@code <T>}.
   *
   * @param rs The {@code ResultSet}
   * @return A {@code ResultSetBeanifier} that will convert the rows in the specified {@code
   *     ResultSet} into JavaBeans of type {@code <T>}
   * @throws SQLException
   */
  public ResultSetBeanifier<T> getBeanifier(ResultSet rs) throws SQLException {
    if (!rs.next()) {
      return EmptyBeanifier.INSTANCE;
    }
    BeanChannel[] channels;
    if ((channels = ref.getPlain()) == null) {
      synchronized (this) {
        if (ref.get() == null) {
          ref.set(channels = createChannels(rs, beanClass, mapper));
        }
      }
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
