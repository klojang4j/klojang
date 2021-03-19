package nl.naturalis.yokete.render;

import java.util.HashMap;
import java.util.List;
import nl.naturalis.common.check.Check;
import nl.naturalis.common.path.Path;
import nl.naturalis.common.path.PathWalker;
import nl.naturalis.yokete.template.Template;
import static java.util.stream.Collectors.toList;
import static nl.naturalis.common.path.PathWalker.DEAD_END;
import static nl.naturalis.common.path.PathWalker.DeadEndAction.RETURN_DEAD_END;

/**
 * The most versatile {@code Accessor} implementation provided by Yokete. It can retrieve values
 * from objects that are (deeply) nested within the source data object. This is done by converting
 * variable names into {@link Path} objects, which are then used by a {@link PathWalker} to retrieve
 * their values. Therefore, when you decide to use a {@code PathAccessor}, your template variables
 * might look like this: {@code ~%html:person.address.street%}.
 *
 * <p>This class is not thread-safe.
 *
 * @author Ayco Holleman
 */
public class PathAccessor implements Accessor {

  private final Template template;
  private final PathWalker walker;
  private final NameMapper mapper;
  private final HashMap<Path, Object> mruData;

  private Object mruObj;

  public PathAccessor(Template template) {
    this(template, NameMapper.NOOP);
  }

  public PathAccessor(Template template, NameMapper nameMapper) {
    this.template = Check.notNull(template, "template").ok();
    Check.notNull(nameMapper, "nameMapper");
    List<Path> paths =
        template
            .getNames()
            .stream()
            .map(varName -> nameMapper.map(template, varName))
            .map(Path::new)
            .collect(toList());
    this.walker = new PathWalker(paths, RETURN_DEAD_END);
    this.mapper = nameMapper;
    this.mruData = new HashMap<>(paths.size());
  }

  @Override
  public Object access(Object from, String varName) throws RenderException {
    if (from != mruObj) {
      walker.readValues(from, mruData);
      mruObj = from;
    }
    String key = mapper.map(template, varName);
    Object val = mruData.getOrDefault(key, UNDEFINED);
    return val == DEAD_END ? UNDEFINED : val;
  }

  @Override
  public Accessor getAccessorForTemplate(Template nestedTemplate, Object nestedSourceData) {
    return new PathAccessor(nestedTemplate, mapper);
  }
}
