package nl.naturalis.yokete.render;

import java.util.*;
import nl.naturalis.common.check.Check;
import nl.naturalis.yokete.template.Template;
import static nl.naturalis.common.check.CommonChecks.containingKey;
import static nl.naturalis.common.check.CommonChecks.notNull;
import static nl.naturalis.yokete.render.RenderException.childSessionsAlreadyCreated;
import static nl.naturalis.yokete.render.RenderException.repetitionMismatch;
import static nl.naturalis.yokete.template.TemplateUtils.getFQName;

class RenderState {

  private static final RenderSession[] ZERO_SESSIONS = new RenderSession[0];
  private static final RenderSession[] ONE_SESSION = new RenderSession[1];

  private final Page factory;
  private final Set<String> todo; // variables that have not been set yet
  private final Map<Template, RenderSession[]> sessions;
  private final Map<Integer, Object> varValues;

  private boolean frozen;

  RenderState(Page factory) {
    this.factory = factory;
    int sz = factory.getTemplate().countNestedTemplates();
    this.sessions = new IdentityHashMap<>(sz);
    this.varValues = new HashMap<>(sz);
    this.todo = new HashSet<>(factory.getTemplate().getVars());
  }

  Page getSessionFactory() {
    return factory;
  }

  RenderSession[] getOrCreateChildSessions(String tmplName, List<?> data) throws RenderException {
    Template nested = factory.getTemplate().getNestedTemplate(tmplName);
    return getOrCreateChildSessions(nested, data);
  }

  RenderSession[] getOrCreateChildSessions(Template t, List<?> data) throws RenderException {
    RenderSession[] children = sessions.get(t);
    if (children == null) {
      if (data.isEmpty()) {
        children = ZERO_SESSIONS;
      } else {
        children = new RenderSession[data.size()];
        for (int i = 0; i < data.size(); ++i) {
          children[i] = factory.newChildSession(t, data.get(i));
        }
      }
      sessions.put(t, children);
    } else if (children.length != data.size()) {
      throw repetitionMismatch(factory.getTemplate(), children, data.size());
    }
    return children;
  }

  RenderSession[] getOrCreateChildSessions(Template t, Accessor<?> acc, int repeats)
      throws RenderException {
    if (t.isTextOnly()) {
      return getOrCreateTextOnlyChildSessions(t, repeats);
    }
    RenderSession[] children = sessions.get(t);
    if (children == null) {
      if (repeats == 0) {
        children = ZERO_SESSIONS;
      } else {
        children = new RenderSession[repeats];
        for (int i = 0; i < repeats; ++i) {
          children[i] = factory.newChildSession(t, acc);
        }
      }
      sessions.put(t, children);
    } else if (children.length != repeats) {
      throw repetitionMismatch(factory.getTemplate(), children, repeats);
    }
    return children;
  }

  RenderSession[] createChildSessions(Template t, Accessor<?> acc, int repeats)
      throws RenderException {
    Check.on(childSessionsAlreadyCreated(t), sessions).isNot(containingKey(), t);
    return getOrCreateChildSessions(t, acc, repeats);
  }

  RenderSession[] getOrCreateTextOnlyChildSessions(Template t, int repeats) throws RenderException {
    // The RenderSession[] array will never contain any actual RenderSession
    // instances for a text-only template. Only its length matters to the
    // Renderer as it determines how often the template is to be repeated.
    RenderSession[] children = sessions.get(t);
    if (children == null) {
      children = createTextOnlySessions(repeats);
      sessions.put(t, children);
      return children;
    }
    throw RenderException.multiPassNotAllowed(t);
  }

  Map<Template, RenderSession[]> getChildSessions() {
    return sessions;
  }

  RenderSession[] getChildSessions(Template template) {
    return sessions.get(template);
  }

  Object getVar(int partIndex) {
    return varValues.get(partIndex);
  }

  void setVar(int partIndex, String[] value) {
    varValues.put(partIndex, value);
  }

  void setVar(int partIndex, Renderable value) {
    varValues.put(partIndex, value);
  }

  boolean isSet(String var) {
    return !todo.contains(var);
  }

  void done(String var) {
    todo.remove(var);
  }

  boolean isFrozen() {
    return frozen;
  }

  void freeze() {
    deepFreeze(this);
  }

  private static void deepFreeze(RenderState state0) {
    state0.frozen = true;
    state0
        .sessions
        .values()
        .stream()
        .flatMap(Arrays::stream)
        .filter(notNull()) // text-only null sessions - don't need freezing anyhow
        .map(RenderSession::getState)
        .forEach(RenderState::deepFreeze);
  }

  List<String> getUnsetCars() {
    ArrayList<String> names = new ArrayList<>();
    collectUnsetVars(this, names);
    return names;
  }

  private static void collectUnsetVars(RenderState state0, ArrayList<String> names) {
    Template t = state0.factory.getTemplate();
    state0.todo.stream().map(var -> getFQName(t, var)).forEach(names::add);
    state0
        .sessions
        .values()
        .stream()
        .flatMap(Arrays::stream)
        .map(RenderSession::getState)
        .forEach(state -> collectUnsetVars(state, names));
  }

  boolean isFullyPopulated() {
    return ready(this);
  }

  private static boolean ready(RenderState state0) {
    if (state0.todo.size() > 0) {
      return false;
    }
    return state0
        .sessions
        .values()
        .stream()
        .flatMap(Arrays::stream)
        .map(RenderSession::getState)
        .allMatch(RenderState::ready);
  }

  private static RenderSession[] createTextOnlySessions(int repeats) {
    switch (repeats) {
      case 0:
        return ZERO_SESSIONS;
      case 1:
        return ONE_SESSION;
      default:
        return new RenderSession[repeats];
    }
  }
}
