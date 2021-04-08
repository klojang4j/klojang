package nl.naturalis.yokete.render;

import java.util.Map;

/**
 * Same as MapAccessor but without the checks and other niceties. Could be used internally if we
 * have guarantees that we don't need them. Much like MonoAccessor and DuoAccessor. Not currently
 * used though.
 *
 * @author Ayco Holleman
 */
class MapAccessor0 implements Accessor<Map<String, Object>> {

  @Override
  public Object access(Map<String, Object> from, String name) throws RenderException {
    return from.get(name);
  }
}
