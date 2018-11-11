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

import org.jparsec.pattern.CharPredicates;
import org.jparsec.pattern.Patterns;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

import static org.jparsec.Asserts.*;
import static org.jparsec.TestParsers.areChars;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Unit test for {@link Scanners}.
 * 
 * @author Ben Yu
 */
@RunWith(Parameterized.class)
public class ScannersTest {

  @Parameterized.Parameters
  public static Collection<Object[]> data() {
          return Arrays.asList(new Object[] {Parser.Mode.PRODUCTION}, new Object[] {Parser.Mode.DEBUG});
  }

  private final Parser.Mode mode;

  public ScannersTest(Parser.Mode mode) {
    this.mode = mode;
  }

  @Test
  public void testIdentifier() {
    Parser<String> scanner = Scanners.IDENTIFIER;
    assertStringScanner(mode, scanner, "abc");
    assertStringScanner(mode, scanner, "abc123");
    assertStringScanner(mode, scanner, "abc 123", " 123");
    assertStringScanner(mode, scanner, "_abc_123");
  }

  @Test
  public void testInteger() {
    Parser<String> scanner = Scanners.INTEGER;
    assertStringScanner(mode, scanner, "123");
    assertStringScanner(mode, scanner, "0");
    assertStringScanner(mode, scanner, "12.3", ".3");
  }

  @Test
  public void testDecimal() {
    Parser<String> scanner = Scanners.DECIMAL;
    assertStringScanner(mode, scanner, "123");
    assertStringScanner(mode, scanner, "0");
    assertStringScanner(mode, scanner, "12.3");
    assertStringScanner(mode, scanner, ".3");
  }

  @Test
  public void testDecInteger() {
    Parser<String> scanner = Scanners.DEC_INTEGER;
    assertStringScanner(mode, scanner, "1230");
    assertFailure(mode, scanner, "0", 1, 1, "decimal integer expected, 0 encountered.");
  }

  @Test
  public void testOctInteger() {
    Parser<String> scanner = Scanners.OCT_INTEGER;
    assertStringScanner(mode, scanner, "01270");
    assertStringScanner(mode, scanner, "0");
    assertFailure(mode, scanner, "12", 1, 1, "octal integer expected, 1 encountered.");
    assertFailure(mode, scanner, "09", 1, 2);
  }

  @Test
  public void testHexInteger() {
    Parser<String> scanner = Scanners.HEX_INTEGER;
    assertStringScanner(mode, scanner, "0X1AF");
    assertStringScanner(mode, scanner, "0xF0");
    assertFailure(mode, scanner, "1", 1, 1, "hexadecimal integer expected, 1 encountered.");
    assertFailure(mode, scanner, "01", 1, 1);
  }

  @Test
  public void testScientificNotation() {
    Parser<String> scanner = Scanners.SCIENTIFIC_NOTATION;
    assertStringScanner(mode, scanner, "0e0");
    assertStringScanner(mode, scanner, "1.0E12");
    assertStringScanner(mode, scanner, "1e+12");
    assertStringScanner(mode, scanner, "1e-12");
    assertFailure(mode, scanner, "", 1, 1, "scientific notation expected, EOF encountered.");
    assertFailure(mode, scanner, "12", 1, 1, "scientific notation expected, 1 encountered");
    assertFailure(mode, scanner, "e", 1, 1);
    assertFailure(mode, scanner, "e1", 1, 1);
  }

  @Test
  public void testMany_withCharPredicate() {
    Parser<Void> scanner = Scanners.many(CharPredicates.IS_ALPHA);
    assertScanner(mode, scanner, "abc123", "123");
    assertScanner(mode, scanner, "123", "123");
  }

  @Test
  public void testMany1_withCharPredicate() {
    Parser<Void> scanner = Scanners.many1(CharPredicates.IS_ALPHA);
    assertScanner(mode, scanner, "abc123", "123");
    assertFailure(mode, scanner, "123", 1, 1, "[a-zA-Z]+ expected, 1 encountered.");
    assertFailure(mode, scanner, "", 1, 1, "[a-zA-Z]+ expected, EOF encountered.");
  }

  @Test
  public void testMany_withPattern() {
    Parser<Void> scanner = Patterns.string("ab").many().toScanner("(ab)*");
    assertNull(scanner.parse("abab"));
    assertScanner(mode, scanner, "aba", "a");
    assertScanner(mode, scanner, "abc", "c");
    assertScanner(mode, scanner, "c", "c");
    assertNull(scanner.parse(""));
  }

