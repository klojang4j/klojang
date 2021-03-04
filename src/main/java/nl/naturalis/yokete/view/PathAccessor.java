package nl.naturalis.yokete.view;

import java.util.HashMap;
import java.util.List;
import java.util.function.UnaryOperator;
import nl.naturalis.common.check.Check;
import nl.naturalis.common.path.Path;
import nl.naturalis.common.path.PathWalker;
import nl.naturalis.common.path.PathWalker.DeadEndAction;
import nl.naturalis.yokete.template.Template;
import static java.util.stream.Collectors.toList;
import static nl.naturalis.common.ObjectMethods.ifTrue;
import static nl.naturalis.common.check.CommonChecks.sameAs;
import static nl.naturalis.common.path.PathWalker.DEAD_END;

/**
 * The most versatile {@code Accessor} implementation provided by Yoketi. It allows you to retrieve
 * values at any depth from the object to extract the values from. This is done by converting
 * template variable names into {@link Path} objects, which are then used by a {@link PathWalker} to
 * descend into the object. Therefore, when you decide to use a {@code PathAccessor}, your template
 * variables might look like this: {@code ~%html:person.address.street%}.
 *
 * @author Ayco Holleman
 */
public class PathAccessor implements Accessor<Object> {

  private final PathWalker walker;
  private final UnaryOperator<String> mapper;
  private final HashMap<Path, Object> mruData;

  private Object mruObj;

  public PathAccessor(Template template) {
    this(template, x -> x);
  }

  public PathAccessor(Template template, UnaryOperator<String> nameMapper) {
    Check.notNull(template, "template");
    Check.notNull(nameMapper, "nameMapper");
    List<Path> paths =
        template.getAllNames().stream().map(nameMapper::apply).map(Path::new).collect(toList());
    this.walker = new PathWalker(paths, DeadEndAction.RETURN_DEAD_END);
    this.mapper = nameMapper;
    this.mruData = new HashMap<>(paths.size());
  }

  @Override
  public Object getValue(Object from, String varName) throws RenderException {
    if (from != mruObj) {
      walker.readValues(from, mruData);
      mruObj = from;
    }
    String key = mapper.apply(varName);
    return ifTrue(mruData.getOrDefault(key, ABSENT), sameAs(), DEAD_END, ABSENT);
  }
}
