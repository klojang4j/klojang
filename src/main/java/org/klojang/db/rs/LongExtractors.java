package org.klojang.db.rs;

import java.math.BigDecimal;
import java.util.HashMap;
import nl.naturalis.common.NumberMethods;
import static java.sql.Types.*;
import static org.klojang.db.rs.RsMethod.*;

class LongExtractors extends HashMap<Integer, RsExtractor<?, ?>> {

  LongExtractors() {
    put(BIGINT, new RsExtractor<>(GET_LONG));
    put(INTEGER, new RsExtractor<>(GET_INT));
    put(SMALLINT, new RsExtractor<>(GET_SHORT));
    put(TINYINT, new RsExtractor<>(GET_BYTE));
    put(REAL, new RsExtractor<Float, Long>(GET_FLOAT, NumberMethods::convert));
    RsMethod<BigDecimal> cr = GET_BIG_DECIMAL;
    RsExtractor<BigDecimal, Long> vp0 = new RsExtractor<>(cr, NumberMethods::convert);
    put(DECIMAL, vp0);
    put(NUMERIC, vp0);
    put(VARCHAR, new RsExtractor<String, Long>(GET_STRING, NumberMethods::parse));
  }
}
