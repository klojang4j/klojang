package nl.naturalis.yokete.db.write;

import java.math.BigDecimal;
import java.util.HashMap;
import static java.sql.Types.*;
import static nl.naturalis.common.ObjectMethods.ifNull;
import static nl.naturalis.yokete.db.write.ParamWriter.*;
import static nl.naturalis.yokete.db.write.PersisterNegotiator.DEFAULT_ENTRY;

class BooleanPersisters extends HashMap<Integer, ValuePersister<?, ?>> {

  BooleanPersisters() {
    put(DEFAULT_ENTRY, new ValuePersister<>(SET_BOOLEAN));
    put(BOOLEAN, new ValuePersister<>(SET_BOOLEAN));
    put(BIT, new ValuePersister<>(SET_BOOLEAN));
    put(INTEGER, new ValuePersister<Boolean, Integer>(SET_INT, b -> asNumber(b, Integer.class)));
    put(BIGINT, new ValuePersister<Boolean, Long>(SET_LONG, b -> asNumber(b, Long.class)));
    ParamWriter<BigDecimal> cw = SET_BIG_DECIMAL;
    put(NUMERIC, new ValuePersister<Boolean, BigDecimal>(cw, b -> asNumber(b, BigDecimal.class)));
    put(DECIMAL, new ValuePersister<Boolean, BigDecimal>(cw, b -> asNumber(b, BigDecimal.class)));
    put(REAL, new ValuePersister<Boolean, Float>(SET_FLOAT, b -> asNumber(b, Float.class)));
    put(FLOAT, new ValuePersister<Boolean, Double>(SET_DOUBLE, b -> asNumber(b, Double.class)));
    put(DOUBLE, new ValuePersister<Boolean, Double>(SET_DOUBLE, b -> asNumber(b, Double.class)));
    put(SMALLINT, new ValuePersister<Boolean, Short>(SET_SHORT, b -> asNumber(b, Short.class)));
    put(TINYINT, new ValuePersister<Boolean, Byte>(SET_BYTE, b -> asNumber(b, Byte.class)));
    put(VARCHAR, new ValuePersister<>(SET_STRING, this::asString));
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
