package org.codehaus.jparsec.internal.util;

import org.codehaus.jparsec.internal.util.Strings;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Unit test for {@link Strings}.
 * 
 * @author Ben Yu
 */
public class StringsTest {

  @Test
  public void testJoin() {
    assertEquals("", Strings.join(", ", new Object[0]));
    assertEquals("1", Strings.join(", ", new Object[]{1}));
    assertEquals("1, 2", Strings.join(", ", new Object[]{1, 2}));
  }

  @Test
  public void testJoin_withStringBuilder() {
    assertEquals("", Strings.join(new StringBuilder(), ", ", new Object[0]).toString());
    assertEquals("1", Strings.join(new StringBuilder(), ", ", new Object[]{1}).toString());
    assertEquals("1, 2", Strings.join(new StringBuilder(), ", ", new Object[]{1, 2}).toString());
  }
}
