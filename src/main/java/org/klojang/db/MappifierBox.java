package org.klojang.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.UnaryOperator;
import org.klojang.x.db.rs.RsToMapTransporter;
import org.klojang.x.db.rs.ValueTransporterCache;
import nl.naturalis.common.check.Check;
import static org.klojang.x.db.rs.RsToMapTransporter.createValueTransporters;
import static nl.naturalis.common.StringMethods.implode;

public class MappifierBox {

  private final AtomicReference<RsToMapTransporter<?>[]> ref = new AtomicReference<>();

  private final UnaryOperator<String> mapper;
  private final boolean verify;

  public MappifierBox() {
    this(UnaryOperator.identity());
  }

  public MappifierBox(UnaryOperator<String> columnToKeyMapper) {
    this(columnToKeyMapper, false);
  }

  public MappifierBox(UnaryOperator<String> columnToKeyMapper, boolean verify) {
    this.mapper = Check.notNull(columnToKeyMapper).ok();
    this.verify = verify;
  }

  public ResultSetMappifier get(ResultSet rs) throws SQLException {
    if (!rs.next()) {
      return EmptyMappifier.INSTANCE;
    }
    RsToMapTransporter<?>[] transporters;
    if ((transporters = ref.getPlain()) == null) {
      synchronized (this) {
        if (ref.get() == null) {
          transporters = createValueTransporters(rs, mapper);
        }
      }
    } else if (verify && !ValueTransporterCache.isCompatible(rs, transporters)) {
      List<String> errors = ValueTransporterCache.getMatchErrors(rs, transporters);
      throw new ResultSetMismatchException(implode(errors, ". "));
    }
    return new DefaultMappifier(rs, transporters);
  }
}
