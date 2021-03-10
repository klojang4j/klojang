package nl.naturalis.yokete.render;

import java.io.PrintStream;
import java.util.List;
import nl.naturalis.yokete.template.Part;
import nl.naturalis.yokete.template.NestedTemplatePart;
import nl.naturalis.yokete.template.TextPart;
import nl.naturalis.yokete.template.VariablePart;

/** @author Ayco Holleman */
class Renderer {

  private final RenderState state;

  Renderer(RenderState state) {
    this.state = state;
  }

  void render(PrintStream ps) {
    List<Part> parts = state.getRenderUnit().getTemplate().getParts();
    for (int i = 0; i < parts.size(); ++i) {
      Part part = parts.get(i);
      if (part.getClass() == TextPart.class) {
        TextPart tp = (TextPart) part;
        ps.append(tp.getText());
      } else if (part.getClass() == VariablePart.class) {
        state.getVar(i).forEach(ps::append);
      } else /* TemplatePart */ {
        NestedTemplatePart tp = (NestedTemplatePart) part;
        state.getSessions(tp.getTemplate()).forEach(s -> render(ps));
      }
    }
  }

  void render(StringBuilder sb) {
    List<Part> parts = state.getRenderUnit().getTemplate().getParts();
    for (int i = 0; i < parts.size(); ++i) {
      Part part = parts.get(i);
      if (part.getClass() == TextPart.class) {
        TextPart tp = (TextPart) part;
        sb.append(tp.getText());
      } else if (part.getClass() == VariablePart.class) {
        state.getVar(i).forEach(sb::append);
      } else /* TemplatePart */ {
        NestedTemplatePart tp = (NestedTemplatePart) part;
        state.getSessions(tp.getTemplate()).forEach(s -> render(sb));
      }
    }
  }

  StringBuilder render() {
    StringBuilder sb = new StringBuilder(1024);
    render(sb);
    return sb;
  }
}
