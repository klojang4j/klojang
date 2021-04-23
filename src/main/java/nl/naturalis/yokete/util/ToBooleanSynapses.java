package nl.naturalis.yokete.util;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import nl.naturalis.common.Bool;
import static java.sql.Types.*;
import static nl.naturalis.yokete.util.ColumnReaders.*;
import static nl.naturalis.yokete.util.SynapseNegotiator.DEFAULT_ENTRY;

class ToBooleanSynapses {

  private ToBooleanSynapses() {}

  static Map<Integer, Synapse<?, ?>> get() {

    Map<Integer, Synapse<?, Boolean>> tmp = new HashMap<>();

    tmp.put(BOOLEAN, new Synapse<>(GET_BOOLEAN));

    Synapse<Integer, Boolean> syn0 = new Synapse<>(GET_INT, x -> Bool.from(x));
    tmp.put(INTEGER, syn0);
    tmp.put(SMALLINT, syn0);
    tmp.put(TINYINT, syn0);
    tmp.put(BIT, syn0);

    tmp.put(FLOAT, new Synapse<>(GET_FLOAT, x -> Bool.from(x)));
    tmp.put(BIGINT, new Synapse<>(GET_LONG, x -> Bool.from(x)));

    Synapse<Double, Boolean> syn1 = new Synapse<>(GET_DOUBLE, x -> Bool.from(x));
    tmp.put(DOUBLE, syn1);
    tmp.put(REAL, syn1);

    Synapse<BigDecimal, Boolean> syn2 = new Synapse<>(GET_BIG_DECIMAL, x -> Bool.from(x));
    tmp.put(NUMERIC, syn2);
    tmp.put(DECIMAL, syn2);

    Synapse<String, Boolean> syn3 = new Synapse<>(GET_STRING, x -> Bool.from(x));
    tmp.put(DEFAULT_ENTRY, syn3);

    return Map.copyOf(tmp);
  }
}
