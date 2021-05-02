package nl.naturalis.yokete.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.UnaryOperator;
import nl.naturalis.common.check.Check;
import nl.naturalis.yokete.db.rs.*;
import static nl.naturalis.common.StringMethods.implode;
import static nl.naturalis.yokete.db.rs.MapValueTransporter.*;
import static nl.naturalis.yokete.db.rs.Transporter.*;

public class MappifierBox {

  private final AtomicReference<MapValueTransporter<?>[]> ref = new AtomicReference<>();

  private final UnaryOperator<String> mapper;
  private final boolean verify;

  public MappifierBox() {
    this(x -> x);
  }

  public MappifierBox(UnaryOperator<String> columToKeyMapper) {
    this(columToKeyMapper, false);
  }

  public MappifierBox(UnaryOperator<String> columToKeyMapper, boolean verify) {
    this.mapper = Check.notNull(columToKeyMapper).ok();
    this.verify = verify;
  }

  public ResultSetMappifier get(ResultSet rs) throws SQLException {
    if (!rs.next()) {
      return EmptyMappifier.INSTANCE;
    }
    MapValueTransporter<?>[] transporters;
    if ((transporters = ref.getPlain()) == null) {
      synchronized (this) {
        if (ref.get() == null) {
          transporters = createTransporters(rs, mapper);
        }
      }
    } else if (verify && !isCompatible(rs, transporters)) {
      List<String> errors = getMatchErrors(rs, transporters);
      throw new ResultSetMismatchException(implode(errors, ". "));
    }
    return new DefaultMappifier(rs, transporters);
  }
}
