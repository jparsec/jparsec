/*****************************************************************************
 * Copyright 2013 (C) jparsec.org                                                *
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

import org.fest.assertions.Assertions;
import org.junit.Test;

import static org.jparsec.pattern.Pattern.MISMATCH;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;


/**
Unit test for {@link Patterns}.

@author Ben Yu
 */
public class PatternsTest {

  @Test
  public void testAlways() {
    assertEquals(0, Patterns.ALWAYS.match("", 0, 0));
    assertEquals(0, Patterns.ALWAYS.match("abc", 0, 0));
    assertEquals(0, Patterns.ALWAYS.match("abc", 1, 2));
  }

  @Test
  public void testNever() {
    assertEquals(MISMATCH, Patterns.NEVER.match("", 0, 0));
    assertEquals(MISMATCH, Patterns.NEVER.match("abc", 0, 0));
    assertEquals(MISMATCH, Patterns.NEVER.match("abc", 1, 2));
  }

  @Test
  public void testAnyChar() {
    assertEquals(1, Patterns.ANY_CHAR.match("a", 0, 1));
    assertEquals(1, Patterns.ANY_CHAR.match("abc", 0, 1));
    assertEquals(1, Patterns.ANY_CHAR.match("abc", 1, 2));
    assertEquals(1, Patterns.ANY_CHAR.match("abc", 0, 2));
    assertEquals(MISMATCH, Patterns.ANY_CHAR.match("", 0, 0));
  }

  @Test
  public void testHasAtLeast() {
    assertEquals(1, Patterns.hasAtLeast(1).match("a", 0, 1));
    assertEquals(1, Patterns.hasAtLeast(1).match("abc", 0, 1));
    assertEquals(1, Patterns.hasAtLeast(1).match("abc", 1, 2));
    assertEquals(2, Patterns.hasAtLeast(2).match("abc", 0, 2));
    assertEquals(2, Patterns.hasAtLeast(2).match("abc", 0, 3));
    assertEquals(MISMATCH, Patterns.hasAtLeast(2).match("a", 0, 1));
  }

  @Test
  public void testHasExact() {
    assertEquals(1, Patterns.hasExact(1).match("a", 0, 1));
    assertEquals(1, Patterns.hasExact(1).match("abc", 0, 1));
    assertEquals(1, Patterns.hasExact(1).match("abc", 1, 2));
    assertEquals(2, Patterns.hasExact(2).match("abc", 0, 2));
    assertEquals(MISMATCH, Patterns.hasExact(2).match("abc", 0, 3));
  }

  @Test
  public void testHasExactThrowsExceptionWhenNIsNegative() {
    try {
      Patterns.hasExact(-1);
      fail();
    } catch (IllegalArgumentException e) {
      assertEquals("n < 0", e.getMessage());
    }
  }

  @Test
  public void testEof() {
    assertEquals(0, Patterns.EOF.match("", 0, 0));
    assertEquals(0, Patterns.EOF.match("abc", 0, 0));
    assertEquals(0, Patterns.EOF.match("abc", 3, 3));
  }

  @Test
  public void testIsChar() {
    assertEquals(1, Patterns.isChar('a').match("a", 0, 1));
    assertEquals(1, Patterns.isChar('a').match(" a", 1, 2));
    assertEquals(MISMATCH, Patterns.isChar('a').match("ba", 0, 1));
    assertEquals(MISMATCH, Patterns.isChar('a').match("a", 0, 0));
  }

  @Test
  public void testIsChar_withPredicate() {
    assertEquals(1, Patterns.isChar(CharPredicates.ALWAYS).match("x", 0, 1));
    assertEquals(1, Patterns.isChar(CharPredicates.ALWAYS).match(" x", 1, 2));
    assertEquals(MISMATCH, Patterns.isChar(CharPredicates.NEVER).match("a", 0, 1));
    assertEquals(MISMATCH, Patterns.isChar(CharPredicates.ALWAYS).match("X", 0, 0));
    assertEquals(MISMATCH, Patterns.isChar(CharPredicates.NEVER).match("X", 0, 0));
  }

