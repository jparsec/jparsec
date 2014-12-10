package org.codehaus.jparsec;

import static java.util.Arrays.asList;
import static org.codehaus.jparsec.Asserts.assertParser;
import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

/**
 * Unit test for {@link Keywords}.
 * 
 * @author Ben Yu
 */
public class KeywordsTest {

  @Test
  public void testLexicon_caseSensitive() {
    List<String> keywords = asList("foo", "Bar");
    Lexicon lexicon = Keywords.lexicon(
        Scanners.IDENTIFIER, keywords, StringCase.CASE_SENSITIVE, TokenizerMaps.IDENTIFIER_FRAGMENT);
    for (String keyword : keywords) {
      assertEquals(Tokens.reserved(keyword), lexicon.word(keyword));
    }
    for (String keyword : keywords) {
      assertParser(lexicon.tokenizer, keyword, Tokens.reserved(keyword));
    }
    assertParser(lexicon.tokenizer, "FOO", Tokens.identifier("FOO"));
    assertParser(lexicon.tokenizer, "baz", Tokens.identifier("baz"));
  }

  @Test
  public void testLexicon_caseInsensitive() {
    List<String> keywords = asList("foo", "Bar");
    Lexicon lexicon = Keywords.lexicon(
        Scanners.IDENTIFIER, keywords, StringCase.CASE_INSENSITIVE, TokenizerMaps.IDENTIFIER_FRAGMENT);
    for (String keyword : keywords) {
      assertEquals(Tokens.reserved(keyword), lexicon.word(keyword));
      assertEquals(Tokens.reserved(keyword), lexicon.word(keyword.toUpperCase()));
    }
    for (String keyword : keywords) {
      assertParser(lexicon.tokenizer, keyword, Tokens.reserved(keyword));
      assertParser(
          lexicon.tokenizer, keyword.toUpperCase(), Tokens.reserved(keyword));
    }
    assertParser(lexicon.tokenizer, "baz", Tokens.identifier("baz"));
  }

  @Test
  public void testUnique() {
    Asserts.assertArrayEquals(
        Keywords.unique(String.CASE_INSENSITIVE_ORDER, "foo", "Foo", "foo", "bar"),
        "bar", "foo");
  }
}
