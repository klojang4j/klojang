package org.klojang.x.db.ps;

import nl.naturalis.common.Bool;
import nl.naturalis.common.NumberMethods;
import static java.sql.Types.*;
import static org.klojang.x.db.ps.PsSetter.*;

class ShortReceivers extends ReceiverLookup<Short> {

  static final Receiver<Short, Short> DEFAULT = new Receiver<>(SET_SHORT);

  ShortReceivers() {
    put(SMALLINT, DEFAULT);
    put(INTEGER, IntReceivers.DEFAULT);
    put(BIGINT, new Receiver<>(SET_LONG));
    put(REAL, new Receiver<>(SET_FLOAT));
    put(FLOAT, DoubleReceivers.DEFAULT);
    put(DOUBLE, DoubleReceivers.DEFAULT);
    putMultiple(new Receiver<>(SET_BIG_DECIMAL), NUMERIC, DECIMAL);
    put(TINYINT, new Receiver<Short, Byte>(SET_BYTE, NumberMethods::convert));
    putMultiple(new Receiver<Short, Boolean>(SET_BOOLEAN, Bool::from), BOOLEAN, BIT);
    put(VARCHAR, Receiver.ANY_TO_STRING);
    put(CHAR, Receiver.ANY_TO_STRING);
  }

  @Override
  Receiver<Short, ?> getDefaultReceiver() {
    return DEFAULT;
  }
}
