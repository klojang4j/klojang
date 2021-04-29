package nl.naturalis.yokete.db.read;

import java.math.BigDecimal;
import java.util.HashMap;
import nl.naturalis.common.NumberMethods;
import static java.sql.Types.*;
import static nl.naturalis.yokete.db.read.ColumnReader.*;

class IntProducers extends HashMap<Integer, ValueProducer<?, ?>> {

  IntProducers() {
    put(INTEGER, new ValueProducer<>(GET_INT));
    put(SMALLINT, new ValueProducer<>(GET_SHORT));
    put(TINYINT, new ValueProducer<>(GET_BYTE));
    put(REAL, new ValueProducer<Float, Integer>(GET_FLOAT, NumberMethods::convert));
    put(BIGINT, new ValueProducer<Long, Integer>(GET_LONG, NumberMethods::convert));
    ValueProducer<Double, Integer> vp0 = new ValueProducer<>(GET_DOUBLE, NumberMethods::convert);
    put(FLOAT, vp0);
    put(DOUBLE, vp0);
    ColumnReader<BigDecimal> cr = GET_BIG_DECIMAL;
    ValueProducer<BigDecimal, Integer> vp1 = new ValueProducer<>(cr, NumberMethods::convert);
    put(NUMERIC, vp1);
    put(DECIMAL, vp1);
    put(VARCHAR, new ValueProducer<String, Integer>(GET_STRING, NumberMethods::parse));
  }
}
