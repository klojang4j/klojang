package nl.naturalis.yokete.db.write;

import java.math.BigDecimal;
import java.util.HashMap;
import static java.sql.Types.*;
import static nl.naturalis.common.ObjectMethods.ifNull;
import static nl.naturalis.yokete.db.write.ColumnWriter.*;
import static nl.naturalis.yokete.db.write.ValueAbsorberNegotiator.DEFAULT_ENTRY;

class BooleanAbsorbers extends HashMap<Integer, ValueAbsorber<?, ?>> {

  BooleanAbsorbers() {
    put(DEFAULT_ENTRY, new ValueAbsorber<>(SET_BOOLEAN));
    put(BOOLEAN, new ValueAbsorber<>(SET_BOOLEAN));
    put(BIT, new ValueAbsorber<>(SET_BOOLEAN));
    put(INTEGER, new ValueAbsorber<Boolean, Integer>(SET_INT, b -> asNumber(b, Integer.class)));
    put(BIGINT, new ValueAbsorber<Boolean, Long>(SET_LONG, b -> asNumber(b, Long.class)));
    ColumnWriter<BigDecimal> cw = SET_BIG_DECIMAL;
    put(NUMERIC, new ValueAbsorber<Boolean, BigDecimal>(cw, b -> asNumber(b, BigDecimal.class)));
    put(DECIMAL, new ValueAbsorber<Boolean, BigDecimal>(cw, b -> asNumber(b, BigDecimal.class)));
    put(REAL, new ValueAbsorber<Boolean, Float>(SET_FLOAT, b -> asNumber(b, Float.class)));
    put(FLOAT, new ValueAbsorber<Boolean, Double>(SET_DOUBLE, b -> asNumber(b, Double.class)));
    put(DOUBLE, new ValueAbsorber<Boolean, Double>(SET_DOUBLE, b -> asNumber(b, Double.class)));
    put(SMALLINT, new ValueAbsorber<Boolean, Short>(SET_SHORT, b -> asNumber(b, Short.class)));
    put(TINYINT, new ValueAbsorber<Boolean, Byte>(SET_BYTE, b -> asNumber(b, Byte.class)));
    put(VARCHAR, new ValueAbsorber<>(SET_STRING, this::asString));
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
