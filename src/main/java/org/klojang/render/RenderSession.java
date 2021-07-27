package org.klojang.render;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Predicate;
import org.klojang.template.Template;
import org.klojang.template.VarGroup;
import org.klojang.template.VariablePart;
import org.klojang.x.template.XVarGroup;
import nl.naturalis.common.Tuple;
import nl.naturalis.common.check.Check;
import nl.naturalis.common.collection.IntList;
import nl.naturalis.common.io.UnsafeByteArrayOutputStream;
import static java.util.Collections.singletonMap;
import static java.util.stream.Collectors.toList;
import static org.klojang.render.Accessor.UNDEFINED;
import static org.klojang.render.RenderException.*;
import static org.klojang.template.TemplateUtils.getFQName;
import static nl.naturalis.common.ArrayMethods.EMPTY_STRING_ARRAY;
import static nl.naturalis.common.CollectionMethods.asUnsafeList;
import static nl.naturalis.common.ObjectMethods.ifNull;
import static nl.naturalis.common.ObjectMethods.isEmpty;
import static nl.naturalis.common.ObjectMethods.n2e;
import static nl.naturalis.common.StringMethods.concat;
import static nl.naturalis.common.check.CommonChecks.*;

/**
 * A {@code RenderSession} is responsible for populating a template and rendering it. Populating the
 * template can be done in multiple passes. By default template variables and nested templates are
 * not rendered. That is, unless you provide them with values, they will just disappear from the
 * template upon rendering. As soon as you render the template (by calling of the {@code render}
 * methods), the {@code RenderSession} effectively becomes immutable. You can render the template
 * again, as often as you like, but the values of its variables are fixed.
 *
 * <p>A {@code RenderSession} is a throw-away object that should go out of scope as quickly as
 * possible. It is cheap to instantiate, but can gain a lot of state as the template gets populated.
 * Therefore, make sure it doesn't survive the request method. A possible exception could be
 * templates that render relatively static content, especially if the cost of populating them is
 * high.
 *
 * <h4>Thead Safety</h4>
 *
 * A {@code RenderSession} carries a lot of state across its methods and is therefore in principle
 * not thread-safe. However, as long as different threads populate different parts of the template
 * (e.g. one thread populates the main table and another thread does the rest), they cannot get in
 * each other's way.
 *
 * @author Ayco Holleman
 */
public class RenderSession {

  private final SessionConfig config;
  private final RenderState state;

  RenderSession(SessionConfig config) {
    this.config = config;
    this.state = new RenderState(config);
  }

  /* METHODS FOR SETTING A SINGLE TEMPLATE VARIABLE */

  /**
   * Sets the specified variable to the specified value. Equivalent to {@link #set(String, Object,
   * XVarGroup) set(varName, value, null)}.
   *
   * @param varName The name of the variable to set
   * @param value The value of the variable
   * @throws RenderException
   */
  public RenderSession set(String varName, Object value) throws RenderException {
    return set(varName, value, null);
  }

  /**
   * Sets the specified variable to the specified value using the {@link Stringifier stringifier}
   * associated with the specified {@link XVarGroup variable group}. If the variable already has a
   * group name prefix (e.g. ~%<b>html</b>:fullName%), the group specified through the prefix will
   * prevail. The {@code defaultGroup} argument is allowed to be {@code null}. Then, if the variable
   * neither has a group name prefix, the {@link StringifierFactory} for this {@code RenderSession}
   * will attempt to find a suitable stringifier by other means; for example, based on the {@link
   * StringifierFactory.Builder#addTypeBasedStringifier(Stringifier, Class...) data type} or the
   * {@link StringifierFactory.Builder#addNameBasedStringifier(Stringifier, String...) name} of the
   * variable. Otherwise, stringifiers assoicated with a variable group will always prevail.
   *
   * @see StringifierFactory.Builder#addGroupStringifier(Stringifier, String...)
   * @param varName The name of the variable to set
   * @param value The value of the variable
   * @param defaultGroup The variable group to assign the variable to if the variable has no group
   *     name prefix. May be {@code null}.
   * @return This {@code RenderSession}
   * @throws RenderException
   */
  public RenderSession set(String varName, Object value, VarGroup defaultGroup)
      throws RenderException {
    Check.on(frozenSession(), state.isFrozen()).is(no());
    Check.notNull(varName, "varName");
    Template template = config.getTemplate();
    Check.on(noSuchVariable(template, varName), template.getVariables()).is(containing(), varName);
    Check.on(alreadySet(template, varName), state.isSet(varName)).is(no());
    if (value == UNDEFINED) {
      // Unless the user is manually going through, and accessing the properties of some source data
      // object, specifying UNDEFINED misses the point of that constant, but since we can't know
      // this, we'll have to accept that value and process it as it is meant to be processed
      // (namely: not).
      return this;
    }
    IntList indices = config.getTemplate().getVarPartIndices().get(varName);
    StringifierFactory sf = config.getStringifierFactory();
    for (int i = 0; i < indices.size(); ++i) {
      int partIndex = indices.get(i);
      VariablePart part = template.getPart(partIndex);
      Stringifier stringifier = sf.getStringifier(part, defaultGroup, value);
      String stringified = stringify(stringifier, varName, value);
      state.setVar(partIndex, new String[] {stringified});
    }
    state.done(varName);
    return this;
  }

