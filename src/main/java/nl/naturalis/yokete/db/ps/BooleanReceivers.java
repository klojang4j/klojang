package nl.naturalis.yokete.db.ps;

import java.util.HashMap;
import static java.sql.Types.BIT;
import static java.sql.Types.BOOLEAN;
import static nl.naturalis.yokete.db.ps.PSSetter.SET_BOOLEAN;
import static nl.naturalis.yokete.db.ps.PSSetter.SET_STRING;
import static nl.naturalis.yokete.db.ps.ReceiverNegotiator.DEFAULT;

class BooleanReceivers extends HashMap<Integer, Receiver<?, ?>> {

  BooleanReceivers() {
    put(DEFAULT, new Receiver<>(SET_STRING, this::asNumberString));
    put(BOOLEAN, new Receiver<>(SET_BOOLEAN));
    put(BIT, new Receiver<>(SET_BOOLEAN));
  }

  private String asNumberString(Boolean b) {
    return b == null || b.equals(Boolean.FALSE) ? "0" : "1";
  }
}
