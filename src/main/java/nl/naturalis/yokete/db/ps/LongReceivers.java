package nl.naturalis.yokete.db.ps;

import java.util.HashMap;
import nl.naturalis.common.Bool;
import nl.naturalis.common.NumberMethods;
import static java.sql.Types.*;
import static nl.naturalis.yokete.db.ps.PSSetter.*;
import static nl.naturalis.yokete.db.ps.ReceiverNegotiator.DEFAULT;

class LongReceivers extends HashMap<Integer, Receiver<?, ?>> {

  LongReceivers() {
    put(DEFAULT, new Receiver<Long, String>(SET_STRING, String::valueOf));
    put(BIGINT, new Receiver<>(SET_LONG));
    put(INTEGER, new Receiver<Long, Integer>(SET_INT, NumberMethods::convert));
    put(SMALLINT, new Receiver<Long, Short>(SET_SHORT, NumberMethods::convert));
    put(TINYINT, new Receiver<Long, Byte>(SET_BYTE, NumberMethods::convert));
    put(BOOLEAN, new Receiver<Long, Boolean>(SET_BOOLEAN, Bool::from));
    put(BIT, new Receiver<Long, Boolean>(SET_BOOLEAN, Bool::from));
  }
}
