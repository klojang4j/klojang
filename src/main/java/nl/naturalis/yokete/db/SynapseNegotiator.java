package nl.naturalis.yokete.db;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import nl.naturalis.common.check.Check;
import nl.naturalis.common.collection.UnmodifiableTypeMap;
import static nl.naturalis.common.ClassMethods.prettyClassName;

/**
 * Finds the most suitable of the ResultSet.getXXX methods for a given Java type. If no sure-fire
 * match can be found, then an "adapter" function can be specified that converts the result of the
 * getXXX method to the required Java type. Therefore what actually gets negotiated is not so much a
 * ResultSet.getXXX method per se, but a Synapse, which is a combination of a ResultSet.getXXX
 * method and a converter function.
 */
class SynapseNegotiator {

  private static SynapseNegotiator INSTANCE;

  static SynapseNegotiator getInstance() {
    if (INSTANCE == null) {
      INSTANCE = new SynapseNegotiator();
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

  private final Map<Class<?>, Map<Integer, Synapse<?, ?>>> synapseCache;

  private SynapseNegotiator() {
    synapseCache = createSynapseCache();
  }

  @SuppressWarnings("unchecked")
  <T, R> Synapse<T, R> getSynapse(Class<R> javaType, int sqlType) {
    if (!synapseCache.containsKey(javaType)) {
      return Check.fail("Type not supported: %s", prettyClassName(javaType));
    }
    Map<Integer, Synapse<?, ?>> synapses = synapseCache.get(javaType);
    if (!synapses.containsKey(sqlType)) {
      if (synapses.containsKey(DEFAULT_ENTRY)) {
        return (Synapse<T, R>) synapses.get(DEFAULT_ENTRY);
      }
      String s0 = SQLTypeNames.getTypeName(sqlType);
      String s1 = prettyClassName(javaType);
      return Check.fail("Cannot convert %s to %s", s0, s1);
    }
    return (Synapse<T, R>) synapses.get(sqlType);
  }

  private static Map<Class<?>, Map<Integer, Synapse<?, ?>>> createSynapseCache() {

    Map<Class<?>, Map<Integer, Synapse<?, ?>>> tmp = new HashMap<>();

    tmp.put(String.class, ToStringSynapses.get());

    Map<Integer, Synapse<?, ?>> synapses = ToIntSynapses.get();
    tmp.put(Integer.class, synapses);
    tmp.put(int.class, synapses);

    synapses = ToByteSynapses.get();
    tmp.put(Byte.class, synapses);
    tmp.put(byte.class, synapses);

    synapses = ToBooleanSynapses.get();
    tmp.put(Boolean.class, synapses);
    tmp.put(boolean.class, synapses);

    tmp.put(LocalDate.class, ToLocalDateSynapses.get());
    tmp.put(LocalDateTime.class, ToLocalDateTimeSynapses.get());

    tmp.put(Enum.class, ToEnumSynapses.get());

    return UnmodifiableTypeMap.copyOf(tmp);
  }
}
