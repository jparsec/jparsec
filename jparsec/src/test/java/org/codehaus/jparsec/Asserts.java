package org.codehaus.jparsec;

import java.util.Arrays;

import org.codehaus.jparsec.error.ParserException;


import junit.framework.Assert;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Extra assertions.
 * 
 * @author Ben Yu
 */
public final class Asserts {
  
  public static void assertFailure(Parser<?> parser, String source, int line, int column) {
    try {
      parser.parse(source);
      Assert.fail();
    } catch (ParserException e) {
      assertEquals(line, e.getLocation().line);
      assertEquals(column, e.getLocation().column);
    }
  }
  
  public static void assertFailure(
      Parser<?> parser, String source, int line, int column, String expectedMessage) {
    try {
      parser.parse(source);
      Assert.fail();
    } catch (ParserException e) {
      assertTrue(e.getMessage(), e.getMessage().contains(expectedMessage));
      assertEquals(line, e.getLocation().line);
      assertEquals(column, e.getLocation().column);
    }
  }
  
  public static void assertFailure(
      Parser<?> parser, String source, int line, int column,
      String module, String expectedMessage) {
    try {
      parser.parse(source, module);
      Assert.fail();
    } catch (ParserException e) {
      assertTrue(e.getMessage(), e.getMessage().contains(module));
      assertTrue(e.getMessage(), e.getMessage().contains(expectedMessage));
      assertEquals(line, e.getLocation().line);
      assertEquals(column, e.getLocation().column);
    }
  }
  
  public static void assertFailure(
      Parser<?> parser, String source, int line, int column, Class<? extends Throwable> cause) {
    try {
      parser.parse(source);
      Assert.fail();
    } catch (ParserException e) {
      assertEquals(line, e.getLocation().line);
      assertEquals(column, e.getLocation().column);
      assertTrue(cause.isInstance(e.getCause()));
    }
  }
  
  public static void assertParser(Parser<?> parser, String source, Object value) {
    assertEquals(value, parser.parse(source));
  }
  
  public static void assertParser(Parser<?> parser, String source, Object value, String rest) {
    assertEquals(value, parser.followedBy(Scanners.string(rest))
        .parse(source));
  }
  
  public static void assertArrayEquals(Object[] actual, Object... expected) {
    assertEquals(Arrays.asList(expected), Arrays.asList(actual));
  }
  
  static void assertScanner(Parser<Void> scanner, String source, String remaining) {
    assertNull(scanner.followedBy(Scanners.string(remaining)).parse(source));
  }
  
  static void assertScanner(Parser<Void> scanner, String source) {
    assertNull(scanner.parse(source));
  }
  
  static void assertStringScanner(Parser<String> scanner, String source, String remaining) {
    assertEquals(source.substring(0, source.length() - remaining.length()),
        scanner.followedBy(Scanners.string(remaining)).parse(source));
  }
  
  static void assertStringScanner(Parser<String> scanner, String source) {
    assertEquals(source, scanner.parse(source));
  }
}
