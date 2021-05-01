package nl.naturalis.yokete.db.write;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.HashMap;
import static java.sql.Types.*;
import static nl.naturalis.yokete.db.write.ParamWriter.*;
import static nl.naturalis.yokete.db.write.PersisterNegotiator.DEFAULT_ENTRY;

public class LocalDatePersisters extends HashMap<Integer, ValuePersister<?, ?>> {

  LocalDatePersisters() {
    ValuePersister<LocalDate, Date> va = new ValuePersister<>(SET_DATE, Date::valueOf);
    put(DEFAULT_ENTRY, va);
    put(DATE, va);
    put(TIMESTAMP, new ValuePersister<LocalDate, Timestamp>(newObjectBinder(TIMESTAMP)));
  }
}
