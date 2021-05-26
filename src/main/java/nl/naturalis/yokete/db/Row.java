package nl.naturalis.yokete.db;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import nl.naturalis.common.NumberMethods;
import nl.naturalis.common.Tuple;
import nl.naturalis.common.check.Check;
import static java.util.stream.Collectors.toList;
import static nl.naturalis.common.ObjectMethods.ifNotNull;
import static nl.naturalis.common.check.CommonChecks.arrayIndexOf;
import static nl.naturalis.common.check.CommonChecks.keyIn;

public class Row {

  private static final String ERR0 = "Column not present in ResultSet: \"%s\"";
  private static final String ERR1 = "Column %s not convertible to %s: %s";
  private static final String ERR2 = "Invalid column number: %d";

  public static Row withColumns(Tuple<String, Object>[] columns) {
    return new Row(columns);
  }

  private final Tuple<String, Object>[] tuples;

  private Map<String, Object> map;

  private Row(Tuple<String, Object>[] tuples) {
    this.tuples = tuples;
  }

  public int getColumnCount() {
    return tuples.length;
  }

  public List<String> getColumnNames() {
    return Arrays.stream(tuples).map(Tuple::getLeft).collect(toList());
  }

  public boolean hasColumn(String colName) {
    return map().keySet().contains(colName);
  }

  public Map<String, Object> toMap() {
    return map;
  }

  @SuppressWarnings("unchecked")
  public <T> T get(String colName) {
    return (T) Check.that(colName).is(keyIn(), map(), ERR0, colName).ok(map::get);
  }

  @SuppressWarnings("unchecked")
  public <T> T get(int colNum) {
    return (T) Check.that(colNum).is(arrayIndexOf(), tuples).ok(i -> tuples[i]);
  }

  public String getString(String colName) {
    Object v = Check.that(colName).is(keyIn(), map(), ERR0, colName).ok(map::get);
    return v == null ? null : v.toString();
  }

  public String getString(int colNum) {
    Object v = Check.that(colNum).is(arrayIndexOf(), tuples).ok(i -> tuples[i]);
    return v == null ? null : v.toString();
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

  // TODO: more of this

  private Object getValue(String colName) {
    return Check.that(colName).is(keyIn(), map(), ERR0, colName).ok(map::get);
  }

  private Object getValue(int colNum) {
    return Check.that(colNum)
        .is(arrayIndexOf(), tuples, ERR2, colNum)
        .ok(i -> tuples[i].getRight());
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
    }
    if (val instanceof Number) {
      return NumberMethods.convert((Number) val, targetType);
    } else if (val.getClass() == String.class) {
      return NumberMethods.parse((String) val, targetType);
    }
    return Check.fail(ERR1, colName, targetType, val);
  }

  private Map<String, Object> map() {
    if (map == null) {
      map = Tuple.toMap(tuples);
    }
    return map;
  }
}
