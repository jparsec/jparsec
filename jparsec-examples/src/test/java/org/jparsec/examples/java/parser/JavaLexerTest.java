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
import org.jparsec.error.ParserException;
import org.jparsec.examples.java.ast.expression.DecimalPointNumberLiteral;
import org.jparsec.examples.java.ast.expression.IntegerLiteral;
import org.jparsec.examples.java.ast.expression.NumberType;
import org.jparsec.examples.java.ast.expression.ScientificNumberLiteral;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Unit test for {@link JavaLexer}.
 * 
 * @author Ben Yu
 */
public class JavaLexerTest {

  @Test
  public void testIdentifier() {
    Parser<String> scanner = JavaLexer.IDENTIFIER;
    assertEquals("foo", scanner.parse("foo"));
    assertEquals("foo_123_", scanner.parse("foo_123_"));
  }

  @Test
  public void testDecimalPointScanner() {
    Parser<Void> scanner = JavaLexer.DECIMAL_POINT_SCANNER;
    scanner.parse("0.1");
    scanner.parse("1.23");
    scanner.parse(".12");
    assertFailure(scanner, "1", 1, 1);
  }

  @Test
  public void testDecimalPointNumber() {
    Parser<DecimalPointNumberLiteral> scanner = JavaLexer.DECIMAL_POINT_NUMBER;
    assertEquals(decimal("1.0", NumberType.DOUBLE), scanner.parse("1.0"));
    assertEquals(decimal("1.0", NumberType.FLOAT), scanner.parse("1.0f"));
    assertEquals(decimal("1.23", NumberType.FLOAT), scanner.parse("1.23F"));
    assertEquals(decimal(".0", NumberType.DOUBLE), scanner.parse(".0D"));
    assertEquals(decimal("10.0", NumberType.DOUBLE), scanner.parse("10.0d"));
  }

  @Test
  public void testScientificNumberLiteral() {
    Parser<ScientificNumberLiteral> scanner = JavaLexer.SCIENTIFIC_NUMBER_LITERAL;
    assertEquals(scientific("1e2", NumberType.DOUBLE), scanner.parse("1e2"));
    assertEquals(scientific("1e2", NumberType.DOUBLE), scanner.parse("1e2d"));
    assertEquals(scientific("1e2", NumberType.DOUBLE), scanner.parse("1e2D"));
    assertEquals(scientific("1e2", NumberType.FLOAT), scanner.parse("1e2f"));
    assertEquals(scientific("1e2", NumberType.FLOAT), scanner.parse("1e2F"));
  }

  @Test
  public void testInteger() {
    Parser<IntegerLiteral> scanner = JavaLexer.INTEGER;
    assertEquals(integer(IntegerLiteral.Radix.DEC, "123", NumberType.INT), scanner.parse("123"));
    assertEquals(integer(IntegerLiteral.Radix.DEC, "10", NumberType.LONG), scanner.parse("10L"));
    assertEquals(integer(IntegerLiteral.Radix.DEC, "10", NumberType.LONG), scanner.parse("10l"));
    assertEquals(integer(IntegerLiteral.Radix.DEC, "1", NumberType.FLOAT), scanner.parse("1F"));
    assertEquals(integer(IntegerLiteral.Radix.DEC, "1", NumberType.FLOAT), scanner.parse("1f"));
    assertEquals(integer(IntegerLiteral.Radix.DEC, "1", NumberType.DOUBLE), scanner.parse("1d"));
    assertEquals(integer(IntegerLiteral.Radix.DEC, "1", NumberType.DOUBLE), scanner.parse("1D"));
    assertEquals(integer(IntegerLiteral.Radix.OCT, "1", NumberType.FLOAT), scanner.parse("01f"));
  }

  @Test
  public void testZero(){
    Parser<IntegerLiteral> scanner = JavaLexer.INTEGER;
    assertEquals(integer(IntegerLiteral.Radix.HEX, "0D", NumberType.INT), scanner.parse("0X0D"));
    assertEquals(integer(IntegerLiteral.Radix.HEX, "0D", NumberType.LONG), scanner.parse("0X0DL"));
    assertEquals(integer(IntegerLiteral.Radix.DEC, "0", NumberType.INT), scanner.parse("0"));
    assertEquals(integer(IntegerLiteral.Radix.DEC, "0", NumberType.DOUBLE), scanner.parse("0d"));
    assertEquals(integer(IntegerLiteral.Radix.OCT, "0", NumberType.INT), scanner.parse("00"));
  }
    
  private static DecimalPointNumberLiteral decimal(String number, NumberType type) {
    return new DecimalPointNumberLiteral(number, type);
  }
  
  private static IntegerLiteral integer(IntegerLiteral.Radix radix, String number, NumberType type) {
    return new IntegerLiteral(radix, number, type);
  }
  
  private static ScientificNumberLiteral scientific(String number, NumberType type) {
    return new ScientificNumberLiteral(number, type);
  }
  
  private static void assertFailure(Parser<?> parser, String source, int line, int column) {
    try {
      parser.parse(source);
      fail();
    } catch (ParserException e) {
      assertEquals(e.getMessage(), line, e.getLocation().line);
      assertEquals(e.getMessage(), column, e.getLocation().column);
    }
  }
}
