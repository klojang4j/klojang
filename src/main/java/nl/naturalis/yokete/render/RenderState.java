package nl.naturalis.yokete.render;

import java.util.*;
import nl.naturalis.yokete.template.Template;
import static nl.naturalis.common.CollectionMethods.initializedList;
import static nl.naturalis.yokete.render.RenderException.repetitionMismatch;

class RenderState {

  private final RenderUnit ru;
  private final Set<String> vToDo; // variables that have not been set yet
  private final Set<String> tToDo; // templates that have not been populated yet
  private final IdentityHashMap<Template, List<RenderSession>> sessions;
  private final Map<Integer, List<String>> varValues;

  RenderState(RenderUnit ru) {
    this.ru = ru;
    this.sessions = new IdentityHashMap<>(ru.getTemplate().countNestedTemplates());
    this.varValues = new HashMap<>(ru.getTemplate().countNestedTemplates());
    this.vToDo = new HashSet<>(ru.getTemplate().getVariableNames());
    this.tToDo = new HashSet<>(ru.getTemplate().getNestedTemplateNames());
  }

  RenderUnit getRenderUnit() {
    return ru;
  }

  List<RenderSession> getOrCreateNestedSessions(RenderUnit ru, int amount) throws RenderException {
    List<RenderSession> mySessions = sessions.get(ru.getTemplate());
    if (mySessions == null) {
      mySessions = initializedList(new RenderSession(ru), amount);
    } else if (mySessions.size() != amount) {
      throw repetitionMismatch(ru.getTemplate().getName(), mySessions.size(), amount);
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
    return !vToDo.contains(var);
  }

  void done(String var) {
    vToDo.remove(var);
  }

  boolean isPopulated(String tmplName) {
    return !tToDo.contains(tmplName);
  }

  void populated(String tmpl) {
    tToDo.remove(tmpl);
  }

  Set<String> getUnsetVariables() {
    return vToDo;
  }

  Set<String> getUnpopulatedTemplates() {
    return tToDo;
  }

  boolean isRenderable() {
    return vToDo.isEmpty() && tToDo.isEmpty();
  }
}
