package nl.naturalis.yokete.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.UnaryOperator;
import nl.naturalis.common.check.Check;
import nl.naturalis.yokete.db.read.*;
import static nl.naturalis.yokete.db.read.MapEntryWriter.*;

public class MappifierBox {

  private final AtomicReference<DefaultMappifier> ref = new AtomicReference<>();

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
    DefaultMappifier rsm;
    if ((rsm = ref.get()) == null) {
      synchronized (this) {
        rsm = new DefaultMappifier(createWriters(rs, mapper));
        ref.setPlain(rsm);
      }
    } else if (verify) {
      Writer.checkCompatibility(rs, rsm.writers);
    }
    return rsm;
  }
}
