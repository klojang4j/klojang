package nl.naturalis.yokete.db.rs;

import java.util.HashMap;
import static java.sql.Types.DATE;
import static java.sql.Types.TIMESTAMP;
import static nl.naturalis.yokete.db.rs.RsMethod.GET_DATE;
import static nl.naturalis.yokete.db.rs.RsMethod.GET_TIMESTAMP;

class LocalDateTimeExtractors extends HashMap<Integer, RsExtractor<?, ?>> {

  LocalDateTimeExtractors() {
    put(DATE, new RsExtractor<>(GET_DATE, d -> d == null ? null : d.toLocalDate().atStartOfDay()));
    put(TIMESTAMP, new RsExtractor<>(GET_TIMESTAMP, d -> d == null ? null : d.toLocalDateTime()));
  }
}
