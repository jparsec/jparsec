package org.codehaus.jparsec;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import org.junit.Test;

/**
 * Unit test for {@link Lexicon}.
 * 
 * @author Ben Yu
 */
public class LexiconTest {

  @Test
  public void testWord() {
    Parser<?> tokenizer = Terminals.CharLiteral.SINGLE_QUOTE_TOKENIZER;
    Lexicon lexicon = new Lexicon(__ -> "foo", tokenizer);
    assertSame(tokenizer, lexicon.tokenizer);
    assertEquals("foo", lexicon.word("whatever"));
  }

  @Test
  public void testWord_throwsForNullValue() {
    Parser<?> tokenizer = Terminals.CharLiteral.SINGLE_QUOTE_TOKENIZER;
    Lexicon lexicon = new Lexicon(__ -> null, tokenizer);
    assertSame(tokenizer, lexicon.tokenizer);
    try {
      lexicon.word("whatever");
      fail();
    } catch (IllegalArgumentException e) {}
  }

}
