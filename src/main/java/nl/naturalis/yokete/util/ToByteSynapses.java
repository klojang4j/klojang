package nl.naturalis.yokete.util;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import static java.sql.Types.*;
import static nl.naturalis.common.NumberMethods.convert;
import static nl.naturalis.common.NumberMethods.parse;
import static nl.naturalis.yokete.util.ColumnReaders.*;
import static nl.naturalis.yokete.util.SynapseNegotiator.*;

class ToByteSynapses {

  private ToByteSynapses() {}

  static Map<Integer, Synapse<?, ?>> get() {
    Map<Integer, Synapse<?, Byte>> tmp = new HashMap<>();

    Synapse<Byte, Byte> syn0 = new Synapse<>(GET_BYTE);
    tmp.put(TINYINT, syn0);
    tmp.put(BIT, syn0);

    Synapse<Integer, Byte> syn1 = new Synapse<>(GET_INT, x -> convert(x, Byte.class));
    tmp.put(INTEGER, syn1);
    tmp.put(SMALLINT, syn1);

    Synapse<Float, Byte> syn2 = new Synapse<>(GET_FLOAT, x -> convert(x, Byte.class));
    tmp.put(FLOAT, syn2);

    Synapse<Long, Byte> syn3 = new Synapse<>(GET_LONG, x -> convert(x, Byte.class));
    tmp.put(BIGINT, syn3);

    Synapse<Double, Byte> syn4 = new Synapse<>(GET_DOUBLE, x -> convert(x, Byte.class));
    tmp.put(DOUBLE, syn4);
    tmp.put(REAL, syn4);

    Synapse<BigDecimal, Byte> syn5 = new Synapse<>(GET_BIG_DECIMAL, x -> convert(x, Byte.class));
    tmp.put(NUMERIC, syn5);
    tmp.put(DECIMAL, syn5);

    Synapse<String, Byte> syn6 = new Synapse<>(GET_STRING, x -> parse(x, Byte.class));
    tmp.put(DEFAULT_ENTRY, syn6);

    return Map.copyOf(tmp);
  }
}
