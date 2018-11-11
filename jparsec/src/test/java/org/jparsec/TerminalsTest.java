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

import org.jparsec.Tokens.Tag;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

import static java.util.Arrays.asList;
import static org.jparsec.Asserts.assertFailure;
import static org.jparsec.Asserts.assertParser;
import static org.jparsec.Scanners.WHITESPACES;
import static org.junit.Assert.*;

/**
 * Unit test for {@link Terminals}.
 * 
 * @author Ben Yu
 */
@RunWith(Parameterized.class)
public class TerminalsTest {

  @Parameterized.Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(new Object[] {Parser.Mode.PRODUCTION}, new Object[] {Parser.Mode.DEBUG});
  }

  private final Parser.Mode mode;

  public TerminalsTest(Parser.Mode mode) {
    this.mode = mode;
  }

  @Test
  public void testSingleQuoteChar() {
    assertEquals((Object) 'a', Terminals.CharLiteral.SINGLE_QUOTE_TOKENIZER.parse("'a'", mode));
    assertEquals((Object) '\'', Terminals.CharLiteral.SINGLE_QUOTE_TOKENIZER.parse("'\\''", mode));
  }

  @Test
  public void testDoubleQuoteString() {
    assertEquals("a\r\n\t",
        Terminals.StringLiteral.DOUBLE_QUOTE_TOKENIZER.parse("\"a\\r\\n\\t\"", mode));
    assertEquals("\"", Terminals.StringLiteral.DOUBLE_QUOTE_TOKENIZER.parse("\"\\\"\"", mode));
  }

  @Test
  public void testSingleQuoteString() {
    assertEquals("ab", Terminals.StringLiteral.SINGLE_QUOTE_TOKENIZER.parse("'ab'", mode));
    assertEquals("a'b", Terminals.StringLiteral.SINGLE_QUOTE_TOKENIZER.parse("'a''b'", mode));
  }

  @Test
  public void testDecimalLiteralTokenizer() {
    assertEquals(Tokens.decimalLiteral("1"), Terminals.DecimalLiteral.TOKENIZER.parse("1", mode));
    assertEquals(Tokens.decimalLiteral("01"), Terminals.DecimalLiteral.TOKENIZER.parse("01"));
    assertEquals(Tokens.decimalLiteral("09"), Terminals.DecimalLiteral.TOKENIZER.parse("09", mode));
    assertEquals(Tokens.decimalLiteral("12"), Terminals.DecimalLiteral.TOKENIZER.parse("12", mode));
    assertEquals(Tokens.decimalLiteral("12.3"), Terminals.DecimalLiteral.TOKENIZER.parse("12.3", mode));
    assertEquals(Tokens.decimalLiteral("0.1"), Terminals.DecimalLiteral.TOKENIZER.parse("0.1", mode));
    assertEquals(Tokens.decimalLiteral(".2"), Terminals.DecimalLiteral.TOKENIZER.parse(".2", mode));
    assertFailure(mode, Terminals.DecimalLiteral.TOKENIZER, "12x", 1, 3, "EOF expected, x encountered.");
  }

  @Test
  public void testIntegerLiteralTokenizer() {
    assertEquals(Tokens.fragment("1", Tag.INTEGER), Terminals.IntegerLiteral.TOKENIZER.parse("1"));
    assertEquals(Tokens.fragment("12", Tag.INTEGER), Terminals.IntegerLiteral.TOKENIZER.parse("12"));
    assertEquals(Tokens.fragment("0", Tag.INTEGER), Terminals.IntegerLiteral.TOKENIZER.parse("0"));
    assertEquals(Tokens.fragment("01", Tag.INTEGER), Terminals.IntegerLiteral.TOKENIZER.parse("01"));
    assertFailure(mode, Terminals.IntegerLiteral.TOKENIZER, "12x", 1, 3, "EOF expected, x encountered.");
  }

  @Test
  public void testScientificNumberLiteralTokenizer() {
    assertEquals(Tokens.scientificNotation("1", "2"), Terminals.ScientificNumberLiteral.TOKENIZER.parse("1E2"));
    assertEquals(Tokens.scientificNotation("1", "2"), Terminals.ScientificNumberLiteral.TOKENIZER.parse("1e+2"));
    assertEquals(Tokens.scientificNotation("10", "-2"), Terminals.ScientificNumberLiteral.TOKENIZER.parse("10E-2"));
    assertFailure(mode, Terminals.ScientificNumberLiteral.TOKENIZER,
        "1e2x", 1, 4, "EOF expected, x encountered.");
  }

  @Test
  public void testLongLiteralDecTokenizer() {
    assertEquals((Object) 1L, Terminals.LongLiteral.DEC_TOKENIZER.parse("1"));
    assertEquals((Object) 10L, Terminals.LongLiteral.DEC_TOKENIZER.parse("10"));
    assertFailure(mode, Terminals.LongLiteral.DEC_TOKENIZER, "0", 1, 1);
    assertFailure(mode, Terminals.LongLiteral.DEC_TOKENIZER, "12x", 1, 3, "EOF expected, x encountered.");
  }

  @Test
  public void testLongLiteralHexTokenizer() {
    assertEquals((Object) 0L, Terminals.LongLiteral.HEX_TOKENIZER.parse("0x0"));
    assertEquals((Object) 0X10L, Terminals.LongLiteral.HEX_TOKENIZER.parse("0X10"));
    assertEquals((Object) 0X1AL, Terminals.LongLiteral.HEX_TOKENIZER.parse("0X1A"));
    assertEquals((Object) 0XFFL, Terminals.LongLiteral.HEX_TOKENIZER.parse("0XFf"));
    assertFailure(mode, Terminals.LongLiteral.HEX_TOKENIZER, "0", 1, 1);
    assertFailure(mode, Terminals.LongLiteral.HEX_TOKENIZER, "1", 1, 1);
    assertFailure(mode, Terminals.LongLiteral.HEX_TOKENIZER,
        "0x12x", 1, 5, "EOF expected, x encountered.");
  }

  @Test
  public void testTokenizeHexAsLong_throwsIfStringIsTooShort() {
    try {
      NumberLiteralsTranslator.tokenizeHexAsLong("0x");
      fail();
    } catch (IllegalStateException e) {}
  }

  @Test
  public void testLongLiteralOctTokenizer() {
    assertEquals((Object) 0L, Terminals.LongLiteral.OCT_TOKENIZER.parse("0"));
    assertEquals((Object) 15L, Terminals.LongLiteral.OCT_TOKENIZER.parse("017"));
    assertFailure(mode, Terminals.LongLiteral.OCT_TOKENIZER, "1", 1, 1);
    assertFailure(mode, Terminals.LongLiteral.OCT_TOKENIZER, "0X1", 1, 2);
    assertFailure(mode, Terminals.LongLiteral.OCT_TOKENIZER, "08", 1, 2);
    assertFailure(mode, Terminals.LongLiteral.OCT_TOKENIZER, "01x", 1, 3, "EOF expected, x encountered.");
  }

  @Test
  public void testLongLiteralTokenizer() {
    assertEquals((Object) 0L, Terminals.LongLiteral.TOKENIZER.parse("0"));
    assertEquals((Object) 8L, Terminals.LongLiteral.TOKENIZER.parse("010"));
    assertEquals((Object) 12L, Terminals.LongLiteral.TOKENIZER.parse("12"));
    assertEquals((Object) 16L, Terminals.LongLiteral.TOKENIZER.parse("0X10"));
    assertEquals((Object) 9L, Terminals.LongLiteral.TOKENIZER.parse("9"));
    assertFailure(mode, Terminals.LongLiteral.TOKENIZER, "1x", 1, 2, "EOF expected, x encountered.");
  }

  @Test
  public void testIdentifierTokenizer() {
    assertEquals(Tokens.identifier("foo"), Terminals.Identifier.TOKENIZER.parse("foo"));
    assertEquals(Tokens.identifier("foo1"), Terminals.Identifier.TOKENIZER.parse("foo1"));
    assertEquals(Tokens.identifier("FOO"), Terminals.Identifier.TOKENIZER.parse("FOO"));
    assertEquals(Tokens.identifier("FOO_2"), Terminals.Identifier.TOKENIZER.parse("FOO_2"));
    assertEquals(Tokens.identifier("_foo"), Terminals.Identifier.TOKENIZER.parse("_foo"));
    assertFailure(mode, Terminals.Identifier.TOKENIZER, "1foo", 1, 1);
  }

  @Test
  public void testCharLiteralParser() {
    assertEquals((Object) 'a', Terminals.CharLiteral.PARSER.from(Terminals.CharLiteral.SINGLE_QUOTE_TOKENIZER, WHITESPACES).parse("'a'"));
  }

  @Test
  public void testLongLiteralParser() {
    assertEquals((Object) 1L, Terminals.LongLiteral.PARSER.from(Terminals.LongLiteral.DEC_TOKENIZER, WHITESPACES).parse("1"));
  }

  @Test
  public void testStringLiteralParser() {
    assertEquals("abc", Terminals.StringLiteral.PARSER.from(
    Terminals.StringLiteral.SINGLE_QUOTE_TOKENIZER, WHITESPACES).parse("'abc'"));
  }

  @Test
  public void testIdentifierParser() {
    assertEquals("foo", Terminals.Identifier.PARSER.from(Terminals.Identifier.TOKENIZER, WHITESPACES).parse("foo"));
  }

  @Test
  public void testIntegerLiteralParser() {
    assertEquals("123", Terminals.IntegerLiteral.PARSER.from(Terminals.IntegerLiteral.TOKENIZER, WHITESPACES).parse("123"));
  }

  @Test
  public void testDecimalLiteralParser() {
    assertEquals("1.23", Terminals.DecimalLiteral.PARSER.from(Terminals.DecimalLiteral.TOKENIZER, WHITESPACES).parse("1.23"));
  }

  @Test
  public void testFromFragment() {
    assertEquals("", Terminals.fromFragment().toString());
    assertEquals("1", Terminals.fromFragment(1).toString());
    TokenMap<String> fromToken = Terminals.fromFragment("foo", 1);
    assertEquals("[foo, 1]", fromToken.toString());
    assertEquals("test", fromToken.map(new Token(0, 3, Tokens.fragment("test", "foo"))));
    assertEquals("test", fromToken.map(new Token(0, 3, Tokens.fragment("test", 1))));
    assertNull(fromToken.map(new Token(0, 3, Tokens.fragment("test", "bar"))));
    assertNull(fromToken.map(new Token(0, 3, Tokens.fragment("test", 2))));
  }

  @Test
  public void testToken_noTokenName() {
    Terminals terminals = Terminals.operators("+", "-");
    Parser<Token> parser = terminals.token().from(terminals.tokenizer(), WHITESPACES);
    assertFailure(mode, parser, "+", 1, 1);
  }

  @Test
  public void testToken_oneTokenNameOnly() {
    Terminals terminals = Terminals.operators("+", "-");
    Parser<Token> parser =
        terminals.token("+").from(terminals.tokenizer(), WHITESPACES);
    assertEquals(new Token(0, 1, Tokens.reserved("+")), parser.parse("+"));
    assertFailure(mode, parser, "-", 1, 1, "+ expected, - encountered.");
  }

  @Test
  public void testToken_tokenNamesListed() {
    Terminals terminals = Terminals.operators("+", "-");
    Parser<Token> parser = terminals.token("+", "-").from(terminals.tokenizer(), WHITESPACES);
    assertEquals(new Token(0, 1, Tokens.reserved("+")), parser.parse("+"));
    assertEquals(new Token(0, 1, Tokens.reserved("-")), parser.parse("-"));
    assertFailure(mode, parser, "*", 1, 1, "+ or - expected, * encountered.");
  }

  @Test
  public void testPhrase() {
    String[] keywords = { "hello", "world", "hell" };
    Terminals terminals = Terminals.operators().words(Scanners.IDENTIFIER).keywords(asList(keywords)).build();
    Parser<?> parser =
        terminals.phrase("hello", "world").from(terminals.tokenizer(), Scanners.WHITESPACES);
    parser.parse("hello   world");
    assertFailure(mode, parser, "hello hell", 1, 7, "world");
    assertFailure(mode, parser, "hell", 1, 1, "hello world");
    assertParser(mode, parser.optional(null), "hello hell", null, "hello hell");
  }

  @Test
  public void testCaseSensitive() {
    String[] keywords = { "foo", "bar", "baz" };
    Terminals terminals = Terminals.operators("+", "-").words(Scanners.IDENTIFIER).keywords(asList(keywords)).build();
    Parser<Token> parser =
        terminals.token("+", "-", "foo", "bar").from(terminals.tokenizer(), WHITESPACES);
    assertEquals(new Token(0, 1, Tokens.reserved("+")), parser.parse("+"));
    assertEquals(new Token(0, 1, Tokens.reserved("-")), parser.parse("-"));
    assertEquals(new Token(0, 3, Tokens.reserved("foo")), parser.parse("foo"));
    assertEquals(new Token(0, 3, Tokens.reserved("bar")), parser.parse("bar"));
    assertFailure(mode, parser, "baz", 1, 1, "+, -, foo or bar expected, baz encountered.");
    assertFailure(mode, parser, "Foo", 1, 1, "+, -, foo or bar expected, Foo encountered.");
    assertFailure(mode, parser, "123", 1, 1, "+, -, foo or bar expected, 1 encountered.");
    assertEquals("FOO", Terminals.Identifier.PARSER.from(terminals.tokenizer(), WHITESPACES).parse("FOO"));
  }

  @Test
  public void testCaseInsensitive() {
    String[] keywords = { "foo", "bar", "baz" };
    Terminals terminals =
        Terminals.operators("+", "-").words(Scanners.IDENTIFIER).caseInsensitiveKeywords(asList(keywords)).build();
    Parser<Token> parser =
        terminals.token("+", "-", "foo", "bar").from(terminals.tokenizer(), WHITESPACES);
    assertEquals(new Token(0, 1, Tokens.reserved("+")), parser.parse("+"));
    assertEquals(new Token(0, 1, Tokens.reserved("-")), parser.parse("-"));
    assertEquals(new Token(0, 3, Tokens.reserved("foo")), parser.parse("foo"));
    assertEquals(new Token(0, 3, Tokens.reserved("foo")), parser.parse("Foo"));
    assertEquals(new Token(0, 3, Tokens.reserved("bar")), parser.parse("bar"));
    assertFailure(mode, parser, "baz", 1, 1, "+, -, foo or bar expected, baz encountered.");
    assertFailure(mode, parser, "123", 1, 1, "+, -, foo or bar expected, 1 encountered.");
    assertEquals("xxx", Terminals.Identifier.PARSER.from(terminals.tokenizer(), WHITESPACES).parse("xxx"));
  }

  @Test
  public void testCaseSensitive_withScanner() {
    Terminals terminals = Terminals
        .operators("+", "-")
        .words(Scanners.INTEGER)
        .keywords("12", "34")
        .build();
  Parser<Token> parser =
      terminals.token("+", "-", "12", "34").from(terminals.tokenizer(), WHITESPACES);
  assertEquals(new Token(0, 1, Tokens.reserved("+")), parser.parse("+"));
  assertEquals(new Token(0, 1, Tokens.reserved("-")), parser.parse("-"));
  assertEquals(new Token(0, 2, Tokens.reserved("12")), parser.parse("12"));
  assertEquals(new Token(0, 2, Tokens.reserved("34")), parser.parse("34"));
  assertFailure(mode, parser, "foo", 1, 1, "+, -, 12 or 34 expected, f encountered.");
  assertFailure(mode, parser, "123", 1, 1, "+, -, 12 or 34 expected, 123 encountered.");
  assertEquals("123", Terminals.Identifier.PARSER.from(terminals.tokenizer(), WHITESPACES).parse("123"));
  }

  @Test
  public void testCaseInsensitive_withScanner() {
    Terminals terminals = Terminals
        .operators("+", "-")
        .words(Scanners.INTEGER)
        .caseInsensitiveKeywords("12", "34")
        .build();
  Parser<Token> parser =
      terminals.token("+", "-", "12", "34").from(terminals.tokenizer(), WHITESPACES);
  assertEquals(new Token(0, 1, Tokens.reserved("+")), parser.parse("+"));
  assertEquals(new Token(0, 1, Tokens.reserved("-")), parser.parse("-"));
  assertEquals(new Token(0, 2, Tokens.reserved("12")), parser.parse("12"));
  assertEquals(new Token(0, 2, Tokens.reserved("34")), parser.parse("34"));
  assertFailure(mode, parser, "foo", 1, 1, "+, -, 12 or 34 expected, f encountered.");
  assertFailure(mode, parser, "123", 1, 1, "+, -, 12 or 34 expected, 123 encountered.");
  assertEquals("123", Terminals.Identifier.PARSER.from(terminals.tokenizer(), WHITESPACES).parse("123"));
  }

  @Test
  public void testCheckDup() {
    Terminals.checkDup(asList("a", "b"), asList("+", "-"));
    Terminals.checkDup(asList("a", "b"), asList("A", "B"));
    assertDup(asList("a", "b"), asList("x", "b"));
  }
  
  private static void assertDup(Iterable<String> a, Iterable<String> b) {
    try {
      Terminals.checkDup(a, b);
      fail();
    } catch (IllegalArgumentException e) {}
  }
}
