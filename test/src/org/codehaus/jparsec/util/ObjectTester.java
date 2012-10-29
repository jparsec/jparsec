package org.codehaus.jparsec.util;

import junit.framework.Assert;

/**
 * Tests any {@link Object} for {@link Object#equals(Object)} and {@link Object#hashCode()}.
 * 
 * @author Ben Yu
 */
public final class ObjectTester {
  private static final class AnotherType {}
  
  public static void assertEqual(Object obj, Object... values) {
    for (Object value : values) {
      Assert.assertEquals(value, obj);
      Assert.assertEquals(obj.hashCode(), value.hashCode());
    }
  }
  
  public static void assertNotEqual(Object obj, Object... values) {
    Assert.assertFalse(obj.equals(new AnotherType()));
    Assert.assertFalse(obj.equals(null));
    for (Object value : values) {
      Assert.assertFalse(obj.equals(value));
    }
  }
}
