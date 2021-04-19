package nl.naturalis.yokete.util;

import java.sql.ResultSet;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

class EmptyMappifier extends ResultSetMappifier {

  static final EmptyMappifier INSTANCE = new EmptyMappifier();

  private EmptyMappifier() {
    super(null);
  }

  @Override
  public Optional<Row> mappify(ResultSet rs) {
    return Optional.empty();
  }

  @Override
  public List<Row> mappifyAtMost(ResultSet rs, int limit) {
    return Collections.emptyList();
  }

  @Override
  public List<Row> mappifyAll(ResultSet rs) {
    return Collections.emptyList();
  }

  @Override
  public List<Row> mappifyAll(ResultSet rs, int sizeEstimate) {
    return Collections.emptyList();
  }
}
