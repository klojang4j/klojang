package org.klojang.render;

import org.klojang.template.Template;
import nl.naturalis.common.ModulePrivate;
import static org.klojang.render.AccessorRegistry.STANDARD_ACCESSORS;
import static org.klojang.render.StringifierRegistry.STANDARD_STRINGIFIERS;

/**
 * For internal use only.
 *
 * @author Ayco Holleman
 */
@ModulePrivate
public final class SessionConfig {

  private final Template template;
  private final AccessorRegistry accessors;
  private final StringifierRegistry stringifiers;

  public SessionConfig(Template template) {
    this(template, STANDARD_ACCESSORS, STANDARD_STRINGIFIERS);
  }

  public SessionConfig(Template template, StringifierRegistry stringifiers) {
    this(template, STANDARD_ACCESSORS, stringifiers);
  }

  public SessionConfig(Template template, AccessorRegistry accessors) {
    this(template, accessors, STANDARD_STRINGIFIERS);
  }

  public SessionConfig(
      Template template, AccessorRegistry accessors, StringifierRegistry stringifiers) {
    this.template = template;
    this.accessors = accessors;
    this.stringifiers = stringifiers;
  }

  public RenderSession newRenderSession() {
    return new RenderSession(this);
  }

  Template getTemplate() {
    return template;
  }

  AccessorRegistry getAccessorFactory() {
    return accessors;
  }

  StringifierRegistry getStringifiers() {
    return stringifiers;
  }

  Accessor<?> getAccessor(Object sourceData) {
    return accessors.getAccessor(sourceData, template);
  }

  RenderSession newChildSession(Template nested) {
    SessionConfig config = new SessionConfig(nested, accessors, stringifiers);
    return config.newRenderSession();
  }
}
