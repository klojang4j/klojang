package nl.naturalis.yokete.db;

import java.util.Map;
import java.util.Set;
import nl.naturalis.common.NumberMethods;
import nl.naturalis.common.check.Check;
import static nl.naturalis.common.ObjectMethods.ifNotNull;
import static nl.naturalis.common.check.CommonChecks.keyIn;

public class Row {

  private static final String ERR0 = "Column not present in ResultSet: \"%s\"";
  private static final String ERR1 = "Column %s not convertible to %s: %s";

  public static Row withData(Map<String, Object> data) {
    return new Row(data);
  }

  private final Map<String, Object> data;

  private Row(Map<String, Object> data) {
    this.data = data;
  }

  public Set<String> getColumnNames() {
    return data.keySet();
  }

  public boolean hasColumn(String colName) {
    return data.keySet().contains(colName);
  }

  public Map<String, Object> getData() {
    return Map.copyOf(data);
  }

  @SuppressWarnings("unchecked")
  public <T> T get(String colName) {
    return (T) Check.that(colName).is(keyIn(), data, ERR0, colName).ok(data::get);
  }

  public String getString(String colName) {
    Object v = Check.that(colName).is(keyIn(), data, ERR0, colName).ok(data::get);
    return v == null ? null : v.toString();
  }

  public Integer getInteger(String colName) {
    return getNullableNumber(colName, Integer.class);
  }

  public int getInt(String colName) {
    return getInt(colName, 0);
  }

  public int getInt(String colName, int nullValue) {
    return ifNotNull(getValue(colName), v -> getNumber(colName, v, Integer.class), nullValue);
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
    return Check.that(colName).is(keyIn(), data, ERR0, colName).ok(data::get);
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
}
