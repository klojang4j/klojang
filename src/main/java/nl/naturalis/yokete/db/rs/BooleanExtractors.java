package nl.naturalis.yokete.db.rs;

import java.math.BigDecimal;
import java.util.HashMap;
import nl.naturalis.common.Bool;
import static java.sql.Types.*;
import static nl.naturalis.yokete.db.rs.RsMethod.*;
import static nl.naturalis.yokete.db.rs.ExtractorNegotiator.DEFAULT;

class BooleanExtractors extends HashMap<Integer, RsExtractor<?, ?>> {

  BooleanExtractors() {
    RsExtractor<Boolean, Boolean> vp0 = new RsExtractor<>(GET_BOOLEAN);
    put(BOOLEAN, vp0);
    put(BIT, vp0);
    put(INTEGER, new RsExtractor<Integer, Boolean>(GET_INT, Bool::from));
    put(SMALLINT, new RsExtractor<Short, Boolean>(GET_SHORT, Bool::from));
    put(TINYINT, new RsExtractor<Byte, Boolean>(GET_BYTE, Bool::from));
    RsExtractor<Double, Boolean> vp1 = new RsExtractor<>(GET_DOUBLE, Bool::from);
    put(FLOAT, vp1);
    put(DOUBLE, vp1);
    put(BIGINT, new RsExtractor<Long, Boolean>(GET_LONG, Bool::from));
    put(REAL, new RsExtractor<Float, Boolean>(GET_FLOAT, Bool::from));
    RsExtractor<BigDecimal, Boolean> vp2 = new RsExtractor<>(GET_BIG_DECIMAL, Bool::from);
    put(NUMERIC, vp2);
    put(DECIMAL, vp2);
    RsExtractor<String, Boolean> vp3 = new RsExtractor<>(GET_STRING, Bool::from);
    put(VARCHAR, vp3);
    put(DEFAULT, vp3);
  }
}
