package nl.naturalis.yokete.util;

import nl.naturalis.common.ExceptionMethods;
import nl.naturalis.common.path.PathWalker;
import nl.naturalis.yokete.view.RenderException;
import nl.naturalis.yokete.view.ViewData;

public class PathViewData<T> implements ViewData {

  private final PathWalker pw;

  private Object obj;

  public PathViewData(PathWalker pw) {
    this.pw = pw;
  }

  public void setData(Object obj) {
    this.obj = obj;
  }

  @Override
  public Object getVariableValue(String var) {
    try {
      Object val = pw.read(obj);
      return val == PathWalker.DEAD_END ? ABSENT : val;
    } catch (Throwable e) {
      throw ExceptionMethods.wrap(e, RenderException::new);
    }
  }
}