  @Test
  public void testRange() {
    assertEquals(1, Patterns.range('a', 'c').match("a", 0, 1));
    assertEquals(1, Patterns.range('a', 'c').match("b", 0, 1));
    assertEquals(1, Patterns.range('a', 'c').match("c", 0, 1));
    assertEquals(1, Patterns.range('a', 'c').match("abc", 0, 1));
    assertEquals(1, Patterns.range('a', 'c').match("ba", 1, 2));
    assertEquals(MISMATCH, Patterns.range('a', 'c').match("d", 0, 1));
    assertEquals(MISMATCH, Patterns.range('a', 'c').match("0", 0, 1));
    assertEquals(MISMATCH, Patterns.range('a', 'c').match("a", 0, 0));
    assertEquals(MISMATCH, Patterns.range('a', 'c').match("a", 0, 0));
  }

  @Test
  public void testAmong() {
    Pattern pattern = Patterns.among("a1");
    assertEquals(1, pattern.match("a", 0, 1));
    assertEquals(1, pattern.match("10", 0, 1));
    assertEquals(1, pattern.match("a", 0, 1));
    assertEquals(MISMATCH, pattern.match("", 0, 0));
    assertEquals(MISMATCH, pattern.match("0", 0, 1));
    assertEquals(MISMATCH, pattern.match("1b", 1, 2));
  }

  @Test
  public void testEscaped() {
    assertEquals(2, Patterns.ESCAPED.match("\\0", 0, 2));
    assertEquals(2, Patterns.ESCAPED.match("x\\0", 1, 3));
    assertEquals(MISMATCH, Patterns.ESCAPED.match("\\0", 0, 1));
    assertEquals(MISMATCH, Patterns.ESCAPED.match("012", 0, 3));
    assertEquals(MISMATCH, Patterns.ESCAPED.match("\\0", 0, 0));
  }

  @Test
  public void testLineComment() {
    assertEquals(4, Patterns.lineComment("//").match("//ab", 0, 4));
    assertEquals(4, Patterns.lineComment("//").match("//ab\n", 0, 5));
    assertEquals(5, Patterns.lineComment("//").match("//ab\r\n", 0, 6));
    assertEquals(MISMATCH, Patterns.lineComment("//").match("/ab\r\n", 0, 6));
    assertEquals(MISMATCH, Patterns.lineComment("//").match("//ab\r\n", 0, 0));
  }

  @Test
  public void testString() {
    assertEquals(3, Patterns.string("abc").match("abcd", 0, 4));
    assertEquals(0, Patterns.string("").match("abcd", 0, 4));
    assertEquals(MISMATCH, Patterns.string("abc").match("ABC", 0, 3));
    assertEquals(MISMATCH, Patterns.string("abc").match("abc", 0, 0));
  }

  @Test
  public void testStringCaseInsensitive() {
    assertEquals(0, Patterns.stringCaseInsensitive("").match("a", 0, 0));
    assertEquals(3, Patterns.stringCaseInsensitive("abc").match("abcd", 0, 4));
    assertEquals(3, Patterns.stringCaseInsensitive("abc").match("ABC", 0, 3));
    assertEquals(MISMATCH, Patterns.stringCaseInsensitive("abc").match("ABx", 0, 3));
    assertEquals(MISMATCH, Patterns.stringCaseInsensitive("abc").match("abc", 0, 0));
    assertEquals(MISMATCH, Patterns.stringCaseInsensitive("abc").match("ab", 0, 0));
  }

  @Test
  public void testNotString() {
    assertEquals(MISMATCH, Patterns.notString("abc").match("abcd", 0, 4));
    assertEquals(MISMATCH, Patterns.notString("").match("abc", 0, 0));
    assertEquals(1, Patterns.notString("abc").match("ABC", 0, 3));
    assertEquals(MISMATCH, Patterns.notString("abc").match("abc", 0, 0));
  }

