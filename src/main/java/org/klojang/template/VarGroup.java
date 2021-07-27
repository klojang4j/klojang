package org.klojang.template;

import org.klojang.render.RenderException;
import org.klojang.render.RenderSession;
import org.klojang.render.Stringifier;
import org.klojang.render.StringifierFactory;
import org.klojang.x.template.XVarGroup;
import nl.naturalis.common.check.Check;
import static org.klojang.x.Messages.ERR_NO_SUCH_VARGROUP;
import static org.klojang.x.template.XVarGroup.withName;
import static nl.naturalis.common.check.CommonChecks.notNull;

/**
 * A {@code VarGroup} lets you group template variables within a template or across templates in
 * order to provide them with a shared {@link Stringifier}. Variables can be assigned to a variable
 * group using a group name prefix. For example: <code>
 * ~%<b>html</b>:lastName%</code> or <code>~%<b>dateformat1</b>:birthDate%</code>. Variable groups
 * can also be assigned programmatically while rendering the template through a {@link
 * RenderSession} instance. No exception is thrown if a template file contains a group name prefix
 * that is not tied to a {@code Stringifier} via the {@link StringifierFactory}. However, when
 * specifying a variable group programmatically, the group <i>must</i> have been registered before
 * with the {@code StringifierFactory} or a {@link RenderException} will be thrown.
 *
 * @author Ayco Holleman
 */
public interface VarGroup {

  /**
   * A predefined variable group corresponding to the {@code text:} prefix. Forces the variable to
   * be stringified using the {@link Stringifier#DEFAULT default stringifier}.
   */
  public static final VarGroup TEXT = withName("text");

  /**
   * A predefined variable group corresponding to the {@code html:} prefix. Variables with this
   * prefix are HTML-escaped. Note that the fact alone that a variable appears inside an HTML
   * element, as in <code>&lt;td&gt;~%age%&lt;/td&gt;</code>, does not mean that the variable <i>has
   * to</i> have the {@code html:} prefix. The {@code age} variable likely is an integer, which does
   * not require any HTML escaping.
   */
  public static final VarGroup HTML = withName("html");

  /**
   * A predefined variable group corresponding to the {@code js:} prefix. Variables with this prefix
   * are JavaScript-escaped. Especially for use in <code>&lt;script&gt;</code> tags.
   */
  public static final VarGroup JS = withName("js");

  /**
   * A predefined variable group corresponding to the {@code attr:} prefix. Works just like the
   * {@code html:} prefix except that the single and double quote characters are also escaped.
   */
  public static final VarGroup ATTR = withName("attr");

  /**
   * A predefined variable group corresponding to the {@code jsattr:} prefix. Variables with this
   * prefix are first JavaScript-escaped and then HTML-escaped like the {@link #ATTR} variable
   * group. Especially for use in HTML attributes that contain JavaScript, like <code>onclick</code>
   * .
   */
  public static final VarGroup JS_ATTR = withName("jsattr");

  /**
   * A predefined variable group corresponding to the {@code param:} prefix. To be used for template
   * variables placed inside a URL as the value of a query parameter. It could also be used in the
   * more unlikely case that the variable functions as the <i>name</i> of the query parameter,
   * because names and values are escaped in the same way in a URL. Note that it does not matter
   * whether the URL as a whole is the value of a JavaScript variable or the contents of an HTML
   * tag. Further escaping using either JavaScript-escaping rules or HTML-escaping rules will not
   * change the value.
   */
  public static final VarGroup PARAM = withName("param");

  /**
   * A predefined variable group corresponding to the {@code path:} prefix. To be used for template
   * variables placed inside a URL as a path segment.
   */
  public static final VarGroup PATH = withName("path");

  /**
   * Returns the {@code VarGroup} instance corresponding to the specified name (which also functions
   * as the prefix). Throws an {@link IllegalArgumentException} if there is no {@code VarGroup} with
   * the specified name.
   *
   * @param name The name or prefix
   * @return The {@code VarGroup} instance corresponding to the specified name
   */
  public static VarGroup forName(String name) {
    VarGroup vg = Check.notNull(name).ok(XVarGroup::get);
    return Check.that(vg).is(notNull(), ERR_NO_SUCH_VARGROUP, name).ok();
  }

  String getName();
}