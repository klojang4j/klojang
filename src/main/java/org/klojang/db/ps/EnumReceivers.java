package org.klojang.db.ps;

import static java.sql.Types.*;
import static org.klojang.db.ps.PsSetter.*;

class EnumReceivers extends ReceiverLookup<Enum<?>> {

  static final Receiver<Enum<?>, Integer> DEFAULT = new Receiver<>(SET_INT, Enum::ordinal);

  static final Receiver<Enum<?>, String> ENUM_TO_STRING =
      new Receiver<>(SET_STRING, Object::toString);

  EnumReceivers() {
    put(INTEGER, DEFAULT);
    put(BIGINT, new Receiver<Enum<?>, Long>(SET_LONG, e -> (long) e.ordinal()));
    put(SMALLINT, new Receiver<Enum<?>, Short>(SET_SHORT, e -> (short) e.ordinal()));
    put(TINYINT, new Receiver<Enum<?>, Byte>(SET_BYTE, e -> (byte) e.ordinal()));
    put(VARCHAR, ENUM_TO_STRING);
    put(CHAR, ENUM_TO_STRING);
  }

  @Override
  Receiver<Enum<?>, ?> getDefaultReceiver() {
    return DEFAULT;
  }
}
