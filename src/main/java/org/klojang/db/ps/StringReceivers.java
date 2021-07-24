package org.klojang.db.ps;

import java.math.BigDecimal;
import nl.naturalis.common.Bool;
import nl.naturalis.common.NumberMethods;
import static java.sql.Types.*;
import static org.klojang.db.ps.PsSetter.*;

class StringReceivers extends ReceiverLookup<String> {

  static final Receiver<String, String> DEFAULT = new Receiver<>(SET_STRING);

  StringReceivers() {
    put(VARCHAR, DEFAULT);
    put(CHAR, DEFAULT);
    put(INTEGER, new Receiver<String, Integer>(SET_INT, NumberMethods::parse));
    put(SMALLINT, new Receiver<String, Short>(SET_SHORT, NumberMethods::parse));
    put(TINYINT, new Receiver<String, Byte>(SET_BYTE, NumberMethods::parse));
    put(BIGINT, new Receiver<String, Long>(SET_LONG, NumberMethods::parse));
    put(NUMERIC, new Receiver<String, BigDecimal>(SET_BIG_DECIMAL, NumberMethods::parse));
    put(REAL, new Receiver<String, Float>(SET_FLOAT, NumberMethods::parse));
    put(FLOAT, new Receiver<String, Double>(SET_DOUBLE, NumberMethods::parse));
    put(DOUBLE, new Receiver<String, Double>(SET_DOUBLE, NumberMethods::parse));
    put(BOOLEAN, new Receiver<String, Boolean>(SET_BOOLEAN, Bool::from));
    put(BIT, new Receiver<Integer, Boolean>(SET_BOOLEAN, Bool::from));
  }

  @Override
  Receiver<String, ?> getDefaultReceiver() {
    return DEFAULT;
  }
}
