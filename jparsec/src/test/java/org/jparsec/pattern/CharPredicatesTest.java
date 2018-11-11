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

import static org.junit.Assert.*;

/**
 * Unit test for {@link CharPredicates}.
 * 
 * @author Ben Yu
 */
public class CharPredicatesTest {

  @Test
  public void testIsChar() {
    CharPredicate predicate = CharPredicates.isChar('a');
    assertTrue(predicate.isChar('a'));
    assertFalse(predicate.isChar('x'));
    assertEquals("a", predicate.toString());
  }

  @Test
  public void testNotChar() {
    CharPredicate predicate = CharPredicates.notChar('a');
    assertFalse(predicate.isChar('a'));
    assertTrue(predicate.isChar('x'));
    assertEquals("^a", predicate.toString());
  }

  @Test
  public void testRange() {
    CharPredicate predicate = CharPredicates.range('1', '3');
    assertTrue(predicate.isChar('1'));
    assertTrue(predicate.isChar('2'));
    assertTrue(predicate.isChar('3'));
    assertFalse(predicate.isChar('0'));
    assertFalse(predicate.isChar('4'));
    assertEquals("[1-3]", predicate.toString());
  }

  @Test
  public void testIsDigit() {
    CharPredicate predicate = CharPredicates.IS_DIGIT;
    assertTrue(predicate.isChar('0'));
    assertTrue(predicate.isChar('9'));
    assertFalse(predicate.isChar('a'));
    assertFalse(predicate.isChar(' '));
    assertEquals("[0-9]", predicate.toString());
  }

  @Test
  public void testNotRange() {
    CharPredicate predicate = CharPredicates.notRange('1', '3');
    assertFalse(predicate.isChar('1'));
    assertFalse(predicate.isChar('2'));
    assertFalse(predicate.isChar('3'));
    assertTrue(predicate.isChar('0'));
    assertTrue(predicate.isChar('4'));
    assertEquals("[^1-3]", predicate.toString());
  }

  @Test
  public void testAmong() {
    CharPredicate predicate = CharPredicates.among("a1");
    assertTrue(predicate.isChar('a'));
    assertTrue(predicate.isChar('1'));
    assertFalse(predicate.isChar(' '));
    assertEquals("[a1]", predicate.toString());
  }

  @Test
  public void testNotAmong() {
    CharPredicate predicate = CharPredicates.notAmong("a1");
    assertFalse(predicate.isChar('a'));
    assertFalse(predicate.isChar('1'));
    assertTrue(predicate.isChar(' '));
    assertEquals("^[a1]", predicate.toString());
  }

  @Test
  public void testIsHexDigit() {
    CharPredicate predicate = CharPredicates.IS_HEX_DIGIT;
    assertFalse(predicate.isChar('g'));
    assertFalse(predicate.isChar(' '));
    assertTrue(predicate.isChar('A'));
    assertTrue(predicate.isChar('a'));
    assertTrue(predicate.isChar('F'));
    assertTrue(predicate.isChar('f'));
    assertTrue(predicate.isChar('0'));
    assertTrue(predicate.isChar('9'));
    assertTrue(predicate.isChar('E'));
    assertTrue(predicate.isChar('1'));
    assertEquals("[0-9a-fA-F]", predicate.toString());
  }

  @Test
  public void testIsUpperCase() {
    CharPredicate predicate = CharPredicates.IS_UPPER_CASE;
    assertFalse(predicate.isChar('a'));
    assertFalse(predicate.isChar('1'));
    assertFalse(predicate.isChar(' '));
    assertTrue(predicate.isChar('A'));
    assertTrue(predicate.isChar('Z'));
    assertEquals("uppercase", predicate.toString());
  }

  @Test
  public void testIsLowerCase() {
    CharPredicate predicate = CharPredicates.IS_LOWER_CASE;
    assertFalse(predicate.isChar('A'));
    assertFalse(predicate.isChar('1'));
    assertFalse(predicate.isChar(' '));
    assertTrue(predicate.isChar('a'));
    assertTrue(predicate.isChar('z'));
    assertEquals("lowercase", predicate.toString());
  }

  @Test
  public void testIsWhitespace() {
    CharPredicate predicate = CharPredicates.IS_WHITESPACE;
    assertFalse(predicate.isChar('A'));
    assertFalse(predicate.isChar('1'));
    assertFalse(predicate.isChar('a'));
    assertTrue(predicate.isChar(' '));
    assertTrue(predicate.isChar('\t'));
    assertTrue(predicate.isChar('\n'));
    assertEquals("whitespace", predicate.toString());
  }

  @Test
  public void testIsAlpha() {
    CharPredicate predicate = CharPredicates.IS_ALPHA;
    assertFalse(predicate.isChar('-'));
    assertFalse(predicate.isChar('1'));
    assertFalse(predicate.isChar('_'));
    assertTrue(predicate.isChar('a'));
    assertTrue(predicate.isChar('Z'));
    assertEquals("[a-zA-Z]", predicate.toString());
  }

  @Test
  public void testIsAlpha_() {
    CharPredicate predicate = CharPredicates.IS_ALPHA_;
    assertFalse(predicate.isChar('-'));
    assertFalse(predicate.isChar('1'));
    assertTrue(predicate.isChar('_'));
    assertTrue(predicate.isChar('a'));
    assertTrue(predicate.isChar('Z'));
    assertEquals("[a-zA-Z_]", predicate.toString());
  }

