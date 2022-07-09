package org.klojang.x.acc;

import java.util.List;

import nl.naturalis.common.path.ErrorCode;
import nl.naturalis.common.path.PathWalkerException;
import org.klojang.template.Accessor;
import org.klojang.template.NameMapper;
import org.klojang.template.RenderException;
import nl.naturalis.common.path.Path;
import nl.naturalis.common.path.PathWalker;

import static java.util.Arrays.asList;

public class PathAccessor implements Accessor<Object> {

  private final NameMapper nm;

  public PathAccessor(NameMapper nm) {
    this.nm = nm;
  }

  @Override
  public Object access(Object data, String property) throws RenderException {
    String path = nm == null ? property : nm.map(property);
    PathWalker pw = new PathWalker(asList(new Path(path)), false);
    try {
      return pw.read(data);
    } catch (PathWalkerException e) {
      return switch (e.getErrorCode()) {
        default -> new RenderException(e.getMessage());
      };
    }
  }

}
