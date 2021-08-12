package org.klojang.x.db.rs;

import java.time.LocalDateTime;
import static java.sql.Types.DATE;
import static java.sql.Types.TIMESTAMP;
import static org.klojang.x.db.rs.RsMethod.GET_DATE;
import static org.klojang.x.db.rs.RsMethod.GET_TIMESTAMP;

class LocalDateTimeExtractors extends ExtractorLookup<LocalDateTime> {

  LocalDateTimeExtractors() {
    add(DATE, new RsExtractor<>(GET_DATE, d -> d == null ? null : d.toLocalDate().atStartOfDay()));
    add(TIMESTAMP, new RsExtractor<>(GET_TIMESTAMP, d -> d == null ? null : d.toLocalDateTime()));
  }
}
