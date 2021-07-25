package org.klojang.x.db.ps;

import nl.naturalis.common.Bool;
import nl.naturalis.common.NumberMethods;
import static java.sql.Types.*;
import static org.klojang.x.db.ps.PsSetter.*;

class DoubleReceivers extends ReceiverLookup<Double> {

  static final Receiver<Double, ?> DEFAULT = new Receiver<>(SET_DOUBLE);

  DoubleReceivers() {
    put(FLOAT, DEFAULT);
    put(DOUBLE, DEFAULT);
    put(BIGINT, new Receiver<Double, Long>(SET_LONG, NumberMethods::convert));
    put(REAL, new Receiver<Double, Float>(SET_FLOAT, NumberMethods::convert));
    put(INTEGER, new Receiver<Double, Integer>(SET_INT, NumberMethods::convert));
    put(SMALLINT, new Receiver<Double, Short>(SET_SHORT, NumberMethods::convert));
    put(TINYINT, new Receiver<Double, Byte>(SET_BYTE, NumberMethods::convert));
    putMultiple(new Receiver<Double, Boolean>(SET_BOOLEAN, Bool::from), BOOLEAN, BIT);
    put(VARCHAR, Receiver.ANY_TO_STRING);
    put(CHAR, Receiver.ANY_TO_STRING);
  }

  @Override
  Receiver<Double, ?> getDefaultReceiver() {
    return DEFAULT;
  }
}
