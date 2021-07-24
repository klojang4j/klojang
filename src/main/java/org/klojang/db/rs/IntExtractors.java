package org.klojang.db.rs;

import nl.naturalis.common.NumberMethods;
import static java.sql.Types.*;
import static org.klojang.db.rs.RsMethod.*;

class IntExtractors extends ExtractorLookup<Integer> {

  IntExtractors() {
    add(INTEGER, new RsExtractor<>(GET_INT));
    add(SMALLINT, new RsExtractor<>(GET_SHORT, x -> (int) x));
    add(TINYINT, new RsExtractor<>(GET_BYTE, x -> (int) x));
    add(BOOLEAN, new RsExtractor<>(GET_BOOLEAN, x -> x ? 1 : 0));
    add(REAL, new RsExtractor<>(GET_FLOAT, NumberMethods::convert));
    add(BIGINT, new RsExtractor<>(GET_LONG, NumberMethods::convert));
    addMore(new RsExtractor<>(GET_DOUBLE, NumberMethods::convert), FLOAT, DOUBLE);
    addMore(new RsExtractor<>(GET_BIG_DECIMAL, NumberMethods::convert), NUMERIC, DECIMAL);
    add(VARCHAR, new RsExtractor<String, Integer>(GET_STRING, NumberMethods::parse));
  }
}
