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

package org.jparsec;

import static org.jparsec.Asserts.assertFailure;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.jparsec.internal.util.Lists;
import org.jparsec.pattern.CharPredicate;
import org.jparsec.pattern.Pattern;
import org.junit.Assert;
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
    return Arrays.asList(new Object[] {Parser.Mode.PRODUCTION}, new Object[] {Parser.Mode.DEBUG});
  }

  private final Parser.Mode mode;

  public IndentationTest(Parser.Mode mode) {
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
    Assert.assertEquals(Pattern.MISMATCH, pattern.match("", 0, 0));
    Assert.assertEquals(Pattern.MISMATCH, pattern.match("a", 0, 1));
    Assert.assertEquals(Pattern.MISMATCH, pattern.match("\\a", 0, 2));
    Assert.assertEquals(Pattern.MISMATCH, pattern.match("\\ \t", 0, 3));
    assertEquals(2, pattern.match("\\\n", 0, 2));
    assertEquals(6, pattern.match("\\  \t\r\n", 0, 6));
  }

  @Test
  public void testInlineWhitespaces() {
    Pattern pattern = Indentation.INLINE_WHITESPACES;
    Assert.assertEquals(Pattern.MISMATCH, pattern.match("", 0, 0));
    Assert.assertEquals(Pattern.MISMATCH, pattern.match("a", 0, 1));
    Assert.assertEquals(Pattern.MISMATCH, pattern.match("\n", 0, 1));
    assertEquals(1, pattern.match(" ", 0, 1));
    assertEquals(2, pattern.match("  ", 0, 2));
    assertEquals(4, pattern.match("  \t\r", 0, 4));
  }

  @Test
  public void testWhitespaces() {
    Parser<Void> scanner = Indentation.WHITESPACES;
    assertEquals("whitespaces", scanner.toString());
    assertNull(scanner.parse("  \r\t\\ \t\n \r", mode));
    Asserts.assertFailure(mode, scanner, " \r\n", 1, 3, "EOF expected, \n encountered.");
  }

  @Test
  public void testAnalyzeIndentations() {
    assertEquals(tokenList(), analyze());
    assertEquals(tokenList("foo"), analyze("foo"));
    assertEquals(tokenList("foo", "bar"), analyze("foo", "bar"));
    assertEquals(tokenList("foo", 1, "bar"), analyze("foo", "\n", "bar"));
    assertEquals(tokenList("foo", 4, "bar"), analyze("foo", "\n", 2, "\n", "bar"));
    assertEquals(tokenList("foo", 2, Indentation.Punctuation.INDENT, "bar", 1, Indentation.Punctuation.OUTDENT, 1, "baz"),
        analyze("foo", "\n", 1, "bar", "\n", "\n", "baz"));
    assertEquals(tokenList(2, "foo", "bar"), analyze(2, "foo", "bar"));
    assertEquals(tokenList(2, "foo", 2, "bar"), analyze(2, "foo", "\n", 1, "bar"));
    assertEquals(tokenList("foo", 2, Indentation.Punctuation.INDENT, "bar", Indentation.Punctuation.OUTDENT),
        analyze("foo", "\n", 1, "bar"));
    assertEquals(tokenList("foo", 2, Indentation.Punctuation.INDENT, "bar", 1, Indentation.Punctuation.OUTDENT, 2, Indentation.Punctuation.INDENT, "baz", Indentation.Punctuation.OUTDENT),
        analyze("foo", "\n", 1, "bar", "\n", "\n", 1, "baz"));
    assertEquals(tokenList("foo", 2, Indentation.Punctuation.INDENT, "bar", 2, "bar2", 4, Indentation.Punctuation.INDENT, "baz", Indentation.Punctuation.OUTDENT, Indentation.Punctuation.OUTDENT),
        analyze("foo", "\n", 1, "bar", "\n", 1, "bar2", "\n", 3, "baz"));
    assertEquals(tokenList(
        "foo", 2, Indentation.Punctuation.INDENT, "bar", 4, Indentation.Punctuation.INDENT, "baz", 3, Indentation.Punctuation.OUTDENT, Indentation.Punctuation.INDENT, "end", Indentation.Punctuation.OUTDENT, Indentation.Punctuation.OUTDENT),
        analyze("foo", "\n", 1, "bar", "\n", 3, "baz", "\n", 2, "end"));
  }

  @Test
  public void testIndent() {
    Parser<Token> parser = new Indentation().indent();
    assertEquals(new Token(0, 0, Indentation.Punctuation.INDENT),
        parser.from(Parsers.constant(tokenList(Indentation.Punctuation.INDENT))).parse("", mode));
    Asserts.assertFailure(mode, parser.from(Parsers.constant(tokenList(Indentation.Punctuation.OUTDENT))), "",
        1, 1, "INDENT expected, OUTDENT encountered.");
  }

  @Test
  public void testOutdent() {
    Parser<Token> parser = new Indentation().outdent();
    assertEquals(new Token(0, 0, Indentation.Punctuation.OUTDENT),
        parser.from(Parsers.constant(tokenList(Indentation.Punctuation.OUTDENT))).parse("", mode));
    Asserts.assertFailure(mode, parser.from(Parsers.constant(tokenList(Indentation.Punctuation.INDENT))), "",
        1, 1, "OUTDENT expected, INDENT encountered.");
  }

  @Test
  public void testLexer() {
    Parser<List<Token>> parser =
        new Indentation().lexer(Scanners.IDENTIFIER, Indentation.WHITESPACES.optional(null));
    assertEquals(
        tokenList("foo", 7, "bar", 4, Indentation.Punctuation.INDENT, "baz", 4, Indentation.Punctuation.INDENT, "bah", 1, "bah", Indentation.Punctuation.OUTDENT, Indentation.Punctuation.OUTDENT),
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
