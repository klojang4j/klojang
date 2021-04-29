package nl.naturalis.yokete.db.read;

import java.math.BigDecimal;
import java.util.HashMap;
import nl.naturalis.common.Bool;
import static java.sql.Types.*;
import static nl.naturalis.yokete.db.read.ColumnReader.*;
import static nl.naturalis.yokete.db.read.ValueProducerNegotiator.DEFAULT_ENTRY;

class BooleanProducers extends HashMap<Integer, ValueProducer<?, ?>> {

  BooleanProducers() {
    ValueProducer<Boolean, Boolean> vp0 = new ValueProducer<>(GET_BOOLEAN);
    put(BOOLEAN, vp0);
    put(BIT, vp0);
    put(INTEGER, new ValueProducer<Integer, Boolean>(GET_INT, Bool::from));
    put(SMALLINT, new ValueProducer<Short, Boolean>(GET_SHORT, Bool::from));
    put(TINYINT, new ValueProducer<Byte, Boolean>(GET_BYTE, Bool::from));
    ValueProducer<Double, Boolean> vp1 = new ValueProducer<>(GET_DOUBLE, Bool::from);
    put(FLOAT, vp1);
    put(DOUBLE, vp1);
    put(BIGINT, new ValueProducer<Long, Boolean>(GET_LONG, Bool::from));
    put(REAL, new ValueProducer<Float, Boolean>(GET_FLOAT, Bool::from));
    ValueProducer<BigDecimal, Boolean> vp2 = new ValueProducer<>(GET_BIG_DECIMAL, Bool::from);
    put(NUMERIC, vp2);
    put(DECIMAL, vp2);
    ValueProducer<String, Boolean> vp3 = new ValueProducer<>(GET_STRING, Bool::from);
    put(VARCHAR, vp3);
    put(DEFAULT_ENTRY, vp3);
  }
}