  /**
   * Sets the specified variable to the concatenation of the values within the specified {@code
   * List}. Unless the variable was declared with an inline {@link EscapeType} no escaping will be
   * applied to the value. The values in the {@code List} are first stringified, then escaped, then
   * concatenated. If the {@code List} is empty, the variable will not be rendered at all (that is,
   * an empty string will be inserted at the location of the variable within the template).
   *
   * @param varName The name of the variable to set
   * @param values The string values to concatenate
   * @return This {@code RenderSession}
   * @throws RenderException
   */
  public RenderSession set(String varName, List<?> values) throws RenderException {
    return set(varName, values, null);
  }

  /**
   * Sets the specified variable to the concatenation of the values within the specified {@code
   * List}. The values in the {@code List} are first stringified, then escaped, then concatenated.
   * If the {@code List} is empty, the variable will not be rendered at all (that is, an empty
   * string will be inserted at the location of the variable within the template).
   *
   * @param varName The name of the variable to set
   * @param values The string values to concatenate
   * @param defaultGroup The variable group to assign the variable to if the variable has no group
   *     name prefix
   * @return This {@code RenderSession}
   * @throws RenderException
   */
  public RenderSession set(String varName, List<?> values, VarGroup defaultGroup)
      throws RenderException {
    return set(varName, values, defaultGroup, null, null, null);
  }

  /**
   * Sets the specified variable to the concatenation of the values within the specified {@code
   * List}. Each value will be prefixed with the specified prefix, suffixed with the specified
   * suffix, and separated from the previous one by the specified separator. The values in the
   * {@code List} are first stringified, then escaped, then enriched with prefix, suffix and
   * separator, and then concatenated. If the {@code List} is empty, the variable will not be
   * rendered at all.
   *
   * @param varName The name of the variable to set
   * @param values The string values to concatenate
   * @param defaultGroup The variable group to assign the variable to if the variable has no group
   *     name prefix. May be {@code null}.
   * @param prefix The prefix to use for each string
   * @param separator The suffix to use for each string
   * @param suffix The separator to use between the stringd
   * @return This {@code RenderSession}
   * @throws RenderException
   */
  public RenderSession set(
      String varName,
      List<?> values,
      VarGroup defaultGroup,
      String prefix,
      String separator,
      String suffix)
      throws RenderException {
    Check.on(frozenSession(), state.isFrozen()).is(no());
    Check.notNull(varName, "varName");
    Check.notNull(values, "values");
    Template t = config.getTemplate();
    Check.on(noSuchVariable(t, varName), t.getVariables()).is(containing(), varName);
    Check.on(alreadySet(t, varName), state.isSet(varName)).is(no());
    IntList indices = config.getTemplate().getVarPartIndices().get(varName);
    if (values.isEmpty()) {
      indices.forEach(i -> state.setVar(i, EMPTY_STRING_ARRAY));
    } else {
      indices.forEachThrowing(i -> setVar(i, values, defaultGroup, prefix, separator, suffix));
    }
    state.done(varName);
    return this;
  }

