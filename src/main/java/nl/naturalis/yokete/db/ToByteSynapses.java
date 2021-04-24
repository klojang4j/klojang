package nl.naturalis.yokete.db;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import nl.naturalis.common.NumberMethods;
import static java.sql.Types.*;
import static nl.naturalis.common.NumberMethods.parse;
import static nl.naturalis.yokete.db.ColumnReaders.*;
import static nl.naturalis.yokete.db.SynapseNegotiator.DEFAULT_ENTRY;

class ToByteSynapses {

  private ToByteSynapses() {}

  static Map<Integer, Synapse<?, ?>> get() {

    Map<Integer, Synapse<?, Byte>> tmp = new HashMap<>();

    Synapse<Byte, Byte> syn0 = new Synapse<>(GET_BYTE);
    tmp.put(TINYINT, syn0);
    tmp.put(BIT, syn0);

    Synapse<Integer, Byte> syn1 = new Synapse<>(GET_INT, NumberMethods::convert);
    tmp.put(INTEGER, syn1);
    tmp.put(SMALLINT, syn1);

    Synapse<Float, Byte> syn2 = new Synapse<>(GET_FLOAT, NumberMethods::convert);
    tmp.put(FLOAT, syn2);

    Synapse<Long, Byte> syn3 = new Synapse<>(GET_LONG, NumberMethods::convert);
    tmp.put(BIGINT, syn3);

    Synapse<Double, Byte> syn4 = new Synapse<>(GET_DOUBLE, NumberMethods::convert);
    tmp.put(DOUBLE, syn4);
    tmp.put(REAL, syn4);

    Synapse<BigDecimal, Byte> syn5 = new Synapse<>(GET_BIG_DECIMAL, NumberMethods::convert);
    tmp.put(NUMERIC, syn5);
    tmp.put(DECIMAL, syn5);

    Synapse<String, Byte> syn6 = new Synapse<>(GET_STRING, x -> parse(x, Byte.class));
    tmp.put(DEFAULT_ENTRY, syn6);

    return Map.copyOf(tmp);
  }
}