  @Test
  public void testMany_withPatternThatConsumesNoInput() {
    Parser<Void> scanner = Patterns.ALWAYS.many().toScanner("*");
    assertNull(scanner.parse(""));
    assertScanner(mode, scanner, "a", "a");
  }

  @Test
  public void testMany1_withPattern() {
    Parser<Void> scanner = Patterns.string("ab").many1().toScanner("(ab)+");
    assertNull(scanner.parse("abab"));
    assertScanner(mode, scanner, "aba", "a");
    assertScanner(mode, scanner, "abc", "c");
    assertFailure(mode, scanner, "c", 1, 1, "(ab)+ expected, c encountered.");
    assertFailure(mode, scanner, "", 1, 1, "(ab)+ expected, EOF encountered.");
  }

  @Test
  public void testMany1_withPatternThatConsumesNoInput() {
    Parser<Void> scanner = Patterns.ALWAYS.many1().toScanner("+");
    assertNull(scanner.parse(""));
    assertScanner(mode, scanner, "a", "a");
  }

  @Test
  public void testString() {
    Parser<Void> scanner = Scanners.string("ab");
    assertNull(scanner.parse("ab"));
    assertScanner(mode, scanner, "abc", "c");
    assertFailure(mode, scanner, "c", 1, 1, "ab expected, c encountered.");
    assertFailure(mode, scanner, "a", 1, 1);
    assertFailure(mode, scanner, "", 1, 1);
  }

  @Test
  public void testWhitespaces() {
    Parser<Void> scanner = Scanners.WHITESPACES;
    assertEquals("whitespaces", scanner.toString());
    assertNull(scanner.parse(" \r\n"));
    assertScanner(mode, scanner, " \r\na", "a");
    assertFailure(mode, scanner, "", 1, 1);
    assertFailure(mode, scanner, "a", 1, 1);
  }

  @Test
  public void testPattern() {
    Parser<Void> scanner = Patterns.INTEGER.toScanner("integer");
    assertNull(scanner.parse("123"));
    assertScanner(mode, scanner, "12a", "a");
    assertFailure(mode, scanner, "", 1, 1, "integer expected, EOF encountered.");
    assertFailure(mode, scanner, "a", 1, 1);
  }

  @Test
  public void testStringCaseInsensitive() {
    Parser<Void> scanner = Scanners.stringCaseInsensitive("ab");
    assertNull(scanner.parse("ab"));
    assertNull(scanner.parse("AB"));
    assertNull(scanner.parse("aB"));
    assertFailure(mode, scanner, "", 1, 1, "ab expected, EOF encountered.");
    assertFailure(mode, scanner, "a", 1, 1);
  }

  @Test
  public void testAnyChar() {
    Parser<Void> scanner = Scanners.ANY_CHAR;
    assertNull(scanner.parse("a"));
    assertNull(scanner.parse("1"));
    assertNull(scanner.parse(" "));
    assertNull(scanner.parse("\n"));
    assertScanner(mode, scanner, "ab", "b");
    assertFailure(mode, scanner, "", 1, 1, "any character expected, EOF encountered.");
    assertEquals("any character", scanner.toString());
  }

  @Test
  public void testIsChar() {
    Parser<Void> scanner = Scanners.isChar('a');
    assertNull(scanner.parse("a"));
    assertScanner(mode, scanner, "abc", "bc");
    assertFailure(mode, scanner, "bc", 1, 1, "a expected, b encountered.");
    assertFailure(mode, scanner, "", 1, 1);
  }

  @Test
  public void testNotChar() {
    Parser<Void> scanner = Scanners.notChar('a');
    assertNull(scanner.parse("b"));
    assertScanner(mode, scanner, "bcd", "cd");
    assertFailure(mode, scanner, "abc", 1, 1, "^a expected, a encountered.");
    assertFailure(mode, scanner, "", 1, 1);
  }

  @Test
  public void testAmong() {
    Parser<Void> scanner = Scanners.among("ab");
    assertNull(scanner.parse("a"));
    assertNull(scanner.parse("b"));
    assertScanner(mode, scanner, "ab", "b");
    assertFailure(mode, scanner, "c", 1, 1, "[ab] expected, c encountered.");
    assertFailure(mode, scanner, "", 1, 1, "[ab] expected, EOF encountered.");
  }

  @Test
  public void testAmong_noChars() {
    Parser<Void> scanner = Scanners.among("");
    assertFailure(mode, scanner, "a", 1, 1, "none expected, a encountered.");
    assertFailure(mode, scanner, "", 1, 1, "none expected, EOF encountered.");
  }

