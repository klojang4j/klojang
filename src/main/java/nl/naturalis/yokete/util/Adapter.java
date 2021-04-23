package nl.naturalis.yokete.util;

@FunctionalInterface
interface Adapter<T, R> {

  R adapt(T resultSetValue, Class<R> targetType, ResultSetReaderConfig cfg);
}
