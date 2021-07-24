package org.klojang.db.ps;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import static java.sql.Types.*;
import static org.klojang.db.ps.PsSetter.SET_TIMESTAMP;
import static nl.naturalis.common.ObjectMethods.ifNotNull;

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
