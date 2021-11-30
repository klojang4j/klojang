package org.klojang.db;

import java.sql.ResultSet;
import java.util.*;
import java.util.function.Function;
import org.klojang.render.Accessor;
import nl.naturalis.common.Bool;
import nl.naturalis.common.NumberMethods;
import nl.naturalis.common.Pair;
import nl.naturalis.common.check.Check;
import static nl.naturalis.common.ObjectMethods.ifNotNull;
import static nl.naturalis.common.check.CommonChecks.between;
import static nl.naturalis.common.check.CommonChecks.keyIn;
import static nl.naturalis.common.check.CommonChecks.negative;
import static nl.naturalis.common.check.CommonChecks.notNull;

/**
 * A thin wrapper around a {@code LinkedHashMap<String,Object>} instance that mimicks some of the
 * behaviour of the {@link ResultSet} class. {@code Row} objects are produced by a {@link
 * ResultSetMappifier} and can be quickly pushed up into the higher layers of your application
 * without them actually acquiring an awkward dependency on {@code java.sql}. Up there they can be
 * inserted directly into templates, without having to register a separate {@link Accessor} for
 * them.
 *
 * <p>Note that an important difference between a {@code Row} and a {@code ResultSet} is that a
 * {@code Row} is writable, too. As with a {@code ResultSet} you can access column values both by
 * column name and by column number, but, unlike {@code ResultSet}, column numbers need to be
 * specified in a zero-based manner.
 *
 * @author Ayco Holleman
 */
public class Row {

  private static final String ERR0 = "No such column: \"%s\"";
  private static final String ERR1 = "Column %s not convertible to %s: %s";
  private static final String ERR2 = "Invalid column number: %d";
  private static final String ERR3 = "Columns already exists: %d";

  private final Map<String, Object> map;

  /**
   * Creates a new {@code Row} from the data in the specified map.
   *
   * @param data The data for the {@code Row}.
   */
  public Row(Map<String, Object> data) {
    this(data, 0);
  }

  /**
   * Creates a new {@code Row} from the data in the specified map. The specified extra capacity is
   * reserved for the addition of new "columns" (i.e. map keys). Null keys are not allowed.
   *
   * @param map The data for the {@code Row}.
   */
  public Row(Map<String, Object> map, int extraCapacity) {
    Check.notNull(map, "data");
    Check.that(extraCapacity, "extraCapacity").isNot(negative());
    int cap = 1 + ((map.size() + extraCapacity) * 4 / 3);
    LinkedHashMap<String, Object> m = new LinkedHashMap<>(cap);
    map.forEach(
        (k, v) -> {
          Check.that(k).is(notNull(), "Map must not contain null keys");
          m.put(k, v);
        });
    this.map = m;
  }

  /**
   * Creates a new {@code Row} with enough capacity to contain the specified number of "columns"
   * (i&#46;e&#46; map keys).
   *
   * @param columnCount
   */
  public Row(int columnCount) {
    map = new LinkedHashMap<>(1 + (columnCount * 4 / 3));
  }

  /**
   * Returns the number of columns in the {@code Row}.
   *
   * @return The number of columns in the {@code Row}
   */
  public int size() {
    return map.size();
  }

  /**
   * Returns the name of the column at the specified index.
   *
   * @param colNum The column index (zero-based)
   * @return The column name
   */
  public String getColumnName(int colNum) {
    Check.that(colNum).is(between(), Pair.of(0, map.size()), ERR2, colNum);
    return (String) map.keySet().toArray()[colNum];
  }

  /**
   * Returns the (zero-based) index of the column with the specified name.
   *
   * @param colName The column name
   * @return The column index (zero-based)
   */
  public int getColumnNumber(String colName) {
    Check.notNull(colName).is(keyIn(), map, ERR0, colName).ok(map::get);
    return List.copyOf(map.keySet()).indexOf(colName);
  }

  /**
   * Returns an unmodifiable {@code Set} containing the column names.
   *
   * @return An unmodifiable {@code Set} containing the column names
   */
  public Set<String> getColumnNames() {
    return Collections.unmodifiableSet(map.keySet());
  }

