package nl.naturalis.yokete.view;

@FunctionalInterface
public interface Stringifier {

  String stringify(String varName, Object value);
}
