package nl.naturalis.yokete.db.rs;

import java.math.BigDecimal;
import java.util.HashMap;
import nl.naturalis.common.NumberMethods;
import static java.sql.Types.*;
import static nl.naturalis.yokete.db.rs.RSGetter.*;
import static nl.naturalis.yokete.db.rs.EmitterNegotiator.DEFAULT;

class ByteEmitters extends HashMap<Integer, Emitter<?, ?>> {

  ByteEmitters() {

    put(TINYINT, new Emitter<Byte, Byte>(GET_BYTE));
    put(INTEGER, new Emitter<Integer, Byte>(GET_INT, NumberMethods::convert));
    put(SMALLINT, new Emitter<Short, Byte>(GET_SHORT, NumberMethods::convert));
    put(REAL, new Emitter<Float, Byte>(GET_FLOAT, NumberMethods::convert));
    put(BIGINT, new Emitter<Long, Byte>(GET_LONG, NumberMethods::convert));
    Emitter<Double, Byte> vp0 = new Emitter<>(GET_DOUBLE, NumberMethods::convert);
    put(FLOAT, vp0);
    put(DOUBLE, vp0);
    RSGetter<BigDecimal> cr = GET_BIG_DECIMAL;
    Emitter<BigDecimal, Byte> vp1 = new Emitter<>(cr, NumberMethods::convert);
    put(NUMERIC, vp1);
    put(DECIMAL, vp1);
    Emitter<String, Byte> vp2 = new Emitter<>(GET_STRING, NumberMethods::parse);
    put(VARCHAR, vp2);
    put(DEFAULT, vp2);
  }
}
