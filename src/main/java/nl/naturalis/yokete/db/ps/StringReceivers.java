package nl.naturalis.yokete.db.ps;

import java.math.BigDecimal;
import java.util.HashMap;
import nl.naturalis.common.Bool;
import nl.naturalis.common.NumberMethods;
import static java.sql.Types.*;
import static nl.naturalis.yokete.db.ps.PSSetter.*;
import static nl.naturalis.yokete.db.ps.ReceiverSelector.DEFAULT;

class StringReceivers extends HashMap<Integer, Receiver<?, ?>> {

  StringReceivers() {
    put(DEFAULT, new Receiver<>(SET_STRING));
    put(VARCHAR, new Receiver<>(SET_STRING));
    put(CHAR, new Receiver<>(SET_STRING));
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
}