  private void setVar(
      int partIndex,
      List<?> values,
      VarGroup defGroup,
      String prefix,
      String separator,
      String suffix)
      throws RenderException {
    VariablePart part = config.getTemplate().getPart(partIndex);
    VarGroup varGroup = part.getVarGroup().orElse(defGroup);
    prefix = n2e(prefix);
    separator = n2e(separator);
    suffix = n2e(suffix);
    boolean enrich = !prefix.isEmpty() || !separator.isEmpty() || !suffix.isEmpty();
    StringifierFactory sf = config.getStringifierFactory();
    // Find first non-null value to increase the chance that we find a suitable
    // stringifier:
    Object nn = values.stream().filter(notNull()).findFirst().orElse(null);
    Stringifier stringifier = sf.getStringifier(part, varGroup, nn);
    String[] stringified = new String[values.size()];
    for (int i = 0; i < values.size(); ++i) {
      String s = stringify(stringifier, part.getName(), values.get(i));
      if (enrich) {
        if (i == 0) {
          s = prefix + s + suffix;
        } else {
          s = separator + prefix + s + suffix;
        }
      }
      stringified[i] = s;
    }
    state.setVar(partIndex, stringified);
  }

  /**
   * Sets the specified variable to the entire output of the specified {@code Renderable}. This
   * allows you to create and populate a template for an HTML snippet once, and then repeatedly (for
   * each render session of the current template) "paste" its output into the current template. See
   * {@link #createRenderable()}.
   *
   * @param varName The template variable to set
   * @param renderable The {@code Renderable}
   * @return This {@code RenderSession}
   * @throws RenderException
   */
  public RenderSession paste(String varName, Renderable renderable) throws RenderException {
    Check.on(frozenSession(), state.isFrozen()).is(no());
    Check.on(illegalValue("varName", varName), varName).is(notNull());
    Check.on(illegalValue("renderable", renderable), renderable).is(notNull());
    Template t = config.getTemplate();
    Check.on(noSuchVariable(t, varName), t.getVariables()).is(containing(), varName);
    Check.on(alreadySet(t, varName), state.isSet(varName)).is(no());
    IntList indices = config.getTemplate().getVarPartIndices().get(varName);
    indices.forEach(i -> state.setVar(i, renderable));
    return this;
  }

  /* METHODS FOR POPULATING A SINGLE NESTED TEMPLATE */

  /**
   * Populates a <i>nested</i> template. The template is populated with values retrieved from the
   * specified source data. Only variables and (doubly) nested templates whose name is present in
   * the {@code names} argument will be populated. No escaping will be applied to the values
   * retrieved from the data object. See {@link #populate(String, Object, EscapeType, String...)}.
   *
   * @param nestedTemplateName The name of the nested template
   * @param sourceData An object that provides data for all or some of the nested template's
   *     variables and nested templates
   * @param names The names of the variables and doubly-nested templates that you want to be
   *     populated using the specified data object
   */
  public RenderSession populate(String nestedTemplateName, Object sourceData, String... names)
      throws RenderException {
    return populate(nestedTemplateName, sourceData, null, names);
  }

  /**
   * Populates a <i>nested</i> template. The template is populated with values retrieved from the
   * specified source data. Only variables and (doubly) nested templates whose name is present in
   * the {@code names} argument will be populated.
   *
   * <h4>Repeating Templates</h4>
   *
   * <p>If the specified object is an array or a {@code Collection}, the template will be repeated
   * for each object in the array or {@code Collection}. This can be used, for example, to generate
   * an HTML table from a nested template that contains just a single row.
   *
   * <h4>Conditional Rendering</h4>
   *
   * <p>If the specified object is an empty array or an empty {@code Collection}, the template will
   * not be rendered at all. This is the mechanism for conditional rendering: "populate" the nested
   * template with an empty array or {@code Collection} and the template will not be rendered. Note,
   * however, that the same can be achieved more easily by just never calling the {@code populate}
   * method for the template it will not be rendered either. By default neither template variables
   * nor nested templates are rendered.
   *
   * <h4>Text-only templates</h4>
   *
   * <p>Although the {@code RenderSession} has a {@link #show(int, String) separate method} for
   * dealing with text-only templates, they can also be made to be rendered by this method.
   *
   * @param nestedTemplateName The name of the nested template
   * @param sourceData An object that provides data for all or some of the nested template's
   *     variables and nested templates
   * @param defaultGroup The variable group to assign the variables to if they have no group name
   *     prefix. May be {@code null}.
   * @param names The names of the variables and doubly-nested templates that you want to be
   *     populated using the specified data object
   * @return This {@code RenderSession}
   * @throws RenderException
   */
  public RenderSession populate(
      String nestedTemplateName, Object sourceData, VarGroup defaultGroup, String... names)
      throws RenderException {
    Check.on(frozenSession(), state.isFrozen()).is(no());
    Template t = getNestedTemplate(nestedTemplateName);
    List<?> data = asUnsafeList(sourceData);
    if (t.isTextOnly()) {
      return show(data.size(), t);
    }
    Check.on(missingSourceData(t), data).is(neverNull());
    return repeat(t, data, defaultGroup, names);
  }

