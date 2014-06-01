package org.codehaus.jparsec.util;

import junit.framework.TestCase;

/**
 * Unit test for {@link Objects}.
 * 
 * @author Ben Yu
 */
public class ObjectsTest extends TestCase {
  
  public void testEquals() {
    assertTrue(Objects.equals(null, null));
    assertFalse(Objects.equals(null, ""));
    assertFalse(Objects.equals("", null));
    assertTrue(Objects.equals("", ""));
  }
  
  public void testHashCode() {
    assertEquals(0, Objects.hashCode(null));
    assertEquals("".hashCode(), Objects.hashCode(""));
  }
  
  public void testIn() {
    assertTrue(Objects.in("b", "a", "b", "c"));
    assertFalse(Objects.in("x", "a", "b", "c"));
  }
}
