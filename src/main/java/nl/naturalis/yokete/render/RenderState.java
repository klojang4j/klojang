package nl.naturalis.yokete.render;

import java.util.*;
import nl.naturalis.yokete.template.Template;
import static nl.naturalis.yokete.render.RenderException.repetitionMismatch;
import static nl.naturalis.yokete.template.TemplateUtils.getFQName;

class RenderState {

  private static final RenderSession[] ZERO_SESSIONS = new RenderSession[0];
  private static final RenderSession[] ONE_SESSION = new RenderSession[1];
  private static final RenderSession[] TWO_SESSIONS = new RenderSession[2];

  private final SessionFactory factory;
  private final Set<String> todo; // variables that have not been set yet
  private final IdentityHashMap<Template, RenderSession[]> sessions;
  private final Map<Integer, String[]> varValues;

  private boolean frozen;

  RenderState(SessionFactory factory) {
    this.factory = factory;
    int sz = factory.getTemplate().countNestedTemplates();
    this.sessions = new IdentityHashMap<>(sz);
    this.varValues = new HashMap<>(sz);
    this.todo = new HashSet<>(factory.getTemplate().getVars());
  }

  SessionFactory getSessionFactory() {
    return factory;
  }

  RenderSession[] createChildSessions(String tmplName, List<?> data) throws RenderException {
    Template nested = factory.getTemplate().getNestedTemplate(tmplName);
    return createChildSessions(nested, data);
  }

  RenderSession[] createChildSessions(Template t, List<?> data) throws RenderException {
    if (t.getNames().isEmpty()) { // this is a text-only template
      return createChildSessions(t, data.size());
    }
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

  // Will only be called for text-only templates
  RenderSession[] createChildSessions(Template t, int repeats) throws RenderException {
    RenderSession[] children = sessions.get(t);
    if (children == null) {
      children = createChildSessions(repeats);
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

  String[] getVar(int partIndex) {
    return varValues.get(partIndex);
  }

  void setVar(int partIndex, String[] value) {
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
    this.frozen = true;
  }

  List<String> getUnsetVarsRecursive() {
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

  boolean isReady() {
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

  private static RenderSession[] createChildSessions(int repeats) {
    switch (repeats) {
      case 0:
        return ZERO_SESSIONS;
      case 1:
        return ONE_SESSION;
      case 2:
        return TWO_SESSIONS;
      default:
        return new RenderSession[repeats];
    }
  }
}