  private RenderSession repeat(Template t, List<?> data, VarGroup defGroup, String... names)
      throws RenderException {
    RenderSession[] sessions = state.getOrCreateChildSessions(t, data.size());
    for (int i = 0; i < sessions.length; ++i) {
      sessions[i].insert(data.get(i), defGroup, names);
    }
    return this;
  }

  /**
   * Enabled or disables the rendering of the specified templates. The specified templates must all
   * be text-only templates, otherwise a {@link RenderException} is thrown. Equivalent to {@link
   * #show(int, String...) show(1, nestedTemplateNames)}.
   *
   * @param nestedTemplateNames The names of the nested templates to be rendered.
   * @return This {@code RenderSession}
   * @throws RenderException
   */
  public RenderSession show(String... nestedTemplateNames) throws RenderException {
    return show(1, nestedTemplateNames);
  }

  /**
   * Enabled or disables the rendering of the specified templates. Each of the specified templates
   * will be repeated the specified number of times. The specified templates must all be text-only
   * templates, otherwise a {@link RenderException} is thrown. A text-only template is a template
   * that does not contain any variables or nested templates. Some reasons you might want to have
   * such a template are:
   *
   * <p>
   *
   * <ul>
   *   <li>You want to conditionally render the (static) HTML inside it
   *   <li>You want to include it in multiple parent templates
   *   <li>You want to reduce clutter in your main template
   * </ul>
   *
   * <p>To <i>disable</i> rendering of a text-only template, specify 0 (zero) for the {@code
   * repeats} argument. Note, however, that by default template variables and nested templates are
   * not rendered in the first place, so you could also just not call this method for the template
   * in question.
   *
   * <p>Specify an empty {@code String} array to enable <i>all</i> text-only templates that have not
   * been explicitly enabled or disabled yet. In that case it <i>does</i> make sense to first
   * explicitly disable the text-only templates that should not be rendered.
   *
   * <p>You could achieve the same by calling {@code populate(nestedTemplateName, null} or {@code
   * populate(nestedTemplateName, new Object[6]} (repeat six times) or {@code
   * populate(nestedTemplateName, new Object[0]} (disable the template). However, the {@code show}
   * method bypasses some code that is irrelevant to text-only templates.
   *
   * @param nestedTemplateNames The names of the nested text-only templates you want to be rendered
   * @return This {@code RenderSession}
   * @throws RenderException
   */
  public RenderSession show(int repeats, String... nestedTemplateNames) throws RenderException {
    Check.on(frozenSession(), state.isFrozen()).is(no());
    Check.that(repeats, "repeats").is(gte(), 0);
    Check.notNull(nestedTemplateNames, "nestedTemplateNames");
    if (nestedTemplateNames.length == 0) {
      for (Template t : config.getTemplate().getNestedTemplates()) {
        if (t.isTextOnly() && state.getChildSessions(t) == null) {
          show(repeats, t);
        }
      }
    } else {
      for (String name : nestedTemplateNames) {
        Check.that(name).is(notNull(), "Template name must not be null");
        Template t = getNestedTemplate(name);
        Check.on(notTextOnly(t), t.isTextOnly()).is(yes());
        show(repeats, t);
      }
    }
    return this;
  }

  private RenderSession show(int repeats, Template nested) throws RenderException {
    state.getOrCreateTextOnlyChildSessions(nested, repeats);
    return this;
  }

