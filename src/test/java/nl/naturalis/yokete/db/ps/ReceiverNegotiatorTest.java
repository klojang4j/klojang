package nl.naturalis.yokete.db.ps;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ReceiverNegotiatorTest {

  @Test
  public void test00() {
    ReceiverNegotiator ng = ReceiverNegotiator.getInstance();
    assertNotNull(ng.getDefaultReceiver(System.Logger.Level.class));
  }
}
