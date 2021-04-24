package nl.naturalis.yokete.db;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import nl.naturalis.common.NumberMethods;
import static java.sql.Types.*;
import static nl.naturalis.common.NumberMethods.parse;
import static nl.naturalis.yokete.db.ColumnReaders.*;
import static nl.naturalis.yokete.db.SynapseNegotiator.DEFAULT_ENTRY;

class ToIntSynapses {

  private ToIntSynapses() {}

  static Map<Integer, Synapse<?, ?>> get() {

    Map<Integer, Synapse<?, Integer>> tmp = new HashMap<>();

    Synapse<Integer, Integer> syn0 = new Synapse<>(GET_INT);
    tmp.put(INTEGER, syn0);
    tmp.put(SMALLINT, syn0);
    tmp.put(TINYINT, syn0);
    tmp.put(BIT, syn0);

    Synapse<Float, Integer> syn1 = new Synapse<>(GET_FLOAT, NumberMethods::convert);
    tmp.put(FLOAT, syn1);

    Synapse<Long, Integer> syn2 = new Synapse<>(GET_LONG, NumberMethods::convert);
    tmp.put(BIGINT, syn2);
    tmp.put(TIMESTAMP, syn2);

    Synapse<Double, Integer> syn3 = new Synapse<>(GET_DOUBLE, NumberMethods::convert);
    tmp.put(BIGINT, syn3);
    tmp.put(TIMESTAMP, syn3);

    Synapse<BigDecimal, Integer> syn4 = new Synapse<>(GET_BIG_DECIMAL, NumberMethods::convert);
    tmp.put(BIGINT, syn4);
    tmp.put(TIMESTAMP, syn4);

    // Otherwise call ResultSet.getString(columnIndex) and pray that the
    // returned value can be parsed into an integer
    Synapse<String, Integer> syn5 = new Synapse<>(GET_STRING, x -> parse(x, Integer.class));
    tmp.put(DEFAULT_ENTRY, syn5);

    return Map.copyOf(tmp);
  }
}
