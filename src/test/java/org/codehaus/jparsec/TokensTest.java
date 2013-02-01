package org.codehaus.jparsec;

import junit.framework.TestCase;

import org.codehaus.jparsec.Tokens.Fragment;
import org.codehaus.jparsec.Tokens.ScientificNotation;
import org.codehaus.jparsec.Tokens.Tag;
import org.codehaus.jparsec.util.ObjectTester;

/**
 * Unit test for {@link Tokens}.
 * 
 * @author Ben Yu
 */
public class TokensTest extends TestCase {
  
  public void testFragment() {
    Fragment fragment = Tokens.fragment("foo", 1);
    assertEquals("foo", fragment.toString());
    assertFragment(1, "foo", fragment);
    ObjectTester.assertEqual(fragment, fragment, Tokens.fragment("foo", 1));
    ObjectTester.assertNotEqual(fragment,
        Tokens.fragment("foo", 2), Tokens.fragment("foo", "1"), Tokens.fragment("bar", 1),
        "foo", 1);
  }
  
  public void testReserved() {
    assertFragment(Tag.RESERVED, "foo", Tokens.reserved("foo"));
  }
  
  public void testWord() {
    assertFragment(Tag.IDENTIFIER, "word", Tokens.identifier("word"));
  }
  
  public void testDecimal() {
    assertFragment(Tag.DECIMAL, "123", Tokens.decimalLiteral("123"));
  }
  
  public void testInteger() {
    assertFragment(Tag.INTEGER, "123", Tokens.integerLiteral("123"));
  }
  
  public void testScientificNumber() {
    ScientificNotation number = Tokens.scientificNotation("1.0", "2");
    assertEquals("1.0E2", number.toString());
    assertEquals("1.0", number.significand);
    assertEquals("2", number.exponent);
    ObjectTester.assertEqual(number, number, Tokens.scientificNotation("1.0", "2"));
    ObjectTester.assertNotEqual(number,
        Tokens.scientificNotation("1.0", "-2"), Tokens.scientificNotation("2", "2"));
  }
  
  private void assertFragment(Object tag, String text, Fragment fragment) {
    assertEquals(text, fragment.text());
    assertEquals(tag, fragment.tag());
  }
}
