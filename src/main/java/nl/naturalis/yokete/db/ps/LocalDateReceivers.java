package nl.naturalis.yokete.db.ps;

import java.sql.Date;
import java.time.LocalDate;
import static java.sql.Types.DATE;
import static java.sql.Types.TIMESTAMP;
import static nl.naturalis.common.ObjectMethods.ifNotNull;
import static nl.naturalis.yokete.db.ps.PsSetter.SET_DATE;
import static nl.naturalis.yokete.db.ps.PsSetter.setObject;

public class LocalDateReceivers extends ReceiverLookup<LocalDate> {

  static final Receiver<LocalDate, Date> DEFAULT =
      new Receiver<>(SET_DATE, d -> ifNotNull(d, Date::valueOf));

  LocalDateReceivers() {
    put(DATE, DEFAULT);
    put(TIMESTAMP, new Receiver<LocalDate, Object>(setObject(TIMESTAMP)));
  }

  @Override
  Receiver<LocalDate, ?> getDefaultReceiver() {
    return DEFAULT;
  }
}
