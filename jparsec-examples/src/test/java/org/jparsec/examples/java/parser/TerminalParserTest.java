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
package org.jparsec.examples.java.parser;

import org.jparsec.Parser;
import org.jparsec.Token;
import org.jparsec.Tokens;
import org.jparsec.error.ParserException;
import org.jparsec.examples.java.ast.expression.DecimalPointNumberLiteral;
import org.jparsec.examples.java.ast.expression.IntegerLiteral;
import org.jparsec.examples.java.ast.expression.IntegerLiteral.Radix;
import org.jparsec.examples.java.ast.expression.NumberType;
import org.jparsec.examples.java.ast.expression.ScientificNumberLiteral;
import org.junit.Assert;
import org.junit.Test;

import static org.jparsec.examples.java.parser.TerminalParser.parse;
import static org.junit.Assert.*;

/**
 * Unit test for {@link TerminalParser}.
 * 
 * @author Ben Yu
 */
public class TerminalParserTest {

  @Test
  public void testParse() {
    assertParser(TerminalParser.term("."),
        "  . /** javadoc */ /* regular doc */ \n // line comment",
        new Token(2, 1, Tokens.reserved(".")));
  }

  @Test
  public void testAdjacent() {
    assertOperator(TerminalParser.adjacent(""), "");
    assertOperator(TerminalParser.adjacent("<"), "<");
    assertOperator(TerminalParser.adjacent(">>"), ">>");
    assertOperator(TerminalParser.adjacent(">>>"), ">>>");
    assertOperator(TerminalParser.adjacent("<<"), "<<");
    assertOperator(TerminalParser.adjacent("<+>"), "<+>");
    assertFailure(TerminalParser.adjacent(">>"), "> >", 1, 4);
    assertFailure(TerminalParser.adjacent(">>"), ">+", 1, 2);
    assertParser(TerminalParser.adjacent(">>").optional(), ">+", null, ">+");
    assertOperator(TerminalParser.adjacent(">>").or(TerminalParser.adjacent(">+")), ">+");
  }

  @Test
  public void testTerm() {
    assertOperator(TerminalParser.term("<<"), "<<");
    assertOperator(TerminalParser.term(">>"), ">>");
    assertOperator(TerminalParser.term(">>>"), ">>>");
    assertOperator(TerminalParser.term("||"), "||");
    assertOperator(TerminalParser.term(">"), ">");
    TerminalParser.parse(TerminalParser.term(">>").followedBy(TerminalParser.term(">")), ">> >");
    assertFailure(TerminalParser.term(">>").followedBy(TerminalParser.term(">")), ">>>", 1, 1);
    try {
      TerminalParser.term("not exist");
      fail();
    } catch (IllegalArgumentException e) {}
  }

  @Test
  public void testLexer() {
    Parser<?> parser = TerminalParser.TOKENIZER;
    assertEquals(new ScientificNumberLiteral("1e2", NumberType.DOUBLE), parser.parse("1e2"));
    assertEquals(new ScientificNumberLiteral("1e2", NumberType.FLOAT), parser.parse("1e2f"));
    assertEquals("foo", parser.parse("\"foo\""));
    assertEquals('a', parser.parse("'a'"));
    assertEquals(Tokens.reserved("import"), parser.parse("import"));
    Assert.assertEquals(new DecimalPointNumberLiteral("1.2", NumberType.DOUBLE), parser.parse("1.2"));
    assertEquals(new IntegerLiteral(Radix.DEC, "1", NumberType.INT), parser.parse("1"));
    assertEquals(new IntegerLiteral(Radix.HEX, "1", NumberType.LONG), parser.parse("0X1L"));
    assertEquals(new IntegerLiteral(Radix.OCT, "1", NumberType.DOUBLE), parser.parse("01D"));
  }
  
  static void assertParser(Parser<?> parser, String source, Object value) {
    assertEquals(value, TerminalParser.parse(parser, source));
  }
  
  static void assertParser(Parser<?> parser, String source, Object value, String rest) {
    assertTrue(source.endsWith(rest));
    assertEquals(value,
        TerminalParser.parse(parser, source.substring(0, source.length() - rest.length())));
  }
  
  static void assertOperator(Parser<?> parser, String source) {
    Token actual = (Token) TerminalParser.parse(parser, source);
    assertEquals(0, actual.index());
    assertEquals(source.length(), actual.length());
    // TODO: do we make adjacent() call Tokens.reserved()?
    // That seems verbose unless we make Tokenizers public.
    assertEquals(source, actual.value().toString());
  }
  
  static <T> void assertResult(
      Parser<T> parser, String source, Class<? extends T> expectedType, String expectedResult) {
    assertToString(expectedType, expectedResult, parse(parser, source));
  }

  static <T> void assertToString(
      Class<? extends T> expectedType, String expectedResult, T result) {
    assertTrue(expectedType.isInstance(result));
    assertEquals(expectedResult, result.toString());
  }
  
  static void assertFailure(Parser<?> parser, String source, int line, int column) {
    assertFailure(parser, source, line, column, "");
  }
  
  static void assertFailure(
      Parser<?> parser, String source, int line, int column, String errorMessage) {
    try {
      TerminalParser.parse(parser, source);
      fail();
    } catch (ParserException e) {
      assertTrue(e.getMessage(), e.getMessage().contains(errorMessage));
      assertEquals(line, e.getLocation().line);
      assertEquals(column, e.getLocation().column);
    }
  }
}