  @Test
  public void testNotStringCaseInsensitive() {
    assertEquals(MISMATCH, Patterns.notStringCaseInsensitive("").match("a", 0, 0));
    assertEquals(MISMATCH, Patterns.notStringCaseInsensitive("abc").match("abcd", 0, 4));
    assertEquals(MISMATCH, Patterns.notStringCaseInsensitive("abc").match("ABC", 0, 3));
    assertEquals(1, Patterns.notStringCaseInsensitive("abc").match("ABx", 0, 3));
    assertEquals(MISMATCH, Patterns.notStringCaseInsensitive("abc").match("abc", 0, 0));
  }

  @Test
  public void testAnd() {
    assertEquals(2, Patterns.and(Patterns.hasAtLeast(1), Patterns.hasAtLeast(2)).match("abc", 0, 3));
    assertEquals(2, Patterns.and(Patterns.hasAtLeast(2), Patterns.hasAtLeast(1)).match("abc", 0, 3));
    assertEquals(MISMATCH, Patterns.and(Patterns.hasAtLeast(1), Patterns.NEVER).match("abc", 0, 3));
    assertEquals(MISMATCH, Patterns.and(Patterns.NEVER, Patterns.hasAtLeast(1), Patterns.ALWAYS).match("abc", 0, 3));
  }

  @Test
  public void testOr() {
    assertEquals(1, Patterns.or(Patterns.hasAtLeast(1), Patterns.hasAtLeast(2)).match("abc", 0, 3));
    assertEquals(2, Patterns.or(Patterns.hasAtLeast(2), Patterns.hasAtLeast(1)).match("abc", 0, 3));
    assertEquals(1, Patterns.or(Patterns.hasAtLeast(1), Patterns.NEVER).match("abc", 0, 3));
    assertEquals(1, Patterns.or(Patterns.NEVER, Patterns.hasAtLeast(1), Patterns.ALWAYS).match("abc", 0, 3));
    assertEquals(MISMATCH, Patterns.or(Patterns.NEVER, Patterns.NEVER).match("abc", 0, 3));
  }

  @Test
  public void testSequence() {
    assertEquals(3, Patterns.sequence(Patterns.hasAtLeast(1), Patterns.hasAtLeast(2)).match("abc", 0, 3));
    assertEquals(MISMATCH, Patterns.sequence(Patterns.hasAtLeast(1), Patterns.hasAtLeast(3)).match("abc", 0, 3));
    assertEquals(MISMATCH, Patterns.sequence(Patterns.NEVER, Patterns.hasAtLeast(2)).match("abc", 0, 3));
  }

  @Test
  public void testRepeat() {
    assertEquals(3, Patterns.repeat(3, CharPredicates.ALWAYS).match("abc", 0, 3));
    assertEquals(2, Patterns.repeat(2, CharPredicates.ALWAYS).match("abc", 0, 3));
    assertEquals(MISMATCH, Patterns.repeat(3, CharPredicates.NEVER).match("abc", 0, 3));
    assertEquals(MISMATCH, Patterns.repeat(3, CharPredicates.ALWAYS).match("abc", 0, 2));
  }

  @Test
  public void testRepeatAnyIsNotEquivalentToHasExact() {
    Assertions.assertThat(Patterns.repeat(2, CharPredicates.ALWAYS).match("abc", 0, 3)) //
    .isNotEqualTo(Patterns.hasExact(2).match("abc", 0, 3));
  }

  @Test
  public void testRepeat_negativeNumberThrows() {
    try {
      Patterns.repeat(-1, CharPredicates.ALWAYS);
      fail();
    } catch (IllegalArgumentException e) {
      assertEquals("n < 0", e.getMessage());
    }
  }

