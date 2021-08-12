package org.klojang.x.db.rs;

import org.klojang.db.ResultSetReadException;
import nl.naturalis.common.NumberMethods;
import static java.sql.Types.*;
import static org.klojang.x.db.rs.RsMethod.*;

class EnumExtractors extends ExtractorLookup<Enum<?>> {

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
    add(BIGINT, new RsExtractor<>(GET_LONG, new NumberAdapter<Long>()));
    add(INTEGER, new RsExtractor<>(GET_INT, new NumberAdapter<Integer>()));
    add(SMALLINT, new RsExtractor<>(GET_SHORT, new NumberAdapter<Short>()));
    add(TINYINT, new RsExtractor<>(GET_BYTE, new NumberAdapter<Byte>()));
    addMultiple(new RsExtractor<>(GET_STRING, new StringAdapter()), CHAR, VARCHAR);
  }

  private static <T extends Number> Enum<?> asOrdinal(T number, Class<Enum<?>> enumClass) {
    if (number == null) {
      return null;
    }
    int i = number.intValue();
    if (i < 0 || i >= enumClass.getEnumConstants().length) {
      String fmt = "Invalid ordinal number for enum type %s: %d";
      String msg = String.format(fmt, enumClass.getSimpleName(), i);
      throw new ResultSetReadException(msg);
    }
    return enumClass.getEnumConstants()[i];
  }

  private static Enum<?> asName(String s, Class<Enum<?>> enumClass) {
    if (s == null) {
      return null;
    }
    for (Enum<?> c : enumClass.getEnumConstants()) {
      if (s.equals(c.name()) || s.equals(c.toString())) {
        return c;
      }
    }
    String fmt = "Unable to parse \"%s\" into %s";
    String msg = String.format(fmt, s, enumClass.getSimpleName());
    throw new ResultSetReadException(msg);
  }
}
