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

package org.codehaus.jparsec;

import static org.codehaus.jparsec.Asserts.assertFailure;
import static org.codehaus.jparsec.Indentation.Punctuation.INDENT;
import static org.codehaus.jparsec.Indentation.Punctuation.OUTDENT;
import static org.codehaus.jparsec.pattern.Pattern.MISMATCH;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.codehaus.jparsec.Parser.Mode;
import org.codehaus.jparsec.internal.util.Lists;
import org.codehaus.jparsec.pattern.CharPredicate;
import org.codehaus.jparsec.pattern.Pattern;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/**
 * Unit test for {@link Indentation}.
 * 
 * @author benyu
 */
@RunWith(Parameterized.class)
public class IndentationTest {

  @Parameterized.Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(new Object[] {Mode.PRODUCTION}, new Object[] {Mode.DEBUG});
  }

  private final Mode mode;

  public IndentationTest(Mode mode) {
    this.mode = mode;
  }

  @Test
  public void testInlineWhitespace() {
    CharPredicate predicate = Indentation.INLINE_WHITESPACE;
    assertTrue(predicate.isChar(' '));
    assertTrue(predicate.isChar('\t'));
    assertTrue(predicate.isChar('\r'));
    assertFalse(predicate.isChar('\n'));
    assertEquals("whitespace", predicate.toString());
  }

  @Test
  public void testLineContinuation() {
    Pattern pattern = Indentation.LINE_CONTINUATION;
    assertEquals(MISMATCH, pattern.match("", 0, 0));
    assertEquals(MISMATCH, pattern.match("a", 0, 1));
    assertEquals(MISMATCH, pattern.match("\\a", 0, 2));
    assertEquals(MISMATCH, pattern.match("\\ \t", 0, 3));
    assertEquals(2, pattern.match("\\\n", 0, 2));
    assertEquals(6, pattern.match("\\  \t\r\n", 0, 6));
  }

  @Test
  public void testInlineWhitespaces() {
    Pattern pattern = Indentation.INLINE_WHITESPACES;
    assertEquals(MISMATCH, pattern.match("", 0, 0));
    assertEquals(MISMATCH, pattern.match("a", 0, 1));
    assertEquals(MISMATCH, pattern.match("\n", 0, 1));
    assertEquals(1, pattern.match(" ", 0, 1));
    assertEquals(2, pattern.match("  ", 0, 2));
    assertEquals(4, pattern.match("  \t\r", 0, 4));
  }

  @Test
  public void testWhitespaces() {
    Parser<Void> scanner = Indentation.WHITESPACES;
    assertEquals("whitespaces", scanner.toString());
    assertNull(scanner.parse("  \r\t\\ \t\n \r", mode));
    assertFailure(mode, scanner, " \r\n", 1, 3, "EOF expected, \n encountered.");
  }

  @Test
  public void testAnalyzeIndentations() {
    assertEquals(tokenList(), analyze());
    assertEquals(tokenList("foo"), analyze("foo"));
    assertEquals(tokenList("foo", "bar"), analyze("foo", "bar"));
    assertEquals(tokenList("foo", 1, "bar"), analyze("foo", "\n", "bar"));
    assertEquals(tokenList("foo", 4, "bar"), analyze("foo", "\n", 2, "\n", "bar"));
    assertEquals(tokenList("foo", 2, INDENT, "bar", 1, OUTDENT, 1, "baz"),
        analyze("foo", "\n", 1, "bar", "\n", "\n", "baz"));
    assertEquals(tokenList(2, "foo", "bar"), analyze(2, "foo", "bar"));
    assertEquals(tokenList(2, "foo", 2, "bar"), analyze(2, "foo", "\n", 1, "bar"));
    assertEquals(tokenList("foo", 2, INDENT, "bar", OUTDENT),
        analyze("foo", "\n", 1, "bar"));
    assertEquals(tokenList("foo", 2, INDENT, "bar", 1, OUTDENT, 2, INDENT, "baz", OUTDENT),
        analyze("foo", "\n", 1, "bar", "\n", "\n", 1, "baz"));
    assertEquals(tokenList("foo", 2, INDENT, "bar", 2, "bar2", 4, INDENT, "baz", OUTDENT, OUTDENT),
        analyze("foo", "\n", 1, "bar", "\n", 1, "bar2", "\n", 3, "baz"));
    assertEquals(tokenList(
        "foo", 2, INDENT, "bar", 4, INDENT, "baz", 3, OUTDENT, INDENT, "end", OUTDENT, OUTDENT),
        analyze("foo", "\n", 1, "bar", "\n", 3, "baz", "\n", 2, "end"));
  }

  @Test
  public void testIndent() {
    Parser<Token> parser = new Indentation().indent();
    assertEquals(new Token(0, 0, INDENT),
        parser.from(Parsers.constant(tokenList(INDENT))).parse("", mode));
    assertFailure(mode, parser.from(Parsers.constant(tokenList(OUTDENT))), "",
        1, 1, "INDENT expected, OUTDENT encountered.");
  }

  @Test
  public void testOutdent() {
    Parser<Token> parser = new Indentation().outdent();
    assertEquals(new Token(0, 0, OUTDENT),
        parser.from(Parsers.constant(tokenList(OUTDENT))).parse("", mode));
    assertFailure(mode, parser.from(Parsers.constant(tokenList(INDENT))), "",
        1, 1, "OUTDENT expected, INDENT encountered.");
  }

  @Test
  public void testLexer() {
    Parser<List<Token>> parser =
        new Indentation().lexer(Scanners.IDENTIFIER, Indentation.WHITESPACES.optional());
    assertEquals("lexer", parser.toString());
    assertEquals(
        tokenList("foo", 7, "bar", 4, INDENT, "baz", 4, INDENT, "bah", 1, "bah", OUTDENT, OUTDENT),
        parser.parse("foo \\ \n\\\n bar \n  baz\n   bah bah ", mode));
  }
  
  private static List<Token> analyze(Object... values) {
    return new Indentation().analyzeIndentations(tokenList(values), "\n");
  }

  private static List<Token> tokenList(Object... values) {
    List<Token> tokenList = Lists.arrayList(values.length);
    int index = 0;
    for (Object value : values) {
      if (value instanceof Integer) {
        index += (Integer) value;
      }
      else if (value instanceof String) {
        int length = value.toString().length();
        tokenList.add(new Token(index, length, value));
        index += length;
      }
      else {
        tokenList.add(new Token(index, 0, value));
      }
    }
    return tokenList;
  }
}
