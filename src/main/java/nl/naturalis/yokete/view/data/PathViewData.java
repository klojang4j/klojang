package nl.naturalis.yokete.view.data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import nl.naturalis.common.path.Path;
import nl.naturalis.common.path.PathWalker;
import nl.naturalis.common.path.PathWalker.DeadEndAction;
import nl.naturalis.yokete.template.Template;
import nl.naturalis.yokete.view.ViewData;
import static java.util.stream.Collectors.toList;
import static nl.naturalis.common.ObjectMethods.ifNotNull;

public class PathViewData extends AbstractViewData {

  private static Map<Template, PathWalker> pwCache = new HashMap<>();

  private Object bean;

  public PathViewData(ViewDataStringifiers stringifiers) {
    super(stringifiers);
  }

  public PathViewData with(Object bean) {
    this.bean = bean;
    return this;
  }

  @Override
  protected Optional<?> getRawValue(Template template, String name) {
    PathWalker pw = pwCache.computeIfAbsent(template, this::newPathWalker);
    Object val = pw.read(bean);
    if (val == PathWalker.DEAD_END) {
      return Optional.empty();
    }
    return ifNotNull(val, Optional::of, NULL);
  }

  @Override
  protected ViewData createViewData(Template template, Object bean) {
    return new PathViewData(stringifiers).with(bean);
  }

  private PathWalker newPathWalker(Template template) {
    List<Path> paths = template.getAllNames().stream().map(Path::new).collect(toList());
    return new PathWalker(paths, DeadEndAction.RETURN_DEAD_END);
  }
}
