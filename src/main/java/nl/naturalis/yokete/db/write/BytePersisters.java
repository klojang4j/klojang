package nl.naturalis.yokete.db.write;

import java.util.HashMap;
import nl.naturalis.common.Bool;
import static java.sql.Types.*;
import static nl.naturalis.yokete.db.write.ParamWriter.*;
import static nl.naturalis.yokete.db.write.PersisterNegotiator.DEFAULT_ENTRY;

class BytePersisters extends HashMap<Integer, ValuePersister<?, ?>> {

  BytePersisters() {
    put(DEFAULT_ENTRY, new ValuePersister<>(SET_BYTE));
    put(TINYINT, new ValuePersister<>(SET_BYTE));
    put(INTEGER, new ValuePersister<Byte, Integer>(SET_INT));
    put(BIGINT, new ValuePersister<Byte, Long>(SET_LONG));
    put(NUMERIC, new ValuePersister<>(SET_BIG_DECIMAL));
    put(REAL, new ValuePersister<>(SET_FLOAT));
    put(FLOAT, new ValuePersister<>(SET_DOUBLE));
    put(DOUBLE, new ValuePersister<>(SET_DOUBLE));
    put(BOOLEAN, new ValuePersister<Integer, Boolean>(SET_BOOLEAN, Bool::from));
    put(BIT, new ValuePersister<Integer, Boolean>(SET_BOOLEAN, Bool::from));
    put(VARCHAR, new ValuePersister<Integer, String>(SET_STRING, String::valueOf));
    put(CHAR, new ValuePersister<Integer, String>(SET_STRING, String::valueOf));
  }
}