  /**
   * Enables the rendering of the specified templates. The templates may themselves contain
   * text-only templates, but they must not contain variables or templates containing variables.
   *
   * @param nestedTemplateNames The names of the nested text-only templates you want to be rendered
   * @return This {@code RenderSession}
   * @throws RenderException
   */
  public RenderSession showRecursive(String... nestedTemplateNames) throws RenderException {
    Check.on(frozenSession(), state.isFrozen()).is(no());
    Check.notNull(nestedTemplateNames, "nestedTemplateNames");
    if (nestedTemplateNames.length == 0) {
      for (Template t : config.getTemplate().getNestedTemplates()) {
        if (t.countVariables() == 0 && state.getChildSessions(t) == null) {
          /*
           * NB Only at this level do we skip templates that cannot possibly be recursively
           * text-only (because the variables already spoil it). As soon as we recurse into deeper
           * nesting levels there is no difference any longer between this branch of the main "if"
           * branch and the other branch: at deeper levels templates must simply be either text-only
           * templates themselves or only contain templates that themselves are (recursively)
           * text-only.
           */
          showRecursive(this, t);
        }
      }
    } else {
      for (String name : nestedTemplateNames) {
        Check.that(name).is(notNull(), "Template name must not be null");
        Template t = getNestedTemplate(name);
        showRecursive(this, t);
      }
    }
    return this;
  }

  private static void showRecursive(RenderSession s0, Template t0) throws RenderException {
    if (t0.isTextOnly()) {
      s0.show(1, t0);
      return;
    }
    Check.on(notTextOnly(t0), t0.countVariables()).is(zero());
    RenderSession s = s0.state.getOrCreateChildSession(t0);
    for (Template t : t0.getNestedTemplates()) {
      showRecursive(s, t);
    }
  }

  /**
   * Convenience method for populating a nested template that contains exactly one variable and zero
   * (doubly) nested templates. If the specified value is an array or a {@code Collection}, the
   * template is going to be repeated for each value <i>within</i> the array or {@code Collection}.
   *
   * @param nestedTemplateName The name of the nested template. <i>Must</i> contain exactly one
   *     variable
   * @return This {@code RenderSession}
   * @throws RenderException
   */
  public RenderSession populateWithValue(String nestedTemplateName, Object value)
      throws RenderException {
    return populateWithValue(nestedTemplateName, value, null);
  }

  /**
   * Convenience method for populating a nested template that contains exactly one variable and zero
   * (doubly) nested templates. The variable may still occur multiple times within the template. If
   * the specified value is an array or a {@code Collection}, the template is going to be repeated
   * for each value <i>within</i> the array or {@code Collection}.
   *
   * @param nestedTemplateName The name of the nested template. <i>Must</i> contain exactly one
   *     variable
   * @param value The value to set the template's one and only variable to
   * @param defaultGroup The variable group to assign the variable to if the variable has no group
   *     name prefix. May be {@code null}.
   * @return This {@code RenderSession}
   * @throws RenderException
   */
  public RenderSession populateWithValue(
      String nestedTemplateName, Object value, VarGroup defaultGroup) throws RenderException {
    Check.on(frozenSession(), state.isFrozen()).is(no());
    Template t = getNestedTemplate(nestedTemplateName);
    Check.on(notMonoTemplate(t), t)
        .has(tmpl -> tmpl.getVariables().size(), eq(), 1)
        .has(tmpl -> tmpl.countNestedTemplates(), eq(), 0);
    String var = t.getVariables().iterator().next();
    List<?> values = Arrays.asList(value).stream().map(v -> singletonMap(var, v)).collect(toList());
    RenderSession[] sessions = state.getOrCreateChildSessions(t, values.size());
    for (int i = 0; i < sessions.length; ++i) {
      sessions[i].insert(values.get(i), defaultGroup);
    }
    return this;
  }

  /**
   * Convenience method for populating a nested template that contains exactly two variables and
   * zero (doubly) nested templates.
   *
   * @param nestedTemplateName The name of the nested template.
   * @param tuples A list of value pairs
   * @return This {@code RenderSession}
   * @throws RenderException
   */
  public <T, U> RenderSession populateWithTuple(String nestedTemplateName, List<Tuple<T, U>> tuples)
      throws RenderException {
    return populateWithTuple(nestedTemplateName, tuples, null);
  }

