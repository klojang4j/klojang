package nl.naturalis.yokete.db;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import nl.naturalis.common.NumberMethods;
import nl.naturalis.common.Tuple;
import nl.naturalis.common.check.Check;
import nl.naturalis.common.collection.IntList;
import static nl.naturalis.common.ObjectMethods.ifNotNull;
import static nl.naturalis.common.check.CommonChecks.between;
import static nl.naturalis.common.check.CommonChecks.keyIn;

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

  public String getColumnName(int colNum) {
    Check.that(colNum).is(between(), IntList.of(0, map.size()), ERR2, colNum);
    return (String) map.keySet().toArray()[colNum];
  }

  public int getColumnNumber(String colName) {
    Check.notNull(colName).is(keyIn(), map, ERR0, colName).ok(map::get);
    return List.copyOf(map.keySet()).indexOf(colName);
  }

  public List<String> getColumnNames() {
    return List.copyOf(map.keySet());
  }

  public boolean hasColumn(String colName) {
    return map.containsKey(colName);
  }

  public Map<String, Object> toMap() {
    return Map.copyOf(map);
  }

  public Object getValue(String colName) {
    return Check.notNull(colName, "colName").is(keyIn(), map, ERR0, colName).ok(map::get);
  }

  public Object getValue(int colNum) {
    return map.get(getColumnName(colNum));
  }

  @SuppressWarnings("unchecked")
  public <T> T get(String colName) {
    return (T) getValue(colName);
  }

  @SuppressWarnings("unchecked")
  public <T> T get(int colNum) {
    return (T) getValue(colNum);
  }

  public String getString(String colName) {
    return ifNotNull(get(colName), Object::toString);
  }

  public String getString(int colNum) {
    return ifNotNull(get(colNum), Object::toString);
  }

  public Integer getInteger(String colName) {
    return getNullableNumber(colName, Integer.class);
  }

  public int getInt(String colName) {
    return getInt(colName, 0);
  }

  public int getInt(int colNum) {
    return getInt(colNum, 0);
  }

  public int getInt(String colName, int nullValue) {
    return ifNotNull(getValue(colName), v -> getNumber(colName, v, Integer.class), nullValue);
  }

  public int getInt(int colNum, int nullValue) {
    return ifNotNull(getValue(colNum), v -> getNumber(colNum, v, Integer.class), nullValue);
  }

  public Double getObjDouble(String colName) {
    return getNullableNumber(colName, Double.class);
  }

  public double getDouble(String colName) {
    return getByte(colName, (byte) 0);
  }

  public double getDouble(String colName, double nullValue) {
    return ifNotNull(getValue(colName), v -> getNumber(colName, v, Double.class), nullValue);
  }

  public Long getObjLong(String colName) {
    return getNullableNumber(colName, Long.class);
  }

  public long getLong(String colName) {
    return getByte(colName, (byte) 0);
  }

  public long getLong(String colName, long nullValue) {
    return ifNotNull(getValue(colName), v -> getNumber(colName, v, Long.class), nullValue);
  }

  public Byte getObjByte(String colName) {
    return getNullableNumber(colName, Byte.class);
  }

  public byte getByte(String colName) {
    return getByte(colName, (byte) 0);
  }

  public byte getByte(String colName, byte nullValue) {
    return ifNotNull(getValue(colName), v -> getNumber(colName, v, Byte.class), nullValue);
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

  private <T extends Number> T getNullableNumber(String colName, Class<T> targetType) {
    Object val = getValue(colName);
    if (val == null) {
      return null;
    } else if (val instanceof Number) {
      return NumberMethods.convert((Number) val, targetType);
    } else if (val.getClass() == String.class) {
      return NumberMethods.parse((String) val, targetType);
    }
    return Check.fail(ERR1, colName, targetType, val);
  }
}