  /**
   * Returns whether or not the row contains a column with the specified name.
   *
   * @param colName The column name
   * @return Whether or not the row contains a column with the specified name
   */
  public boolean hasColumn(String colName) {
    return map.containsKey(colName);
  }

  /**
   * Returns an unmodifiable {@code Map} containing the column-name-to-column-value mappings.
   *
   * @return An unmodifiable {@code Map} containing the column-name-to-column-value mappings
   */
  public Map<String, Object> toMap() {
    return Collections.unmodifiableMap(map);
  }

  /**
   * Returns the value of the column with the specified name.
   *
   * @param colName The column name
   * @return The value
   */
  public Object getValue(String colName) {
    return Check.notNull(colName, "colName").is(keyIn(), map, ERR0, colName).ok(map::get);
  }

  /**
   * Returns the value of the column at the specified index.
   *
   * @param colNum The column number
   * @return The value
   */
  public Object getValue(int colNum) {
    return map.get(getColumnName(colNum));
  }

  /**
   * Returns the value of the column with the specified name, casting it to the specified type.
   *
   * @param <T> The type to cast the value to
   * @param colName The column name
   * @return The value
   */
  @SuppressWarnings("unchecked")
  public <T> T get(String colName) {
    return (T) getValue(colName);
  }

  /**
   * Returns the value of the column at the specified index, casting it to the specified type.
   *
   * @param <T> The type to cast the value to
   * @param colNum The column number
   * @return The value
   */
  @SuppressWarnings("unchecked")
  public <T> T get(int colNum) {
    return (T) getValue(colNum);
  }

  /**
   * Returns the value of the specified column as a {@code String}.
   *
   * @param colName The column name
   * @return A {@code String} representation of the value
   */
  public String getString(String colName) {
    return ifNotNull(getValue(colName), Object::toString);
  }

  /**
   * Returns the value of the specified column as a {@code String}.
   *
   * @param colNum The column index
   * @return A {@code String} representation of the value
   */
  public String getString(int colNum) {
    return ifNotNull(getValue(colNum), Object::toString);
  }

  /**
   * Returns the value of the specified column as a {@code String}, or 0 (zero) if the value was
   * {@code null}.
   *
   * @param colName The column name
   * @return An {@code int} representation of the value
   */
  public int getInt(String colName) {
    return getInt(colName, 0);
  }

  /**
   * Returns the value of the specified column as an {@code int}, or 0 (zero) if the value was
   * {@code null}.
   *
   * @param colName The column index
   * @return An {@code int} representation of the value
   */
  public int getInt(int colNum) {
    return getInt(colNum, 0);
  }

  /**
   * Returns the value of the specified column as an {@code int}, or {@code nullValue} if the value
   * was {@code null}.
   *
   * @param colName The column name
   * @return An {@code int} representation of the value
   */
  public int getInt(String colName, int nullValue) {
    Object v = getValue(colName);
    return v == null ? nullValue : getNumber(colName, v, Integer.class);
  }

  /**
   * Returns the value of the specified column as an {@code int}, or {@code nullValue} if the value
   * was {@code null}.
   *
   * @param colName The column int
   * @return An {@code int} representation of the value
   */
  public int getInt(int colNum, int nullValue) {
    Object v = getValue(colNum);
    return v == null ? nullValue : getNumber(colNum, v, Integer.class);
  }

  /**
   * Returns the value of the specified column as a {@code double}, or 0 (zero) if the value was
   * {@code null}.
   *
   * @param colName The column name
   * @return An {@code int} representation of the value
   */
  public double getDouble(String colName) {
    return getDouble(colName, 0);
  }

  /**
   * Returns the value of the specified column as a {@code double}, or 0 (zero) if the value was
   * {@code null}.
   *
   * @param colNum The column index
   * @return An {@code int} representation of the value
   */
  public double getDouble(int colNum) {
    return getDouble(colNum, 0);
  }

  /**
   * Returns the value of the specified column as a {@code double}, or {@code nullValue} if the
   * value was {@code null}.
   *
   * @param colName The column name
   * @return A {@code double} representation of the value
   */
  public double getDouble(String colName, double nullValue) {
    Object v = getValue(colName);
    return v == null ? nullValue : getNumber(colName, v, Double.class);
  }

