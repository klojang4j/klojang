package nl.naturalis.yokete.db.read;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import nl.naturalis.common.check.Check;
import nl.naturalis.common.collection.UnmodifiableTypeMap;
import nl.naturalis.yokete.db.SQLTypeNames;
import static nl.naturalis.common.ClassMethods.prettyClassName;

/**
 * Finds the most suitable of the ResultSet.getXXX methods for a given Java type. If no sure-fire
 * match can be found, then an "adapter" function can be specified that converts the result of the
 * getXXX method to the required Java type. Therefore what actually gets negotiated is not so much a
 * ResultSet.getXXX method per se, but a Synapse, which is a combination of a ResultSet.getXXX
 * method and a converter function.
 */
class ValueProducerNegotiator {

  private static ValueProducerNegotiator INSTANCE;

  static ValueProducerNegotiator getInstance() {
    if (INSTANCE == null) {
      INSTANCE = new ValueProducerNegotiator();
    }
    return INSTANCE;
  }

  /*
   * The entry within the nested maps of the SYNAPSE_CACHE that points to the Synapse to use for a
   * Java type if the nested map does not contain a specific synapse for the requested SQL type.
   * Make sure DEFAULT does not correspond to any of the (int) constants in the java.sql.Types
   * class. If there is no reasonable default, you don't need to specify one.
   */
  static final Integer DEFAULT_ENTRY = Integer.MIN_VALUE;

  private final Map<Class<?>, Map<Integer, ValueProducer<?, ?>>> producers;

  private ValueProducerNegotiator() {
    producers = createProducers();
  }

  @SuppressWarnings("unchecked")
  <COLUMN_TYPE, FIELD_TYPE> ValueProducer<COLUMN_TYPE, FIELD_TYPE> getProducer(
      Class<FIELD_TYPE> fieldType, int sqlType) {
    if (!producers.containsKey(fieldType)) {
      return Check.fail("Type not supported: %s", prettyClassName(fieldType));
    }
    Map<Integer, ValueProducer<?, ?>> syns = producers.get(fieldType);
    if (!syns.containsKey(sqlType)) {
      if (syns.containsKey(DEFAULT_ENTRY)) {
        return (ValueProducer<COLUMN_TYPE, FIELD_TYPE>) syns.get(DEFAULT_ENTRY);
      }
      String s0 = SQLTypeNames.getTypeName(sqlType);
      String s1 = prettyClassName(fieldType);
      return Check.fail("Cannot convert %s to %s", s0, s1);
    }
    return (ValueProducer<COLUMN_TYPE, FIELD_TYPE>) syns.get(sqlType);
  }

  private static Map<Class<?>, Map<Integer, ValueProducer<?, ?>>> createProducers() {

    Map<Class<?>, Map<Integer, ValueProducer<?, ?>>> tmp = new HashMap<>();

    Map<Integer, ValueProducer<?, ?>> producers;
    tmp.put(String.class, my(new StringProducers()));
    producers = my(new IntProducers());
    tmp.put(Integer.class, producers);
    tmp.put(int.class, producers);
    producers = my(new LongProducers());
    tmp.put(Long.class, producers);
    tmp.put(long.class, producers);
    producers = my(new ByteProducers());
    tmp.put(Byte.class, producers);
    tmp.put(byte.class, producers);
    producers = my(new BooleanProducers());
    tmp.put(Boolean.class, producers);
    tmp.put(boolean.class, producers);
    tmp.put(LocalDate.class, my(new LocalDateProducers()));
    tmp.put(LocalDateTime.class, my(new LocalDateTimeProducers()));

    tmp.put(Enum.class, new EnumProducers());

    // TODO: more of this

    return UnmodifiableTypeMap.copyOf(tmp);
  }

  private static Map<Integer, ValueProducer<?, ?>> my(Map<Integer, ValueProducer<?, ?>> src) {
    return Map.copyOf(src);
  }
}
