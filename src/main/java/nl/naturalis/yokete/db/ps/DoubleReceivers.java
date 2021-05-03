package nl.naturalis.yokete.db.ps;

import java.util.HashMap;
import nl.naturalis.common.Bool;
import nl.naturalis.common.NumberMethods;
import static java.sql.Types.*;
import static nl.naturalis.yokete.db.ps.PSSetter.*;
import static nl.naturalis.yokete.db.ps.ReceiverNegotiator.DEFAULT;

class DoubleReceivers extends HashMap<Integer, Receiver<?, ?>> {

  DoubleReceivers() {
    put(DEFAULT, new Receiver<Double, String>(SET_STRING, String::valueOf));
    put(FLOAT, new Receiver<>(SET_DOUBLE));
    put(DOUBLE, new Receiver<>(SET_DOUBLE));
    put(BIGINT, new Receiver<Double, Long>(SET_LONG, NumberMethods::convert));
    put(REAL, new Receiver<Double, Float>(SET_FLOAT, NumberMethods::convert));
    put(INTEGER, new Receiver<Long, Integer>(SET_INT, NumberMethods::convert));
    put(SMALLINT, new Receiver<Long, Short>(SET_SHORT, NumberMethods::convert));
    put(TINYINT, new Receiver<Long, Byte>(SET_BYTE, NumberMethods::convert));
    put(BOOLEAN, new Receiver<Long, Boolean>(SET_BOOLEAN, Bool::from));
    put(BIT, new Receiver<Long, Boolean>(SET_BOOLEAN, Bool::from));
  }
}
