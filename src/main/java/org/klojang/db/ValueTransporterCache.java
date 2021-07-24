package org.klojang.db;

import java.sql.ResultSet;
import java.util.HashMap;
import java.util.function.UnaryOperator;
import org.klojang.db.rs.RsStrongIdentifier;
import org.klojang.db.rs.RsToBeanTransporter;
import org.klojang.db.rs.RsToMapTransporter;

class ValueTransporterCache {

  static final ValueTransporterCache INSTANCE = new ValueTransporterCache();

  private final HashMap<RsStrongIdentifier, RsToBeanTransporter<?, ?>[]> bvt = new HashMap<>();
  private final HashMap<RsStrongIdentifier, RsToMapTransporter<?>[]> mvt = new HashMap<>();

  private ValueTransporterCache() {}

  RsToBeanTransporter<?, ?>[] getBeanValueSetters(
      ResultSet rs, Class<?> clazz, UnaryOperator<String> mapper) {
    RsStrongIdentifier id = new RsStrongIdentifier(rs);
    return bvt.computeIfAbsent(id, k -> RsToBeanTransporter.createSetters(rs, clazz, mapper));
  }

  RsToMapTransporter<?>[] getMapValueTransporters(ResultSet rs, UnaryOperator<String> mapper) {
    RsStrongIdentifier id = new RsStrongIdentifier(rs);
    return mvt.computeIfAbsent(id, k -> RsToMapTransporter.createMapValueSetters(rs, mapper));
  }
}
