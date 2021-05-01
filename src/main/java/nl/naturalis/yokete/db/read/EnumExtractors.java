package nl.naturalis.yokete.db.read;

import java.util.HashMap;
import nl.naturalis.common.NumberMethods;
import nl.naturalis.yokete.db.ResultSetReadException;
import static java.sql.Types.*;
import static nl.naturalis.yokete.db.read.ColumnReader.*;

class EnumExtractors extends HashMap<Integer, ValueExtractor<?, ?>> {

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

  EnumExtractors() {
    put(BIGINT, new ValueExtractor<>(GET_LONG, new NumberAdapter<Long>()));
    put(INTEGER, new ValueExtractor<>(GET_INT, new NumberAdapter<Integer>()));
    put(SMALLINT, new ValueExtractor<>(GET_SHORT, new NumberAdapter<Short>()));
    put(TINYINT, new ValueExtractor<>(GET_BYTE, new NumberAdapter<Byte>()));
    ValueExtractor<String, Enum<?>> vp0 = new ValueExtractor<>(GET_STRING, new StringAdapter());
    put(VARCHAR, vp0);
    put(CHAR, vp0);
  }

  private static <T extends Number> Enum<?> asOrdinal(T number, Class<Enum<?>> t) {
    int i = number.intValue();
    if (i < 0 || i >= t.getEnumConstants().length) {
      String fmt = "Invalid ordinal number for enum type %s: %d";
      String msg = String.format(fmt, t.getSimpleName(), i);
      throw new ResultSetReadException(msg);
    }
    return t.getEnumConstants()[i];
  }

  private static Enum<?> asName(String s, Class<Enum<?>> t) {
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
