package nl.naturalis.yokete.db;

import java.sql.ResultSet;
import java.util.HashMap;
import java.util.function.UnaryOperator;
import nl.naturalis.yokete.db.rs.BeanValueSetter;
import nl.naturalis.yokete.db.rs.MapEntryProducer;
import nl.naturalis.yokete.db.rs.ResultSetIdentifier;

class TransporterCache {

  static final TransporterCache INSTANCE = new TransporterCache();

  private final HashMap<ResultSetIdentifier, BeanValueSetter<?, ?>[]> bvt = new HashMap<>();
  private final HashMap<ResultSetIdentifier, MapEntryProducer<?>[]> mvt = new HashMap<>();

  private TransporterCache() {}

  BeanValueSetter<?, ?>[] getBeanValueSetters(
      ResultSet rs, Class<?> clazz, UnaryOperator<String> mapper) {
    ResultSetIdentifier id = new ResultSetIdentifier(rs);
    return bvt.computeIfAbsent(id, k -> BeanValueSetter.createSetters(rs, clazz, mapper));
  }

  MapEntryProducer<?>[] getMapValueTransporters(ResultSet rs, UnaryOperator<String> mapper) {
    ResultSetIdentifier id = new ResultSetIdentifier(rs);
    return mvt.computeIfAbsent(id, k -> MapEntryProducer.createMapValueSetters(rs, mapper));
  }
}
