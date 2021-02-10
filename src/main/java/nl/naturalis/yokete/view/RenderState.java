package nl.naturalis.yokete.view;

import java.util.*;
import static nl.naturalis.common.CollectionMethods.initializedList;
import static nl.naturalis.yokete.view.RenderException.templateRepetitionMismatch;

class RenderState {

  private final Template template;

  /**
   * Maps each nested template to a list of (nested) renderers. The size of the list determines how
   * often the the template is going to be repeated inside the parent template.
   */
  private final IdentityHashMap<Template, List<Renderer>> renderers;

  /**
   * A sparsely populated list containing the values of the template variables. Each populated
   * element in the list corresponds to a variable part. The unpopulated elements correspond to text
   * parts or template parts.
   */
  private final List<String> varValues;

  private final Set<String> unsetVars; // variables which have not been set yet
  private final Set<String> unsetTemplates; // templates which have not been populated yet

  RenderState(Template template) {
    this.template = template;
    this.renderers = new IdentityHashMap<>(template.countNestedTemplates());
    List<Part> parts = template.getParts();
    this.varValues = initializedList(String.class, parts.size());
    this.unsetVars = new HashSet<>(template.getVariableNames());
    this.unsetTemplates = new HashSet<>(template.getNestedTemplateNames());
  }

  List<Renderer> getRenderersForTemplate(Template template, String tmplName, int amount)
      throws RenderException {
    List<Renderer> myRenderers = renderers.get(template);
    if (myRenderers == null) {
      myRenderers = new ArrayList<>(amount);
    } else if (myRenderers.size() != amount) {
      throw templateRepetitionMismatch(tmplName, myRenderers.size(), amount);
    }
    return myRenderers;
  }

  void setVar(int partIndex, String value) {
    varValues.set(partIndex, value);
  }

  boolean isVariableSet(String var) {
    return !unsetVars.contains(var);
  }

  void doneWithVar(String var) {
    unsetVars.remove(var);
  }

  boolean isTemplatePopulated(String tmpl) {
    return !unsetTemplates.contains(tmpl);
  }

  void templateNowPopulated(String tmpl) {
    unsetTemplates.remove(tmpl);
  }
}
