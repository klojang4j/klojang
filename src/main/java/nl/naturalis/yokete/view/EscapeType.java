package nl.naturalis.yokete.view;

/**
 * Symbolic constants for various text-escaping methods.
 *
 * @author Ayco Holleman
 */
public enum EscapeType {
  /**
   * Do not apply any escaping. This is the {@code EscapeType} when a template variable is to be
   * substituted with an entire block of already properly escaped HTML, Javascript or HTML. This can
   * also be specified within the template variable itself. For example: <code>~%text:tableRows%
   * </code>
   */
  ESCAPE_NONE("text"),
  /**
   * The type of escaping to be used for template variables inserted into HTML tags. For example:
   *
   * <p>
   *
   * <pre>
   * &lt;tr&gt;&lt;td&gt;~%fullName%&lt;/td&gt;&lt;/tr&gt;
   * </pre>
   *
   * <p>This can also be specified within the template variable itself:
   *
   * <pre>
   * &lt;tr&gt;&lt;td&gt;~%html:fullName%&lt;/td&gt;&lt;/tr&gt;
   * </pre>
   */
  ESCAPE_HTML("html"),
  /**
   * The type of escaping to be used for template variables inserted into Javascript. For example:
   *
   * <p>
   *
   * <pre>
   * &lt;script&gt;
   *  var fullName = "~%fullName%";
   * &lt;/script&gt;
   * </pre>
   *
   * <p>This can also be specified within the template variable itself:
   *
   * <pre>
   * &lt;script&gt;
   *  var fullName = '~%js:fullName%';
   * &lt;/script&gt;
   * </pre>
   */
  ESCAPE_JS("js");

  /**
   * Parses the specified string into an {@code EscapeType}. Valid values are "text", "html" and
   * "js", corresponding to the available variable type prefixes (e.g. ~%html:fullName%). The string
   * is also allowed to be null, in which case this method returns null. This corresponds to a
   * variable without a type prefix (~%fullName%).
   *
   * @param s The string to parse
   * @return The {@code EscapeType} corresponding to the string
   */
  public static EscapeType parse(String s) {
    if (s == null) {
      return null;
    }
    switch (s) {
      case "text":
        return ESCAPE_NONE;
      case "html":
        return ESCAPE_HTML;
      case "js":
        return ESCAPE_JS;
    }
    throw new IllegalArgumentException("No such escape type: \"" + s + "\"");
  }

  private final String prefix;

  private EscapeType(String prefix) {
    this.prefix = prefix;
  }

  /**
   * Returns the equivalent prefix for template variables.
   *
   * @return The equivalent prefix for template variables
   */
  public String getPrefix() {
    return prefix;
  }
}
