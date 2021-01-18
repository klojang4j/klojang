package nl.naturalis.yokete.data;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.util.*;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import nl.naturalis.common.ExceptionMethods;
import nl.naturalis.common.check.Check;
import static java.lang.invoke.MethodHandles.lookup;
import static java.lang.invoke.MethodHandles.privateLookupIn;
import static java.util.stream.Collectors.toSet;
import static nl.naturalis.common.ObjectMethods.ifNull;
import static nl.naturalis.common.check.CommonChecks.notNull;

/**
 * Maps a result set row to a model bean. This class does not have any public constructors. It is
 * meant to be subclassed, with the subclass invoking its protected {@link
 * #ResultSetMapper(Supplier, Class) constructor}.
 *
 * @author Ayco Holleman
 * @param <T> The type of the model bean
 */
public abstract class ResultSetMapper<T> {

  private static final String ERR_MAP_TO_NULL = "Illegal to-null mapping for field %s.%s";

  private static class RWInfo {
    final Class<?> fieldType;
    final String fieldName;
    final String columnName;
    final MethodHandle reader;
    final VarHandle writer;

    RWInfo(String fieldName) {
      this(null, fieldName, null, null, null);
    }

    RWInfo(
        Class<?> fieldType,
        String fieldName,
        String columnName,
        MethodHandle reader,
        VarHandle writer) {
      this.fieldType = fieldType;
      this.fieldName = fieldName;
      this.columnName = columnName;
      this.reader = reader;
      this.writer = writer;
    }

    String getFieldName() {
      return fieldName;
    }
  }

  // MethodHandle corresponding to java.sql.ResultSet#getString(columnLabel)
  private static final MethodHandle RESULTSET_GET_STRING;
  // MethodHandle corresponding to java.sql.ResultSet#getInt(columnLabel)
  private static final MethodHandle RESULTSET_GET_INT;

  static {
    try {
      MethodType mt = MethodType.methodType(String.class, String.class);
      RESULTSET_GET_STRING = lookup().findVirtual(ResultSet.class, "getString", mt);
      mt = MethodType.methodType(int.class, String.class);
      RESULTSET_GET_INT = lookup().findVirtual(ResultSet.class, "getInt", mt);
    } catch (NoSuchMethodException | IllegalAccessException e) {
      throw ExceptionMethods.uncheck(e);
    }
  }

  private static final Map<Class<?>, List<RWInfo>> rwCache = new HashMap<>();

  private final Supplier<T> instanceFactory;
  private final List<RWInfo> rwInfo;

  /**
   * Reads the current row in the {@code ResultSet} into a model bean and returns the model bean.
   *
   * @param rs The {@code ResultSet}
   * @return A model bean
   */
  public T read(ResultSet rs) {
    try {
      return readOne(rs);
    } catch (Throwable t) {
      throw ExceptionMethods.uncheck(t);
    }
  }

  /**
   * Reads all (remaining) rows from the specified {@code ResultSet} and converts them into model
   * beans.
   *
   * @param rs The {@code ResultSet}
   * @return A {@code List} of model beans
   */
  public List<T> readAll(ResultSet rs) {
    List<T> all = new ArrayList<>();
    try {
      while (rs.next()) {
        all.add(readOne(rs));
      }
    } catch (Throwable t) {
      throw ExceptionMethods.uncheck(t);
    }
    return all;
  }

  /**
   * Reads at most {@code limit} rows from the specified {@code ResultSet} and converts them into
   * model beans.
   *
   * @param rs The {@code ResultSet}
   * @param limit The maximum number of rows to read from the {@code ResultSet}
   * @return A {@code List} of model beans
   */
  public List<T> readAll(ResultSet rs, int limit) {
    List<T> all = new ArrayList<>(limit);
    try {
      for (int i = 0; i < limit && rs.next(); ++i) {
        all.add(readOne(rs));
      }
    } catch (Throwable t) {
      throw ExceptionMethods.uncheck(t);
    }
    return all;
  }

  /**
   * Temporarily unmaps the specified fields. This can be useful when mapping SQL queries that
   * SELECT only a few columns of the table or view corresponding to the bean class. The new mapping
   * only applies to this instance of {@code ResultSetMapper}. Previously and subsequently
   * instantiated instances will not be affected.
   *
   * @param fields The fields to unmap
   */
  public void unmap(String... fields) {
    Set<RWInfo> subset = Arrays.stream(fields).map(RWInfo::new).collect(toSet());
    TreeSet<RWInfo> all = new TreeSet<>(Comparator.comparing(RWInfo::getFieldName));
    all.addAll(rwInfo);
    all.removeAll(subset);
  }

