package nl.naturalis.yokete.db.ps;

import java.time.LocalDateTime;
import java.util.HashMap;
import static java.sql.Types.TIMESTAMP;
import static nl.naturalis.yokete.db.ps.PSSetter.SET_OBJECT_FOR_TIMESTAMP;

public class LocalDateTimeReceivers extends HashMap<Integer, Receiver<?, ?>> {

  LocalDateTimeReceivers() {
    put(TIMESTAMP, new Receiver<LocalDateTime, Object>(SET_OBJECT_FOR_TIMESTAMP));
  }
}
