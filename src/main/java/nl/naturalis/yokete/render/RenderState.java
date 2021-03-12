package nl.naturalis.yokete.render;

import java.util.*;
import nl.naturalis.yokete.template.Template;
import static nl.naturalis.common.CollectionMethods.initializedList;
import static nl.naturalis.yokete.render.RenderException.repetitionMismatch;

class RenderState {

  private final RenderSessionFactory rsf;
  private final Set<String> vToDo; // variables that have not been set yet
  private final IdentityHashMap<Template, List<RenderSession>> sessions;
  private final Map<Integer, List<String>> varValues;

  RenderState(RenderSessionFactory rsf) {
    this.rsf = rsf;
    this.sessions = new IdentityHashMap<>(rsf.getTemplate().countNestedTemplates());
    this.varValues = new HashMap<>(rsf.getTemplate().countNestedTemplates());
    this.vToDo = new HashSet<>(rsf.getTemplate().getVariableNames());
  }

  RenderSessionFactory getRenderUnit() {
    return rsf;
  }

  List<RenderSession> getOrCreateNestedSessions(String tmplName, int amount)
      throws RenderException {
    List<RenderSession> mySessions = sessions.get(rsf.getTemplate());
    if (mySessions == null) {
      mySessions = initializedList(i -> rsf.newChildSession(tmplName), amount);
    } else if (mySessions.size() != amount) {
      throw repetitionMismatch(rsf.getTemplate().getName(), mySessions.size(), amount);
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

  Set<String> getUnsetVariables() {
    return vToDo;
  }

  boolean isRenderable() {
    return vToDo.isEmpty();
  }
}