  @Test
  public void testMany() {
    assertEquals(3, Patterns.many(CharPredicates.ALWAYS).match("abc", 0, 3));
    assertEquals(0, Patterns.many(CharPredicates.NEVER).match("abc", 0, 3));
    assertEquals(0, Patterns.many(CharPredicates.ALWAYS).match("", 0, 0));
  }

  @Test
  public void testMany1() {
    assertEquals(3, Patterns.many1(CharPredicates.ALWAYS).match("abc", 0, 3));
    assertEquals(MISMATCH, Patterns.many1(CharPredicates.ALWAYS).match("abc", 0, 0));
    assertEquals(MISMATCH, Patterns.many1(CharPredicates.NEVER).match("abc", 0, 3));
    assertEquals(MISMATCH, Patterns.many1(CharPredicates.ALWAYS).match("", 0, 0));
  }

  @Test
  public void testMany_withMin() {
    assertEquals(3, Patterns.atLeast(3, CharPredicates.ALWAYS).match("abc", 0, 3));
    assertEquals(MISMATCH, Patterns.atLeast(4, CharPredicates.ALWAYS).match("abc", 0, 3));
    assertEquals(MISMATCH, Patterns.atLeast(1, CharPredicates.NEVER).match("abc", 0, 3));
    assertEquals(MISMATCH, Patterns.atLeast(1, CharPredicates.ALWAYS).match("", 0, 0));
  }

  @Test
  public void testMany_negativeNumberThrows() {
    try {
      Patterns.atLeast(-1, CharPredicates.ALWAYS);
      fail();
    } catch (IllegalArgumentException e) {
      assertEquals("min < 0", e.getMessage());
    }
  }

  @Test
  public void testSome() {
    assertEquals(2, Patterns.atMost(2, CharPredicates.ALWAYS).match("abc", 0, 3));
    assertEquals(0, Patterns.atMost(0, CharPredicates.ALWAYS).match("abc", 0, 3));
    assertEquals(0, Patterns.atMost(1, CharPredicates.NEVER).match("abc", 0, 3));
    assertEquals(0, Patterns.atMost(2, CharPredicates.ALWAYS).match("", 0, 0));
  }

  @Test
  public void testSome_withMin() {
    assertEquals(3, Patterns.times(1, 4, CharPredicates.ALWAYS).match("abc", 0, 3));
    assertEquals(MISMATCH, Patterns.times(4, 5, CharPredicates.ALWAYS).match("abc", 0, 3));
    assertEquals(MISMATCH, Patterns.times(1, 1, CharPredicates.NEVER).match("abc", 0, 3));
    assertEquals(MISMATCH, Patterns.times(1, 1, CharPredicates.ALWAYS).match("", 0, 0));
  }

  @Test
  public void testSome_negativeMaxThrows() {
    try {
      Patterns.atMost(-1, CharPredicates.ALWAYS);
      fail();
    } catch (IllegalArgumentException e) {
      assertEquals("max < 0", e.getMessage());
    }
    try {
      Patterns.times(0, -1, CharPredicates.ALWAYS);
      fail();
    } catch (IllegalArgumentException e) {
      assertEquals("max < 0", e.getMessage());
    }
  }

  @Test
  public void testSome_negativeMinThrows() {
    try {
      Patterns.times(-1, 1, CharPredicates.ALWAYS);
      fail();
    } catch (IllegalArgumentException e) {
      assertEquals("min < 0", e.getMessage());
    }
  }

  @Test
  public void testSome_minBiggerThanMaxThrows() {
    try {
      Patterns.times(1, 0, CharPredicates.ALWAYS);
      fail();
    } catch (IllegalArgumentException e) {
      assertEquals("min > max", e.getMessage());
    }
  }

