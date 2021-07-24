package org.klojang.db.ps;

import nl.naturalis.common.Bool;
import static java.sql.Types.*;
import static org.klojang.db.ps.PsSetter.*;

class ByteReceivers extends ReceiverLookup<Byte> {

  static final Receiver<Byte, Byte> DEFAULT = new Receiver<>(SET_BYTE);

  ByteReceivers() {
    put(TINYINT, DEFAULT);
    put(INTEGER, IntReceivers.DEFAULT);
    put(SMALLINT, new Receiver<>(SET_SHORT));
    put(BIGINT, new Receiver<>(SET_LONG));
    put(REAL, new Receiver<>(SET_FLOAT));
    put(FLOAT, DoubleReceivers.DEFAULT);
    put(DOUBLE, DoubleReceivers.DEFAULT);
    put(NUMERIC, new Receiver<>(SET_BIG_DECIMAL));
    put(DECIMAL, new Receiver<>(SET_BIG_DECIMAL));
    put(BOOLEAN, new Receiver<Byte, Boolean>(SET_BOOLEAN, Bool::from));
    put(BIT, new Receiver<Byte, Boolean>(SET_BOOLEAN, Bool::from));
    put(VARCHAR, Receiver.ANY_TO_STRING);
    put(CHAR, Receiver.ANY_TO_STRING);
  }

  @Override
  Receiver<Byte, ?> getDefaultReceiver() {
    return DEFAULT;
  }
}
