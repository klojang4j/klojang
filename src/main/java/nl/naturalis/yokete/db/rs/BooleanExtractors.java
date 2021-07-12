package nl.naturalis.yokete.db.rs;

import nl.naturalis.common.Bool;
import static java.sql.Types.*;
import static nl.naturalis.yokete.db.rs.RsMethod.*;

class BooleanExtractors extends ExtractorLookup<Boolean> {

  BooleanExtractors() {
    addMore(new RsExtractor<>(GET_BOOLEAN), BOOLEAN, BIT);
    add(INTEGER, new RsExtractor<>(GET_INT, Bool::from));
    add(SMALLINT, new RsExtractor<>(GET_SHORT, Bool::from));
    add(TINYINT, new RsExtractor<>(GET_BYTE, Bool::from));
    addMore(new RsExtractor<>(GET_DOUBLE, Bool::from), FLOAT, DOUBLE);
    put(BIGINT, new RsExtractor<Long, Boolean>(GET_LONG, Bool::from));
    put(REAL, new RsExtractor<Float, Boolean>(GET_FLOAT, Bool::from));
    addMore(new RsExtractor<>(GET_BIG_DECIMAL, Bool::from), NUMERIC, DECIMAL);
    add(VARCHAR, new RsExtractor<>(GET_STRING, Bool::from));
  }
}
