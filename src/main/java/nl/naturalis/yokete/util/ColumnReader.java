package nl.naturalis.yokete.util;

import java.lang.invoke.MethodHandle;

class ColumnReader {

  private final MethodHandle method;

  // If this is corresponds to ResultSet.getObject(int, Class), then
  // classArgument will be the Class object passed in as the second
  // argument to getObject. In anyother case classArgument will be
  // null.
  private final Class<?> classArgument;

  ColumnReader(MethodHandle method) {
    this(method, null);
  }

  ColumnReader(MethodHandle method, Class<?> classArgument) {
    this.method = method;
    this.classArgument = classArgument;
  }

  MethodHandle getMethod() {
    return method;
  }

  Class<?> getClassArgument() {
    return classArgument;
  }
}
