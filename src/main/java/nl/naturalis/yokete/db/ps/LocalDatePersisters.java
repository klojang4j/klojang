package nl.naturalis.yokete.db.ps;

import java.sql.Date;
import java.time.LocalDate;
import java.util.HashMap;
import static java.sql.Types.DATE;
import static java.sql.Types.TIMESTAMP;
import static nl.naturalis.yokete.db.ps.ParamWriter.SET_DATE;
import static nl.naturalis.yokete.db.ps.ParamWriter.SET_OBJECT_FOR_TIMESTAMP;
import static nl.naturalis.yokete.db.ps.PersisterNegotiator.DEFAULT_ENTRY;

public class LocalDatePersisters extends HashMap<Integer, ValuePersister<?, ?>> {

  LocalDatePersisters() {
    ValuePersister<LocalDate, Date> va = new ValuePersister<>(SET_DATE, Date::valueOf);
    put(DEFAULT_ENTRY, va);
    put(DATE, va);
    put(TIMESTAMP, new ValuePersister<LocalDate, Object>(SET_OBJECT_FOR_TIMESTAMP));
  }
}
