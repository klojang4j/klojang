package nl.naturalis.yokete.db.rs;

import java.util.HashMap;
import nl.naturalis.common.NumberMethods;
import nl.naturalis.yokete.db.ResultSetReadException;
import static java.sql.Types.*;
import static nl.naturalis.yokete.db.rs.RSGetter.*;

class EnumEmitters extends HashMap<Integer, Emitter<?, ?>> {

  private static class NumberAdapter<T extends Number> implements Adapter<T, Enum<?>> {

    @Override
    public Enum<?> adapt(T i, Class<Enum<?>> t) {
      return asOrdinal(i, t);
    }
  }

  private static class StringAdapter implements Adapter<String, Enum<?>> {

    @Override
    public Enum<?> adapt(String s, Class<Enum<?>> t) {
      int i;
      try {
        i = NumberMethods.parse(s, Integer.class);
      } catch (IllegalArgumentException e) {
        return asName(s, t);
      }
      return asOrdinal(i, t);
    }
  }

  EnumEmitters() {
    put(BIGINT, new Emitter<>(GET_LONG, new NumberAdapter<Long>()));
    put(INTEGER, new Emitter<>(GET_INT, new NumberAdapter<Integer>()));
    put(SMALLINT, new Emitter<>(GET_SHORT, new NumberAdapter<Short>()));
    put(TINYINT, new Emitter<>(GET_BYTE, new NumberAdapter<Byte>()));
    Emitter<String, Enum<?>> vp0 = new Emitter<>(GET_STRING, new StringAdapter());
    put(VARCHAR, vp0);
    put(CHAR, vp0);
  }

  private static <T extends Number> Enum<?> asOrdinal(T number, Class<Enum<?>> t) {
    if (number == null) {
      return null;
    }
    int i = number.intValue();
    if (i < 0 || i >= t.getEnumConstants().length) {
      String fmt = "Invalid ordinal number for enum type %s: %d";
      String msg = String.format(fmt, t.getSimpleName(), i);
      throw new ResultSetReadException(msg);
    }
    return t.getEnumConstants()[i];
  }

  private static Enum<?> asName(String s, Class<Enum<?>> t) {
    if (s == null) {
      return null;
    }
    for (Enum<?> c : t.getEnumConstants()) {
      if (s.equals(c.name()) || s.equals(c.toString())) {
        return c;
      }
    }
    String fmt = "Unable to parse \"%s\" into %s";
    String msg = String.format(fmt, s, t.getSimpleName());
    throw new ResultSetReadException(msg);
  }
}
