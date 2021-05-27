package nl.naturalis.yokete.db.ps;

import nl.naturalis.common.Bool;
import nl.naturalis.common.NumberMethods;
import static java.sql.Types.*;
import static nl.naturalis.yokete.db.ps.PSSetter.SET_BOOLEAN;
import static nl.naturalis.yokete.db.ps.PSSetter.SET_BYTE;
import static nl.naturalis.yokete.db.ps.PSSetter.SET_INT;
import static nl.naturalis.yokete.db.ps.PSSetter.SET_SHORT;

class IntReceivers extends ReceiverLookup<Integer> {

  static final Receiver<Integer, Integer> DEFAULT = new Receiver<>(SET_INT);

  IntReceivers() {
    put(INTEGER, DEFAULT);
    put(SMALLINT, new Receiver<Integer, Short>(SET_SHORT, NumberMethods::convert));
    put(TINYINT, new Receiver<Integer, Byte>(SET_BYTE, NumberMethods::convert));
    putMultiple(new Receiver<Integer, Boolean>(SET_BOOLEAN, Bool::from), BOOLEAN, BIT);
    put(VARCHAR, Receiver.ANY_TO_STRING);
    put(CHAR, Receiver.ANY_TO_STRING);
  }

  @Override
  Receiver<Integer, ?> getDefaultReceiver() {
    return DEFAULT;
  }
}
