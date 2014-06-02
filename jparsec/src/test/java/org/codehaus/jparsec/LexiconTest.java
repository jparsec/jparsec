package org.codehaus.jparsec;

import org.codehaus.jparsec.functors.Map;
import org.codehaus.jparsec.functors.Maps;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

/**
 * Unit test for {@link Lexicon}.
 * 
 * @author Ben Yu
 */
public class LexiconTest {

  @Test
  public void testWord() {
    Map<String, Object> map = Maps.<String, Object>constant("foo");
    Parser<?> tokenizer = Terminals.CharLiteral.SINGLE_QUOTE_TOKENIZER;
    Lexicon lexicon = new Lexicon(map, tokenizer);
    assertSame(tokenizer, lexicon.tokenizer);
    assertEquals("foo", lexicon.word("whatever"));
  }

  @Test
  public void testWord_throwsForNullValue() {
    Map<String, Object> map = Maps.<String, Object>constant(null);
    Parser<?> tokenizer = Terminals.CharLiteral.SINGLE_QUOTE_TOKENIZER;
    Lexicon lexicon = new Lexicon(map, tokenizer);
    assertSame(tokenizer, lexicon.tokenizer);
    try {
      lexicon.word("whatever");
      fail();
    } catch (IllegalArgumentException e) {}
  }

}
