package nl.naturalis.yokete.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.UnaryOperator;
import nl.naturalis.common.check.Check;
import nl.naturalis.yokete.db.rs.MapValueSetter;
import static nl.naturalis.common.StringMethods.implode;
import static nl.naturalis.yokete.db.rs.MapValueSetter.createMapValueSetters;
import static nl.naturalis.yokete.db.rs.ValueTransporter.getMatchErrors;
import static nl.naturalis.yokete.db.rs.ValueTransporter.isCompatible;

public class MappifierBox {

  private final AtomicReference<MapValueSetter<?>[]> ref = new AtomicReference<>();

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
    MapValueSetter<?>[] setters;
    if ((setters = ref.getPlain()) == null) {
      synchronized (this) {
        if (ref.get() == null) {
          setters = createMapValueSetters(rs, mapper);
        }
      }
    } else if (verify && !isCompatible(rs, setters)) {
      List<String> errors = getMatchErrors(rs, setters);
      throw new ResultSetMismatchException(implode(errors, ". "));
    }
    return new DefaultMappifier(rs, setters);
  }
}
