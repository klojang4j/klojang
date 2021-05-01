package nl.naturalis.yokete.db.read;

import java.sql.Date;
import java.time.LocalDate;
import java.util.HashMap;
import static java.sql.Types.DATE;
import static java.sql.Types.TIMESTAMP;
import static nl.naturalis.yokete.db.read.ColumnReader.GET_DATE;
import static nl.naturalis.yokete.db.read.ColumnReader.newObjectReader;

class LocalDateExtractors extends HashMap<Integer, ValueExtractor<?, ?>> {

  LocalDateExtractors() {
    put(DATE, new ValueExtractor<>(GET_DATE, Date::toLocalDate));
    put(TIMESTAMP, new ValueExtractor<>(newObjectReader(LocalDate.class)));
  }
}
