package nl.naturalis.yokete.view;

import java.io.PrintStream;
import java.util.List;

/** @author Ayco Holleman */
class Renderer {

  private final RenderState state;

  Renderer(RenderState state) {
    this.state = state;
  }

  void render(PrintStream ps) {
    List<Part> parts = state.getTemplate().getParts();
    for (int i = 0; i < parts.size(); ++i) {
      Part part = parts.get(i);
      if (part.getClass() == TextPart.class) {
        TextPart tp = (TextPart) part;
        ps.append(tp.getText());
      } else if (part.getClass() == VariablePart.class) {
        state.getVar(i).forEach(ps::append);
      } else /* TemplatePart */ {
        TemplatePart tp = (TemplatePart) part;
        state.getSessions(tp.getTemplate()).forEach(s -> render(ps));
      }
    }
  }

  void render(StringBuilder sb) {
    List<Part> parts = state.getTemplate().getParts();
    for (int i = 0; i < parts.size(); ++i) {
      Part part = parts.get(i);
      if (part.getClass() == TextPart.class) {
        TextPart tp = (TextPart) part;
        sb.append(tp.getText());
      } else if (part.getClass() == VariablePart.class) {
        state.getVar(i).forEach(sb::append);
      } else /* TemplatePart */ {
        TemplatePart tp = (TemplatePart) part;
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
