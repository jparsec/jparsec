package org.codehaus.jparsec;

import org.codehaus.jparsec.util.ObjectTester;

import junit.framework.TestCase;

/**
 * Unit test for {@link Token}.
 * 
 * @author Ben Yu
 */
public class TokenTest extends TestCase {
  
  public void testLength() {
    assertEquals(1, new Token(0, 1, null).length());
  }
  
  public void testIndex() {
    assertEquals(1, new Token(1, 2, null).index());
  }
  
  public void testValue() {
    assertEquals("value", new Token(1, 2, "value").value());
  }
  
  public void testToString() {
    assertEquals("value", new Token(1, 2, "value").toString());
    assertEquals("null", new Token(1, 2, null).toString());
  }
  
  public void testEquals() {
    ObjectTester.assertEqual(new Token(1, 2, "value"), new Token(1, 2, "value"));
    ObjectTester.assertEqual(new Token(1, 2, null), new Token(1, 2, null));
    ObjectTester.assertNotEqual(new Token(1, 2, "value"),
        new Token(2, 2, "value"), new Token(1, 3, "value"),
        new Token(1, 2, "value2"), new Token(1, 2, null));
  }
}
