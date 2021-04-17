package nl.naturalis.yokete.util;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import nl.naturalis.common.Bool;
import nl.naturalis.common.check.Check;
import static java.sql.Types.*;
import static nl.naturalis.common.ClassMethods.prettyClassName;
import static nl.naturalis.common.NumberMethods.convert;
import static nl.naturalis.common.NumberMethods.parse;
import static nl.naturalis.yokete.util.ColumnReaders.*;

/**
 * Finds the most suitable of the ResultSet.getXXX methods for a given Java type. If no sure-fire
 * match can be found, then possibly an "adapter" function can be specified that converts the result
 * of a getXXX method to the specified Java type. Therefore what actually gets negotiated is not so
 * much a ResultSet.getXXX method per se, but a Synapse, which is a combination of a
 * ResultSet.getXXX method and a converter function.
 */
class SynapseNegotiator {

  private static SynapseNegotiator INSTANCE;

  public static SynapseNegotiator getInstance() {
    if (INSTANCE == null) {
      INSTANCE = new SynapseNegotiator();
    }
    return INSTANCE;
  }

  /*
   * The entry within the nested map within the SYNAPSE_CACHE that points to the Synapse to use for
   * a Java type if the nested map does not contain an entry for the specified SQL type. Make sure
   * DEFAULT does not correspond to any of the constants in the java.sql.Types class. If there is no
   * reasonable default, you don't need to specify one.
   */
  private static final Integer DEFAULT_SYNAPSE_ENTRY = Integer.MIN_VALUE;
  private static final ColumnReader BIG_DECIMAL_GETTER = newGetObjectInvoker(BigDecimal.class);

  private final Map<Class<?>, Map<Integer, Synapse>> synapseCache;

  private SynapseNegotiator() {
    synapseCache = createSynapseCache();
  }

  Synapse getSynapse(Class<?> javaType, int sqlType) {
    if (!synapseCache.containsKey(javaType)) {
      return Check.fail("Type not supported: %s", prettyClassName(javaType));
    }
    Map<Integer, Synapse> synapses = synapseCache.get(javaType);
    if (!synapses.containsKey(sqlType)) {
      if (synapses.containsKey(DEFAULT_SYNAPSE_ENTRY)) {
        return synapses.get(DEFAULT_SYNAPSE_ENTRY);
      }
      String s0 = SQLTypeNames.getTypeName(sqlType);
      String s1 = prettyClassName(javaType);
      return Check.fail("Cannot convert %s to %s", s0, s1);
    }
    return synapses.get(sqlType);
  }

  private static Map<Class<?>, Map<Integer, Synapse>> createSynapseCache() {
    Map<Class<?>, Map<Integer, Synapse>> tmp = new HashMap<>();
    tmp.put(String.class, createToStringSynapses());
    tmp.put(Integer.class, createToIntSynapses());
    tmp.put(int.class, createToIntSynapses());
    tmp.put(Boolean.class, createToBooleanSynapses());
    tmp.put(boolean.class, createToBooleanSynapses());
    tmp.put(Byte.class, createToByteSynapses());
    tmp.put(byte.class, createToByteSynapses());
    // TODO: Add more ...
    return Map.copyOf(tmp);
  }

  private static Map<Integer, Synapse> createToStringSynapses() {
    // If the Java type is String.class we always call ResultSet.getString()
    // no matter the actual SQL type of the column we are reading.
    return Map.of(DEFAULT_SYNAPSE_ENTRY, new Synapse(GET_STRING));
  }

  private static Map<Integer, Synapse> createToIntSynapses() {
    Map<Integer, Synapse> tmp = new HashMap<>();

    Synapse synapse = new Synapse(GET_INT);
    tmp.put(INTEGER, synapse);
    tmp.put(SMALLINT, synapse);
    tmp.put(TINYINT, synapse);
    tmp.put(BIT, synapse);

    Function<Object, Object> adapter = obj -> convert((Number) obj, Integer.class);

    tmp.put(FLOAT, new Synapse(GET_FLOAT, adapter));

    synapse = new Synapse(GET_LONG, adapter);
    tmp.put(BIGINT, synapse);
    tmp.put(TIMESTAMP, synapse);

    synapse = new Synapse(GET_DOUBLE, adapter);
    tmp.put(DOUBLE, synapse);
    tmp.put(REAL, synapse);

    synapse = new Synapse(BIG_DECIMAL_GETTER, adapter);
    tmp.put(NUMERIC, synapse);
    tmp.put(DECIMAL, synapse);

    synapse = new Synapse(GET_STRING, obj -> parse((String) obj, Integer.class));
    tmp.put(DEFAULT_SYNAPSE_ENTRY, synapse);

    return Map.copyOf(tmp);
  }

  private static Map<Integer, Synapse> createToByteSynapses() {
    Map<Integer, Synapse> tmp = new HashMap<>();

    Synapse synapse = new Synapse(GET_BYTE);
    tmp.put(TINYINT, synapse);
    tmp.put(BIT, synapse);

    Function<Object, Object> adapter = obj -> convert((Number) obj, Byte.class);
    synapse = new Synapse(GET_INT, adapter);
    tmp.put(INTEGER, synapse);
    tmp.put(SMALLINT, synapse);

    tmp.put(FLOAT, new Synapse(GET_FLOAT, adapter));
    tmp.put(BIGINT, new Synapse(GET_LONG, adapter));

    synapse = new Synapse(GET_DOUBLE, adapter);
    tmp.put(DOUBLE, synapse);
    tmp.put(REAL, synapse);

    synapse = new Synapse(BIG_DECIMAL_GETTER, adapter);
    tmp.put(NUMERIC, synapse);
    tmp.put(DECIMAL, synapse);

    synapse = new Synapse(GET_STRING, obj -> parse((String) obj, Byte.class));
    return Map.copyOf(tmp);
  }

  private static Map<Integer, Synapse> createToBooleanSynapses() {
    Map<Integer, Synapse> tmp = new HashMap<>();

    tmp.put(BOOLEAN, new Synapse(GET_BOOLEAN));

    Synapse synapse = new Synapse(GET_INT, Bool::from);
    tmp.put(INTEGER, synapse);
    tmp.put(SMALLINT, synapse);
    tmp.put(TINYINT, synapse);
    tmp.put(BIT, synapse);

    tmp.put(FLOAT, new Synapse(GET_FLOAT, Bool::from));
    tmp.put(BIGINT, new Synapse(GET_LONG, Bool::from));

    synapse = new Synapse(GET_DOUBLE, Bool::from);
    tmp.put(DOUBLE, synapse);
    tmp.put(REAL, synapse);

    synapse = new Synapse(BIG_DECIMAL_GETTER, Bool::from);
    tmp.put(NUMERIC, synapse);
    tmp.put(DECIMAL, synapse);

    synapse = new Synapse(GET_STRING, Bool::from);
    tmp.put(DEFAULT_SYNAPSE_ENTRY, synapse);

    return Map.copyOf(tmp);
  }
}
