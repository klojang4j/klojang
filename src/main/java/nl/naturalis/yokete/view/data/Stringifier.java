package nl.naturalis.yokete.view.data;

@FunctionalInterface
public interface Stringifier {

  public static final Stringifier BASIC = o -> o == null ? "" : o.toString();

  String stringify(Object value);
}
