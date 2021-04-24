package nl.naturalis.yokete.db;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import nl.naturalis.common.Bool;
import static java.sql.Types.*;
import static nl.naturalis.yokete.db.ColumnReaders.*;
import static nl.naturalis.yokete.db.SynapseNegotiator.DEFAULT_ENTRY;

class ToBooleanSynapses {

  private ToBooleanSynapses() {}

  static Map<Integer, Synapse<?, ?>> get() {

    Map<Integer, Synapse<?, Boolean>> tmp = new HashMap<>();

    tmp.put(BOOLEAN, new Synapse<>(GET_BOOLEAN));

    Synapse<Integer, Boolean> syn0 = new Synapse<>(GET_INT, Bool::from);
    tmp.put(INTEGER, syn0);
    tmp.put(SMALLINT, syn0);
    tmp.put(TINYINT, syn0);
    tmp.put(BIT, syn0);

    tmp.put(FLOAT, new Synapse<Float, Boolean>(GET_FLOAT, Bool::from));
    tmp.put(BIGINT, new Synapse<Long, Boolean>(GET_LONG, Bool::from));

    Synapse<Double, Boolean> syn1 = new Synapse<>(GET_DOUBLE, Bool::from);
    tmp.put(DOUBLE, syn1);
    tmp.put(REAL, syn1);

    Synapse<BigDecimal, Boolean> syn2 = new Synapse<>(GET_BIG_DECIMAL, Bool::from);
    tmp.put(NUMERIC, syn2);
    tmp.put(DECIMAL, syn2);

    Synapse<String, Boolean> syn3 = new Synapse<>(GET_STRING, Bool::from);
    tmp.put(DEFAULT_ENTRY, syn3);

    return Map.copyOf(tmp);
  }
}