  @Test
  public void testLonger() {
    assertEquals(0, Patterns.longer(Patterns.ALWAYS, Patterns.NEVER).match("", 0, 0));
    assertEquals(MISMATCH, Patterns.longer(Patterns.NEVER, Patterns.NEVER).match("", 0, 0));
    assertEquals(0, Patterns.longer(Patterns.ALWAYS, Patterns.ALWAYS).match("", 0, 0));
    assertEquals(1, Patterns.longer(Patterns.hasAtLeast(1), Patterns.NEVER).match("a", 0, 1));
    assertEquals(1, Patterns.longer(Patterns.hasAtLeast(1), Patterns.ALWAYS).match("a", 0, 1));
    assertEquals(2, Patterns.longer(Patterns.hasAtLeast(1), Patterns.hasExact(2)).match("ab", 0, 2));
  }

  @Test
  public void testLongest() {
    assertEquals(0, Patterns.longest(Patterns.ALWAYS, Patterns.NEVER, Patterns.ALWAYS).match("", 0, 0));
    assertEquals(MISMATCH, Patterns.longest(Patterns.NEVER, Patterns.NEVER, Patterns.NEVER).match("", 0, 0));
    assertEquals(0, Patterns.longest(Patterns.ALWAYS, Patterns.ALWAYS, Patterns.ALWAYS).match("", 0, 0));
    assertEquals(1, Patterns.longest(Patterns.hasAtLeast(1), Patterns.NEVER, Patterns.ALWAYS).match("a", 0, 1));
    assertEquals(2, Patterns.longest(Patterns.hasAtLeast(1), Patterns.hasExact(2), Patterns.NEVER).match("ab", 0, 2));
  }

  @Test
  public void testShorter() {
    assertEquals(0, Patterns.shorter(Patterns.ALWAYS, Patterns.NEVER).match("", 0, 0));
    assertEquals(MISMATCH, Patterns.shorter(Patterns.NEVER, Patterns.NEVER).match("", 0, 0));
    assertEquals(0, Patterns.shorter(Patterns.ALWAYS, Patterns.ALWAYS).match("", 0, 0));
    assertEquals(1, Patterns.shorter(Patterns.hasAtLeast(1), Patterns.NEVER).match("a", 0, 1));
    assertEquals(0, Patterns.shorter(Patterns.hasAtLeast(1), Patterns.ALWAYS).match("a", 0, 1));
    assertEquals(1, Patterns.shorter(Patterns.hasAtLeast(1), Patterns.hasExact(2)).match("ab", 0, 2));
  }

  @Test
  public void testShortest() {
    assertEquals(0, Patterns.shortest(Patterns.ALWAYS, Patterns.NEVER, Patterns.ALWAYS).match("", 0, 0));
    assertEquals(MISMATCH, Patterns.shortest(Patterns.NEVER, Patterns.NEVER, Patterns.NEVER).match("", 0, 0));
    assertEquals(0, Patterns.shortest(Patterns.ALWAYS, Patterns.ALWAYS, Patterns.ALWAYS).match("", 0, 0));
    assertEquals(0, Patterns.shortest(Patterns.hasAtLeast(1), Patterns.NEVER, Patterns.ALWAYS).match("a", 0, 1));
    assertEquals(1, Patterns.shortest(Patterns.hasAtLeast(1), Patterns.hasExact(2), Patterns.NEVER).match("ab", 0, 2));
  }

  @Test
  public void testDecimalL() {
    assertEquals(2, Patterns.STRICT_DECIMAL.match("12a", 0, 3));
    assertEquals(3, Patterns.STRICT_DECIMAL.match("12.a", 0, 4));
    assertEquals(2, Patterns.STRICT_DECIMAL.match("0.", 0, 2));
    assertEquals(5, Patterns.STRICT_DECIMAL.match("12.34 ", 0, 6));
    assertEquals(MISMATCH, Patterns.STRICT_DECIMAL.match(".34 ", 0, 3));
    assertEquals(MISMATCH, Patterns.STRICT_DECIMAL.match("a.34 ", 0, 4));
    assertEquals(MISMATCH, Patterns.STRICT_DECIMAL.match("", 0, 0));
  }

