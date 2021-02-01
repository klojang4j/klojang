package nl.naturalis.yokete.view;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import static nl.naturalis.yokete.view.Regex.*;

class TemplateParser {

  static final TemplateParser INSTANCE = new TemplateParser();

  private TemplateParser() {}

  List<Part> parse(String template) {
    template = removeComments(template);
    template = unhideVariables(template);
    template = unhideTemplates(template);
    LinkedList<Part> parts = new LinkedList<>();
    parseNestedTemplates(template, parts);
    parseVariables(parts);
    return new ArrayList<>(parts);
  }

  private static void parseNestedTemplates(String template, LinkedList<Part> parts) {
    Matcher matcher = REGEX_NESTED_TEMPLATE.matcher(template);
    int end = 0; // end index of the previous nested template
    while (matcher.find()) {
      int start = matcher.start();
      if (start > end) {
        String parseLater = template.substring(end, start);
        parts.add(new UnparsedPart(parseLater, end, start));
      }
      String name = matcher.group(1);
      String src = matcher.group(2);
      end = matcher.end();
      parts.add(new TemplatePart(name, Template.parse(src), start, end));
    }
    if (end < template.length()) {
      String parseLater = template.substring(end);
      parts.add(new UnparsedPart(parseLater, end, template.length()));
    }
  }

  private static void parseVariables(LinkedList<Part> parseResult) {
    for (int i = 0; i < parseResult.size(); ++i) {
      Part p = parseResult.get(i);
      if (p.getClass() == UnparsedPart.class) {
        List<Part> parts = parseVariables((UnparsedPart) p);
        parseResult.remove(i);
        parseResult.addAll(i, parts);
      }
    }
  }

  private static List<Part> parseVariables(UnparsedPart unparsed) {
    String contents = unparsed.getContents();
    int offset = unparsed.start();
    List<Part> parts = new ArrayList<>();
    Matcher matcher = REGEX_VARIABLE.matcher(contents);
    int end = 0;
    while (matcher.find()) {
      int start = matcher.start();
      if (start > end) {
        String text = contents.substring(end, start);
        parts.add(new TextPart(text, end + offset, start + offset));
      }
      EscapeType et = EscapeType.parse(matcher.group(2));
      String name = matcher.group(3);
      end = matcher.end();
      parts.add(new VariablePart(et, name, start + offset, end + offset));
    }
    if (end < contents.length()) {
      String text = contents.substring(end);
      parts.add(new TextPart(text, end + offset, contents.length() + offset));
    }
    return parts;
  }

  // Replaces "<!-- ~%myVar% -->" with "~%myVar%", after which there is no longer any difference
  // between "hidden" and "visible" variables, and they can all be processed further in the same
  // way.
  private static String unhideVariables(String template) {
    Matcher matcher = REGEX_HIDDEN_VAR.matcher(template);
    template = matcher.replaceAll(r -> r.group(1));
    return template;
  }

  private static String unhideTemplates(String template) {
    Matcher matcher = REGEX_HIDDEN_NESTED_TMPL.matcher(template);
    template = matcher.replaceAll(r -> r.group(1));
    return template;
  }

  private static String removeComments(String template) {
    template = REGEX_COMMENT_LINE.matcher(template).replaceAll("");
    template = REGEX_COMMENT_BLOCK.matcher(template).replaceAll("");
    return template;
  }
}
