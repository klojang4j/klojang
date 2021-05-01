package nl.naturalis.yokete.db.read;

import java.math.BigDecimal;
import java.util.HashMap;
import nl.naturalis.common.NumberMethods;
import static java.sql.Types.*;
import static nl.naturalis.yokete.db.read.ColumnReader.*;

class IntExtractors extends HashMap<Integer, ValueExtractor<?, ?>> {

  IntExtractors() {
    put(INTEGER, new ValueExtractor<>(GET_INT));
    put(SMALLINT, new ValueExtractor<>(GET_SHORT));
    put(TINYINT, new ValueExtractor<>(GET_BYTE));
    put(REAL, new ValueExtractor<Float, Integer>(GET_FLOAT, NumberMethods::convert));
    put(BIGINT, new ValueExtractor<Long, Integer>(GET_LONG, NumberMethods::convert));
    ValueExtractor<Double, Integer> vp0 = new ValueExtractor<>(GET_DOUBLE, NumberMethods::convert);
    put(FLOAT, vp0);
    put(DOUBLE, vp0);
    ColumnReader<BigDecimal> cr = GET_BIG_DECIMAL;
    ValueExtractor<BigDecimal, Integer> vp1 = new ValueExtractor<>(cr, NumberMethods::convert);
    put(NUMERIC, vp1);
    put(DECIMAL, vp1);
    put(VARCHAR, new ValueExtractor<String, Integer>(GET_STRING, NumberMethods::parse));
  }
}
