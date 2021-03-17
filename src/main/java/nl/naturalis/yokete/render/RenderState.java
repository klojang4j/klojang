package nl.naturalis.yokete.render;

import java.util.*;
import nl.naturalis.yokete.template.Template;
import nl.naturalis.yokete.template.TemplateUtils;
import static nl.naturalis.common.CollectionMethods.initializedList;
import static nl.naturalis.yokete.render.RenderException.repetitionMismatch;

class RenderState {

  private final SessionFactory factory;
  private final Set<String> todo; // variables that have not been set yet
  private final IdentityHashMap<Template, List<RenderSession>> sessions;
  private final Map<Integer, String[]> varValues;

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

  List<RenderSession> createChildSessions(String tmplName, int amount) throws RenderException {
    Template nested = factory.getTemplate().getNestedTemplate(tmplName);
    return createChildSessions(nested, amount);
  }

  List<RenderSession> createChildSessions(Template nested, int amount) throws RenderException {
    List<RenderSession> children = sessions.get(nested);
    if (children == null) {
      children = initializedList(i -> factory.newChildSession(nested), amount);
      sessions.put(nested, children);
    } else if (children.size() != amount) {
      throw repetitionMismatch(factory.getTemplate(), children, amount);
    }
    return children;
  }

  Map<Template, List<RenderSession>> getChildSessions() {
    return sessions;
  }

  List<RenderSession> getChildSessions(Template template) {
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

  Set<String> getUnsetVars() {
    Set<String> names = new LinkedHashSet<>();
    collectUnsetVars(this, names);
    return names;
  }

  private static void collectUnsetVars(RenderState state0, Set<String> names) {
    state0
        .todo
        .stream()
        .map(n -> TemplateUtils.getFQName(state0.factory.getTemplate(), n))
        .forEach(names::add);
    state0
        .sessions
        .values()
        .stream()
        .flatMap(List::stream)
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
        .flatMap(List::stream)
        .map(RenderSession::getState)
        .allMatch(RenderState::ready);
  }
}
