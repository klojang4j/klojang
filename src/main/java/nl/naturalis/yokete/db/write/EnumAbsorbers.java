package nl.naturalis.yokete.db.write;

import java.util.HashMap;
import static java.sql.Types.*;
import static nl.naturalis.yokete.db.write.ColumnWriter.*;

class EnumAbsorbers extends HashMap<Integer, ValueAbsorber<?, ?>> {

  EnumAbsorbers() {
    put(INTEGER, new ValueAbsorber<Enum<?>, Integer>(SET_INT, Enum::ordinal));
    put(BIGINT, new ValueAbsorber<Enum<?>, Long>(SET_LONG, e -> (long) e.ordinal()));
    put(SMALLINT, new ValueAbsorber<Enum<?>, Short>(SET_SHORT, e -> (short) e.ordinal()));
    put(TINYINT, new ValueAbsorber<Enum<?>, Byte>(SET_BYTE, e -> (byte) e.ordinal()));
    put(VARCHAR, new ValueAbsorber<Enum<?>, String>(SET_STRING, Object::toString));
  }
}