  @Test
  public void testAmong_oneChar() {
    Parser<Void> scanner = Scanners.among("a");
    assertNull(scanner.parse("a"));
    assertScanner(mode, scanner, "ab", "b");
    assertFailure(mode, scanner, "b", 1, 1, "a expected, b encountered.");
    assertFailure(mode, scanner, "", 1, 1);
  }

  @Test
  public void testNotAmong() {
    Parser<Void> scanner = Scanners.notAmong("ab");
    assertNull(scanner.parse("0"));
    assertScanner(mode, scanner, "0a", "a");
    assertFailure(mode, scanner, "a", 1, 1, "^[ab] expected, a encountered.");
    assertFailure(mode, scanner, "b", 1, 1);
    assertFailure(mode, scanner, "", 1, 1);
  }

  @Test
  public void testNotAmong_noChars() {
    Parser<Void> scanner = Scanners.notAmong("");
    assertNull(scanner.parse("0"));
    assertScanner(mode, scanner, "ab", "b");
    assertFailure(mode, scanner, "", 1, 1, "any character expected, EOF encountered.");
  }

  @Test
  public void testNotAmong_oneChar() {
    Parser<Void> scanner = Scanners.notAmong("a");
    assertNull(scanner.parse("0"));
    assertScanner(mode, scanner, "0a", "a");
    assertFailure(mode, scanner, "a", 1, 1);
    assertFailure(mode, scanner, "", 1, 1);
  }

  @Test
  public void testLineComment() {
    Parser<Void> scanner = Scanners.lineComment("#");
    assertNull(scanner.parse("#hello world"));
    assertScanner(mode, scanner, "#hello world\n", "\n");
    assertScanner(mode, scanner, "#hello world\r\n", "\n");
    assertScanner(mode, scanner, "#\n", "\n");
    assertNull(scanner.parse("#"));
    assertFailure(mode, scanner, "", 1, 1);
    assertFailure(mode, scanner, "\n", 1, 1);
    assertFailure(mode, scanner, "a", 1, 1);
  }

  @Test
  public void testJavaLineComment() {
    Parser<Void> scanner = Scanners.JAVA_LINE_COMMENT;
    assertNull(scanner.parse("//hello"));
  }

  @Test
  public void testSqlLineComment() {
    Parser<Void> scanner = Scanners.SQL_LINE_COMMENT;
    assertNull(scanner.parse("--hello"));
  }

  @Test
  public void testHaskellLineComment() {
    Parser<Void> scanner = Scanners.HASKELL_LINE_COMMENT;
    assertNull(scanner.parse("--hello"));
  }

  @Test
  public void testDoubleQuoteString() {
    Parser<String> scanner = Scanners.DOUBLE_QUOTE_STRING;
    assertStringScanner(mode, scanner, "\"\"");
    assertStringScanner(mode, scanner, "\"a b'c\"");
    assertStringScanner(mode, scanner, "\"a\\\\\\\"1\"");
    assertFailure(mode, scanner, "", 1, 1);
    assertFailure(mode, scanner, "ab", 1, 1);
    assertFailure(mode, scanner, "\"ab", 1, 4);
    assertFailure(mode, scanner, "\"\\\"", 1, 4);
  }

  @Test
  public void testSingleQuoteString() {
    Parser<String> scanner = Scanners.SINGLE_QUOTE_STRING;
    assertStringScanner(mode, scanner, "''");
    assertStringScanner(mode, scanner, "'a'");
    assertStringScanner(mode, scanner, "'foo'");
    assertStringScanner(mode, scanner, "'foo''s day'");
  }

  @Test
  public void testSingleQuoteChar() {
    Parser<String> scanner = Scanners.SINGLE_QUOTE_CHAR;
    assertStringScanner(mode, scanner, "'a'");
    assertStringScanner(mode, scanner, "'\\a'");
    assertStringScanner(mode, scanner, "'\\\\'");
    assertStringScanner(mode, scanner, "'\\\"'");
    assertFailure(mode, scanner, "", 1, 1);
    assertFailure(mode, scanner, "ab", 1, 1);
    assertFailure(mode, scanner, "''", 1, 2);
    assertFailure(mode, scanner, "'\\'", 1, 4);
  }

