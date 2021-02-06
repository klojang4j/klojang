package nl.naturalis.yokete.util;

import nl.naturalis.common.ExceptionMethods;
import nl.naturalis.common.path.PathWalker;
import nl.naturalis.yokete.view.RenderException;

public class PathViewData<T> extends AbstractViewData {

  private final PathWalker pw;

  private Object obj;

  public PathViewData(PathWalker pw, ViewDataStringifiers stringifiers) {
    super(stringifiers);
    this.pw = pw;
  }

  public void setData(Object obj) {
    this.obj = obj;
  }

  @Override
  protected Object getRawValue(String var) {
    try {
      Object val = pw.read(obj);
      return val == PathWalker.DEAD_END ? ABSENT : val;
    } catch (Throwable e) {
      throw ExceptionMethods.wrap(e, RenderException::new);
    }
  }
}
