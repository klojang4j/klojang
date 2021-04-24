package nl.naturalis.yokete.db;

import java.sql.Date;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import static java.sql.Types.DATE;

class ToLocalDateTimeSynapses {

  private static final Function<Date, LocalDateTime> SQLDATE_TO_LOCALDATETIME =
      d -> d.toLocalDate().atStartOfDay();

  private ToLocalDateTimeSynapses() {}

  static Map<Integer, Synapse<?, ?>> get() {
    Map<Integer, Synapse<?, LocalDateTime>> tmp = new HashMap<>();
    tmp.put(DATE, new Synapse<>(ColumnReader.GET_DATE, SQLDATE_TO_LOCALDATETIME));
    return Map.copyOf(tmp);
  }
}
