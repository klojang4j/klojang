package org.klojang.x.db.rs;

import nl.naturalis.common.Bool;
import static java.sql.Types.*;
import static org.klojang.x.db.rs.RsMethod.*;

class BooleanExtractors extends ExtractorLookup<Boolean> {

  BooleanExtractors() {
    addMultiple(new RsExtractor<>(GET_BOOLEAN), BOOLEAN, BIT);
    add(INTEGER, new RsExtractor<>(GET_INT, Bool::from));
    add(SMALLINT, new RsExtractor<>(GET_SHORT, Bool::from));
    add(TINYINT, new RsExtractor<>(GET_BYTE, Bool::from));
    addMultiple(new RsExtractor<>(GET_DOUBLE, Bool::from), FLOAT, DOUBLE);
    put(BIGINT, new RsExtractor<Long, Boolean>(GET_LONG, Bool::from));
    put(REAL, new RsExtractor<Float, Boolean>(GET_FLOAT, Bool::from));
    addMultiple(new RsExtractor<>(GET_BIG_DECIMAL, Bool::from), NUMERIC, DECIMAL);
    addMultiple(new RsExtractor<>(GET_STRING, Bool::from), CHAR, VARCHAR);
  }
}
