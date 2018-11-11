/*****************************************************************************
 * Copyright (C) jparsec.org                                                *
 * ------------------------------------------------------------------------- *
 * Licensed under the Apache License, Version 2.0 (the "License");           *
 * you may not use this file except in compliance with the License.          *
 * You may obtain a copy of the License at                                   *
 *                                                                           *
 * http://www.apache.org/licenses/LICENSE-2.0                                *
 *                                                                           *
 * Unless required by applicable law or agreed to in writing, software       *
 * distributed under the License is distributed on an "AS IS" BASIS,         *
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  *
 * See the License for the specific language governing permissions and       *
 * limitations under the License.                                            *
 *****************************************************************************/
package org.jparsec.pattern;

import org.junit.Test;

import static org.jparsec.pattern.Pattern.MISMATCH;
import static org.junit.Assert.*;

/**
 * Unit test for {@link Pattern}.
 * 
 * @author Ben Yu
 */
public class PatternTest {

  @Test
  public void testMismatch() {
    assertTrue(MISMATCH < 0);
  }

  @Test
  public void testNext() {
    assertEquals(2, Patterns.hasAtLeast(1).next(Patterns.hasAtLeast(1)).match("abc", 0, 3));
    assertEquals(MISMATCH, Patterns.ALWAYS.next(Patterns.NEVER).match("abc", 0, 3));
    assertEquals(MISMATCH, Patterns.NEVER.next(Patterns.ALWAYS).match("abc", 0, 3));
  }

  @Test
  public void testNot() {
    assertEquals(0, Patterns.NEVER.not().match("abc", 0, 3));
    assertEquals(MISMATCH, Patterns.ALWAYS.not().match("abc", 0, 3));
  }

  @Test
  public void testOr() {
    assertEquals(1, Patterns.hasAtLeast(1).or(Patterns.hasAtLeast(2)).match("abc", 0, 2));
    assertEquals(1, Patterns.hasAtLeast(1).or(Patterns.hasAtLeast(2)).match("abc", 0, 1));
    assertEquals(0, Patterns.NEVER.or(Patterns.ALWAYS).match("abc", 0, 0));
    assertEquals(MISMATCH, Patterns.NEVER.or(Patterns.NEVER).match("", 0, 0));
  }

  @Test
  public void testOptional() {
    assertEquals(0, Patterns.NEVER.optional().match("", 0, 0));
    assertEquals(0, Patterns.ALWAYS.optional().match("", 0, 0));
    assertEquals(1, Patterns.hasAtLeast(1).optional().match("abc", 0, 3));
  }

  @Test
  public void testPeek() {
    assertEquals(0, Patterns.hasAtLeast(1).peek().match("abc", 0, 3));
    assertEquals(0, Patterns.ALWAYS.peek().match("abc", 0, 3));
    assertEquals(MISMATCH, Patterns.NEVER.peek().match("abc", 0, 3));
  }

  @Test
  public void testRepeat() {
    assertEquals(0, Patterns.ALWAYS.times(2).match("abc", 0, 3));
    assertEquals(2, Patterns.hasAtLeast(1).times(2).match("abc", 0, 3));
    assertEquals(MISMATCH, Patterns.hasAtLeast(1).times(2).match("abc", 0, 1));
    assertEquals(MISMATCH, Patterns.NEVER.times(2).match("abc", 0, 3));
    assertEquals(0, Patterns.hasAtLeast(1).times(0).match("abc", 0, 3));
  }

  @Test
  public void testRepeat_throwsForNegativeNumber() {
    try {
      Patterns.ALWAYS.times(-1);
      fail();
    } catch (IllegalArgumentException e) {
      assertEquals("n < 0", e.getMessage());
    }
  }

  @Test
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

  @Test
  public void testMany() {
    assertEquals(0, Patterns.NEVER.many().match("abc", 0, 3));
    assertEquals(0, Patterns.ALWAYS.many().match("abc", 0, 3));
    assertEquals(3, Patterns.hasAtLeast(1).many().match("abc", 0, 3));
    assertEquals(4, Patterns.hasAtLeast(2).many().match("abcde", 0, 5));
  }

  @Test
  public void testMany1() {
    assertEquals(MISMATCH, Patterns.NEVER.many1().match("abc", 0, 3));
    assertEquals(0, Patterns.ALWAYS.many1().match("abc", 0, 3));
    assertEquals(3, Patterns.hasAtLeast(1).many1().match("abc", 0, 3));
    assertEquals(4, Patterns.hasAtLeast(2).many1().match("abcde", 0, 5));
  }

  @Test
  public void testMany_withMin() {
    assertEquals(0, Patterns.ALWAYS.atLeast(2).match("abc", 0, 3));
    assertEquals(0, Patterns.ALWAYS.atLeast(0).match("abc", 0, 3));
    assertEquals(0, Patterns.NEVER.atLeast(0).match("abc", 0, 3));
    assertEquals(3, Patterns.hasAtLeast(1).atLeast(2).match("abc", 0, 3));
    assertEquals(4, Patterns.hasAtLeast(2).atLeast(2).match("abcde", 0, 5));
    assertEquals(MISMATCH, Patterns.NEVER.atLeast(2).match("abc", 0, 3));
    assertEquals(MISMATCH, Patterns.hasAtLeast(1).atLeast(2).match("abc", 0, 1));
  }

  @Test
  public void testMany_throwsForNegativeMin() {
    try {
      Patterns.ALWAYS.atLeast(-1);
      fail();
    } catch (IllegalArgumentException e) {
      assertEquals("min < 0", e.getMessage());
    }
  }

  @Test
  public void testSome() {
    assertEquals(0, Patterns.NEVER.atMost(2).match("abc", 0, 3));
    assertEquals(0, Patterns.ALWAYS.atMost(2).match("abc", 0, 3));
    assertEquals(0, Patterns.NEVER.atMost(0).match("abc", 0, 3));
    assertEquals(0, Patterns.ALWAYS.atMost(0).match("abc", 0, 3));
    assertEquals(2, Patterns.hasAtLeast(1).atMost(2).match("abc", 0, 3));
    assertEquals(4, Patterns.hasAtLeast(2).atMost(2).match("abcde", 0, 5));
  }

  @Test
  public void testSome_withMin() {
    assertEquals(MISMATCH, Patterns.NEVER.times(1, 2).match("abc", 0, 3));
    assertEquals(0, Patterns.ALWAYS.times(1, 2).match("abc", 0, 3));
    assertEquals(0, Patterns.NEVER.times(0, 1).match("abc", 0, 3));
    assertEquals(2, Patterns.hasAtLeast(1).times(1, 2).match("abc", 0, 3));
    assertEquals(4, Patterns.hasAtLeast(2).times(1, 2).match("abcde", 0, 5));
  }

  @Test
  public void testSome_throwsForNegativeMax() {
    try {
      Patterns.ALWAYS.atMost(-1);
      fail();
    } catch (IllegalArgumentException e) {
      assertEquals("max < 0", e.getMessage());
    }
  }

  @Test
  public void testSome_throwsForNegativeMinMax() {
    try {
      Patterns.ALWAYS.times(-1, 1);
      fail();
    } catch (IllegalArgumentException e) {
      assertEquals("min < 0", e.getMessage());
    }
    try {
      Patterns.ALWAYS.times(1, -1);
      fail();
    } catch (IllegalArgumentException e) {
      assertEquals("max < 0", e.getMessage());
    }
  }

}
