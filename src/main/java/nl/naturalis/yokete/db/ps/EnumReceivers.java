package nl.naturalis.yokete.db.ps;

import java.util.HashMap;
import static java.sql.Types.*;
import static nl.naturalis.yokete.db.ps.PSSetter.*;
import static nl.naturalis.yokete.db.ps.ReceiverSelector.DEFAULT;

class EnumReceivers extends HashMap<Integer, Receiver<?, ?>> {

  EnumReceivers() {
    put(DEFAULT, new Receiver<Enum<?>, Integer>(SET_INT, Enum::ordinal));
    put(INTEGER, new Receiver<Enum<?>, Integer>(SET_INT, Enum::ordinal));
    put(INTEGER, new Receiver<Enum<?>, Integer>(SET_INT, Enum::ordinal));
    put(BIGINT, new Receiver<Enum<?>, Long>(SET_LONG, e -> (long) e.ordinal()));
    put(SMALLINT, new Receiver<Enum<?>, Short>(SET_SHORT, e -> (short) e.ordinal()));
    put(TINYINT, new Receiver<Enum<?>, Byte>(SET_BYTE, e -> (byte) e.ordinal()));
    put(VARCHAR, new Receiver<Enum<?>, String>(SET_STRING, e -> String.valueOf(e.ordinal())));
  }
}