  @Test
  public void testDecimalR() {
    assertEquals(2, Patterns.FRACTION.match(".1", 0, 2));
    assertEquals(2, Patterns.FRACTION.match(".0a", 0, 3));
    assertEquals(2, Patterns.FRACTION.match(".1", 0, 2));
    assertEquals(MISMATCH, Patterns.FRACTION.match("12.34 ", 0, 6));
    assertEquals(MISMATCH, Patterns.FRACTION.match("a.34 ", 0, 4));
    assertEquals(MISMATCH, Patterns.FRACTION.match(". ", 0, 2));
    assertEquals(MISMATCH, Patterns.FRACTION.match("", 0, 0));
  }

  @Test
  public void testDecimal() {
    assertEquals(3, Patterns.DECIMAL.match("1.2", 0, 3));
    assertEquals(2, Patterns.DECIMAL.match("12", 0, 2));
    assertEquals(2, Patterns.DECIMAL.match(".1", 0, 2));
    assertEquals(MISMATCH, Patterns.DECIMAL.match(".", 0, 1));
    assertEquals(MISMATCH, Patterns.DECIMAL.match("", 0, 0));
  }

  @Test
  public void testWord() {
    assertEquals(1, Patterns.WORD.match("a", 0, 1));
    assertEquals(1, Patterns.WORD.match("A", 0, 1));
    assertEquals(1, Patterns.WORD.match("_", 0, 1));
    assertEquals(MISMATCH, Patterns.WORD.match("0", 0, 1));
    assertEquals(6, Patterns.WORD.match("abc_01", 0, 6));
    assertEquals(MISMATCH, Patterns.WORD.match("", 0, 0));
  }

  @Test
  public void testInteger() {
    assertEquals(1, Patterns.INTEGER.match("1", 0, 1));
    assertEquals(2, Patterns.INTEGER.match("12a", 0, 3));
    assertEquals(MISMATCH, Patterns.INTEGER.match("a", 0, 1));
    assertEquals(MISMATCH, Patterns.INTEGER.match("", 0, 0));
  }

  @Test
  public void testOctInteger() {
    assertEquals(1, Patterns.OCT_INTEGER.match("0", 0, 1));
    assertEquals(2, Patterns.OCT_INTEGER.match("01", 0, 2));
    assertEquals(3, Patterns.OCT_INTEGER.match("0078", 0, 4));
    assertEquals(MISMATCH, Patterns.OCT_INTEGER.match("1", 0, 1));
    assertEquals(MISMATCH, Patterns.OCT_INTEGER.match("", 0, 0));
  }

  @Test
  public void testDecInteger() {
    assertEquals(1, Patterns.DEC_INTEGER.match("1", 0, 1));
    assertEquals(3, Patterns.DEC_INTEGER.match("109", 0, 3));
    assertEquals(MISMATCH, Patterns.DEC_INTEGER.match("0", 0, 1));
    assertEquals(MISMATCH, Patterns.DEC_INTEGER.match("", 0, 0));
  }

  @Test
  public void testHexInteger() {
    assertEquals(4, Patterns.HEX_INTEGER.match("0x3F", 0, 4));
    assertEquals(4, Patterns.HEX_INTEGER.match("0XAf", 0, 4));
    assertEquals(3, Patterns.HEX_INTEGER.match("0X0", 0, 3));
    assertEquals(MISMATCH, Patterns.HEX_INTEGER.match("0X", 0, 2));
    assertEquals(MISMATCH, Patterns.HEX_INTEGER.match("0X", 0, 1));
    assertEquals(MISMATCH, Patterns.HEX_INTEGER.match("0X", 0, 0));
  }

