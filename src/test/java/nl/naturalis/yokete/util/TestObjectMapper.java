package nl.naturalis.yokete.util;

import nl.naturalis.yokete.util.ResultSetMapper;

public class TestObjectMapper extends ResultSetMapper<TestObject> {

  TestObjectMapper() {
    super(TestObject::new, TestObject.class);
  }
}