  /**
   * Temporarily unmaps all but the specified fields. This can be useful when mapping SQL queries
   * that SELECT only a few columns of the table or view corresponding to the bean class. The new
   * mapping only applies to this instance of {@code ResultSetMapper}. Previously and subsequently
   * instantiated instances will not be affected.
   *
   * @param fields The fields to retain
   */
  public void unmapAllBut(String... fields) {
    Set<RWInfo> subset = Arrays.stream(fields).map(RWInfo::new).collect(toSet());
    TreeSet<RWInfo> all = new TreeSet<>(Comparator.comparing(RWInfo::getFieldName));
    all.addAll(rwInfo);
    all.retainAll(subset);
  }

  /**
   * Creates a new {@code ResultSetMapper}. Subclasses should create a no-arg constructor that calls
   * this constructor. For example:
   *
   * <p>
   *
   * <pre>
   * class DepartmentMapper extends ResultSetMapper {
   *   public DepartmentMapper() {
   *     super(Department::new, Department.class);
   *   }
   * }
   * </pre>
   *
   * @param instanceFactory A supplier of new instances of the model bean class
   * @param beanClass The model bean class
   */
  protected ResultSetMapper(Supplier<T> instanceFactory, Class<T> beanClass) {
    Check.notNull(nameMapper(), "nameMapper");
    Check.notNull(notMapped(), "notMapped");
    Check.notNull(mapSpecial(), "mapSpecial");
    this.instanceFactory = Check.notNull(instanceFactory).ok();
    List<RWInfo> rwInfo = Check.notNull(beanClass).ok(rwCache::get);
    if (rwInfo == null) {
      rwInfo = createReadWriteInfo(beanClass);
      rwCache.put(beanClass, rwInfo);
    }
    // Every instance gets its own copy of the RWInfo list, so it can temporarily unmap fields
    // without touching the central cache.
    this.rwInfo = new ArrayList<>(rwInfo);
  }

  /**
   * Provide a string operator that maps field names to result set column labels. By default a
   * straight one-to-one mapping is used. The returned name mapping mechanism will be used for all
   * instances of {@code ResultSetMapper}. Two alternative name mappers are provided in this
   * package: {@link CamelToSnake} and {@link CamelToSnakeUpperCase}.
   *
   * @return A string operator that maps field names to result set column labels
   */
  protected UnaryOperator<String> nameMapper() {
    return x -> x;
  }

  /**
   * Provide a set of field names that must remain unmapped. By default all fields will be mapped.
   * The returned set will be used for all instances of {@code ResultSetMapper}.
   *
   * @return a set of field names that must remain unmapped
   */
  protected Set<String> notMapped() {
    return Collections.emptySet();
  }

  /**
   * Provide a map of field-to-column mappings that override the mapping mechanism provided by
   * {@link #nameMapper()}. The returned map will be used for all instances of {@code
   * ResultSetMapper}.
   *
   * @return A map of field-to-column mappings that override the mapping mechanism provided by
   *     {@code getNameMapper()}
   */
  protected Map<String, String> mapSpecial() {
    return Collections.emptyMap();
  }

  private List<RWInfo> createReadWriteInfo(Class<?> beanClass) {
    Field[] fields = beanClass.getDeclaredFields();
    List<RWInfo> rwInfo = new ArrayList<>(fields.length - notMapped().size());
    try {
      Lookup lookup = privateLookupIn(beanClass, lookup());
      for (Field f : fields) {
        String fn = f.getName();
        if (notMapped().contains(fn)) {
          continue;
        }
        String cn = ifNull(mapSpecial().get(fn), () -> nameMapper().apply(fn));
        Check.that(cn).is(notNull(), ERR_MAP_TO_NULL, beanClass.getName(), fn);
        VarHandle vh = lookup.unreflectVarHandle(f);
        if (f.getType() == String.class) {
          rwInfo.add(new RWInfo(f.getType(), fn, cn, RESULTSET_GET_STRING, vh));
        } else if (f.getType() == int.class || f.getType() == Integer.class) {
          rwInfo.add(new RWInfo(f.getType(), fn, cn, RESULTSET_GET_INT, vh));
        }
      }
    } catch (IllegalAccessException e) {
      throw ExceptionMethods.uncheck(e);
    }
    return rwInfo;
  }

  private T readOne(ResultSet rs) throws Throwable {
    T bean = instanceFactory.get();
    List<RWInfo> rwInfo = rwCache.get(bean.getClass());
    for (RWInfo rwi : rwInfo) {
      Object val = rwi.reader.invoke(rs, rwi.columnName);
      if (rs.wasNull() && !rwi.fieldType.isPrimitive()) {
        rwi.writer.set(bean, null);
      } else if (val != null) {
        rwi.writer.set(bean, val);
      }
    }
    return bean;
  }
}
