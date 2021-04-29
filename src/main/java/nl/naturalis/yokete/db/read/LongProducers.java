package nl.naturalis.yokete.db.read;

import java.math.BigDecimal;
import java.util.HashMap;
import nl.naturalis.common.NumberMethods;
import static java.sql.Types.*;
import static nl.naturalis.yokete.db.read.ColumnReader.*;

class LongProducers extends HashMap<Integer, ValueProducer<?, ?>> {

  LongProducers() {
    put(BIGINT, new ValueProducer<>(GET_LONG));
    put(INTEGER, new ValueProducer<>(GET_INT));
    put(SMALLINT, new ValueProducer<>(GET_SHORT));
    put(TINYINT, new ValueProducer<>(GET_BYTE));
    put(REAL, new ValueProducer<Float, Long>(GET_FLOAT, NumberMethods::convert));
    ColumnReader<BigDecimal> cr = GET_BIG_DECIMAL;
    ValueProducer<BigDecimal, Long> vp0 = new ValueProducer<>(cr, NumberMethods::convert);
    put(DECIMAL, vp0);
    put(NUMERIC, vp0);
    put(VARCHAR, new ValueProducer<String, Long>(GET_STRING, NumberMethods::parse));
  }
}
