package org.codehaus.jparsec.internal.util;

import org.codehaus.jparsec.internal.util.Objects;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Unit test for {@link Objects}.
 * 
 * @author Ben Yu
 */
public class ObjectsTest {

  @Test
  public void testEquals() {
    assertTrue(Objects.equals(null, null));
    assertFalse(Objects.equals(null, ""));
    assertFalse(Objects.equals("", null));
    assertTrue(Objects.equals("", ""));
  }

  @Test
  public void testHashCode() {
    assertEquals(0, Objects.hashCode(null));
    assertEquals("".hashCode(), Objects.hashCode(""));
  }

  @Test
  public void testIn() {
    assertTrue(Objects.in("b", "a", "b", "c"));
    assertFalse(Objects.in("x", "a", "b", "c"));
  }

}
