package nl.naturalis.yokete.db.ps;

import nl.naturalis.common.Bool;
import nl.naturalis.common.NumberMethods;
import static java.sql.Types.*;
import static nl.naturalis.yokete.db.ps.PSSetter.*;

class LongReceivers extends ReceiverLookup<Long> {

  static final Receiver<Long, Long> DEFAULT = new Receiver<>(SET_LONG);

  LongReceivers() {
    put(BIGINT, DEFAULT);
    put(INTEGER, new Receiver<Long, Integer>(SET_INT, NumberMethods::convert));
    put(SMALLINT, new Receiver<Long, Short>(SET_SHORT, NumberMethods::convert));
    put(TINYINT, new Receiver<Long, Byte>(SET_BYTE, NumberMethods::convert));
    put(BOOLEAN, new Receiver<Long, Boolean>(SET_BOOLEAN, Bool::from));
    put(BIT, new Receiver<Long, Boolean>(SET_BOOLEAN, Bool::from));
    put(VARCHAR, Receiver.ANY_TO_STRING);
    put(CHAR, Receiver.ANY_TO_STRING);
  }

  @Override
  Receiver<Long, ?> getDefaultReceiver() {
    return DEFAULT;
  }
}
