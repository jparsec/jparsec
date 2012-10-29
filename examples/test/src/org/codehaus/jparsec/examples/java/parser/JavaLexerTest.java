package org.codehaus.jparsec.examples.java.parser;

import junit.framework.TestCase;

import org.codehaus.jparsec.Parser;
import org.codehaus.jparsec.error.ParserException;
import org.codehaus.jparsec.examples.java.ast.expression.DecimalPointNumberLiteral;
import org.codehaus.jparsec.examples.java.ast.expression.IntegerLiteral;
import org.codehaus.jparsec.examples.java.ast.expression.NumberType;
import org.codehaus.jparsec.examples.java.ast.expression.ScientificNumberLiteral;
import org.codehaus.jparsec.examples.java.ast.expression.IntegerLiteral.Radix;

/**
 * Unit test for {@link JavaLexer}.
 * 
 * @author Ben Yu
 */
public class JavaLexerTest extends TestCase {
  
  public void testIdentifier() {
    Parser<String> scanner = JavaLexer.IDENTIFIER;
    assertEquals("foo", scanner.parse("foo"));
    assertEquals("foo_123_", scanner.parse("foo_123_"));
  }
  public void testDecimalPointScanner() {
    Parser<Void> scanner = JavaLexer.DECIMAL_POINT_SCANNER;
    scanner.parse("0.1");
    scanner.parse("1.23");
    scanner.parse(".12");
    assertFailure(scanner, "1", 1, 1);
  }
  
  public void testDecimalPointNumber() {
    Parser<DecimalPointNumberLiteral> scanner = JavaLexer.DECIMAL_POINT_NUMBER;
    assertEquals(decimal("1.0", NumberType.DOUBLE), scanner.parse("1.0"));
    assertEquals(decimal("1.0", NumberType.FLOAT), scanner.parse("1.0f"));
    assertEquals(decimal("1.23", NumberType.FLOAT), scanner.parse("1.23F"));
    assertEquals(decimal(".0", NumberType.DOUBLE), scanner.parse(".0D"));
    assertEquals(decimal("10.0", NumberType.DOUBLE), scanner.parse("10.0d"));
  }
  
  public void testScientificNumberLiteral() {
    Parser<ScientificNumberLiteral> scanner = JavaLexer.SCIENTIFIC_NUMBER_LITERAL;
    assertEquals(scientific("1e2", NumberType.DOUBLE), scanner.parse("1e2"));
    assertEquals(scientific("1e2", NumberType.DOUBLE), scanner.parse("1e2d"));
    assertEquals(scientific("1e2", NumberType.DOUBLE), scanner.parse("1e2D"));
    assertEquals(scientific("1e2", NumberType.FLOAT), scanner.parse("1e2f"));
    assertEquals(scientific("1e2", NumberType.FLOAT), scanner.parse("1e2F"));
  }
  
  public void testInteger() {
    Parser<IntegerLiteral> scanner = JavaLexer.INTEGER;
    assertEquals(integer(Radix.DEC, "123", NumberType.INT), scanner.parse("123"));
    assertEquals(integer(Radix.DEC, "10", NumberType.LONG), scanner.parse("10L"));
    assertEquals(integer(Radix.DEC, "10", NumberType.LONG), scanner.parse("10l"));
    assertEquals(integer(Radix.DEC, "1", NumberType.FLOAT), scanner.parse("1F"));
    assertEquals(integer(Radix.DEC, "1", NumberType.FLOAT), scanner.parse("1f"));
    assertEquals(integer(Radix.DEC, "1", NumberType.DOUBLE), scanner.parse("1d"));
    assertEquals(integer(Radix.DEC, "1", NumberType.DOUBLE), scanner.parse("1D"));
    assertEquals(integer(Radix.OCT, "0", NumberType.DOUBLE), scanner.parse("0d"));
    assertEquals(integer(Radix.OCT, "1", NumberType.FLOAT), scanner.parse("01f"));
    assertEquals(integer(Radix.HEX, "0D", NumberType.INT), scanner.parse("0X0D"));
    assertEquals(integer(Radix.HEX, "0D", NumberType.LONG), scanner.parse("0X0DL"));
  }
  
  private static DecimalPointNumberLiteral decimal(String number, NumberType type) {
    return new DecimalPointNumberLiteral(number, type);
  }
  
  private static IntegerLiteral integer(Radix radix, String number, NumberType type) {
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
