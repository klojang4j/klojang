package nl.naturalis.yokete.db.rs;

import java.util.HashMap;
import static nl.naturalis.yokete.db.rs.RSGetter.GET_STRING;
import static nl.naturalis.yokete.db.rs.EmitterNegotiator.DEFAULT;

class StringEmitters extends HashMap<Integer, Emitter<?, ?>> {

  StringEmitters() {
    put(DEFAULT, new Emitter<>(GET_STRING));
  }
}
