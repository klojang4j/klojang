package nl.naturalis.yokete.accessors;

import nl.naturalis.common.Pair;
import nl.naturalis.yokete.render.Accessor;
import nl.naturalis.yokete.render.RenderException;
import nl.naturalis.yokete.render.RenderSession;
import nl.naturalis.yokete.template.Template;

/** @author Ayco Holleman */
public class TupleAccessor implements Accessor<Pair<Object>> {

  private final String varName;

  /**
   * Creates a new {@code TupleAccessor} for a two-variable template where one of the variables has
   * the specified name. Any other name passed to the {@link #access(Pair, String) access} method
   * must be the name of the other variable, since before calling this method, the {@link
   * RenderSession} will check whether the specified variable name is valid in the first place,
   * given the template to be populated.
   *
   * @param varName
   */
  public TupleAccessor(String varName) {
    this.varName = varName;
  }

  /** {@inheritDoc} */
  @Override
  public Object access(Pair<Object> sourceData, String name) throws RenderException {
    if (name.equals(varName)) {
      return sourceData.getFirst();
    }
    return sourceData.getSecond();
  }

  /** {@inheritDoc} */
  @Override
  public Accessor<?> getAccessorForTemplate(Template nestedTemplate, Object nestedSourceData) {
    return null;
  }
}
