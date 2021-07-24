package org.klojang.db.ps;

import org.junit.jupiter.api.Test;
import org.klojang.db.ps.ReceiverNegotiator;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ReceiverNegotiatorTest {

  @Test
  public void test00() {
    ReceiverNegotiator ng = ReceiverNegotiator.getInstance();
    assertNotNull(ng.getDefaultReceiver(System.Logger.Level.class));
  }
}
