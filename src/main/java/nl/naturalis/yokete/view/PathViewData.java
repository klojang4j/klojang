package nl.naturalis.yokete.view;

import nl.naturalis.common.ExceptionMethods;
import nl.naturalis.common.path.PathWalker;

public class PathViewData<T> implements ViewData {

  private final PathWalker pw;
  private final Object obj;

  public PathViewData(PathWalker pw, Object obj) {
    this.pw = pw;
    this.obj = obj;
  }

  @Override
  public Object get(String var) {
    try {
      Object val = pw.read(obj);
      return val == PathWalker.DEAD_END ? ABSENT : val;
    } catch (Throwable e) {
      throw ExceptionMethods.wrap(e, RenderException::new);
    }
  }
}
