package nl.naturalis.yokete.db.write;

import java.util.HashMap;
import static java.sql.Types.*;
import static nl.naturalis.yokete.db.write.ParamWriter.*;
import static nl.naturalis.yokete.db.write.PersisterNegotiator.DEFAULT_ENTRY;

class EnumPersisters extends HashMap<Integer, ValuePersister<?, ?>> {

  EnumPersisters() {
    put(DEFAULT_ENTRY, new ValuePersister<Enum<?>, Integer>(SET_INT, Enum::ordinal));
    put(INTEGER, new ValuePersister<Enum<?>, Integer>(SET_INT, Enum::ordinal));
    put(INTEGER, new ValuePersister<Enum<?>, Integer>(SET_INT, Enum::ordinal));
    put(BIGINT, new ValuePersister<Enum<?>, Long>(SET_LONG, e -> (long) e.ordinal()));
    put(SMALLINT, new ValuePersister<Enum<?>, Short>(SET_SHORT, e -> (short) e.ordinal()));
    put(TINYINT, new ValuePersister<Enum<?>, Byte>(SET_BYTE, e -> (byte) e.ordinal()));
    put(VARCHAR, new ValuePersister<Enum<?>, String>(SET_STRING, e -> String.valueOf(e.ordinal())));
  }
}