  @Test
  public void testIsAlphaNumeric() {
    CharPredicate predicate = CharPredicates.IS_ALPHA_NUMERIC;
    assertFalse(predicate.isChar('-'));
    assertFalse(predicate.isChar('_'));
    assertTrue(predicate.isChar('1'));
    assertTrue(predicate.isChar('a'));
    assertTrue(predicate.isChar('Z'));
    assertEquals("[0-9a-zA-Z]", predicate.toString());
  }

  @Test
  public void testIsAlphaNumeric_() {
    CharPredicate predicate = CharPredicates.IS_ALPHA_NUMERIC_;
    assertFalse(predicate.isChar('-'));
    assertTrue(predicate.isChar('1'));
    assertTrue(predicate.isChar('_'));
    assertTrue(predicate.isChar('a'));
    assertTrue(predicate.isChar('Z'));
    assertEquals("[0-9a-zA-Z_]", predicate.toString());
  }

  @Test
  public void testIsLetter() {
    CharPredicate predicate = CharPredicates.IS_LETTER;
    assertFalse(predicate.isChar('-'));
    assertFalse(predicate.isChar('1'));
    assertFalse(predicate.isChar('_'));
    assertTrue(predicate.isChar('a'));
    assertTrue(predicate.isChar('Z'));
    assertEquals("letter", predicate.toString());
  }

  @Test
  public void testAlways() {
    assertTrue(CharPredicates.ALWAYS.isChar('a'));
    assertTrue(CharPredicates.ALWAYS.isChar('>'));
    assertTrue(CharPredicates.ALWAYS.isChar('0'));
    assertEquals("any character", CharPredicates.ALWAYS.toString());
  }

  @Test
  public void testNever() {
    assertFalse(CharPredicates.NEVER.isChar('a'));
    assertFalse(CharPredicates.NEVER.isChar('>'));
    assertFalse(CharPredicates.NEVER.isChar('0'));
    assertEquals("none", CharPredicates.NEVER.toString());
  }

  @Test
  public void testNot() {
    assertFalse(CharPredicates.not(CharPredicates.ALWAYS).isChar('a'));
    assertTrue(CharPredicates.not(CharPredicates.NEVER).isChar('a'));
    assertEquals("^any character", CharPredicates.not(CharPredicates.ALWAYS).toString());
  }

  @Test
  public void testAnd() {
    assertSame(CharPredicates.ALWAYS, CharPredicates.and());
    assertSame(CharPredicates.IS_ALPHA, CharPredicates.and(CharPredicates.IS_ALPHA));
    assertFalse(CharPredicates.and(CharPredicates.ALWAYS, CharPredicates.NEVER).isChar('a'));
    assertFalse(CharPredicates.and(CharPredicates.NEVER, CharPredicates.ALWAYS).isChar('a'));
    assertFalse(CharPredicates.and(CharPredicates.NEVER, CharPredicates.NEVER).isChar('a'));
    assertTrue(CharPredicates.and(CharPredicates.ALWAYS, CharPredicates.ALWAYS).isChar('a'));
    assertFalse(CharPredicates.and(CharPredicates.ALWAYS, CharPredicates.NEVER, CharPredicates.ALWAYS).isChar('a'));
    assertFalse(CharPredicates.and(CharPredicates.NEVER, CharPredicates.ALWAYS, CharPredicates.ALWAYS).isChar('a'));
    assertFalse(CharPredicates.and(CharPredicates.NEVER, CharPredicates.NEVER, CharPredicates.NEVER).isChar('a'));
    assertTrue(CharPredicates.and(CharPredicates.ALWAYS, CharPredicates.ALWAYS, CharPredicates.ALWAYS).isChar('a'));
    assertEquals("any character and none", CharPredicates.and(CharPredicates.ALWAYS, CharPredicates.NEVER).toString());
    assertEquals("any character and none and any character", CharPredicates.and(CharPredicates.ALWAYS, CharPredicates.NEVER, CharPredicates.ALWAYS).toString());
  }

  @Test
  public void testOr() {
    assertSame(CharPredicates.NEVER, CharPredicates.or());
    assertSame(CharPredicates.IS_ALPHA, CharPredicates.or(CharPredicates.IS_ALPHA));
    assertTrue(CharPredicates.or(CharPredicates.ALWAYS, CharPredicates.NEVER).isChar('a'));
    assertTrue(CharPredicates.or(CharPredicates.NEVER, CharPredicates.ALWAYS).isChar('a'));
    assertTrue(CharPredicates.or(CharPredicates.ALWAYS, CharPredicates.ALWAYS).isChar('a'));
    assertFalse(CharPredicates.or(CharPredicates.NEVER, CharPredicates.NEVER).isChar('a'));
    assertTrue(CharPredicates.or(CharPredicates.ALWAYS, CharPredicates.NEVER, CharPredicates.NEVER).isChar('a'));
    assertTrue(CharPredicates.or(CharPredicates.NEVER, CharPredicates.NEVER, CharPredicates.ALWAYS).isChar('a'));
    assertTrue(CharPredicates.or(CharPredicates.ALWAYS, CharPredicates.ALWAYS, CharPredicates.ALWAYS).isChar('a'));
    assertFalse(CharPredicates.or(CharPredicates.NEVER, CharPredicates.NEVER, CharPredicates.NEVER).isChar('a'));
    assertEquals("any character or none", CharPredicates.or(CharPredicates.ALWAYS, CharPredicates.NEVER).toString());
    assertEquals("any character or none or any character", CharPredicates.or(CharPredicates.ALWAYS, CharPredicates.NEVER, CharPredicates.ALWAYS).toString());
  }
}
