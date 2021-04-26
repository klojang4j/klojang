package nl.naturalis.yokete.db;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import nl.naturalis.common.NumberMethods;
import static java.sql.Types.*;
import static nl.naturalis.yokete.db.ColumnReader.*;
import static nl.naturalis.yokete.db.SynapseNegotiator.DEFAULT_ENTRY;

class ToLongSynapses {

  private ToLongSynapses() {}

  static Map<Integer, Synapse<?, ?>> get() {

    Map<Integer, Synapse<?, Long>> tmp = new HashMap<>();

    Synapse<Long, Long> syn0 = new Synapse<>(GET_LONG);
    tmp.put(BIGINT, syn0);
    tmp.put(INTEGER, syn0);
    tmp.put(SMALLINT, syn0);
    tmp.put(TINYINT, syn0);
    tmp.put(BIT, syn0);
    tmp.put(TIMESTAMP, syn0);

    Synapse<Float, Long> syn1 = new Synapse<>(GET_FLOAT, NumberMethods::convert);
    tmp.put(FLOAT, syn1);

    Synapse<BigDecimal, Long> syn2 = new Synapse<>(GET_BIG_DECIMAL, NumberMethods::convert);
    tmp.put(DECIMAL, syn2);
    tmp.put(REAL, syn2);

    // Otherwise call ResultSet.getString(columnIndex) and pray that the
    // returned value can be parsed into an integer
    Synapse<String, Long> syn3 = new Synapse<>(GET_STRING, NumberMethods::parse);
    tmp.put(DEFAULT_ENTRY, syn3);

    return Map.copyOf(tmp);
  }
}
