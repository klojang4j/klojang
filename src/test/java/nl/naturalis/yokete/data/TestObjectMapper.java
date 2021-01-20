package nl.naturalis.yokete.data;

public class TestObjectMapper extends ResultSetMapper<TestObject> {

  TestObjectMapper() {
    super(TestObject::new, TestObject.class);
  }
}
