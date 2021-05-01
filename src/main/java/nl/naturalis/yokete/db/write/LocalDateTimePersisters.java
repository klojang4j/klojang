package nl.naturalis.yokete.db.write;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashMap;
import static java.sql.Types.TIMESTAMP;
import static nl.naturalis.yokete.db.write.ParamWriter.newObjectBinder;

public class LocalDateTimePersisters extends HashMap<Integer, ValuePersister<?, ?>> {

  LocalDateTimePersisters() {
    put(TIMESTAMP, new ValuePersister<LocalDateTime, Timestamp>(newObjectBinder(TIMESTAMP)));
  }
}
