package org.codehaus.jparsec;

import org.codehaus.jparsec.Tokens.Fragment;
import org.codehaus.jparsec.Tokens.Tag;
import org.codehaus.jparsec.functors.Map;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Unit test for {@link TokenizerMaps}.
 * 
 * @author Ben Yu
 */
public class TokenizerMapsTest {

  @Test
  public void testFragment() {
    assertFragment("foo", TokenizerMaps.fragment("foo"));
  }

  @Test
  public void testReservedFragment() {
    assertFragment(Tag.RESERVED, TokenizerMaps.RESERVED_FRAGMENT);
  }

  @Test
  public void testIdentifierFragment() {
    assertFragment(Tag.IDENTIFIER, TokenizerMaps.IDENTIFIER_FRAGMENT);
  }

  @Test
  public void testIntegerFragment() {
    assertFragment(Tag.INTEGER, TokenizerMaps.INTEGER_FRAGMENT);
  }

  @Test
  public void testDecimalFragment() {
    assertFragment(Tag.DECIMAL, TokenizerMaps.DECIMAL_FRAGMENT);
  }

  @Test
  public void testSingleQuoteChar() {
    assertEquals("SINGLE_QUOTE_CHAR", TokenizerMaps.SINGLE_QUOTE_CHAR.toString());
    assertEquals(Character.valueOf('a'), TokenizerMaps.SINGLE_QUOTE_CHAR.map("'a'"));
    assertEquals(Character.valueOf('a'), TokenizerMaps.SINGLE_QUOTE_CHAR.map("'\\a'"));
    try {
      TokenizerMaps.SINGLE_QUOTE_CHAR.map("'abc'");
      fail();
    } catch (IllegalStateException e) {}
  }

  @Test
  public void testDecAsLong() {
    assertEquals("DEC_AS_LONG", TokenizerMaps.DEC_AS_LONG.toString());
    assertEquals(Long.valueOf(123L), TokenizerMaps.DEC_AS_LONG.map("123"));
  }

  @Test
  public void testOctAsLong() {
    assertEquals("OCT_AS_LONG", TokenizerMaps.OCT_AS_LONG.toString());
    assertEquals(Long.valueOf(10L), TokenizerMaps.OCT_AS_LONG.map("012"));
  }

  @Test
  public void testHexAsLong() {
    assertEquals("HEX_AS_LONG", TokenizerMaps.HEX_AS_LONG.toString());
    assertEquals(Long.valueOf(255L), TokenizerMaps.HEX_AS_LONG.map("0xff"));
  }

  @Test
  public void testDoubleQuoteString() {
    assertEquals("DOUBLE_QUOTE_STRING", TokenizerMaps.DOUBLE_QUOTE_STRING.toString());
    assertEquals("c:\\home", TokenizerMaps.DOUBLE_QUOTE_STRING.map("\"c:\\\\home\""));
  }

  @Test
  public void testSingleQuoteString() {
    assertEquals("SINGLE_QUOTE_STRING", TokenizerMaps.SINGLE_QUOTE_STRING.toString());
    assertEquals("'a'", TokenizerMaps.SINGLE_QUOTE_STRING.map("'''a'''"));
  }

  @Test
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
