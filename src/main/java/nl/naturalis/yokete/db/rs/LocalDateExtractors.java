package nl.naturalis.yokete.db.rs;

import java.sql.Date;
import java.time.LocalDate;
import java.util.HashMap;
import static java.sql.Types.DATE;
import static java.sql.Types.TIMESTAMP;
import static nl.naturalis.common.ObjectMethods.ifNotNull;
import static nl.naturalis.yokete.db.rs.RsMethod.GET_DATE;
import static nl.naturalis.yokete.db.rs.RsMethod.objectGetter;

class LocalDateExtractors extends HashMap<Integer, RsExtractor<?, ?>> {

  LocalDateExtractors() {
    put(DATE, new RsExtractor<>(GET_DATE, x -> ifNotNull(x, Date::toLocalDate)));
    put(TIMESTAMP, new RsExtractor<>(objectGetter(LocalDate.class)));
  }
}