  @Test
  public void testJavaDelimiter() {
    Parser<Void> scanner = Scanners.JAVA_DELIMITER;
    assertNull(scanner.parse(""));
    assertNull(scanner.parse(" "));
    assertNull(scanner.parse("//comment"));
    assertNull(scanner.parse("/*comment*/"));
    assertNull(scanner.parse("  //line comment\n\t/*block comment*/ "));
    assertScanner(mode, scanner, "a", "a");
  }

  @Test
  public void testSqlDelimiter() {
    Parser<Void> scanner = Scanners.SQL_DELIMITER;
    assertNull(scanner.parse(""));
    assertNull(scanner.parse(" "));
    assertNull(scanner.parse("--comment"));
    assertNull(scanner.parse("/*comment*/"));
    assertNull(scanner.parse("  --line comment\n\t/*block comment*/ "));
    assertScanner(mode, scanner, "a", "a");
  }

  @Test
  public void testHaskellDelimiter() {
    Parser<Void> scanner = Scanners.HASKELL_DELIMITER;
    assertNull(scanner.parse(""));
    assertNull(scanner.parse(" "));
    assertNull(scanner.parse("--comment"));
    assertNull(scanner.parse("{-comment-}"));
    assertNull(scanner.parse("  --line comment\n\t{-block comment-} "));
    assertScanner(mode, scanner, "a", "a");
  }

  @Test
  public void testJavaBlockComment() {
    Parser<Void> scanner = Scanners.JAVA_BLOCK_COMMENT;
    assertNull(scanner.parse("/* this is a comment */"));
    assertNull(scanner.parse("/** another comment */"));
    assertNull(scanner.parse("/** \"comment\" again **/"));
    assertScanner(mode, scanner, "/*comment*/not comment*/", "not comment*/");
    assertFailure(mode, scanner, "", 1, 1);
    assertFailure(mode, scanner, "/*a *", 1, 6);
  }

  @Test
  public void testSqlBlockComment() {
    Parser<Void> scanner = Scanners.SQL_BLOCK_COMMENT;
    assertNull(scanner.parse("/* this is a comment */"));
    assertNull(scanner.parse("/** another comment */"));
    assertNull(scanner.parse("/** \"comment\" again **/"));
    assertScanner(mode, scanner, "/*comment*/not comment*/", "not comment*/");
    assertFailure(mode, scanner, "", 1, 1);
    assertFailure(mode, scanner, "/*a *", 1, 6);
  }

  @Test
  public void testHaskellBlockComment() {
    Parser<Void> scanner = Scanners.HASKELL_BLOCK_COMMENT;
    assertNull(scanner.parse("{- this is a comment -}"));
    assertNull(scanner.parse("{-- another comment -}"));
    assertNull(scanner.parse("{-- \"comment\" again --}"));
    assertScanner(mode, scanner, "{-comment-}not comment-}", "not comment-}");
    assertFailure(mode, scanner, "", 1, 1);
    assertFailure(mode, scanner, "{-a -", 1, 6);
  }

  @Test
  public void testBlockComment() {
    Parser<Void> scanner = Scanners.blockComment("<<", ">>");
    assertNull(scanner.parse("<< this is a comment >>"));
    assertNull(scanner.parse("<<< another comment >>"));
    assertNull(scanner.parse("<<< \"comment\" again >>"));
    assertScanner(mode, scanner, "<<comment>>not comment>>", "not comment>>");
    assertFailure(mode, scanner, "", 1, 1);
    assertFailure(mode, scanner, "<<a >", 1, 6);
  }

  @Test
  public void testBlockComment_emptyQuotes() {
    Parser<Void> scanner = Scanners.blockComment("", "");
    assertScanner(mode, scanner, "abc", "abc");
    assertNull(scanner.parse(""));
  }

  @Test
  public void testBlockComment_withQuotedPattern() {
    Parser<Void> scanner = Scanners.blockComment("<<", ">>", Patterns.hasAtLeast(1));
    assertNull(scanner.parse("<<abc>>"));
    assertNull(scanner.parse("<<>>"));
    assertNull(scanner.parse("<<<>>"));
    assertScanner(mode, scanner, "<<a>>>\n", ">\n");
    assertFailure(mode, scanner, "", 1, 1);
    assertFailure(mode, scanner, "a", 1, 1);
  }

  @Test
  public void testBlockComment_withEmptyQuotedPattern() {
    Parser<Void> scanner = Scanners.blockComment("<<", ">>", Patterns.ALWAYS);
    assertNull(scanner.parse("<<>>"));
    assertFailure(mode, scanner, "<<a>>", 1, 3);
    assertFailure(mode, scanner, "", 1, 1);
    assertFailure(mode, scanner, "a", 1, 1);
  }

