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

import org.jparsec.error.ParserException;
import org.junit.Assert;

import java.util.Arrays;

import static org.junit.Assert.*;

/**
 * Extra assertions.
 * 
 * @author Ben Yu
 */
public final class Asserts {
  
  public static void assertFailure(
    Parser.Mode mode, Parser<?> parser, String source, int line, int column) {
    try {
      parser.parse(source, mode);
      Assert.fail();
    } catch (ParserException e) {
      assertEquals(line, e.getLine());
      assertEquals(column, e.getColumn());
    }
  }
  
  public static void assertFailure(
    Parser.Mode mode, Parser<?> parser,
    String source, int line, int column, String expectedMessage) {
    try {
      parser.parse(source, mode);
      Assert.fail();
    } catch (ParserException e) {
      assertTrue(e.getMessage(), e.getMessage().contains(expectedMessage));
      assertEquals(line, e.getLine());
      assertEquals(column, e.getColumn());
    }
  }
  
  @SuppressWarnings("deprecation")
  public static void assertFailure(
      Parser<?> parser, String source, int line, int column,
      String module, String expectedMessage) {
    try {
      parser.parse(source, module);
      Assert.fail();
    } catch (ParserException e) {
      assertTrue(e.getMessage(), e.getMessage().contains(module));
      assertTrue(e.getMessage(), e.getMessage().contains(expectedMessage));
      assertEquals(line, e.getLine());
      assertEquals(column, e.getColumn());
    }
  }
  
  public static void assertFailure(
    Parser.Mode mode, Parser<?> parser, String source, int line, int column, Class<? extends Throwable> cause) {
    try {
      parser.parse(source, mode);
      Assert.fail();
    } catch (ParserException e) {
      assertEquals(line, e.getLine());
      assertEquals(column, e.getColumn());
      assertTrue(cause.isInstance(e.getCause()));
    }
  }
  
  public static void assertParser(
    Parser.Mode mode, Parser<?> parser, String source, Object value, String rest) {
    assertEquals(value, parser.followedBy(Scanners.string(rest))
        .parse(source, mode));
  }
  
  public static void assertArrayEquals(Object[] actual, Object... expected) {
    assertEquals(Arrays.asList(expected), Arrays.asList(actual));
  }
  
  static void assertScanner(
    Parser.Mode mode, Parser<Void> scanner, String source, String remaining) {
    assertNull(scanner.followedBy(Scanners.string(remaining)).parse(source, mode));
  }
  
  static void assertStringScanner(
    Parser.Mode mode, Parser<String> scanner, String source, String remaining) {
    assertEquals(source.substring(0, source.length() - remaining.length()),
        scanner.followedBy(Scanners.string(remaining)).parse(source, mode));
  }
  
  static void assertStringScanner(Parser.Mode mode, Parser<String> scanner, String source) {
    assertEquals(source, scanner.parse(source, mode));
  }
}
