package nl.naturalis.yokete.db.read;

import java.time.LocalDateTime;
import java.util.HashMap;
import static java.sql.Types.DATE;
import static java.sql.Types.TIMESTAMP;
import static nl.naturalis.yokete.db.read.ColumnReader.GET_DATE;
import static nl.naturalis.yokete.db.read.ColumnReader.newObjectReader;

class LocalDateTimeProducers extends HashMap<Integer, ValueProducer<?, ?>> {

  LocalDateTimeProducers() {
    put(DATE, new ValueProducer<>(GET_DATE, d -> d.toLocalDate().atStartOfDay()));
    put(TIMESTAMP, new ValueProducer<>(newObjectReader(LocalDateTime.class)));
  }
}
