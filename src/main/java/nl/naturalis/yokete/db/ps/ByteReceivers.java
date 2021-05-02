package nl.naturalis.yokete.db.ps;

import java.util.HashMap;
import nl.naturalis.common.Bool;
import static java.sql.Types.*;
import static nl.naturalis.yokete.db.ps.PSSetter.*;
import static nl.naturalis.yokete.db.ps.ReceiverNegotiator.DEFAULT;

class ByteReceivers extends HashMap<Integer, Receiver<?, ?>> {

  ByteReceivers() {
    put(DEFAULT, new Receiver<>(SET_BYTE));
    put(TINYINT, new Receiver<>(SET_BYTE));
    put(INTEGER, new Receiver<Byte, Integer>(SET_INT));
    put(BIGINT, new Receiver<Byte, Long>(SET_LONG));
    put(NUMERIC, new Receiver<>(SET_BIG_DECIMAL));
    put(REAL, new Receiver<>(SET_FLOAT));
    put(FLOAT, new Receiver<>(SET_DOUBLE));
    put(DOUBLE, new Receiver<>(SET_DOUBLE));
    put(BOOLEAN, new Receiver<Integer, Boolean>(SET_BOOLEAN, Bool::from));
    put(BIT, new Receiver<Integer, Boolean>(SET_BOOLEAN, Bool::from));
    put(VARCHAR, new Receiver<Integer, String>(SET_STRING, String::valueOf));
    put(CHAR, new Receiver<Integer, String>(SET_STRING, String::valueOf));
  }
}
