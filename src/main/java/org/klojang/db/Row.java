package org.klojang.db;

import java.sql.ResultSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.klojang.render.Accessor;
import nl.naturalis.common.Bool;
import nl.naturalis.common.NumberMethods;
import nl.naturalis.common.Tuple;
import nl.naturalis.common.check.Check;
import nl.naturalis.common.collection.IntList;
import static nl.naturalis.common.ObjectMethods.ifNotNull;
import static nl.naturalis.common.check.CommonChecks.between;
import static nl.naturalis.common.check.CommonChecks.keyIn;

/**
 * A thin wrapper around a {@code Map&lt;String,Object&gt;} instance that mimicks some of the
 * behaviours of {@link ResultSet}. {@code Row} objects are produced by a {@link ResultSetMappifier}
 * and can be quickly pushed up into the higher layers of your application without them actually
 * acquiring an awkward dependency on {@code java.sql}. {@code Row} objects can be inserted directly
 * into templates, without having to register a separate {@link Accessor} for them. (Under the hood
 * an automatically registered {@code RowAccessor} is used.)
 *
 * <p>Note that an important difference between a {@code Row} and a {@code ResultSet} is that a
 * {@code Row} is writable, too. Also, contrary to JDBC, column numbers need to be specified in a
 * zero-based manner.
 *
 * @author Ayco Holleman
 */
public class Row {

  private static final String ERR0 = "No such column: \"%s\"";
  private static final String ERR1 = "Column %s not convertible to %s: %s";
  private static final String ERR2 = "Invalid column number: %d";
  private static final String ERR3 = "Columns already exists: %d";

  public static Row withColumns(Tuple<String, Object>[] columns) {
    return new Row(columns);
  }

  public static Row fromMap(Map<String, Object> data) {
    return new Row(data);
  }

  private final Map<String, Object> map;

  private Row(Tuple<String, Object>[] columns) {
    // Reserve some extra space for potentital additions
    map = Tuple.toMap(columns, () -> new LinkedHashMap<>(columns.length + 4));
  }

  private Row(Map<String, Object> data) {
    map = new LinkedHashMap<>(data.size() + 4);
    map.putAll(data);
  }

  public int size() {
    return map.size();
  }

  /**
   * Returns the name of the column with the specified index.
   *
   * @param colNum The column index (zero-based)
   * @return The column name
   */
  public String getColumnName(int colNum) {
    Check.that(colNum).is(between(), IntList.of(0, map.size()), ERR2, colNum);
    return (String) map.keySet().toArray()[colNum];
  }

  /**
   * Returns the index of the column with the specified name.
   *
   * @param colName The column name
   * @return The column index (zero-based)
   */
  public int getColumnNumber(String colName) {
    Check.notNull(colName).is(keyIn(), map, ERR0, colName).ok(map::get);
    return List.copyOf(map.keySet()).indexOf(colName);
  }

