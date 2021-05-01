package nl.naturalis.yokete.db.write;

import java.time.LocalDateTime;
import java.util.HashMap;
import static java.sql.Types.TIMESTAMP;
import static nl.naturalis.yokete.db.write.ParamWriter.SET_OBJECT_FOR_TIMESTAMP;

public class LocalDateTimePersisters extends HashMap<Integer, ValuePersister<?, ?>> {

  LocalDateTimePersisters() {
    put(TIMESTAMP, new ValuePersister<LocalDateTime, Object>(SET_OBJECT_FOR_TIMESTAMP));
  }
}
