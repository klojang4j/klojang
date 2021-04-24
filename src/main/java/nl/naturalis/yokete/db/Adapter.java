package nl.naturalis.yokete.db;

@FunctionalInterface
interface Adapter<COLUMN_TYPE, TARGET_TYPE> {

  TARGET_TYPE adapt(COLUMN_TYPE columnValue, Class<TARGET_TYPE> targetType);
}
