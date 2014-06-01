package org.codehaus.jparsec;

import junit.framework.TestCase;

import org.codehaus.jparsec.Tokens.Fragment;
import org.codehaus.jparsec.Tokens.Tag;
import org.codehaus.jparsec.functors.Map;

/**
 * Unit test for {@link TokenizerMaps}.
 * 
 * @author Ben Yu
 */
public class TokenizerMapsTest extends TestCase {
  
  public void testFragment() {
    assertFragment("foo", TokenizerMaps.fragment("foo"));
  }
  
  public void testReservedFragment() {
    assertFragment(Tag.RESERVED, TokenizerMaps.RESERVED_FRAGMENT);
  }
  
  public void testIdentifierFragment() {
    assertFragment(Tag.IDENTIFIER, TokenizerMaps.IDENTIFIER_FRAGMENT);
  }
  
  public void testIntegerFragment() {
    assertFragment(Tag.INTEGER, TokenizerMaps.INTEGER_FRAGMENT);
  }
  
  public void testDecimalFragment() {
    assertFragment(Tag.DECIMAL, TokenizerMaps.DECIMAL_FRAGMENT);
  }
  
  public void testSingleQuoteChar() {
    assertEquals("SINGLE_QUOTE_CHAR", TokenizerMaps.SINGLE_QUOTE_CHAR.toString());
    assertEquals(Character.valueOf('a'), TokenizerMaps.SINGLE_QUOTE_CHAR.map("'a'"));
    assertEquals(Character.valueOf('a'), TokenizerMaps.SINGLE_QUOTE_CHAR.map("'\\a'"));
    try {
      TokenizerMaps.SINGLE_QUOTE_CHAR.map("'abc'");
      fail();
    } catch (IllegalStateException e) {}
  }
  
  public void testDecAsLong() {
    assertEquals("DEC_AS_LONG", TokenizerMaps.DEC_AS_LONG.toString());
    assertEquals(Long.valueOf(123L), TokenizerMaps.DEC_AS_LONG.map("123"));
  }
  
  public void testOctAsLong() {
    assertEquals("OCT_AS_LONG", TokenizerMaps.OCT_AS_LONG.toString());
    assertEquals(Long.valueOf(10L), TokenizerMaps.OCT_AS_LONG.map("012"));
  }
  
  public void testHexAsLong() {
    assertEquals("HEX_AS_LONG", TokenizerMaps.HEX_AS_LONG.toString());
    assertEquals(Long.valueOf(255L), TokenizerMaps.HEX_AS_LONG.map("0xff"));
  }
  
  public void testDoubleQuoteString() {
    assertEquals("DOUBLE_QUOTE_STRING", TokenizerMaps.DOUBLE_QUOTE_STRING.toString());
    assertEquals("c:\\home", TokenizerMaps.DOUBLE_QUOTE_STRING.map("\"c:\\\\home\""));
  }
  
  public void testSingleQuoteString() {
    assertEquals("SINGLE_QUOTE_STRING", TokenizerMaps.SINGLE_QUOTE_STRING.toString());
    assertEquals("'a'", TokenizerMaps.SINGLE_QUOTE_STRING.map("'''a'''"));
  }
  
  public void testScientificNotation() {
    assertEquals("SCIENTIFIC_NOTATION", TokenizerMaps.SCIENTIFIC_NOTATION.toString());
    assertEquals(new Tokens.ScientificNotation("1", "2"),
        TokenizerMaps.SCIENTIFIC_NOTATION.map("1e2"));
    assertEquals(new Tokens.ScientificNotation("1", "2"),
        TokenizerMaps.SCIENTIFIC_NOTATION.map("1e+2"));
    assertEquals(new Tokens.ScientificNotation("1", "-2"),
        TokenizerMaps.SCIENTIFIC_NOTATION.map("1e-2"));
    assertEquals(new Tokens.ScientificNotation("1.2", "30"),
        TokenizerMaps.SCIENTIFIC_NOTATION.map("1.2E30"));
    assertEquals(new Tokens.ScientificNotation("0", "0"),
        TokenizerMaps.SCIENTIFIC_NOTATION.map("0E0"));
  }

  private void assertFragment(Object tag, Map<String, Fragment> map) {
    Fragment fragment = map.map("foo");
    assertEquals(tag, fragment.tag());
    assertEquals("foo", fragment.text());
    assertEquals(tag.toString(), map.toString());
  }
}
