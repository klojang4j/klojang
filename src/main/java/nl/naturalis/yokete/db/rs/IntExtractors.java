package nl.naturalis.yokete.db.rs;

import java.math.BigDecimal;
import java.util.HashMap;
import nl.naturalis.common.NumberMethods;
import static java.sql.Types.*;
import static nl.naturalis.yokete.db.rs.RsMethod.*;

class IntExtractors extends HashMap<Integer, RsExtractor<?, ?>> {

  IntExtractors() {
    put(INTEGER, new RsExtractor<>(GET_INT));
    put(SMALLINT, new RsExtractor<>(GET_SHORT, x -> (int) x));
    put(TINYINT, new RsExtractor<>(GET_BYTE, x -> (int) x));
    put(BOOLEAN, new RsExtractor<>(GET_BOOLEAN, x -> x ? 1 : 0));
    put(REAL, new RsExtractor<Float, Integer>(GET_FLOAT, NumberMethods::convert));
    put(BIGINT, new RsExtractor<Long, Integer>(GET_LONG, NumberMethods::convert));
    RsExtractor<Double, Integer> vp0 = new RsExtractor<>(GET_DOUBLE, NumberMethods::convert);
    put(FLOAT, vp0);
    put(DOUBLE, vp0);
    RsMethod<BigDecimal> cr = GET_BIG_DECIMAL;
    RsExtractor<BigDecimal, Integer> vp1 = new RsExtractor<>(cr, NumberMethods::convert);
    put(NUMERIC, vp1);
    put(DECIMAL, vp1);
    put(VARCHAR, new RsExtractor<String, Integer>(GET_STRING, NumberMethods::parse));
  }
}
