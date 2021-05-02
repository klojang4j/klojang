package nl.naturalis.yokete.db.ps;

import java.util.HashMap;
import nl.naturalis.common.Bool;
import nl.naturalis.common.NumberMethods;
import static java.sql.Types.*;
import static nl.naturalis.yokete.db.ps.PSSetter.*;
import static nl.naturalis.yokete.db.ps.ReceiverNegotiator.DEFAULT;

class IntReceivers extends HashMap<Integer, Receiver<?, ?>> {

  IntReceivers() {
    put(DEFAULT, new Receiver<>(SET_INT));
    put(INTEGER, new Receiver<>(SET_INT));
    put(BIGINT, new Receiver<>(SET_LONG));
    put(NUMERIC, new Receiver<>(SET_BIG_DECIMAL));
    put(REAL, new Receiver<>(SET_FLOAT));
    put(FLOAT, new Receiver<>(SET_DOUBLE));
    put(DOUBLE, new Receiver<>(SET_DOUBLE));
    put(SMALLINT, new Receiver<Integer, Short>(SET_SHORT, NumberMethods::convert));
    put(TINYINT, new Receiver<Integer, Byte>(SET_BYTE, NumberMethods::convert));
    put(BOOLEAN, new Receiver<Integer, Boolean>(SET_BOOLEAN, Bool::from));
    put(BIT, new Receiver<Integer, Boolean>(SET_BOOLEAN, Bool::from));
    put(TINYINT, new Receiver<Integer, Byte>(SET_BYTE, NumberMethods::convert));
    put(VARCHAR, new Receiver<Integer, String>(SET_STRING, String::valueOf));
    put(CHAR, new Receiver<Integer, String>(SET_STRING, String::valueOf));
  }
}