  @Test
  public void testBlockComment_withQuotedPatternThatMismatches() {
    Parser<Void> scanner = Scanners.blockComment("<<", ">>", Patterns.NEVER);
    assertNull(scanner.parse("<<>>"));
    assertFailure(mode, scanner, "<<a>>", 1, 3);
    assertFailure(mode, scanner, "", 1, 1);
    assertFailure(mode, scanner, "a", 1, 1);
  }

  @Test
  public void testBlockComment_withParsers() {
    Parser<Void> scanner = Scanners.blockComment(
        Scanners.string("<!--"), Scanners.string("-->"), Scanners.ANY_CHAR);
    assertNull(scanner.parse("<!--abc-->"));
    assertNull(scanner.parse("<!---->"));
    assertFailure(mode, scanner, "", 1, 1);
    assertFailure(mode, scanner, "a", 1, 1);
  }

  @Test
  public void testBlockComment_withQuotedParserThatMatchesEmpty() {
    Parser<Void> scanner = Scanners.blockComment(
        Scanners.string("<!--"), Scanners.string("-->"),
        Patterns.ALWAYS.toScanner("nothing"));
    assertNull(scanner.parse("<!---->"));
    assertFailure(mode, scanner, "", 1, 1);
    assertFailure(mode, scanner, "<!-", 1, 1);
  }

  @Test
  public void testBlockComment_withQuotedParserThatMismatches() {
    Parser<Void> scanner = Scanners.blockComment(
        Scanners.string("<!--"), Scanners.string("-->"),
        Patterns.NEVER.toScanner("nothing"));
    assertNull(scanner.parse("<!---->"));
    assertFailure(mode, scanner, "", 1, 1);
    assertFailure(mode, scanner, "<!-", 1, 1);
  }

  @Test
  public void testNestableBlockComment() {
    Parser<Void> scanner = Scanners.nestableBlockComment("/*", "*/");
    assertEquals("nestable block comment", scanner.toString());
    assertNull(scanner.parse("/* not nested */"));
    assertNull(scanner.parse("/* this is /*nested*/ */"));
    assertFailure(mode, scanner, "", 1, 1);
    assertFailure(mode, scanner, "/*", 1, 3);
    assertFailure(mode, scanner, "/* /**/", 1, 8);
    assertFailure(mode, scanner, "/* /**/*", 1, 9);
  }

  @Test
  public void testNestableBlockComment_withQuotedPattern() {
    Parser<Void> scanner = Scanners.nestableBlockComment("<!--", "-->", Patterns.ANY_CHAR);
    assertNull(scanner.parse("<!-- not nested -->"));
    assertNull(scanner.parse("<!-- this is <!--nested--> -->"));
    assertFailure(mode, scanner, "", 1, 1);
    assertFailure(mode, scanner, "<!--", 1, 5);
    assertFailure(mode, scanner, "<!-- <!---->", 1, 13);
    assertFailure(mode, scanner, "<!-- <!---->-", 1, 14);
  }

  @Test
  public void testNestableBlockComment_withQuotedParser() {
    Parser<Void> scanner = Scanners.nestableBlockComment(
        Scanners.string("<!--"), Scanners.string("-->"),
        Scanners.isChar(CharPredicates.not(CharPredicates.IS_DIGIT)));
    assertNull(scanner.parse("<!-- not nested -->"));
    assertNull(scanner.parse("<!-- this is <!--nested--> -->"));
    assertFailure(mode, scanner, "", 1, 1);
    assertFailure(mode, scanner, "<!-- 1-->", 1, 6);
    assertFailure(mode, scanner, "<!--", 1, 5);
    assertFailure(mode, scanner, "<!-- <!---->", 1, 13);
    assertFailure(mode, scanner, "<!-- <!---->-", 1, 14);
  }

  @Test
  public void testNestedBlockComment_partialMatch() {
    Parser<Void> scanner = Scanners.nestableBlockComment(
        areChars("/*"), areChars("*/"), Scanners.isChar('a').many());
    assertNull(scanner.parse("/*aaa*/"));
    assertNull(scanner.parse("/*a/*aa*/a*/"));
    assertFailure(mode, scanner, "/**a", 1, 4, "/ expected, a encountered.");
    assertFailure(mode, scanner, "/*/a", 1, 4, "* expected, a encountered.");
    assertFailure(mode, scanner, "/**a", 1, 4, "/ expected, a encountered.");
  }

