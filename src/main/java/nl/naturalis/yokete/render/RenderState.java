package nl.naturalis.yokete.render;

import java.util.*;
import nl.naturalis.yokete.template.Template;
import static nl.naturalis.common.CollectionMethods.initializedList;
import static nl.naturalis.yokete.render.RenderException.repetitionMismatch;

class RenderState {

  private final SessionFactory factory;
  private final Set<String> toDo; // variables that have not been set yet
  private final IdentityHashMap<Template, List<RenderSession>> sessions;
  private final Map<Integer, List<String>> varValues;

  RenderState(SessionFactory factory) {
    this.factory = factory;
    this.sessions = new IdentityHashMap<>(factory.getTemplate().countNestedTemplates());
    this.varValues = new HashMap<>(factory.getTemplate().countNestedTemplates());
    this.toDo = new HashSet<>(factory.getTemplate().getVars());
  }

  SessionFactory getSessionFactory() {
    return factory;
  }

  List<RenderSession> getOrCreateNestedSessions(String tmplName, int amount)
      throws RenderException {
    List<RenderSession> mySessions = sessions.get(factory.getTemplate());
    if (mySessions == null) {
      mySessions = initializedList(i -> factory.newChildSession(tmplName), amount);
    } else if (mySessions.size() != amount) {
      throw repetitionMismatch(factory.getTemplate().getName(), mySessions.size(), amount);
    }
    return mySessions;
  }

  List<RenderSession> getSessions(Template template) {
    return sessions.get(template);
  }

  List<String> getVar(int partIndex) {
    return varValues.get(partIndex);
  }

  void setVar(int partIndex, List<String> value) {
    varValues.put(partIndex, value);
  }

  boolean isSet(String var) {
    return !toDo.contains(var);
  }

  void done(String var) {
    toDo.remove(var);
  }

  Set<String> getUnsetVariables() {
    return toDo;
  }

  boolean isRenderable() {
    return toDo.isEmpty();
  }
}
