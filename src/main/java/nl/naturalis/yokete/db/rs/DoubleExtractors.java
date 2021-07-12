package nl.naturalis.yokete.db.rs;

import nl.naturalis.common.NumberMethods;
import static java.sql.Types.*;
import static nl.naturalis.yokete.db.rs.RsMethod.*;

class DoubleExtractors extends ExtractorLookup<Double> {

  DoubleExtractors() {
    addMore(new RsExtractor<>(GET_DOUBLE), FLOAT, DOUBLE);
    add(INTEGER, new RsExtractor<>(GET_INT));
    add(SMALLINT, new RsExtractor<>(GET_SHORT));
    add(TINYINT, new RsExtractor<>(GET_BYTE));
    add(BOOLEAN, new RsExtractor<>(GET_BOOLEAN, x -> x ? 1.0 : 0));
    add(REAL, new RsExtractor<>(GET_FLOAT));
    add(BIGINT, new RsExtractor<>(GET_LONG));
    addMore(new RsExtractor<>(GET_BIG_DECIMAL, NumberMethods::convert), NUMERIC, DECIMAL);
    add(VARCHAR, new RsExtractor<>(GET_STRING, NumberMethods::parse));
  }
}