  /**
   * Returns the value of the specified column as a {@code double}, or {@code nullValue} if the
   * value was {@code null}.
   *
   * @param colNum The column index
   * @return A {@code double} representation of the value
   */
  public double getDouble(int colNum, double nullValue) {
    Object v = getValue(colNum);
    return v == null ? nullValue : getNumber(colNum, v, Double.class);
  }

  /**
   * Returns the value of the specified column as a {@code float}, or 0 (zero) if the value was
   * {@code null}.
   *
   * @param colName The column name
   * @return A {@code float} representation of the value
   */
  public float getFloat(String colName) {
    return getFloat(colName, 0F);
  }

  /**
   * Returns the value of the specified column as a {@code float}, or 0 (zero) if the value was
   * {@code null}.
   *
   * @param colNum The column index
   * @return A {@code float} representation of the value
   */
  public float getFloat(int colNum) {
    return getFloat(colNum, 0F);
  }

  /**
   * Returns the value of the specified column as a {@code float}, or {@code nullValue} if the value
   * was {@code null}.
   *
   * @param colName The column name
   * @return A {@code float} representation of the value
   */
  public float getFloat(String colName, float nullValue) {
    Object v = getValue(colName);
    return v == null ? nullValue : getNumber(colName, v, Float.class);
  }

  /**
   * Returns the value of the specified column as a {@code float}, or {@code nullValue} if the value
   * was {@code null}.
   *
   * @param colNum The column index
   * @return A {@code float} representation of the value
   */
  public float getFloat(int colNum, float nullValue) {
    Object v = getValue(colNum);
    return v == null ? nullValue : getNumber(colNum, v, Float.class);
  }

  /**
   * Returns the value of the specified column as a {@code long}, or 0 (zero) if the value was
   * {@code null}.
   *
   * @param colName The column name
   * @return A {@code long} representation of the value
   */
  public long getLong(String colName) {
    return getLong(colName, 0L);
  }

  /**
   * Returns the value of the specified column as a {@code long}, or 0 (zero) if the value was
   * {@code null}.
   *
   * @param colNum The column index
   * @return A {@code long} representation of the value
   */
  public long getLong(int colNum) {
    return getLong(colNum, 0L);
  }

  /**
   * Returns the value of the specified column as a {@code long}, or {@code nullValue} if the value
   * was {@code null}.
   *
   * @param colName The column name
   * @return A {@code long} representation of the value
   */
  public long getLong(String colName, long nullValue) {
    Object v = getValue(colName);
    return v == null ? nullValue : getNumber(colName, v, Long.class);
  }

  /**
   * Returns the value of the specified column as a {@code long}, or {@code nullValue} if the value
   * was {@code null}.
   *
   * @param colNum The column index
   * @return A {@code long} representation of the value
   */
  public long getLong(int colNum, long nullValue) {
    Object v = getValue(colNum);
    return v == null ? nullValue : getNumber(colNum, v, Long.class);
  }

  /**
   * Returns the value of the specified column as a {@code short}, or 0 (zero) if the value was
   * {@code null}.
   *
   * @param colName The column name
   * @return A {@code short} representation of the value
   */
  public short getShort(String colName) {
    return getShort(colName, (short) 0);
  }

  /**
   * Returns the value of the specified column as a {@code short}, or 0 (zero) if the value was
   * {@code null}.
   *
   * @param colNum The column index
   * @return A {@code short} representation of the value
   */
  public short getShort(int colNum) {
    return getShort(colNum, (short) 0);
  }

  /**
   * Returns the value of the specified column as a {@code short}, or {@code nullValue} if the value
   * was {@code null}.
   *
   * @param colName The column name
   * @return A {@code short} representation of the value
   */
  public short getShort(String colName, short nullValue) {
    Object v = getValue(colName);
    return v == null ? nullValue : getNumber(colName, v, Short.class);
  }

  /**
   * Returns the value of the specified column as a {@code short}, or {@code nullValue} if the value
   * was {@code null}.
   *
   * @param colName The column name
   * @return A {@code short} representation of the value
   */
  public short getShort(int colNum, short nullValue) {
    Object v = getValue(colNum);
    return v == null ? nullValue : getNumber(colNum, v, Short.class);
  }