  @Test
  public void testNestedBlockComment_notLogicalPartialMatch() {
    Parser<Void> scanner = Scanners.nestableBlockComment(
        Scanners.isChar('/').asDelimiter().next(Scanners.isChar('*')),
        Scanners.isChar('*').asDelimiter().next(Scanners.isChar('/')),
        Scanners.among("*/"));
    assertNull(scanner.parse("/*****/"));
    assertNull(scanner.parse("/*//****/*/"));
    assertFailure(mode, scanner, "/***//*/", 1, 6);
  }

  @Test
  public void testNestableBlockComment_quotedConsumesNoChar() {
    Parser<Void> scanner = Scanners.nestableBlockComment("<!--", "-->", Patterns.ALWAYS);
    assertFailure(mode, scanner, "<!-- -->", 1, 5, IllegalStateException.class);
  }

  @Test
  public void testNestableBlockComment_openQuoteConsumesNoChar() {
    Parser<Void> scanner = Scanners.nestableBlockComment(
        Parsers.always(), Scanners.string("*/"), Scanners.ANY_CHAR);
    assertFailure(mode, scanner, "/**/", 1, 1, IllegalStateException.class);
  }

  @Test
  public void testNestableBlockComment_closeQuoteConsumesNoChar() {
    Parser<Void> scanner = Scanners.nestableBlockComment(
        Scanners.string("/*"), Parsers.always(), Scanners.ANY_CHAR);
    assertFailure(mode, scanner, "/* */", 1, 3, IllegalStateException.class);
  }

  @Test
  public void testQuoted_byChar() {
    Parser<String> scanner = Scanners.quoted('<', '>');
    assertStringScanner(mode, scanner, "<abc123>");
    assertFailure(mode, scanner, "<a", 1, 3);
  }

  @Test
  @SuppressWarnings("deprecation")
  public void testQuoted() {
    Parser<String> scanner = Scanners.quoted(Scanners.isChar('<'), Scanners.isChar('>'),
        Patterns.INTEGER.toScanner("number"));
    assertStringScanner(mode, scanner, "<>");
    assertStringScanner(mode, scanner, "<123>");
    assertFailure(mode, scanner, "", 1, 1);
    assertFailure(mode, scanner, "<12", 1, 4);
    assertFailure(mode, scanner, "<a>", 1, 2);
  }

  @Test
  @SuppressWarnings("deprecation")
  public void testQuoted_quotedParserConsumeNoChar() {
    Parser<String> scanner =
        Scanners.quoted(Scanners.isChar('<'), Scanners.isChar('>'), Parsers.always());
    assertStringScanner(mode, scanner, "<>");
    assertFailure(mode, scanner, "", 1, 1);
    assertFailure(mode, scanner, "<a>", 1, 2);
  }

  @Test
  public void testNestedScanner() {
    Parser<Void> scanner = Scanners.nestedScanner(
        Scanners.isChar(CharPredicates.IS_ALPHA).skipMany1(), Scanners.isChar('a').skipTimes(2));
    assertEquals("nested scanner", scanner.toString());
    assertNull(scanner.parse("aa"));
    assertNull(scanner.parse("aabb"));
    assertFailure(mode, scanner, "ab", 1, 2);
    assertFailure(mode, scanner, "01", 1, 1);
    assertNull(Scanners.isChar(' ').next(scanner).parse(" aa"));
    assertNull(Scanners.isChar(' ').next(scanner).parse(" aab"));
    assertScanner(mode, Scanners.isChar(' ').next(scanner), " aab1", "1");
    assertScanner(mode, Scanners.isChar(' ').next(scanner), " aa1", "1");
  }

  @Test
  public void veryLongDoublyQuotedStringWithEscapedDoubleQuotes() {
    String quoted = "\"" + replicate(1000, "\n\\\"dsvtrbdfvbgf\\\"") + "\"";
    assertEquals(quoted, Scanners.DOUBLE_QUOTE_STRING.parse(quoted));
  }

  @Test
  public void veryLongStringWithEscapedSingleQuotes() {
    String quoted = replicate(1000, "a''bc");
    assertEquals("'"+ quoted  +"'", Scanners.SINGLE_QUOTE_STRING.parse("'"+ quoted  +"'"));
  }

  private static String replicate(int times, String s) {
    StringBuilder builder = new StringBuilder();
    for (int i = 0; i < times; i++) {
      builder.append(s);
    }
    return builder.toString();
  }
}
