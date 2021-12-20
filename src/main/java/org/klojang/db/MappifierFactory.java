package org.klojang.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicReference;
import org.klojang.template.NameMapper;
import org.klojang.x.db.rs.RowChannel;
import nl.naturalis.common.check.Check;
import static org.klojang.x.db.rs.RowChannel.createChannels;

/**
 * A Factory for {@link ResultSetMappifier} instances. The {@link ResultSet result sets} passed to a
 * {@code MappifierFactory} in return for a beanifier instance cannot just be any arbitrary {@code
 * ResultSet}; they must all be created from the same SQL query. The very first {@code ResultSet}
 * passed to a {@code MappifierFactory} is used to create and cache the objects needed to convert
 * the {@code ResultSet} into a JavaBean. Subsequent calls to {@link #getMappifier(ResultSet)} will
 * use these objects, too. Hence, all result sets passed to {@code getMappifier} must be
 * <i>compatible</i> with the first one: they must have at least as many columns and the column
 * types must match those of the first result set. Column names do in fact no longer matter. The
 * column-to-property mapping is set up and fixed after the first call to {@code getMappifier}.
 *
 * @author Ayco Holleman
 */
public class MappifierFactory {

  private final AtomicReference<RowChannel<?>[]> ref = new AtomicReference<>();

  private final NameMapper mapper;

  /**
   * Creates a new {@code MappifierFactory}. Column names will be mapped {@link NameMapper#AS_IS
   * as-is} to map keys.
   */
  public MappifierFactory() {
    this(NameMapper.AS_IS);
  }

  /**
   * Creates a new {@code MappifierFactory}.
   *
   * @param columnToKeyMapper A {@code NameMapper} mapping column names to map keys
   */
  public MappifierFactory(NameMapper columnToKeyMapper) {
    this.mapper = Check.notNull(columnToKeyMapper).ok();
  }

  public ResultSetMappifier getMappifier(ResultSet rs) throws SQLException {
    if (!rs.next()) {
      return EmptyMappifier.INSTANCE;
    }
    RowChannel<?>[] channels;
    if ((channels = ref.getPlain()) == null) {
      synchronized (this) {
        if (ref.get() == null) {
          ref.set(channels = createChannels(rs, mapper));
        }
      }
    }
    return new DefaultMappifier(rs, channels);
  }
}
