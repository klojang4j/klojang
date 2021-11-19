package org.klojang.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.klojang.render.NameMapper;
import org.klojang.x.db.rs.RowChannel;
import org.klojang.x.db.rs.ChannelCache;
import nl.naturalis.common.check.Check;
import static org.klojang.x.db.rs.RowChannel.createChannels;
import static nl.naturalis.common.StringMethods.implode;

/**
 * A Factory
 *
 * @author Ayco Holleman
 */
public class MappifierBox {

  private final AtomicReference<RowChannel<?>[]> ref = new AtomicReference<>();

  private final NameMapper mapper;
  private final boolean verify;

  public MappifierBox() {
    this(NameMapper.AS_IS);
  }

  public MappifierBox(NameMapper columnToKeyMapper) {
    this(columnToKeyMapper, false);
  }

  public MappifierBox(NameMapper columnToKeyMapper, boolean verify) {
    this.mapper = Check.notNull(columnToKeyMapper).ok();
    this.verify = verify;
  }

  public ResultSetMappifier get(ResultSet rs) throws SQLException {
    if (!rs.next()) {
      return EmptyMappifier.INSTANCE;
    }
    RowChannel<?>[] transporters;
    if ((transporters = ref.getPlain()) == null) {
      synchronized (this) {
        if (ref.get() == null) {
          transporters = createChannels(rs, mapper);
        }
      }
    } else if (verify && !ChannelCache.isCompatible(rs, transporters)) {
      List<String> errors = ChannelCache.getMatchErrors(rs, transporters);
      throw new ResultSetMismatchException(implode(errors, ". "));
    }
    return new DefaultMappifier(rs, transporters);
  }
}
