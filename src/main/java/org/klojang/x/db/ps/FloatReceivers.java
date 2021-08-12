package org.klojang.x.db.ps;

import nl.naturalis.common.Bool;
import nl.naturalis.common.NumberMethods;
import static java.sql.Types.*;
import static org.klojang.x.db.ps.PsSetter.*;

class FloatReceivers extends ReceiverLookup<Float> {

  static final Receiver<Float, ?> DEFAULT = new Receiver<>(SET_FLOAT);

  FloatReceivers() {
    put(REAL, DEFAULT);
    putMultiple(new Receiver<Float, Double>(SET_DOUBLE, NumberMethods::convert), FLOAT, DOUBLE);
    put(BIGINT, new Receiver<Float, Long>(SET_LONG, NumberMethods::convert));
    put(INTEGER, new Receiver<Float, Integer>(SET_INT, NumberMethods::convert));
    put(SMALLINT, new Receiver<Float, Short>(SET_SHORT, NumberMethods::convert));
    put(TINYINT, new Receiver<Float, Byte>(SET_BYTE, NumberMethods::convert));
    putMultiple(new Receiver<Float, Boolean>(SET_BOOLEAN, Bool::from), BOOLEAN, BIT);
    put(VARCHAR, Receiver.ANY_TO_STRING);
    put(CHAR, Receiver.ANY_TO_STRING);
  }

  @Override
  Receiver<Float, ?> getDefaultReceiver() {
    return DEFAULT;
  }
}
