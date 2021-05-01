package nl.naturalis.yokete.db.rs;

import java.math.BigDecimal;
import java.util.HashMap;
import nl.naturalis.common.NumberMethods;
import static java.sql.Types.*;
import static nl.naturalis.yokete.db.rs.RSGetter.*;

class LongEmitters extends HashMap<Integer, Emitter<?, ?>> {

  LongEmitters() {
    put(BIGINT, new Emitter<>(GET_LONG));
    put(INTEGER, new Emitter<>(GET_INT));
    put(SMALLINT, new Emitter<>(GET_SHORT));
    put(TINYINT, new Emitter<>(GET_BYTE));
    put(REAL, new Emitter<Float, Long>(GET_FLOAT, NumberMethods::convert));
    RSGetter<BigDecimal> cr = GET_BIG_DECIMAL;
    Emitter<BigDecimal, Long> vp0 = new Emitter<>(cr, NumberMethods::convert);
    put(DECIMAL, vp0);
    put(NUMERIC, vp0);
    put(VARCHAR, new Emitter<String, Long>(GET_STRING, NumberMethods::parse));
  }
}
