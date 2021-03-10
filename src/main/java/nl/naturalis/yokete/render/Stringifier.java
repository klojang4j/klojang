package nl.naturalis.yokete.render;

@FunctionalInterface
public interface Stringifier {

  String stringify(String varName, Object value);
}
