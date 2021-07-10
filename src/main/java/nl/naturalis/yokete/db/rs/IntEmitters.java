package nl.naturalis.yokete.db.rs;

import java.math.BigDecimal;
import java.util.HashMap;
import nl.naturalis.common.NumberMethods;
import static java.sql.Types.*;
import static nl.naturalis.yokete.db.rs.RSGetter.*;

class IntEmitters extends HashMap<Integer, Emitter<?, ?>> {

  IntEmitters() {
    put(INTEGER, new Emitter<>(GET_INT));
    put(SMALLINT, new Emitter<>(GET_SHORT, x -> NumberMethods.convert(x, Integer.class)));
    put(TINYINT, new Emitter<>(GET_BYTE, x -> NumberMethods.convert(x, Integer.class)));
    put(REAL, new Emitter<Float, Integer>(GET_FLOAT, NumberMethods::convert));
    put(BIGINT, new Emitter<Long, Integer>(GET_LONG, NumberMethods::convert));
    Emitter<Double, Integer> vp0 = new Emitter<>(GET_DOUBLE, NumberMethods::convert);
    put(FLOAT, vp0);
    put(DOUBLE, vp0);
    RSGetter<BigDecimal> cr = GET_BIG_DECIMAL;
    Emitter<BigDecimal, Integer> vp1 = new Emitter<>(cr, NumberMethods::convert);
    put(NUMERIC, vp1);
    put(DECIMAL, vp1);
    put(VARCHAR, new Emitter<String, Integer>(GET_STRING, NumberMethods::parse));
  }
}
