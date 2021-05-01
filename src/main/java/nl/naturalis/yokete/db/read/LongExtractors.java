package nl.naturalis.yokete.db.read;

import java.math.BigDecimal;
import java.util.HashMap;
import nl.naturalis.common.NumberMethods;
import static java.sql.Types.*;
import static nl.naturalis.yokete.db.read.ColumnReader.*;

class LongExtractors extends HashMap<Integer, ValueExtractor<?, ?>> {

  LongExtractors() {
    put(BIGINT, new ValueExtractor<>(GET_LONG));
    put(INTEGER, new ValueExtractor<>(GET_INT));
    put(SMALLINT, new ValueExtractor<>(GET_SHORT));
    put(TINYINT, new ValueExtractor<>(GET_BYTE));
    put(REAL, new ValueExtractor<Float, Long>(GET_FLOAT, NumberMethods::convert));
    ColumnReader<BigDecimal> cr = GET_BIG_DECIMAL;
    ValueExtractor<BigDecimal, Long> vp0 = new ValueExtractor<>(cr, NumberMethods::convert);
    put(DECIMAL, vp0);
    put(NUMERIC, vp0);
    put(VARCHAR, new ValueExtractor<String, Long>(GET_STRING, NumberMethods::parse));
  }
}
