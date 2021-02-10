package nl.naturalis.yokete.view;

import java.util.*;
import java.util.regex.Matcher;
import static nl.naturalis.yokete.view.InvalidTemplateException.duplicateTemplateName;
import static nl.naturalis.yokete.view.InvalidTemplateException.duplicateVarName;
import static nl.naturalis.yokete.view.Regex.*;

class TemplateParser {

  static final TemplateParser INSTANCE = new TemplateParser();

  private TemplateParser() {}

  List<Part> parse(String template) throws InvalidTemplateException {
    template = removeComments(template);
    template = unhideVariables(template);
    template = unhideTemplates(template);
    LinkedList<Part> parts = new LinkedList<>();
    Set<String> tmplNames = parseNestedTemplates(template, parts);
    parseVariables(parts, tmplNames);
    return List.copyOf(new ArrayList<>(parts));
  }

  private static Set<String> parseNestedTemplates(String template, LinkedList<Part> parts)
      throws InvalidTemplateException {
    Matcher matcher = REGEX_NESTED_TEMPLATE.matcher(template);
    Set<String> names = new HashSet<>();
    int end = 0; // end index of the previous nested template
    while (matcher.find()) {
      int start = matcher.start();
      if (start > end) {
        String parseLater = template.substring(end, start);
        parts.add(new UnparsedPart(parseLater, end, start));
      }
      String name = matcher.group(1);
      if (names.contains(name)) {
        throw duplicateTemplateName(name);
      }
      names.add(name);
      String src = matcher.group(2);
      end = matcher.end();
      parts.add(new TemplatePart(name, Template.parse(src), start, end));
    }
    if (end < template.length()) {
      String parseLater = template.substring(end);
      parts.add(new UnparsedPart(parseLater, end, template.length()));
    }
    return names;
  }

  private static void parseVariables(LinkedList<Part> parseResult, Set<String> tmplNames)
      throws InvalidTemplateException {
    for (int i = 0; i < parseResult.size(); ++i) {
      Part p = parseResult.get(i);
      if (p.getClass() == UnparsedPart.class) {
        // Divide up the part into variable parts and text parts
        List<Part> parts = parseVariables((UnparsedPart) p, tmplNames);
        parseResult.remove(i);
        parseResult.addAll(i, parts);
      }
    }
  }

  private static List<Part> parseVariables(UnparsedPart unparsed, Set<String> tmplNames)
      throws InvalidTemplateException {
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
      if (tmplNames.contains(name)) {
        throw duplicateVarName(name);
      }
      end = matcher.end();
      parts.add(new VariablePart(et, name, start + offset, end + offset));
    }
    if (end < contents.length()) {
      String text = contents.substring(end);
      parts.add(new TextPart(text, end + offset, contents.length() + offset));
    }
    return parts;
  }

  // Replaces "<!-- ~%myVar% -->" with "~%myVar%", after which there is no  difference any longer
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
