package nl.naturalis.yokete.db.read;

import java.sql.Date;
import java.time.LocalDate;
import java.util.HashMap;
import static java.sql.Types.DATE;
import static java.sql.Types.TIMESTAMP;
import static nl.naturalis.yokete.db.read.ColumnReader.GET_DATE;
import static nl.naturalis.yokete.db.read.ColumnReader.newObjectReader;

class LocalDateProducers extends HashMap<Integer, ValueProducer<?, ?>> {

  LocalDateProducers() {
    put(DATE, new ValueProducer<>(GET_DATE, Date::toLocalDate));
    put(TIMESTAMP, new ValueProducer<>(newObjectReader(LocalDate.class)));
  }
}
