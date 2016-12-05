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
package org.jparsec.examples.sql.parser;

import org.jparsec.Parser;
import org.jparsec.error.ParserException;
import org.jparsec.examples.sql.ast.QualifiedName;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Unit test for {@link TerminalParser}.
 * 
 * @author Ben Yu
 */
public class TerminalParserTest {

  @Test
  public void testNumber() {
    assertParser(TerminalParser.NUMBER, "1.2 ", "1.2");
    assertParser(TerminalParser.NUMBER, " 1", "1");
  }

  @Test
  public void testName() {
    assertParser(TerminalParser.NAME, "foo", "foo");
    assertParser(TerminalParser.NAME, " foo\n", "foo");
    assertParser(TerminalParser.NAME, "[foo]", "foo");
    assertParser(TerminalParser.NAME, "[ foo] /*comment*/", "foo");
    assertParser(TerminalParser.NAME, "[select] --comment", "select");
    assertFailure(TerminalParser.NAME, "select", 1, 1);
  }

  @Test
  public void testString() {
    assertParser(TerminalParser.STRING, "'foo'", "foo");
    assertParser(TerminalParser.STRING, "'foo''s'", "foo's");
    assertParser(TerminalParser.STRING, "''", "");
  }

  @Test
  public void testQualifiedName() {
    assertQualifiedName("foo", "foo");
    assertQualifiedName("foo.bar", "foo", "bar");
    assertQualifiedName("foo . bar.[select]", "foo", "bar", "select");
  }

  @Test
  public void testPhrase() {
    TerminalParser.parse(TerminalParser.phrase("inner join"), " inner join ");
    assertFailure(TerminalParser.phrase("inner join"), "[inner] join", 1, 1);
  }

  @Test
  public void testTerm() {
    TerminalParser.parse(TerminalParser.term("select"), "select");
    TerminalParser.parse(TerminalParser.term("select"), "SELECT");
    TerminalParser.parse(TerminalParser.term("select"), " SELEcT --coment");
    assertFailure(TerminalParser.term("select"), "[select]", 1, 1);
  }
  
  private static void assertQualifiedName(String source, String... names) {
    QualifiedName qname = TerminalParser.parse(TerminalParser.QUALIFIED_NAME, source);
    assertEquals(new QualifiedName(Arrays.asList(names)), qname);
  }
  
  static void assertParser(Parser<?> parser, String source, Object value) {
    assertEquals(value, TerminalParser.parse(parser, source));
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
