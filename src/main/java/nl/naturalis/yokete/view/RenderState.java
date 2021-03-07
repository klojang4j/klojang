package nl.naturalis.yokete.view;

import java.util.*;
import nl.naturalis.yokete.template.Template;
import static nl.naturalis.common.CollectionMethods.initializedList;
import static nl.naturalis.yokete.view.RenderException.repetitionMismatch;

class RenderState {

  private final Template template;
  private final Set<String> vToDo; // variables that have not been set yet
  private final Set<String> tToDo; // templates that have not been populated yet
  private final IdentityHashMap<Template, List<RenderSession>> sessions;
  private final Map<Integer, List<String>> varValues;

  RenderState(Template template) {
    this.template = template;
    this.sessions = new IdentityHashMap<>(template.countTemplates());
    this.varValues = new HashMap<>(template.countTemplates());
    this.vToDo = new HashSet<>(template.getVariableNames());
    this.tToDo = new HashSet<>(template.getTemplateNames());
  }

  Template getTemplate() {
    return template;
  }

  List<RenderSession> getOrCreateNestedSessions(Template template, int amount)
      throws RenderException {
    List<RenderSession> mySessions = sessions.get(template);
    if (mySessions == null) {
      mySessions = initializedList(new RenderSession(template), amount);
    } else if (mySessions.size() != amount) {
      throw repetitionMismatch(template.getName(), mySessions.size(), amount);
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
