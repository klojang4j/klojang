package nl.naturalis.yokete.db.rs;

import java.time.LocalDateTime;
import java.util.HashMap;
import static java.sql.Types.DATE;
import static java.sql.Types.TIMESTAMP;
import static nl.naturalis.yokete.db.rs.RSGetter.GET_DATE;
import static nl.naturalis.yokete.db.rs.RSGetter.objectGetter;

class LocalDateTimeEmitters extends HashMap<Integer, Emitter<?, ?>> {

  LocalDateTimeEmitters() {
    put(DATE, new Emitter<>(GET_DATE, d -> d.toLocalDate().atStartOfDay()));
    put(TIMESTAMP, new Emitter<>(objectGetter(LocalDateTime.class)));
  }
}