  /**
   * Returns the value of the specified column as a {@code byte}, or 0 (zero) if the value was
   * {@code null}.
   *
   * @param colName The column name
   * @return A {@code byte} representation of the value
   */
  public byte getByte(String colName) {
    return getByte(colName, (byte) 0);
  }

  /**
   * Returns the value of the specified column as a {@code byte}, or 0 (zero) if the value was
   * {@code null}.
   *
   * @param colName The column name
   * @return A {@code byte} representation of the value
   */
  public byte getByte(int colNum) {
    return getByte(colNum, (byte) 0);
  }

  /**
   * Returns the value of the specified column as a {@code byte}, or {@code nullValue} if the value
   * was {@code null}.
   *
   * @param colName The column name
   * @return A {@code byte} representation of the value
   */
  public byte getByte(String colName, byte nullValue) {
    Object v = getValue(colName);
    return v == null ? nullValue : getNumber(colName, v, Byte.class);
  }

  /**
   * Returns the value of the specified column as a {@code byte}, or {@code nullValue} if the value
   * was {@code null}.
   *
   * @param colNum The column index
   * @return A {@code byte} representation of the value
   */
  public byte getByte(int colNum, byte nullValue) {
    Object v = getValue(colNum);
    return v == null ? nullValue : getNumber(colNum, v, Byte.class);
  }

  /**
   * Returns the value of the specified column as a {@code boolean}, or {@code false} if the value
   * was {@code null}.
   *
   * @param colName The column name
   * @return An {@code int} representation of the value
   */
  public boolean getBoolean(String colName) {
    return getBoolean(colName, false);
  }

  /**
   * Returns the value of the specified column as a {@code boolean}, or {@code false} if the value
   * was {@code null}.
   *
   * @param colNum The column index
   * @return An {@code int} representation of the value
   */
  public boolean getBoolean(int colNum) {
    return getBoolean(colNum, false);
  }

  /**
   * Returns the value of the specified column as a {@code boolean}, or {@code nullValue} if the
   * value was {@code null}.
   *
   * @param colName The column name
   * @return A {@code boolean} representation of the value
   */
  public boolean getBoolean(String colName, boolean nullValue) {
    return ifNotNull(getValue(colName), Bool::from, nullValue);
  }

  /**
   * Returns the value of the specified column as a {@code boolean}, or {@code nullValue} if the
   * value was {@code null}.
   *
   * @param colNum The column index
   * @return A {@code boolean} representation of the value
   */
  public boolean getBoolean(int colNum, boolean nullValue) {
    return ifNotNull(getValue(colNum), Bool::from, nullValue);
  }

  /**
   * Return the value of the specified column as an {@code Integer}.
   *
   * @param colName The column name
   * @return An {@code Integer} representation of the value
   */
  public Integer getInteger(String colName) {
    return getNullableNumber(colName, Integer.class);
  }

  /**
   * Return the value of the specified column as an {@code Integer}.
   *
   * @param colNum The column index
   * @return An {@code Integer} representation of the value
   */
  public Integer getInteger(int colNum) {
    return getNullableNumber(colNum, Integer.class);
  }

  /**
   * Return the value of the specified column as a {@code Double}.
   *
   * @param colName The column name
   * @return A {@code Double} representation of the value
   */
  public Double getDoubleObj(String colName) {
    return getNullableNumber(colName, Double.class);
  }

  /**
   * Return the value of the specified column as a {@code Double}.
   *
   * @param colNum The column index
   * @return A {@code Double} representation of the value
   */
  public Double getDoubleObj(int colNum) {
    return getNullableNumber(colNum, Double.class);
  }

  /**
   * Return the value of the specified column as a {@code Float}.
   *
   * @param colName The column name
   * @return A {@code Float} representation of the value
   */
  public Float getFloatObj(String colName) {
    return getNullableNumber(colName, Float.class);
  }

  /**
   * Return the value of the specified column as a {@code Float}.
   *
   * @param colNum The column index
   * @return A {@code Float} representation of the value
   */
  public Float getFloatObj(int colNum) {
    return getNullableNumber(colNum, Float.class);
  }

