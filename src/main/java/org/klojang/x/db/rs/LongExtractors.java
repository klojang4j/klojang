package org.klojang.x.db.rs;

import nl.naturalis.common.NumberMethods;
import static java.sql.Types.*;
import static org.klojang.x.db.rs.RsMethod.*;

class LongExtractors extends ExtractorLookup<Long> {

  LongExtractors() {
    add(BIGINT, new RsExtractor<>(GET_LONG));
    add(INTEGER, new RsExtractor<>(GET_INT));
    add(SMALLINT, new RsExtractor<>(GET_SHORT));
    add(TINYINT, new RsExtractor<>(GET_BYTE));
    add(REAL, new RsExtractor<Float, Long>(GET_FLOAT, NumberMethods::convert));
    addMultiple(new RsExtractor<>(GET_DOUBLE, NumberMethods::convert), FLOAT, DOUBLE);
    addMultiple(new RsExtractor<>(GET_BIG_DECIMAL, NumberMethods::convert), NUMERIC, DECIMAL);
    addMultiple(new RsExtractor<>(GET_STRING, NumberMethods::parse), VARCHAR, CHAR);
  }
}
