package nl.naturalis.yokete.db.ps;

import java.util.HashMap;
import nl.naturalis.common.Bool;
import nl.naturalis.common.NumberMethods;
import static java.sql.Types.*;
import static nl.naturalis.yokete.db.ps.ParamWriter.*;
import static nl.naturalis.yokete.db.ps.PersisterNegotiator.DEFAULT_ENTRY;

class IntPersisters extends HashMap<Integer, ValuePersister<?, ?>> {

  IntPersisters() {
    put(DEFAULT_ENTRY, new ValuePersister<>(SET_INT));
    put(INTEGER, new ValuePersister<>(SET_INT));
    put(BIGINT, new ValuePersister<>(SET_LONG));
    put(NUMERIC, new ValuePersister<>(SET_BIG_DECIMAL));
    put(REAL, new ValuePersister<>(SET_FLOAT));
    put(FLOAT, new ValuePersister<>(SET_DOUBLE));
    put(DOUBLE, new ValuePersister<>(SET_DOUBLE));
    put(SMALLINT, new ValuePersister<Integer, Short>(SET_SHORT, NumberMethods::convert));
    put(TINYINT, new ValuePersister<Integer, Byte>(SET_BYTE, NumberMethods::convert));
    put(BOOLEAN, new ValuePersister<Integer, Boolean>(SET_BOOLEAN, Bool::from));
    put(BIT, new ValuePersister<Integer, Boolean>(SET_BOOLEAN, Bool::from));
    put(TINYINT, new ValuePersister<Integer, Byte>(SET_BYTE, NumberMethods::convert));
    put(VARCHAR, new ValuePersister<Integer, String>(SET_STRING, String::valueOf));
    put(CHAR, new ValuePersister<Integer, String>(SET_STRING, String::valueOf));
  }
}