  /**
   * Return the value of the specified column as a {@code Long}.
   *
   * @param colName The column name
   * @return A {@code Long} representation of the value
   */
  public Long getLongObj(String colName) {
    return getNullableNumber(colName, Long.class);
  }

  /**
   * Return the value of the specified column as a {@code Long}.
   *
   * @param colNum The column index
   * @return A {@code Long} representation of the value
   */
  public Long getLongObj(int colNum) {
    return getNullableNumber(colNum, Long.class);
  }

  /**
   * Return the value of the specified column as a {@code Short}.
   *
   * @param colName The column name
   * @return A {@code Short} representation of the value
   */
  public Short getShortObj(String colName) {
    return getNullableNumber(colName, Short.class);
  }

  /**
   * Return the value of the specified column as a {@code Short}.
   *
   * @param colNum The column index
   * @return A {@code Short} representation of the value
   */
  public Short getShortObj(int colNum) {
    return getNullableNumber(colNum, Short.class);
  }

  /**
   * Return the value of the specified column as a {@code Byte}.
   *
   * @param colName The column name
   * @return A {@code Byte} representation of the value
   */
  public Byte getByteObj(String colName) {
    return getNullableNumber(colName, Byte.class);
  }

  /**
   * Return the value of the specified column as a {@code Byte}.
   *
   * @param colNum The column index
   * @return A {@code Byte} representation of the value
   */
  public Byte getByteObj(int colNum) {
    return getNullableNumber(colNum, Byte.class);
  }

  /**
   * Returns the value of the specified column as a {@code Boolean}.
   *
   * @param colName The column name
   * @return A {@code boolean} representation of the value
   */
  public Boolean getBooleanObj(String colName) {
    return ifNotNull(getValue(colName), Bool::from);
  }

  /**
   * Returns the value of the specified column as a {@code Boolean}.
   *
   * @param colNum The column index
   * @return A {@code Boolean} representation of the value
   */
  public Boolean getBooleanObj(int colNum) {
    return ifNotNull(getValue(colNum), Bool::from);
  }

  /**
   * Returns the value of the specified column as an {@code enum} of the specified type.
   *
   * @param <T> The type of the {@code enum} class
   * @param colName The column name
   * @return A {@code Boolean} representation of the value
   */
  public <T extends Enum<T>> T getEnum(String colName, Class<T> enumClass) {
    return getEnum(colName, enumClass, null);
  }

  /**
   * Returns the value of the specified column as an {@code enum} of the specified type.
   *
   * @param <T> The type of the {@code enum} class
   * @param colNum The column index
   * @return A {@code Boolean} representation of the value
   */
  public <T extends Enum<T>> T getEnum(int colNum, Class<T> enumClass) {
    return getEnum(colNum, enumClass, null);
  }

  /**
   * Returns the value of the specified column as an {@code enum} of the specified type, or {@code
   * nullValue} if the value was {@code null}.
   *
   * @param <T> The type of the {@code enum} class
   * @param colName The column name
   * @return A {@code Boolean} representation of the value
   */
  @SuppressWarnings("unchecked")
  public <T extends Enum<T>> T getEnum(String colName, Class<T> enumClass, Enum<T> nullValue) {
    return (T) ifNotNull(getValue(colName), enumClass::cast, nullValue);
  }

  /**
   * Returns the value of the specified column as an {@code enum} of the specified type, or {@code
   * nullValue} if the value was {@code null}.
   *
   * @param <T> The type of the {@code enum} class
   * @param colNum The column index
   * @return A {@code Boolean} representation of the value
   */
  @SuppressWarnings("unchecked")
  public <T extends Enum<T>> T getEnum(int colNum, Class<T> enumClass, Enum<T> nullValue) {
    return (T) ifNotNull(getValue(colNum), enumClass::cast, nullValue);
  }

  /**
   * Returns the value of the specified column as an {@code enum} using the specified function to
   * parse the value into an {@code enum} constant.
   *
   * @param <T> The type of the {@code enum} class
   * @param colName The column name
   * @return A {@code Boolean} representation of the value
   */
  public <T extends Enum<T>> T getEnum(String colName, Function<Object, T> parser) {
    return getEnum(colName, parser, null);
  }

