package org.klojang.db.rs;

import java.math.BigDecimal;
import java.util.HashMap;
import nl.naturalis.common.NumberMethods;
import static java.sql.Types.*;
import static org.klojang.db.rs.ExtractorNegotiator.DEFAULT;
import static org.klojang.db.rs.RsMethod.*;

class ByteExtractors extends HashMap<Integer, RsExtractor<?, ?>> {

  ByteExtractors() {
    put(TINYINT, new RsExtractor<Byte, Byte>(GET_BYTE));
    put(INTEGER, new RsExtractor<Integer, Byte>(GET_INT, NumberMethods::convert));
    put(SMALLINT, new RsExtractor<Short, Byte>(GET_SHORT, NumberMethods::convert));
    put(REAL, new RsExtractor<Float, Byte>(GET_FLOAT, NumberMethods::convert));
    put(BIGINT, new RsExtractor<Long, Byte>(GET_LONG, NumberMethods::convert));
    RsExtractor<Double, Byte> vp0 = new RsExtractor<>(GET_DOUBLE, NumberMethods::convert);
    put(FLOAT, vp0);
    put(DOUBLE, vp0);
    RsMethod<BigDecimal> cr = GET_BIG_DECIMAL;
    RsExtractor<BigDecimal, Byte> vp1 = new RsExtractor<>(cr, NumberMethods::convert);
    put(NUMERIC, vp1);
    put(DECIMAL, vp1);
    RsExtractor<String, Byte> vp2 = new RsExtractor<>(GET_STRING, NumberMethods::parse);
    put(VARCHAR, vp2);
    put(DEFAULT, vp2);
  }
}
