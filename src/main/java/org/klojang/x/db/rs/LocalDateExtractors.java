package org.klojang.x.db.rs;

import java.sql.Date;
import java.time.LocalDate;
import java.util.HashMap;
import static java.sql.Types.DATE;
import static java.sql.Types.TIMESTAMP;
import static org.klojang.x.db.rs.RsMethod.GET_DATE;
import static org.klojang.x.db.rs.RsMethod.objectGetter;
import static nl.naturalis.common.ObjectMethods.ifNotNull;

class LocalDateExtractors extends HashMap<Integer, RsExtractor<?, ?>> {

  LocalDateExtractors() {
    put(DATE, new RsExtractor<>(GET_DATE, x -> ifNotNull(x, Date::toLocalDate)));
    put(TIMESTAMP, new RsExtractor<>(objectGetter(LocalDate.class)));
  }
}
