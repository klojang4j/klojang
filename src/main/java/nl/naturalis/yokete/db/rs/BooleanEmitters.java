package nl.naturalis.yokete.db.rs;

import java.math.BigDecimal;
import java.util.HashMap;
import nl.naturalis.common.Bool;
import static java.sql.Types.*;
import static nl.naturalis.yokete.db.rs.RSGetter.*;
import static nl.naturalis.yokete.db.rs.EmitterNegotiator.DEFAULT;

class BooleanEmitters extends HashMap<Integer, Emitter<?, ?>> {

  BooleanEmitters() {
    Emitter<Boolean, Boolean> vp0 = new Emitter<>(GET_BOOLEAN);
    put(BOOLEAN, vp0);
    put(BIT, vp0);
    put(INTEGER, new Emitter<Integer, Boolean>(GET_INT, Bool::from));
    put(SMALLINT, new Emitter<Short, Boolean>(GET_SHORT, Bool::from));
    put(TINYINT, new Emitter<Byte, Boolean>(GET_BYTE, Bool::from));
    Emitter<Double, Boolean> vp1 = new Emitter<>(GET_DOUBLE, Bool::from);
    put(FLOAT, vp1);
    put(DOUBLE, vp1);
    put(BIGINT, new Emitter<Long, Boolean>(GET_LONG, Bool::from));
    put(REAL, new Emitter<Float, Boolean>(GET_FLOAT, Bool::from));
    Emitter<BigDecimal, Boolean> vp2 = new Emitter<>(GET_BIG_DECIMAL, Bool::from);
    put(NUMERIC, vp2);
    put(DECIMAL, vp2);
    Emitter<String, Boolean> vp3 = new Emitter<>(GET_STRING, Bool::from);
    put(VARCHAR, vp3);
    put(DEFAULT, vp3);
  }
}
