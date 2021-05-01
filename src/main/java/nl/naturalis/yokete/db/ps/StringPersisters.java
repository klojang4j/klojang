package nl.naturalis.yokete.db.ps;

import java.math.BigDecimal;
import java.util.HashMap;
import nl.naturalis.common.Bool;
import nl.naturalis.common.NumberMethods;
import static java.sql.Types.*;
import static nl.naturalis.yokete.db.ps.ParamWriter.*;
import static nl.naturalis.yokete.db.ps.PersisterNegotiator.DEFAULT_ENTRY;

class StringPersisters extends HashMap<Integer, ValuePersister<?, ?>> {

  StringPersisters() {
    put(DEFAULT_ENTRY, new ValuePersister<>(SET_STRING));
    put(VARCHAR, new ValuePersister<>(SET_STRING));
    put(CHAR, new ValuePersister<>(SET_STRING));
    put(INTEGER, new ValuePersister<String, Integer>(SET_INT, NumberMethods::parse));
    put(SMALLINT, new ValuePersister<String, Short>(SET_SHORT, NumberMethods::parse));
    put(TINYINT, new ValuePersister<String, Byte>(SET_BYTE, NumberMethods::parse));
    put(BIGINT, new ValuePersister<String, Long>(SET_LONG, NumberMethods::parse));
    put(NUMERIC, new ValuePersister<String, BigDecimal>(SET_BIG_DECIMAL, NumberMethods::parse));
    put(REAL, new ValuePersister<String, Float>(SET_FLOAT, NumberMethods::parse));
    put(FLOAT, new ValuePersister<String, Double>(SET_DOUBLE, NumberMethods::parse));
    put(DOUBLE, new ValuePersister<String, Double>(SET_DOUBLE, NumberMethods::parse));
    put(BOOLEAN, new ValuePersister<String, Boolean>(SET_BOOLEAN, Bool::from));
    put(BIT, new ValuePersister<Integer, Boolean>(SET_BOOLEAN, Bool::from));
  }
}
