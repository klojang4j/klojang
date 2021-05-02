package nl.naturalis.yokete.db.ps;

import java.sql.Date;
import java.time.LocalDate;
import java.util.HashMap;
import static java.sql.Types.DATE;
import static java.sql.Types.TIMESTAMP;
import static nl.naturalis.yokete.db.ps.PSSetter.SET_DATE;
import static nl.naturalis.yokete.db.ps.PSSetter.SET_OBJECT_FOR_TIMESTAMP;
import static nl.naturalis.yokete.db.ps.ReceiverNegotiator.DEFAULT;

public class LocalDateReceivers extends HashMap<Integer, Receiver<?, ?>> {

  LocalDateReceivers() {
    Receiver<LocalDate, Date> va = new Receiver<>(SET_DATE, Date::valueOf);
    put(DEFAULT, va);
    put(DATE, va);
    put(TIMESTAMP, new Receiver<LocalDate, Object>(SET_OBJECT_FOR_TIMESTAMP));
  }
}