  /**
   * Returns the value of the specified column as an {@code enum} using the specified function to
   * parse the value into an {@code enum} constant.
   *
   * @param <T> The type of the {@code enum} class
   * @param colNum The column index
   * @return A {@code Boolean} representation of the value
   */
  public <T extends Enum<T>> T getEnum(int colNum, Function<Object, T> parser) {
    return getEnum(colNum, parser, null);
  }

  /**
   * Returns the value of the specified column as an {@code enum} using the specified function to
   * parse the value into an {@code enum} constant, or {@code nullValue} if the value was {@code
   * null}.
   *
   * @param colNum The column index
   * @return A {@code Boolean} representation of the value
   */
  @SuppressWarnings("unchecked")
  public <T extends Enum<T>> T getEnum(
      String colName, Function<Object, T> parser, Enum<T> nullValue) {
    return (T) ifNotNull(getValue(colName), parser::apply, nullValue);
  }

  /**
   * Returns the value of the specified column as an {@code enum} using the specified function to
   * parse the value into an {@code enum} constant, or {@code nullValue} if the value was {@code
   * null}.
   *
   * @param colNum The column index
   * @return A {@code Boolean} representation of the value
   */
  @SuppressWarnings("unchecked")
  public <T extends Enum<T>> T getEnum(int colNum, Function<Object, T> parser, Enum<T> nullValue) {
    return (T) ifNotNull(getValue(colNum), parser::apply, nullValue);
  }

  /**
   * Updates the value of the column with the specified name.
   *
   * @param colName The column name
   * @param value The new value
   */
  public void setColumn(String colName, Object value) {
    Check.notNull(colName, "colName").is(keyIn(), map, ERR0, colName);
    map.put(colName, value);
  }

  /**
   * Updates the value of the column at the specified index.
   *
   * @param colName The column name
   * @param value The new value
   */
  public void setColumn(int colNum, Object value) {
    map.put(getColumnName(colNum), value);
  }

  /**
   * Appends a new column to the row.
   *
   * @param colName The name of the new column
   * @param value The value of the new column
   */
  public void addColumn(String colName, Object value) {
    Check.notNull(colName, "colName").isNot(keyIn(), map, ERR3, colName);
    map.put(colName, value);
  }

  /**
   * Updates or appends a column, depending on whether the column with the specified name exists.
   *
   * @param colName The name of the column to update or append
   * @param value The new value
   */
  public void setOrAddColumn(String colName, Object value) {
    Check.notNull(colName, "colName");
    map.put(colName, value);
  }

  @Override
  public int hashCode() {
    return map.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    } else if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    Row other = (Row) obj;
    return map.equals(other.map);
  }

  @Override
  public String toString() {
    return map.toString();
  }

  private static <T extends Number> T getNumber(int colNum, Object val, Class<T> targetType) {
    return getNumber(String.valueOf(colNum), val, targetType);
  }

  private static <T extends Number> T getNumber(String colName, Object val, Class<T> targetType) {
    if (val instanceof Number) {
      return NumberMethods.convert((Number) val, targetType);
    } else if (val.getClass() == String.class) {
      return NumberMethods.parse((String) val, targetType);
    }
    return Check.fail(ERR1, colName, targetType, val);
  }

  private <T extends Number> T getNullableNumber(int colNum, Class<T> targetType) {
    return getNullableNumber(String.valueOf(colNum), getValue(colNum), targetType);
  }

  private <T extends Number> T getNullableNumber(String colName, Class<T> targetType) {
    return getNullableNumber(colName, getValue(colName), targetType);
  }

  private static <T extends Number> T getNullableNumber(
      String col, Object val, Class<T> targetType) {
    if (val == null) {
      return null;
    } else if (val instanceof Number) {
      return NumberMethods.convert((Number) val, targetType);
    } else if (val.getClass() == String.class) {
      return NumberMethods.parse((String) val, targetType);
    }
    return Check.fail(ERR1, col, targetType, val);
  }
}