  /**
   * Convenience method for populating a nested template that contains exactly two variables and
   * zero (doubly) nested templates. The variables may still occur multiple times within the
   * template. The size of the list of tuples determines how often the template is going to be
   * repeated.
   *
   * @param nestedTemplateName The name of the nested template
   * @param tuples A list of value pairs
   * @param defaultGroup The variable group to assign the variables to if they have no group name
   *     prefix
   * @return This {@code RenderSession}
   * @throws RenderException
   */
  public <T, U> RenderSession populateWithTuple(
      String nestedTemplateName, List<Tuple<T, U>> tuples, VarGroup defaultGroup)
      throws RenderException {
    Check.on(frozenSession(), state.isFrozen()).is(no());
    Check.on(illegalValue("tuples", tuples), tuples).is(neverNull());
    Template t = getNestedTemplate(nestedTemplateName);
    Check.on(notTupleTemplate(t), t)
        .has(tmpl -> tmpl.getVariables().size(), eq(), 2)
        .has(tmpl -> tmpl.countNestedTemplates(), eq(), 0);
    String[] vars = t.getVariables().toArray(new String[2]);
    List<Map<String, Object>> data =
        tuples
            .stream()
            .map(tuple -> Map.of(vars[0], tuple.getLeft(), vars[1], tuple.getRight()))
            .collect(toList());
    RenderSession[] sessions = state.getOrCreateChildSessions(t, data.size());
    for (int i = 0; i < sessions.length; ++i) {
      sessions[i].insert(data.get(i), defaultGroup);
    }
    return this;
  }

  /* METHODS FOR POPULATING WHATEVER IS IN THE PROVIDED OBJECT */

  /**
   * Populates the <i>current</i> template (the template for which this {@code RenderSession} was
   * created). No escaping will be applied to the values extracted from the source data.
   *
   * @param sourceData An object that provides data for all or some of the template variables and
   *     nested templates
   * @param escapeType The escape type to use
   * @param names The names of the variables nested templates names that must be populated. Not
   *     specifying any name (or {@code null}) indicates that you want all variables and nested
   *     templates to be populated.
   * @return This {@code RenderSession}
   * @throws RenderException
   */
  public RenderSession insert(Object sourceData, String... names) throws RenderException {
    return insert(sourceData, null, names);
  }

  /**
   * Populates the <i>current</i> template (the template for which this {@code RenderSession} was
   * created) using the provided source data object. The {@code RenderSession} will attempt to
   * populate all variables and nested templates except those whose name is present in the {@code
   * names} array. The source data object is not required to populate the entire template in one
   * shot. You can call this and similar methods multiple times until you are satisfied and ready to
   * {@link #render(OutputStream) render} the template.
   *
   * @param sourceData An object that provides data for all or some of the template variables and
   *     nested templates
   * @param defaultGroup The variable group to assign the variables to if they have no group name
   *     prefix. May be {@code null}.
   * @param names The names of the variables nested templates names that must be populated. Not
   *     specifying any name (or {@code null}) indicates that you want all variables and nested
   *     templates to be populated.
   * @return This {@code RenderSession}
   * @throws RenderException
   */
  public RenderSession insert(Object sourceData, VarGroup defaultGroup, String... names)
      throws RenderException {
    Check.on(frozenSession(), state.isFrozen()).is(no());
    if (sourceData == null) {
      Template t = config.getTemplate();
      Check.on(notTextOnly(t), t.isTextOnly()).is(yes());
      // If we get past this check, the entire template is in fact
      // static HTML. Pretty expensive way to render static HTML,
      // but no reason not to support it.
      return this;
    }
    processVars(sourceData, defaultGroup, names);
    processTmpls(sourceData, defaultGroup, names);
    return this;
  }

  @SuppressWarnings("unchecked")
  private <T> void processVars(T data, VarGroup defGroup, String[] names) throws RenderException {
    Set<String> varNames;
    if (isEmpty(names)) {
      varNames = config.getTemplate().getVariables();
    } else {
      varNames = new HashSet<>(config.getTemplate().getVariables());
      varNames.retainAll(Set.of(names));
    }
    Accessor<T> acc = (Accessor<T>) config.getAccessor(data);
    for (String varName : varNames) {
      Object value = acc.access(data, varName);
      if (value != UNDEFINED) {
        set(varName, value, defGroup);
      }
    }
  }

  @SuppressWarnings("unchecked")
  private <T> void processTmpls(T data, VarGroup defaultGroup, String[] names)
      throws RenderException {
    Set<String> tmplNames;
    if (isEmpty(names)) {
      tmplNames = config.getTemplate().getNestedTemplateNames();
    } else {
      tmplNames = new HashSet<>(config.getTemplate().getNestedTemplateNames());
      tmplNames.retainAll(Set.of(names));
    }
    Accessor<T> acc = (Accessor<T>) config.getAccessor(data);
    for (String name : tmplNames) {
      Object nestedData = acc.access(data, name);
      if (nestedData != UNDEFINED) {
        populate(name, nestedData, defaultGroup, names);
      }
    }
  }

