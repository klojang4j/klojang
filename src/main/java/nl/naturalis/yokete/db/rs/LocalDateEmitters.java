package nl.naturalis.yokete.db.rs;

import java.sql.Date;
import java.time.LocalDate;
import java.util.HashMap;
import static java.sql.Types.DATE;
import static java.sql.Types.TIMESTAMP;
import static nl.naturalis.yokete.db.rs.RSGetter.GET_DATE;
import static nl.naturalis.yokete.db.rs.RSGetter.objectGetter;

class LocalDateEmitters extends HashMap<Integer, Emitter<?, ?>> {

  LocalDateEmitters() {
    put(DATE, new Emitter<>(GET_DATE, Date::toLocalDate));
    put(TIMESTAMP, new Emitter<>(objectGetter(LocalDate.class)));
  }
}
