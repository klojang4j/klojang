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
    put(INTEGER, new Receiver<>(SET_INT));
    put(SMALLINT, new Receiver<>(SET_SHORT));
    put(BIGINT, new Receiver<>(SET_LONG));
    put(REAL, new Receiver<>(SET_FLOAT));
    put(FLOAT, new Receiver<>(SET_DOUBLE));
    put(DOUBLE, new Receiver<>(SET_DOUBLE));
    put(NUMERIC, new Receiver<>(SET_BIG_DECIMAL));
    put(DECIMAL, new Receiver<>(SET_BIG_DECIMAL));
    put(BOOLEAN, new Receiver<Byte, Boolean>(SET_BOOLEAN, Bool::from));
    put(BIT, new Receiver<Byte, Boolean>(SET_BOOLEAN, Bool::from));
    put(VARCHAR, new Receiver<Byte, String>(SET_STRING, String::valueOf));
    put(CHAR, new Receiver<Byte, String>(SET_STRING, String::valueOf));
  }
}
