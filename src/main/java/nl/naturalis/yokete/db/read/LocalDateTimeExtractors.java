package nl.naturalis.yokete.db.read;

import java.time.LocalDateTime;
import java.util.HashMap;
import static java.sql.Types.DATE;
import static java.sql.Types.TIMESTAMP;
import static nl.naturalis.yokete.db.read.ColumnReader.GET_DATE;
import static nl.naturalis.yokete.db.read.ColumnReader.newObjectReader;

class LocalDateTimeExtractors extends HashMap<Integer, ValueExtractor<?, ?>> {

  LocalDateTimeExtractors() {
    put(DATE, new ValueExtractor<>(GET_DATE, d -> d.toLocalDate().atStartOfDay()));
    put(TIMESTAMP, new ValueExtractor<>(newObjectReader(LocalDateTime.class)));
  }
}
