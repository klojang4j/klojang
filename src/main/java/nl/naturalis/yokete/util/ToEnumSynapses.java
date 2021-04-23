package nl.naturalis.yokete.util;

import java.util.HashMap;
import java.util.Map;
import nl.naturalis.common.NumberMethods;
import static nl.naturalis.yokete.util.ColumnReaders.*;
import static java.sql.Types.*;

class ToEnumSynapses {

  private static final String ERR_BASE = "Cannot convert %s to %s";
  private static final String ERR_OUT_OF_RANGE = ERR_BASE + ": invalid ordinal number";

  private static class IntToEnumAdapter implements Adapter<Integer, Enum<?>> {

    @Override
    public Enum<?> adapt(Integer i, Class<Enum<?>> t, ResultSetReaderConfig cfg) {
      if (i < 0 || i >= t.getEnumConstants().length) {
        String msg = String.format(ERR_OUT_OF_RANGE, i, t.getSimpleName());
        throw new ResultSetReadException(msg);
      }
      return t.getEnumConstants()[i];
    }
  }

  private static class StringToEnumAdapter implements Adapter<String, Enum<?>> {

    @Override
    public Enum<?> adapt(String s, Class<Enum<?>> t, ResultSetReaderConfig cfg) {
      int i;
      try {
        i = NumberMethods.parse(s, Integer.class);
      } catch (IllegalArgumentException e) {
        for (Enum<?> c : t.getEnumConstants()) {
          if (s.equals(c.name()) || s.equals(c.toString())) {
            return c;
          }
        }
        String msg = String.format(ERR_BASE, s, t.getSimpleName());
        throw new ResultSetReadException(msg);
      }
      if (i < 0 || i >= t.getEnumConstants().length) {
        String msg = String.format(ERR_OUT_OF_RANGE, i, t.getSimpleName());
        throw new ResultSetReadException(msg);
      }
      return t.getEnumConstants()[i];
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
}
