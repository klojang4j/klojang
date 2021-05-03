package nl.naturalis.yokete.db.rs;

import java.sql.ResultSet;
import java.util.HashMap;
import java.util.function.UnaryOperator;
import nl.naturalis.common.ModulePrivate;

@ModulePrivate
public class TransporterCache {

  public static final TransporterCache INSTANCE = new TransporterCache();

  private final HashMap<ResultSetIdentifier, BeanValueTransporter<?, ?>[]> bvt = new HashMap<>();
  private final HashMap<ResultSetIdentifier, MapValueTransporter<?>[]> mvt = new HashMap<>();

  private TransporterCache() {}

  public BeanValueTransporter<?, ?>[] getBeanValueTransporters(
      ResultSet rs, Class<?> clazz, UnaryOperator<String> mapper) {
    ResultSetIdentifier id = new ResultSetIdentifier(rs);
    return bvt.computeIfAbsent(id, k -> BeanValueTransporter.createTransporters(rs, clazz, mapper));
  }

  public MapValueTransporter<?>[] getMapValueTransporters(
      ResultSet rs, UnaryOperator<String> mapper) {
    ResultSetIdentifier id = new ResultSetIdentifier(rs);
    return mvt.computeIfAbsent(id, k -> MapValueTransporter.createTransporters(rs, mapper));
  }
}
