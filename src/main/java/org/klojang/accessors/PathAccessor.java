package org.klojang.accessors;

import java.util.List;
import org.klojang.render.Accessor;
import org.klojang.render.NameMapper;
import org.klojang.render.RenderException;
import nl.naturalis.common.invoke.NoSuchPropertyException;
import nl.naturalis.common.path.Path;
import nl.naturalis.common.path.PathWalker;
import nl.naturalis.common.path.PathWalker.DeadEndAction;

public class PathAccessor implements Accessor<Object> {

  private final NameMapper nm;

  public PathAccessor(NameMapper nm) {
    this.nm = nm;
  }

  @Override
  public Object access(Object data, String property) throws RenderException {
    String path = nm == null ? property : nm.map(property);
    try {
      return new PathWalker(List.of(new Path(path)), DeadEndAction.THROW_EXCEPTION).read(data);
    } catch (NoSuchPropertyException e) {
      return UNDEFINED;
    }
  }
}