  @Test
  public void testScientificNumber() {
    Pattern pattern = Patterns.SCIENTIFIC_NOTATION;
    assertEquals(3, pattern.match("0e1", 0, 3));
    assertEquals(5, pattern.match("12e12", 0, 5));
    assertEquals(6, pattern.match("0.1E12", 0, 6));
    assertEquals(7, pattern.match("1.9E+12", 0, 7));
    assertEquals(5, pattern.match("1E-12", 0, 5));
    assertEquals(5, pattern.match("1e-12", 0, 5));
    assertEquals(MISMATCH, pattern.match("e", 0, 1));
    assertEquals(MISMATCH, pattern.match("0", 0, 1));
    assertEquals(MISMATCH, pattern.match("e", 0, 0));
    assertEquals(MISMATCH, pattern.match("e1", 0, 0));
  }

  @Test
  public void testRegex() {
    assertEquals(3, Patterns.regex("a*").match("aaab", 0, 4));
    assertEquals(3, Patterns.regex("a*").match("aaab", 0, 3));
    assertEquals(2, Patterns.regex("a*").match("aaab", 0, 2));
    assertEquals(0, Patterns.regex("a*").match("bbbb", 2, 2));
    assertEquals(MISMATCH, Patterns.regex("a+").match("aaab", 0, 0));
    assertEquals(MISMATCH, Patterns.regex("a*").match("aaab", 3, 2));
  }

  @Test
  public void testRegexpPattern() {
    assertEquals(3, Patterns.REGEXP_PATTERN.match("/a/", 0, 3));
    assertEquals(7, Patterns.REGEXP_PATTERN.match("/ab\\c./", 0, 7));
    assertEquals(MISMATCH, Patterns.REGEXP_PATTERN.match("/ab\\/", 0, 5));
    assertEquals(MISMATCH, Patterns.REGEXP_PATTERN.match("A", 0, 1));
    assertEquals(MISMATCH, Patterns.REGEXP_PATTERN.match("/a/", 0, 2));
    assertEquals(MISMATCH, Patterns.REGEXP_PATTERN.match("/a/", 0, 1));
    assertEquals(MISMATCH, Patterns.REGEXP_PATTERN.match("/a/", 0, 0));
  }

  @Test
  public void testRegexpModifiers() {
    assertEquals(2, Patterns.REGEXP_MODIFIERS.match("ab", 0, 2));
    assertEquals(1, Patterns.REGEXP_MODIFIERS.match("ab", 0, 1));
    assertEquals(0, Patterns.REGEXP_MODIFIERS.match("ab", 0, 0));
  }

  @Test
  public void testToString() throws Exception {
    assertEquals("[a-zA-Z]", Patterns.isChar(CharPredicates.IS_ALPHA).toString());
    assertEquals(".{3,}", Patterns.hasAtLeast(3).toString());
    assertEquals("(foo & .{2})", Patterns.and(Patterns.string("foo"), Patterns.hasExact(2)).toString());
    assertEquals("(foo | .{2,})", Patterns.or(Patterns.string("foo"), Patterns.hasAtLeast(2)).toString());
    assertEquals("(bar & c{3})", Patterns.and(Patterns.string("bar"), Patterns.isChar('c').times(3)).toString());
    assertEquals("c{3}", Patterns.repeat(3, CharPredicates.isChar('c')).toString());
    assertEquals("foo{2,}", Patterns.string("foo").atLeast(2).toString());
    assertEquals("foo+", Patterns.string("foo").many1().toString());
    assertEquals("!(foo)", Patterns.notString("foo").toString());
    assertEquals("a+", Patterns.many1(CharPredicates.isChar('a')).toString());
    assertEquals("foo*", Patterns.string("foo").many().toString());
    assertEquals("a*", Patterns.many(CharPredicates.isChar('a')).toString());
    assertEquals("foo?", Patterns.string("foo").optional().toString());
    assertEquals("foobar", Patterns.string("foo").next(Patterns.string("bar")).toString());
    assertEquals("foo{0,2}", Patterns.string("foo").atMost(2).toString());
    assertEquals("!(FOO)", Patterns.not(Patterns.stringCaseInsensitive("foo")).toString());
    assertEquals("(?:foo)", Patterns.string("foo").peek().toString());
  }

}
