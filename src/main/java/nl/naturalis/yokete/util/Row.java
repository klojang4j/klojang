package nl.naturalis.yokete.util;

import java.sql.ResultSet;
import java.util.HashMap;
import nl.naturalis.common.NumberMethods;
import nl.naturalis.common.check.Check;
import static nl.naturalis.common.check.CommonChecks.keyIn;

/**
 * Extension of {@code HashMap} with a bit of sugar coating useful when reading SQL {@link ResultSet
 * result sets}. It deviates from its {@code HashMap} in that it does not allow {@code keys}, since
 * there's will not be any columns in a {@code ResultSet} with a {@code null} column label.
 *
 * @author Ayco Holleman
 */
public class Row extends HashMap<String, Object> {

  private static final String ERR0 = "No such key: %s";
  private static final String ERR1 = "Key %s not convertible to %s: %s";

  Row() {}

  Row(int initialCapacity) {
    super(initialCapacity);
  }

  @SuppressWarnings("unchecked")
  public <T> T valueOf(String key) {
    return (T) Check.that(key).is(keyIn(), this, ERR0, key).ok(this::get);
  }

  public String getString(String key) {
    Object v = Check.that(key).is(keyIn(), this, ERR0, key).ok(this::get);
    return v == null ? null : v.toString();
  }

  public Integer getInt(String key) {
    Object v = Check.that(key).is(keyIn(), this, ERR0, key).ok(this::get);
    return v == null ? null : getNumber(key, v, Integer.class);
  }

  public Integer getInt(String key, int defaultValue) {
    Object v = Check.that(key).is(keyIn(), this, ERR0, key).ok(this::get);
    return v == null ? defaultValue : getNumber(key, v, Integer.class);
  }

  public Byte getByte(String key) {
    Object v = Check.that(key).is(keyIn(), this, ERR0, key).ok(this::get);
    return v == null ? null : getNumber(key, v, Byte.class);
  }

  public Byte getByte(String key, byte defaultValue) {
    Object v = Check.that(key).is(keyIn(), this, ERR0, key).ok(this::get);
    return v == null ? defaultValue : getNumber(key, v, Byte.class);
  }

  private static <T extends Number> T getNumber(String key, Object val, Class<T> targetType) {
    if (val instanceof Number) {
      return NumberMethods.convert((Number) val, targetType);
    } else if (val.getClass() == String.class) {
      return NumberMethods.parse((String) val, targetType);
    }
    return Check.fail(ERR1, key, targetType, val);
  }
}
