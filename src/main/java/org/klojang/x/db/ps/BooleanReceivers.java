package org.klojang.x.db.ps;

import static java.sql.Types.BIT;
import static java.sql.Types.BOOLEAN;
import static java.sql.Types.CHAR;
import static java.sql.Types.VARCHAR;
import static org.klojang.x.db.ps.PsSetter.SET_BOOLEAN;
import static org.klojang.x.db.ps.PsSetter.SET_STRING;

class BooleanReceivers extends ReceiverLookup<Boolean> {

  static final Receiver<Boolean, ?> DEFAULT = new Receiver<>(SET_BOOLEAN);

  BooleanReceivers() {
    put(BOOLEAN, DEFAULT);
    put(BIT, DEFAULT);
    putMultiple(new Receiver<>(SET_STRING, this::asNumberString), VARCHAR, CHAR);
  }

  private String asNumberString(Boolean b) {
    return b == null || b.equals(Boolean.FALSE) ? "0" : "1";
  }

  @Override
  Receiver<Boolean, ?> getDefaultReceiver() {
    return DEFAULT;
  }
}
