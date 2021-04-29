package nl.naturalis.yokete.db.read;

import java.math.BigDecimal;
import java.util.HashMap;
import nl.naturalis.common.NumberMethods;
import static java.sql.Types.*;
import static nl.naturalis.yokete.db.read.ColumnReader.*;
import static nl.naturalis.yokete.db.read.ValueProducerNegotiator.DEFAULT_ENTRY;

class ByteProducers extends HashMap<Integer, ValueProducer<?, ?>> {

  ByteProducers() {

    put(TINYINT, new ValueProducer<Byte, Byte>(GET_BYTE));
    put(INTEGER, new ValueProducer<Integer, Byte>(GET_INT, NumberMethods::convert));
    put(SMALLINT, new ValueProducer<Short, Byte>(GET_SHORT, NumberMethods::convert));
    put(REAL, new ValueProducer<Float, Byte>(GET_FLOAT, NumberMethods::convert));
    put(BIGINT, new ValueProducer<Long, Byte>(GET_LONG, NumberMethods::convert));
    ValueProducer<Double, Byte> vp0 = new ValueProducer<>(GET_DOUBLE, NumberMethods::convert);
    put(FLOAT, vp0);
    put(DOUBLE, vp0);
    ColumnReader<BigDecimal> cr = GET_BIG_DECIMAL;
    ValueProducer<BigDecimal, Byte> vp1 = new ValueProducer<>(cr, NumberMethods::convert);
    put(NUMERIC, vp1);
    put(DECIMAL, vp1);
    ValueProducer<String, Byte> vp2 = new ValueProducer<>(GET_STRING, NumberMethods::parse);
    put(VARCHAR, vp2);
    put(DEFAULT_ENTRY, vp2);
  }
}
