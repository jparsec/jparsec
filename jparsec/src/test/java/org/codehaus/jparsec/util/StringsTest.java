package org.codehaus.jparsec.util;

import junit.framework.TestCase;

/**
 * Unit test for {@link Strings}.
 * 
 * @author Ben Yu
 */
public final class StringsTest extends TestCase {
  
  public void testJoin() {
    assertEquals("", Strings.join(", ", new Object[0]));
    assertEquals("1", Strings.join(", ", new Object[]{1}));
    assertEquals("1, 2", Strings.join(", ", new Object[]{1, 2}));
  }
  
  public void testJoin_withStringBuilder() {
    assertEquals("", Strings.join(new StringBuilder(), ", ", new Object[0]).toString());
    assertEquals("1", Strings.join(new StringBuilder(), ", ", new Object[]{1}).toString());
    assertEquals("1, 2", Strings.join(new StringBuilder(), ", ", new Object[]{1, 2}).toString());
  }
}
