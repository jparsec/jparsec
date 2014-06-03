package org.codehaus.jparsec;

import org.codehaus.jparsec.util.ObjectTester;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Unit test for {@link Token}.
 * 
 * @author Ben Yu
 */
public class TokenTest {

  @Test
  public void testLength() {
    assertEquals(1, new Token(0, 1, null).length());
  }

  @Test
  public void testIndex() {
    assertEquals(1, new Token(1, 2, null).index());
  }

  @Test
  public void testValue() {
    assertEquals("value", new Token(1, 2, "value").value());
  }

  @Test
  public void testToString() {
    assertEquals("value", new Token(1, 2, "value").toString());
    assertEquals("null", new Token(1, 2, null).toString());
  }

  @Test
  public void testEquals() {
    ObjectTester.assertEqual(new Token(1, 2, "value"), new Token(1, 2, "value"));
    ObjectTester.assertEqual(new Token(1, 2, null), new Token(1, 2, null));
    ObjectTester.assertNotEqual(new Token(1, 2, "value"),
        new Token(2, 2, "value"), new Token(1, 3, "value"),
        new Token(1, 2, "value2"), new Token(1, 2, null));
  }

}
