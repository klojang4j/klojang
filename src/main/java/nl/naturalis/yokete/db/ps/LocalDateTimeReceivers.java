package nl.naturalis.yokete.db.ps;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashMap;
import static java.sql.Types.TIMESTAMP;
import static nl.naturalis.common.ObjectMethods.ifNotEmpty;
import static nl.naturalis.yokete.db.ps.PSSetter.SET_TIMESTAMP;
import static nl.naturalis.yokete.db.ps.ReceiverNegotiator.DEFAULT;

public class LocalDateTimeReceivers extends HashMap<Integer, Receiver<?, ?>> {

  LocalDateTimeReceivers() {
    Receiver<LocalDateTime, Timestamp> va =
        new Receiver<>(SET_TIMESTAMP, x -> ifNotEmpty(x, Timestamp::valueOf));
    put(DEFAULT, va);
    put(TIMESTAMP, va);
  }
}
