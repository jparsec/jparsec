package org.codehaus.jparsec.pattern;

import static org.codehaus.jparsec.pattern.Pattern.MISMATCH;
import static org.codehaus.jparsec.pattern.Patterns.ALWAYS;
import static org.codehaus.jparsec.pattern.Patterns.NEVER;
import junit.framework.TestCase;

/**
 * Unit test for {@link Pattern}.
 * 
 * @author Ben Yu
 */
public class PatternTest extends TestCase {
  public void testMismatch() {
    assertTrue(MISMATCH < 0);
  }
  
  public void testNext() {
    assertEquals(2, Patterns.hasAtLeast(1).next(Patterns.hasAtLeast(1)).match("abc", 0, 3));
    assertEquals(MISMATCH, ALWAYS.next(NEVER).match("abc", 0, 3));
    assertEquals(MISMATCH, NEVER.next(ALWAYS).match("abc", 0, 3));
  }
  
  public void testNot() {
    assertEquals(0, NEVER.not().match("abc", 0, 3));
    assertEquals(MISMATCH, ALWAYS.not().match("abc", 0, 3));
  }
  
  public void testOr() {
    assertEquals(1, Patterns.hasAtLeast(1).or(Patterns.hasAtLeast(2)).match("abc", 0, 2));
    assertEquals(1, Patterns.hasAtLeast(1).or(Patterns.hasAtLeast(2)).match("abc", 0, 1));
    assertEquals(0, NEVER.or(ALWAYS).match("abc", 0, 0));
    assertEquals(MISMATCH, NEVER.or(NEVER).match("", 0, 0));
  }
  
  public void testOptional() {
    assertEquals(0, NEVER.optional().match("", 0, 0));
    assertEquals(0, ALWAYS.optional().match("", 0, 0));
    assertEquals(1, Patterns.hasAtLeast(1).optional().match("abc", 0, 3));
  }
  
  public void testPeek() {
    assertEquals(0, Patterns.hasAtLeast(1).peek().match("abc", 0, 3));
    assertEquals(0, ALWAYS.peek().match("abc", 0, 3));
    assertEquals(MISMATCH, NEVER.peek().match("abc", 0, 3));
  }
  
  public void testRepeat() {
    assertEquals(0, ALWAYS.repeat(2).match("abc", 0, 3));
    assertEquals(2, Patterns.hasAtLeast(1).repeat(2).match("abc", 0, 3));
    assertEquals(MISMATCH, Patterns.hasAtLeast(1).repeat(2).match("abc", 0, 1));
    assertEquals(MISMATCH, NEVER.repeat(2).match("abc", 0, 3));
    assertEquals(0, Patterns.hasAtLeast(1).repeat(0).match("abc", 0, 3));
  }
  
  public void testRepeat_throwsForNegativeNumber() {
    try {
      ALWAYS.repeat(-1);
      fail();
    } catch (IllegalArgumentException e) {
      assertEquals("n < 0", e.getMessage());
    }
  }
  
  public void testIfElse() {
    assertEquals(3,
        Patterns.isChar('a').ifelse(Patterns.string("bc"), Patterns.INTEGER)
            .match("abcd", 0, 4));
    assertEquals(MISMATCH,
        Patterns.isChar('a').ifelse(Patterns.string("bd"), Patterns.INTEGER)
            .match("abcd", 0, 4));
    assertEquals(2,
        Patterns.isChar('a').ifelse(Patterns.string("bc"), Patterns.INTEGER)
            .match("12c", 0, 3));
    assertEquals(MISMATCH,
        Patterns.isChar('a').ifelse(Patterns.string("bc"), Patterns.INTEGER)
            .match("xxx", 0, 3));
  }
  
  public void testMany() {
    assertEquals(0, NEVER.many().match("abc", 0, 3));
    assertEquals(0, ALWAYS.many().match("abc", 0, 3));
    assertEquals(3, Patterns.hasAtLeast(1).many().match("abc", 0, 3));
    assertEquals(4, Patterns.hasAtLeast(2).many().match("abcde", 0, 5));
  }
  
  public void testMany1() {
    assertEquals(MISMATCH, NEVER.many1().match("abc", 0, 3));
    assertEquals(0, ALWAYS.many1().match("abc", 0, 3));
    assertEquals(3, Patterns.hasAtLeast(1).many1().match("abc", 0, 3));
    assertEquals(4, Patterns.hasAtLeast(2).many1().match("abcde", 0, 5));
  }
  
  public void testMany_withMin() {
    assertEquals(0, ALWAYS.many(2).match("abc", 0, 3));
    assertEquals(0, ALWAYS.many(0).match("abc", 0, 3));
    assertEquals(0, NEVER.many(0).match("abc", 0, 3));
    assertEquals(3, Patterns.hasAtLeast(1).many(2).match("abc", 0, 3));
    assertEquals(4, Patterns.hasAtLeast(2).many(2).match("abcde", 0, 5));
    assertEquals(MISMATCH, NEVER.many(2).match("abc", 0, 3));
    assertEquals(MISMATCH, Patterns.hasAtLeast(1).many(2).match("abc", 0, 1));
  }
  
  public void testMany_throwsForNegativeMin() {
    try {
      ALWAYS.many(-1);
      fail();
    } catch (IllegalArgumentException e) {
      assertEquals("min < 0", e.getMessage());
    }
  }
  
  public void testSome() {
    assertEquals(0, NEVER.some(2).match("abc", 0, 3));
    assertEquals(0, ALWAYS.some(2).match("abc", 0, 3));
    assertEquals(0, NEVER.some(0).match("abc", 0, 3));
    assertEquals(0, ALWAYS.some(0).match("abc", 0, 3));
    assertEquals(2, Patterns.hasAtLeast(1).some(2).match("abc", 0, 3));
    assertEquals(4, Patterns.hasAtLeast(2).some(2).match("abcde", 0, 5));
  }
  
  public void testSome_withMin() {
    assertEquals(MISMATCH, NEVER.some(1, 2).match("abc", 0, 3));
    assertEquals(0, ALWAYS.some(1, 2).match("abc", 0, 3));
    assertEquals(0, NEVER.some(0, 1).match("abc", 0, 3));
    assertEquals(2, Patterns.hasAtLeast(1).some(1, 2).match("abc", 0, 3));
    assertEquals(4, Patterns.hasAtLeast(2).some(1, 2).match("abcde", 0, 5));
  }
  
  public void testSome_throwsForNegativeMax() {
    try {
      ALWAYS.some(-1);
      fail();
    } catch (IllegalArgumentException e) {
      assertEquals("max < 0", e.getMessage());
    }
  }
  
  public void testSome_throwsForNegativeMinMax() {
    try {
      ALWAYS.some(-1, 1);
      fail();
    } catch (IllegalArgumentException e) {
      assertEquals("min < 0", e.getMessage());
    }
    try {
      ALWAYS.some(1, -1);
      fail();
    } catch (IllegalArgumentException e) {
      assertEquals("max < 0", e.getMessage());
    }
  }
}
