package nl.naturalis.yokete.render;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;
import nl.naturalis.yokete.template.*;
import static java.util.Arrays.stream;

class Renderer {

  private final RenderState state;

  Renderer(RenderState state) {
    this.state = state;
  }

  void render(OutputStream out) {
    PrintStream ps = out instanceof PrintStream ? (PrintStream) out : new PrintStream(out);
    render(state, ps);
  }

  void render(StringBuilder sb) {
    render(state, sb);
  }

  private static void render(RenderState state0, PrintStream ps) {
    List<Part> parts = state0.getSessionFactory().getTemplate().getParts();
    for (int i = 0; i < parts.size(); ++i) {
      Part part = parts.get(i);
      if (part.getClass() == TextPart.class) {
        TextPart tp = (TextPart) part;
        ps.append(tp.getText());
      } else if (part.getClass() == VariablePart.class) {
        if (state0.getVar(i) != null) {
          Arrays.stream(state0.getVar(i)).forEach(ps::append);
        }
      } else /* TemplatePart */ {
        NestedTemplatePart ntp = (NestedTemplatePart) part;
        RenderSession[] sessions = state0.getChildSessions(ntp.getTemplate());
        if (sessions != null) {
          Template t = ntp.getTemplate();
          if (t.isTextOnly()) {
            // The RenderSession[] array will contain only null values
            // and we just want to know its length to determine the
            // number of repetitions
            String text = ((TextPart) t.getParts().get(0)).getText();
            IntStream.range(0, sessions.length).forEach(x -> ps.append(text));
          } else {
            stream(sessions).map(RenderSession::getState).forEach(s -> render(s, ps));
          }
        }
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
        if (state0.getVar(i) != null) {
          Arrays.stream(state0.getVar(i)).forEach(sb::append);
        }
      } else /* TemplatePart */ {
        NestedTemplatePart ntp = (NestedTemplatePart) part;
        RenderSession[] sessions = state0.getChildSessions(ntp.getTemplate());
        if (sessions != null) {
          Template t = ntp.getTemplate();
          if (t.isTextOnly()) {
            String text = ((TextPart) t.getParts().get(0)).getText();
            IntStream.range(0, sessions.length).forEach(x -> sb.append(text));
          } else {
            stream(sessions).map(RenderSession::getState).forEach(s -> render(s, sb));
          }
        }
      }
    }
  }
}
