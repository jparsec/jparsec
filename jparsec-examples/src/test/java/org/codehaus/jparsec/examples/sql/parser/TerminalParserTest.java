package org.codehaus.jparsec.examples.sql.parser;

import org.codehaus.jparsec.Parser;
import org.codehaus.jparsec.error.ParserException;
import org.codehaus.jparsec.examples.sql.ast.QualifiedName;
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
