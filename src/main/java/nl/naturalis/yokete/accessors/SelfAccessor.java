package nl.naturalis.yokete.accessors;

import nl.naturalis.yokete.render.Accessor;
import nl.naturalis.yokete.render.RenderException;
import nl.naturalis.yokete.render.RenderSession;

/**
 * A non-implementation of {@link Accessor} that returns the provided source data as-is, without
 * actually accessing it in whatever way. Used by {@link RenderSession#populateWithValue(String,
 * Object, nl.naturalis.yokete.template.VarGroup)}.
 *
 * @author Ayco Holleman
 */
public class SelfAccessor implements Accessor<Object> {

  public SelfAccessor() {}

  /** Ignores the {@code name} argument and returns {@code sourceData}. */
  @Override
  public Object access(Object sourceData, String name) throws RenderException {
    return sourceData;
  }
}
