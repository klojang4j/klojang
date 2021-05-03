package nl.naturalis.yokete.db.ps;

import java.util.HashMap;
import nl.naturalis.common.Bool;
import nl.naturalis.common.NumberMethods;
import static java.sql.Types.*;
import static nl.naturalis.yokete.db.ps.PSSetter.*;
import static nl.naturalis.yokete.db.ps.ReceiverNegotiator.DEFAULT;

class IntReceivers extends HashMap<Integer, Receiver<?, ?>> {

  IntReceivers() {
    put(DEFAULT, new Receiver<Integer, String>(SET_STRING, String::valueOf));
    put(INTEGER, new Receiver<>(SET_INT));
    put(SMALLINT, new Receiver<Integer, Short>(SET_SHORT, NumberMethods::convert));
    put(TINYINT, new Receiver<Integer, Byte>(SET_BYTE, NumberMethods::convert));
    put(BOOLEAN, new Receiver<Integer, Boolean>(SET_BOOLEAN, Bool::from));
    put(BIT, new Receiver<Integer, Boolean>(SET_BOOLEAN, Bool::from));
  }
}