  /* MISCELLANEOUS METHODS */

  /**
   * Returns a {@code RenderSession} for the specified nested template. The {@code RenderSession}
   * inherits the {@link AccessorFactory accessors} and {@link StringifierFactory stringifiers} from
   * the parent session (i.e. <i>this</i> {@code RenderSession}).
   *
   * @param nestedTemplateName The nested template for which to create the child session
   * @return A child session that you can (and should) populate yourself
   * @throws RenderException
   */
  public RenderSession in(String nestedTemplateName) throws RenderException {
    Check.on(frozenSession(), state.isFrozen()).is(no());
    Template t = getNestedTemplate(nestedTemplateName);
    return state.getOrCreateChildSession(t);
  }

  /**
   * Returns the child sessions that have been created for the specified nested template. This
   * method throws a {@code RenderException} if no child sessions have been created yet for the
   * specified nested template.
   *
   * @param nestedTemplateName The nested template
   * @return A {@code List} of child sessions
   * @throws RenderException
   */
  public List<RenderSession> getChildSessions(String nestedTemplateName) throws RenderException {
    Check.on(frozenSession(), state.isFrozen()).is(no());
    Template t = getNestedTemplate(nestedTemplateName);
    RenderSession[] sessions = state.getChildSessions(t);
    Check.on(noChildSessionsYet(t), t).is(notNull());
    return List.of(sessions);
  }

  /* RENDER METHODS */

  /**
   * Returns whether or not the template is fully populated. That is, all variables have been set
   * and all nested templates (however deeply nested) have been populated. Note that you may not
   * <i>want</i> the template to be fully populated. Nested templates whose rendering depended on a
   * condition that turned out to evaluate to {@code false} will not be populated.
   *
   * @return Whether or not the template is fully populated
   */
  public boolean isFullyPopulated() {
    return state.isFullyPopulated();
  }

  /**
   * Returns a {@code Renderable} instance that allows you to render the current template. See
   * {@link #paste(String, Renderable)}.
   *
   * @return A {@code Renderable} instance allows you to render the current template
   */
  public Renderable createRenderable() {
    state.freeze();
    return new Renderer(state);
  }

  /**
   * Writes the render result to the specified {@code OutputStream}. Shortcut for {@code
   * createRenderable().render(out)}.
   *
   * @param out The output stream to which to write the render result
   * @throws RenderException
   */
  public void render(OutputStream out) {
    createRenderable().render(out);
  }

  /**
   * Appends the render result to the specified {@code StringBuilder}. Shortcut for {@code
   * createRenderable().render(sb)}.
   *
   * @param sb The {@code StringBuilder} to which to append the render result
   * @throws RenderException
   */
  public void render(StringBuilder sb) {
    createRenderable().render(sb);
  }

  /**
   * Returns the render result as a {@code String}.
   *
   * @return The render result
   */
  public String render() {
    UnsafeByteArrayOutputStream out = new UnsafeByteArrayOutputStream(1024);
    createRenderable().render(out);
    return new String(out.toByteArray(), 0, out.byteCount(), StandardCharsets.UTF_8);
  }

  @Override
  public String toString() {
    return concat(
        getClass().getSimpleName(),
        " ",
        System.identityHashCode(this),
        " for template ",
        getFQName(config.getTemplate()),
        " (",
        ifNull(config.getTemplate().getPath(), "inline"),
        ")");
  }

  RenderState getState() {
    return state;
  }

  private Template getNestedTemplate(String name) throws RenderException {
    Check.notNull(name, "nestedTemplateName");
    Check.on(noSuchTemplate(config.getTemplate(), name), name).is(validTemplateName());
    return config.getTemplate().getNestedTemplate(name);
  }

  private Predicate<String> validTemplateName() {
    return s -> config.getTemplate().getNestedTemplateNames().contains(s);
  }

  private String stringify(Stringifier stringifier, String varName, Object value)
      throws RenderException {
    try {
      String s = stringifier.toString(value);
      if (s == null) {
        throw BadStringifierException.stringifierReturnedNull(config.getTemplate(), varName);
      }
      return s;
    } catch (NullPointerException e) {
      throw BadStringifierException.stringifierNotNullResistant(config.getTemplate(), varName);
    }
  }
}