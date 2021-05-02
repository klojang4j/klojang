package nl.naturalis.yokete.db.ps;

import java.util.HashMap;
import java.util.Map;
import static nl.naturalis.yokete.db.ps.PSSetter.*;

class PSSetters {

  private static PSSetters INSTANCE;

  static PSSetters getInstance() {
    if (INSTANCE == null) {
      INSTANCE = new PSSetters();
    }
    return INSTANCE;
  }

  private Map<Class<?>, PSSetter<?>> cache;

  private PSSetters() {
    cache = createCache();
  }

  private Map<Class<?>, PSSetter<?>> createCache() {
    Map<Class<?>, PSSetter<?>> tmp = new HashMap<>();
    tmp.put(String.class, SET_STRING);
    tmp.put(int.class, SET_INT);
    tmp.put(Integer.class, SET_INT);
    tmp.put(long.class, SET_LONG);
    tmp.put(Long.class, SET_LONG);
    tmp.put(short.class, SET_SHORT);
    tmp.put(Short.class, SET_SHORT);
    tmp.put(byte.class, SET_BYTE);
    tmp.put(Byte.class, SET_BYTE);
    tmp.put(double.class, SET_DOUBLE);
    tmp.put(Double.class, SET_DOUBLE);
    tmp.put(float.class, SET_FLOAT);
    tmp.put(Float.class, SET_FLOAT);
    tmp.put(boolean.class, SET_BOOLEAN);
    tmp.put(Boolean.class, SET_BOOLEAN);
    return null;
  }
}
