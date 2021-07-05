package nl.naturalis.yokete.db.ps;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import static java.sql.Types.*;
import static nl.naturalis.common.ObjectMethods.ifNotNull;
import static nl.naturalis.yokete.db.ps.PSSetter.SET_TIMESTAMP;

public class LocalDateTimeReceivers extends ReceiverLookup<LocalDateTime> {

  static final Receiver<LocalDateTime, Timestamp> DEFAULT =
      new Receiver<>(SET_TIMESTAMP, d -> ifNotNull(d, Timestamp::valueOf));

  LocalDateTimeReceivers() {
    put(TIMESTAMP, DEFAULT);
  }

  @Override
  Receiver<LocalDateTime, ?> getDefaultReceiver() {
    return DEFAULT;
  }
}
