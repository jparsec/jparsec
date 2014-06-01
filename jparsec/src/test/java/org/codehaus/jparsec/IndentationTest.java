/*****************************************************************************
 * Copyright (C) Codehaus.org                                                *
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
import static org.codehaus.jparsec.Asserts.assertParser;
import static org.codehaus.jparsec.Asserts.assertScanner;
import static org.codehaus.jparsec.Indentation.Punctuation.INDENT;
import static org.codehaus.jparsec.Indentation.Punctuation.OUTDENT;
import static org.codehaus.jparsec.pattern.Pattern.MISMATCH;

import java.util.List;

import junit.framework.TestCase;

import org.codehaus.jparsec.pattern.CharPredicate;
import org.codehaus.jparsec.pattern.Pattern;
import org.codehaus.jparsec.util.Lists;

/**
 * Unit test for {@link Indentation}.
 * 
 * @author benyu
 */
public class IndentationTest extends TestCase {
  
  public void testInlineWhitespace() {
    CharPredicate predicate = Indentation.INLINE_WHITESPACE;
    assertTrue(predicate.isChar(' '));
    assertTrue(predicate.isChar('\t'));
    assertTrue(predicate.isChar('\r'));
    assertFalse(predicate.isChar('\n'));
    assertEquals("whitespace", predicate.toString());
  }
  
  public void testLineContinuation() {
    Pattern pattern = Indentation.LINE_CONTINUATION;
    assertEquals(MISMATCH, pattern.match("", 0, 0));
    assertEquals(MISMATCH, pattern.match("a", 0, 1));
    assertEquals(MISMATCH, pattern.match("\\a", 0, 2));
    assertEquals(MISMATCH, pattern.match("\\ \t", 0, 3));
    assertEquals(2, pattern.match("\\\n", 0, 2));
    assertEquals(6, pattern.match("\\  \t\r\n", 0, 6));
  }
  
  public void testInlineWhitespaces() {
    Pattern pattern = Indentation.INLINE_WHITESPACES;
    assertEquals(MISMATCH, pattern.match("", 0, 0));
    assertEquals(MISMATCH, pattern.match("a", 0, 1));
    assertEquals(MISMATCH, pattern.match("\n", 0, 1));
    assertEquals(1, pattern.match(" ", 0, 1));
    assertEquals(2, pattern.match("  ", 0, 2));
    assertEquals(4, pattern.match("  \t\r", 0, 4));
  }
  
  public void testWhitespaces() {
    Parser<Void> scanner = Indentation.WHITESPACES;
    assertEquals("whitespaces", scanner.toString());
    assertScanner(scanner, "  \r\t\\ \t\n \r");
    assertFailure(scanner, " \r\n", 1, 3, "EOF expected, \n encountered.");
  }
  
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
  
  public void testIndent() {
    Parser<Token> parser = new Indentation().indent();
    assertParser(parser.from(Parsers.constant(tokenList(INDENT))),
        "", new Token(0, 0, INDENT));
    assertFailure(parser.from(Parsers.constant(tokenList(OUTDENT))), "",
        1, 1, "INDENT expected, OUTDENT encountered.");
  }
  
  public void testOutdent() {
    Parser<Token> parser = new Indentation().outdent();
    assertParser(parser.from(Parsers.constant(tokenList(OUTDENT))),
        "", new Token(0, 0, OUTDENT));
    assertFailure(parser.from(Parsers.constant(tokenList(INDENT))), "",
        1, 1, "OUTDENT expected, INDENT encountered.");
  }
  
  public void testLexer() {
    Parser<List<Token>> parser =
        new Indentation().lexer(Scanners.IDENTIFIER, Indentation.WHITESPACES.optional());
    assertEquals("lexer", parser.toString());
    assertParser(parser, "foo \\ \n\\\n bar \n  baz\n   bah bah ",
        tokenList("foo", 7, "bar", 4, INDENT, "baz", 4, INDENT, "bah", 1, "bah", OUTDENT, OUTDENT));
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
