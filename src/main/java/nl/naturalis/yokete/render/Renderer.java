package nl.naturalis.yokete.render;

import java.io.PrintStream;
import java.util.List;
import nl.naturalis.yokete.template.Part;
import nl.naturalis.yokete.template.NestedTemplatePart;
import nl.naturalis.yokete.template.TextPart;
import nl.naturalis.yokete.template.VariablePart;

class Renderer {

  private final RenderState state;

  Renderer(RenderState state) {
    this.state = state;
  }

  void render(PrintStream ps) {
    render(state, ps);
  }

  void render(StringBuilder sb) {
    render(state, sb);
  }

  StringBuilder render() {
    StringBuilder sb = new StringBuilder(1024);
    render(sb);
    return sb;
  }

  private static void render(RenderState state0, PrintStream ps) {
    List<Part> parts = state0.getSessionFactory().getTemplate().getParts();
    for (int i = 0; i < parts.size(); ++i) {
      Part part = parts.get(i);
      if (part.getClass() == TextPart.class) {
        TextPart tp = (TextPart) part;
        ps.append(tp.getText());
      } else if (part.getClass() == VariablePart.class) {
        state0.getVar(i).forEach(ps::append);
      } else /* TemplatePart */ {
        NestedTemplatePart ntp = (NestedTemplatePart) part;
        /*
         * For each of the render state's child sessions, render the
         * nested template using the child session's render state, thus
         * recursing down to the lowest-level templates.
         */
        state0
            .getChildSessions(ntp.getTemplate())
            .stream()
            .map(RenderSession::getState)
            .forEach(state -> render(state, ps));
      }
    }
  }

  private static void render(RenderState state0, StringBuilder sb) {
    List<Part> parts = state0.getSessionFactory().getTemplate().getParts();
    for (int i = 0; i < parts.size(); ++i) {
      Part part = parts.get(i);
      if (part.getClass() == TextPart.class) {
        TextPart tp = (TextPart) part;
        sb.append(tp.getText());
      } else if (part.getClass() == VariablePart.class) {
        state0.getVar(i).forEach(sb::append);
      } else /* TemplatePart */ {
        NestedTemplatePart tp = (NestedTemplatePart) part;
        /*
         * For each of the specified render state's child sessions, render
         * the nested template using the child session's state, thus recursing
         * down to the lowest-level templates
         */
        state0
            .getChildSessions(tp.getTemplate())
            .stream()
            .map(RenderSession::getState)
            .forEach(state -> render(state, sb));
      }
    }
  }
}
