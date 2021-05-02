package nl.naturalis.yokete.db.ps;

import java.math.BigDecimal;
import java.util.HashMap;
import static java.sql.Types.*;
import static nl.naturalis.common.ObjectMethods.ifNull;
import static nl.naturalis.yokete.db.ps.PSSetter.*;
import static nl.naturalis.yokete.db.ps.ReceiverNegotiator.DEFAULT;

class BooleanReceivers extends HashMap<Integer, Receiver<?, ?>> {

  BooleanReceivers() {
    put(DEFAULT, new Receiver<>(SET_BOOLEAN));
    put(BOOLEAN, new Receiver<>(SET_BOOLEAN));
    put(BIT, new Receiver<>(SET_BOOLEAN));
    put(INTEGER, new Receiver<Boolean, Integer>(SET_INT, b -> asNumber(b, Integer.class)));
    put(BIGINT, new Receiver<Boolean, Long>(SET_LONG, b -> asNumber(b, Long.class)));
    PSSetter<BigDecimal> cw = SET_BIG_DECIMAL;
    put(NUMERIC, new Receiver<Boolean, BigDecimal>(cw, b -> asNumber(b, BigDecimal.class)));
    put(DECIMAL, new Receiver<Boolean, BigDecimal>(cw, b -> asNumber(b, BigDecimal.class)));
    put(REAL, new Receiver<Boolean, Float>(SET_FLOAT, b -> asNumber(b, Float.class)));
    put(FLOAT, new Receiver<Boolean, Double>(SET_DOUBLE, b -> asNumber(b, Double.class)));
    put(DOUBLE, new Receiver<Boolean, Double>(SET_DOUBLE, b -> asNumber(b, Double.class)));
    put(SMALLINT, new Receiver<Boolean, Short>(SET_SHORT, b -> asNumber(b, Short.class)));
    put(TINYINT, new Receiver<Boolean, Byte>(SET_BYTE, b -> asNumber(b, Byte.class)));
    put(VARCHAR, new Receiver<>(SET_STRING, this::asString));
  }

  private static <T extends Number> T asNumber(Boolean b, Class<T> numberType) {
    if (b == null || b.equals(Boolean.FALSE)) {
      return numberType.cast((byte) 0);
    }
    return numberType.cast((byte) 1);
  }

  private String asString(Boolean b) {
    return ifNull(b, Boolean.FALSE).toString();
  }
}
