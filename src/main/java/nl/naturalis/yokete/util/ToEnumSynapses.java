package nl.naturalis.yokete.util;

import java.util.HashMap;
import java.util.Map;
import nl.naturalis.common.NumberMethods;
import static nl.naturalis.yokete.util.ColumnReaders.*;
import static java.sql.Types.*;

class ToEnumSynapses {

  private static class IntToEnumAdapter implements Adapter<Integer, Enum<?>> {

    @Override
    public Enum<?> adapt(Integer i, Class<Enum<?>> t) {
      return asOrdinal(i, t);
    }
  }

  private static class StringToEnumAdapter implements Adapter<String, Enum<?>> {

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

  private ToEnumSynapses() {}

  static Map<Integer, Synapse<?, ?>> get() {

    Map<Integer, Synapse<?, Enum<?>>> tmp = new HashMap<>();

    Synapse<Integer, Enum<?>> syn0 = new Synapse<>(GET_INT, new IntToEnumAdapter());
    tmp.put(INTEGER, syn0);
    tmp.put(SMALLINT, syn0);
    tmp.put(TINYINT, syn0);
    tmp.put(BIT, syn0);

    Synapse<String, Enum<?>> syn1 = new Synapse<>(GET_STRING, new StringToEnumAdapter());
    tmp.put(VARCHAR, syn1);
    tmp.put(CHAR, syn1);

    return Map.copyOf(tmp);
  }

  private static Enum<?> asOrdinal(Integer i, Class<Enum<?>> t) {
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
