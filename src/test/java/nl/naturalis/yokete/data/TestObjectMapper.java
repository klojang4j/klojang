package nl.naturalis.yokete.data;

import nl.naturalis.yokete.util.ResultSetMapper;

public class TestObjectMapper extends ResultSetMapper<TestObject> {

  TestObjectMapper() {
    super(TestObject::new, TestObject.class);
  }
}