  /**
   * Returns an unmodifiable {@code List} of the column names.
   *
   * @return An unmodifiable {@code List} of the column names
   */
  public List<String> getColumnNames() {
    return List.copyOf(map.keySet());
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
   * Converts this row to an unmodifiable {@code Map} containing the column-name-to-column-value
   * mappings.
   *
   * @return An unmodifiable {@code Map} containing the column-name-to-column-value mappings
   */
  public Map<String, Object> toMap() {
    return Map.copyOf(map);
  }

  /**
   * Returns the value of the column with the specified name
   *
   * @param colName The column name
   * @return The value
   */
  public Object getValue(String colName) {
    return Check.notNull(colName, "colName").is(keyIn(), map, ERR0, colName).ok(map::get);
  }

  /**
   * Returns the value of the column with the specified index.
   *
   * @param colNum The column number (zero-based)
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
   * Returns the value of the column with the specified index, casting it to the specified type.
   *
   * @param <T> The type to cast the value to
   * @param colNum The column number (zero-based)
   * @return The value
   */
  @SuppressWarnings("unchecked")
  public <T> T get(int colNum) {
    return (T) getValue(colNum);
  }

  /**
   * Returns the value of the specified column as a {@code String}.
   *
   * @param colName
   * @return
   */
  public String getString(String colName) {
    return ifNotNull(getValue(colName), Object::toString);
  }

  /**
   * Returns the value of the specified column as a {@code String}.
   *
   * @param colNum
   * @return
   */
  public String getString(int colNum) {
    return ifNotNull(getValue(colNum), Object::toString);
  }

  public int getInt(String colName) {
    return getInt(colName, 0);
  }

  public int getInt(int colNum) {
    return getInt(colNum, 0);
  }

  public int getInt(String colName, int nullValue) {
    Object v = getValue(colName);
    return v == null ? nullValue : getNumber(colName, v, Integer.class);
  }

  public int getInt(int colNum, int nullValue) {
    Object v = getValue(colNum);
    return v == null ? nullValue : getNumber(colNum, v, Integer.class);
  }

  public double getDouble(String colName) {
    return getDouble(colName, 0);
  }

  public double getDouble(String colName, double nullValue) {
    Object v = getValue(colName);
    return v == null ? nullValue : getNumber(colName, v, Double.class);
  }

  public float getFloat(String colName) {
    return getFloat(colName, 0F);
  }

  public float getFloat(String colName, float nullValue) {
    Object v = getValue(colName);
    return v == null ? nullValue : getNumber(colName, v, Float.class);
  }

  public long getLong(String colName) {
    return getLong(colName, 0L);
  }

  public long getLong(String colName, long nullValue) {
    Object v = getValue(colName);
    return v == null ? nullValue : getNumber(colName, v, Long.class);
  }

  public short getShort(String colName) {
    return getShort(colName, (short) 0);
  }

  public short getShort(String colName, short nullValue) {
    Object v = getValue(colName);
    return v == null ? nullValue : getNumber(colName, v, Short.class);
  }

  public byte getByte(String colName) {
    return getByte(colName, (byte) 0);
  }

  public byte getByte(String colName, byte nullValue) {
    Object v = getValue(colName);
    return v == null ? nullValue : getNumber(colName, v, Byte.class);
  }

  public boolean getBoolean(String colName) {
    return getBoolean(colName, false);
  }

  public boolean getBoolean(String colName, boolean nullValue) {
    return ifNotNull(getValue(colName), Bool::from, nullValue);
  }

  public Integer getInteger(String colName) {
    return getNullableNumber(colName, Integer.class);
  }

  public Integer getInteger(int colNum) {
    return getNullableNumber(colNum, Integer.class);
  }

  public Double getDoubleObj(String colName) {
    return getNullableNumber(colName, Double.class);
  }

  public Double getDoubleObj(int colNum) {
    return getNullableNumber(colNum, Double.class);
  }

  public Float getFloatObj(String colName) {
    return getNullableNumber(colName, Float.class);
  }

  public Float getFloatObj(int colNum) {
    return getNullableNumber(colNum, Float.class);
  }

  public Long getLongObj(String colName) {
    return getNullableNumber(colName, Long.class);
  }

  public Long getLongObj(int colNum) {
    return getNullableNumber(colNum, Long.class);
  }

  public Short getShortObj(String colName) {
    return getNullableNumber(colName, Short.class);
  }

  public Short getShortObj(int colNum) {
    return getNullableNumber(colNum, Short.class);
  }

  public Byte getByteObj(String colName) {
    return getNullableNumber(colName, Byte.class);
  }

  public Byte getByteObj(int colNum) {
    return getNullableNumber(colNum, Byte.class);
  }

  public <T extends Enum<T>> T getEnum(String colName, Class<T> enumClass) {
    return getEnum(colName, enumClass, null);
  }

  @SuppressWarnings("unchecked")
  public <T extends Enum<T>> T getEnum(String colName, Class<T> enumClass, Enum<T> nullValue) {
    return (T) ifNotNull(getValue(colName), enumClass::cast, nullValue);
  }

  public <T extends Enum<T>> T getEnum(String colName, Function<Object, T> parser) {
    return getEnum(colName, parser, null);
  }

  @SuppressWarnings("unchecked")
  public <T extends Enum<T>> T getEnum(
      String colName, Function<Object, T> parser, Enum<T> nullValue) {
    return (T) ifNotNull(getValue(colName), parser::apply, nullValue);
  }

  // TODO: more of the above

  public void setColumn(String colName, Object value) {
    Check.notNull(colName, "colName").is(keyIn(), map, ERR0, colName);
    map.put(colName, value);
  }

  public void setColumn(int colNum, Object value) {
    map.put(getColumnName(colNum), value);
  }

  public void addColumn(String colName, Object value) {
    Check.notNull(colName, "colName").isNot(keyIn(), map, ERR3, colName);
    map.put(colName, value);
  }

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
