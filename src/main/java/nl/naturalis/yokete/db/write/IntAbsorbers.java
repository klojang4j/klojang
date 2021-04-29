package nl.naturalis.yokete.db.write;

import java.util.HashMap;
import nl.naturalis.common.Bool;
import nl.naturalis.common.NumberMethods;
import static java.sql.Types.*;
import static nl.naturalis.yokete.db.write.ColumnWriter.*;
import static nl.naturalis.yokete.db.write.ValueAbsorberNegotiator.DEFAULT_ENTRY;

class IntAbsorbers extends HashMap<Integer, ValueAbsorber<?, ?>> {

  IntAbsorbers() {
    put(DEFAULT_ENTRY, new ValueAbsorber<>(SET_INT));
    put(INTEGER, new ValueAbsorber<>(SET_INT));
    put(BIGINT, new ValueAbsorber<>(SET_LONG));
    put(NUMERIC, new ValueAbsorber<>(SET_BIG_DECIMAL));
    put(REAL, new ValueAbsorber<>(SET_FLOAT));
    put(FLOAT, new ValueAbsorber<>(SET_DOUBLE));
    put(DOUBLE, new ValueAbsorber<>(SET_DOUBLE));
    put(SMALLINT, new ValueAbsorber<Integer, Short>(SET_SHORT, NumberMethods::convert));
    put(TINYINT, new ValueAbsorber<Integer, Byte>(SET_BYTE, NumberMethods::convert));
    put(BOOLEAN, new ValueAbsorber<Integer, Boolean>(SET_BOOLEAN, Bool::from));
    put(BIT, new ValueAbsorber<Integer, Boolean>(SET_BOOLEAN, Bool::from));
    put(TINYINT, new ValueAbsorber<Integer, Byte>(SET_BYTE, NumberMethods::convert));
    put(VARCHAR, new ValueAbsorber<Integer, String>(SET_STRING, String::valueOf));
    put(CHAR, new ValueAbsorber<Integer, String>(SET_STRING, String::valueOf));
  }
}
