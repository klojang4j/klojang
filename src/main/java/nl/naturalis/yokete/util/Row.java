package nl.naturalis.yokete.util;

import java.math.BigInteger;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import nl.naturalis.common.check.Check;

/**
 * Extension of {@code HashMap} with a bit of sugar coating useful when reading SQL {@link ResultSet
 * result sets}. It deviates from its {@code HashMap} in that it does not allow {@code keys}, since
 * there's will not be any columns in a {@code ResultSet} with a {@code null} column label.
 *
 * @author Ayco Holleman
 */
public class Row extends HashMap<String, Object> {

  public Row() {}

  public Row(int initialCapacity) {
    super(initialCapacity);
  }

  public Row(Map<? extends String, ? extends Object> m) {
    super(m);
  }

  public Row(int initialCapacity, float loadFactor) {
    super(initialCapacity, loadFactor);
  }

  @SuppressWarnings("unchecked")
  public <T> T valueOf(String key) {
    return (T) Check.notNull(key).ok(this::get);
  }

  public String getString(String key) {
    Object v = Check.notNull(key).ok(this::get);
    return v == null ? null : v.toString();
  }

  public int getInt(String key) {
    return getInt(key, 0);
  }

  public int getInt(String key, int defaultValue) {
    Object v = Check.notNull(key).ok(this::get);
    if (v == null) {
      return defaultValue;
    } else if (v.getClass() == Integer.class) {
      return (Integer) get(key);
    } else if (v instanceof Number) {
      return ((Number) v).intValue();
    } else if (v.getClass() == String.class) {
      BigInteger bi = new BigInteger((String) v);
      bi.intValueExact();
    }
    return Check.fail("Cannot convert key \"%s\" to int", key);
  }

  public int getByte(String key) {
    return getInt(key, 0);
  }

  public int getByte(String key, int defaultValue) {
    Object v = Check.notNull(key).ok(this::get);
    if (v == null) {
      return defaultValue;
    } else if (v.getClass() == Byte.class) {
      return (Byte) get(key);
    } else if (v instanceof Number) {
      return ((Number) v).intValue();
    } else if (v.getClass() == String.class) {
      BigInteger bi = new BigInteger((String) v);
      bi.byteValueExact();
    }
    return Check.fail("Cannot convert key \"%s\" to int", key);
  }
}
