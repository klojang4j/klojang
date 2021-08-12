package org.klojang.x.db.rs;

import java.sql.Date;
import java.time.LocalDate;
import static java.sql.Types.DATE;
import static java.sql.Types.TIMESTAMP;
import static org.klojang.x.db.rs.RsMethod.GET_DATE;
import static org.klojang.x.db.rs.RsMethod.objectGetter;
import static nl.naturalis.common.ObjectMethods.ifNotNull;

class LocalDateExtractors extends ExtractorLookup<LocalDate> {

  LocalDateExtractors() {
    add(DATE, new RsExtractor<>(GET_DATE, x -> ifNotNull(x, Date::toLocalDate)));
    add(TIMESTAMP, new RsExtractor<>(objectGetter(LocalDate.class)));
  }
}
