package org.codehaus.jparsec.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * Tests any {@link Object} for {@link Object#equals(Object)} and {@link Object#hashCode()}.
 * 
 * @author Ben Yu
 */
public final class ObjectTester {
  private static final class AnotherType {}
  
  public static void assertEqual(Object obj, Object... values) {
    for (Object value : values) {
      assertEquals(value, obj);
      assertEquals(obj.hashCode(), value.hashCode());
    }
  }
  
  public static void assertNotEqual(Object obj, Object... values) {
    assertFalse(obj.equals(new AnotherType()));
    assertFalse(obj.equals(null));
    for (Object value : values) {
      assertFalse(obj.equals(value));
    }
  }
}
