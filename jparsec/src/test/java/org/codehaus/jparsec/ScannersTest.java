package org.codehaus.jparsec;

import static org.codehaus.jparsec.Asserts.assertFailure;
import static org.codehaus.jparsec.Asserts.assertScanner;
import static org.codehaus.jparsec.Asserts.assertStringScanner;
import static org.codehaus.jparsec.TestParsers.areChars;
import static org.junit.Assert.assertEquals;

import org.codehaus.jparsec.pattern.CharPredicates;
import org.codehaus.jparsec.pattern.Patterns;
import org.junit.Test;

/**
 * Unit test for {@link Scanners}.
 * 
 * @author Ben Yu
 */
public class ScannersTest {

  @Test
  public void testIdentifier() {
    Parser<String> scanner = Scanners.IDENTIFIER;
    assertStringScanner(scanner, "abc");
    assertStringScanner(scanner, "abc123");
    assertStringScanner(scanner, "abc 123", " 123");
    assertStringScanner(scanner, "_abc_123");
  }

  @Test
  public void testInteger() {
    Parser<String> scanner = Scanners.INTEGER;
    assertStringScanner(scanner, "123");
    assertStringScanner(scanner, "0");
    assertStringScanner(scanner, "12.3", ".3");
  }

  @Test
  public void testDecimal() {
    Parser<String> scanner = Scanners.DECIMAL;
    assertStringScanner(scanner, "123");
    assertStringScanner(scanner, "0");
    assertStringScanner(scanner, "12.3");
    assertStringScanner(scanner, ".3");
  }

  @Test
  public void testDecInteger() {
    Parser<String> scanner = Scanners.DEC_INTEGER;
    assertStringScanner(scanner, "1230");
    assertFailure(scanner, "0", 1, 1, "decimal integer expected, 0 encountered.");
  }

  @Test
  public void testOctInteger() {
    Parser<String> scanner = Scanners.OCT_INTEGER;
    assertStringScanner(scanner, "01270");
    assertStringScanner(scanner, "0");
    assertFailure(scanner, "12", 1, 1, "octal integer expected, 1 encountered.");
    assertFailure(scanner, "09", 1, 2);
  }

  @Test
  public void testHexInteger() {
    Parser<String> scanner = Scanners.HEX_INTEGER;
    assertStringScanner(scanner, "0X1AF");
    assertStringScanner(scanner, "0xF0");
    assertFailure(scanner, "1", 1, 1, "hexadecimal integer expected, 1 encountered.");
    assertFailure(scanner, "01", 1, 1);
  }

  @Test
  public void testScientificNotation() {
    Parser<String> scanner = Scanners.SCIENTIFIC_NOTATION;
    assertStringScanner(scanner, "0e0");
    assertStringScanner(scanner, "1.0E12");
    assertStringScanner(scanner, "1e+12");
    assertStringScanner(scanner, "1e-12");
    assertFailure(scanner, "", 1, 1, "scientific notation expected, EOF encountered.");
    assertFailure(scanner, "12", 1, 1, "scientific notation expected, 1 encountered");
    assertFailure(scanner, "e", 1, 1);
    assertFailure(scanner, "e1", 1, 1);
  }

  @Test
  public void testMany_withCharPredicate() {
    Parser<Void> scanner = Scanners.many(CharPredicates.IS_ALPHA);
    assertScanner(scanner, "abc123", "123");
    assertScanner(scanner, "123", "123");
  }

  @Test
  public void testMany1_withCharPredicate() {
    Parser<Void> scanner = Scanners.many1(CharPredicates.IS_ALPHA);
    assertScanner(scanner, "abc123", "123");
    assertFailure(scanner, "123", 1, 1, "[a-zA-Z]+ expected, 1 encountered.");
    assertFailure(scanner, "", 1, 1, "[a-zA-Z]+ expected, EOF encountered.");
  }

  @Test
  public void testMany_withPattern() {
    Parser<Void> scanner = Scanners.many(Patterns.string("ab"), "(ab)*");
    assertScanner(scanner, "abab");
    assertScanner(scanner, "aba", "a");
    assertScanner(scanner, "abc", "c");
    assertScanner(scanner, "c", "c");
    assertScanner(scanner, "");
  }

  @Test
  public void testMany_withPatternThatConsumesNoInput() {
    Parser<Void> scanner = Scanners.many(Patterns.ALWAYS, "*");
    assertScanner(scanner, "");
    assertScanner(scanner, "a", "a");
  }

  @Test
  public void testMany1_withPattern() {
    Parser<Void> scanner = Scanners.many1(Patterns.string("ab"), "(ab)+");
    assertScanner(scanner, "abab");
    assertScanner(scanner, "aba", "a");
    assertScanner(scanner, "abc", "c");
    assertFailure(scanner, "c", 1, 1, "(ab)+ expected, c encountered.");
    assertFailure(scanner, "", 1, 1, "(ab)+ expected, EOF encountered.");
  }

  @Test
  public void testMany1_withPatternThatConsumesNoInput() {
    Parser<Void> scanner = Scanners.many1(Patterns.ALWAYS, "+");
    assertScanner(scanner, "");
    assertScanner(scanner, "a", "a");
  }

  @Test
  public void testString() {
    Parser<Void> scanner = Scanners.string("ab");
    assertScanner(scanner, "ab");
    assertScanner(scanner, "abc", "c");
    assertFailure(scanner, "c", 1, 1, "ab expected, c encountered.");
    assertFailure(scanner, "a", 1, 1);
    assertFailure(scanner, "", 1, 1);
  }

  @Test
  public void testWhitespaces() {
    Parser<Void> scanner = Scanners.WHITESPACES;
    assertEquals("whitespaces", scanner.toString());
    assertScanner(scanner, " \r\n");
    assertScanner(scanner, " \r\na", "a");
    assertFailure(scanner, "", 1, 1);
    assertFailure(scanner, "a", 1, 1);
  }

  @Test
  public void testPattern() {
    Parser<Void> scanner = Scanners.pattern(Patterns.INTEGER, "integer");
    assertScanner(scanner, "123");
    assertScanner(scanner, "12a", "a");
    assertFailure(scanner, "", 1, 1, "integer expected, EOF encountered.");
    assertFailure(scanner, "a", 1, 1);
  }

  @Test
  public void testStringCaseInsensitive() {
    Parser<Void> scanner = Scanners.stringCaseInsensitive("ab");
    assertScanner(scanner, "ab");
    assertScanner(scanner, "AB");
    assertScanner(scanner, "aB");
    assertFailure(scanner, "", 1, 1, "ab expected, EOF encountered.");
    assertFailure(scanner, "a", 1, 1);
  }

  @Test
  public void testAnyChar() {
    Parser<Void> scanner = Scanners.ANY_CHAR;
    assertScanner(scanner, "a");
    assertScanner(scanner, "1");
    assertScanner(scanner, " ");
    assertScanner(scanner, "\n");
    assertScanner(scanner, "ab", "b");
    assertFailure(scanner, "", 1, 1, "any character expected, EOF encountered.");
    assertEquals("any character", scanner.toString());
  }

  @Test
  public void testIsChar() {
    Parser<Void> scanner = Scanners.isChar('a');
    assertScanner(scanner, "a");
    assertScanner(scanner, "abc", "bc");
    assertFailure(scanner, "bc", 1, 1, "a expected, b encountered.");
    assertFailure(scanner, "", 1, 1);
  }

  @Test
  public void testNotChar() {
    Parser<Void> scanner = Scanners.notChar('a');
    assertScanner(scanner, "b");
    assertScanner(scanner, "bcd", "cd");
    assertFailure(scanner, "abc", 1, 1, "^a expected, a encountered.");
    assertFailure(scanner, "", 1, 1);
  }

  @Test
  public void testAmong() {
    Parser<Void> scanner = Scanners.among("ab");
    assertScanner(scanner, "a");
    assertScanner(scanner, "b");
    assertScanner(scanner, "ab", "b");
    assertFailure(scanner, "c", 1, 1, "[ab] expected, c encountered.");
    assertFailure(scanner, "", 1, 1, "[ab] expected, EOF encountered.");
  }

  @Test
  public void testAmong_noChars() {
    Parser<Void> scanner = Scanners.among("");
    assertFailure(scanner, "a", 1, 1, "none expected, a encountered.");
    assertFailure(scanner, "", 1, 1, "none expected, EOF encountered.");
  }

  @Test
  public void testAmong_oneChar() {
    Parser<Void> scanner = Scanners.among("a");
    assertScanner(scanner, "a");
    assertScanner(scanner, "ab", "b");
    assertFailure(scanner, "b", 1, 1, "a expected, b encountered.");
    assertFailure(scanner, "", 1, 1);
  }

  @Test
  public void testNotAmong() {
    Parser<Void> scanner = Scanners.notAmong("ab");
    assertScanner(scanner, "0");
    assertScanner(scanner, "0a", "a");
    assertFailure(scanner, "a", 1, 1, "^[ab] expected, a encountered.");
    assertFailure(scanner, "b", 1, 1);
    assertFailure(scanner, "", 1, 1);
  }

  @Test
  public void testNotAmong_noChars() {
    Parser<Void> scanner = Scanners.notAmong("");
    assertScanner(scanner, "0");
    assertScanner(scanner, "ab", "b");
    assertFailure(scanner, "", 1, 1, "any character expected, EOF encountered.");
  }

  @Test
  public void testNotAmong_oneChar() {
    Parser<Void> scanner = Scanners.notAmong("a");
    assertScanner(scanner, "0");
    assertScanner(scanner, "0a", "a");
    assertFailure(scanner, "a", 1, 1);
    assertFailure(scanner, "", 1, 1);
  }

  @Test
  public void testLineComment() {
    Parser<Void> scanner = Scanners.lineComment("#");
    assertScanner(scanner, "#hello world");
    assertScanner(scanner, "#hello world\n", "\n");
    assertScanner(scanner, "#hello world\r\n", "\n");
    assertScanner(scanner, "#\n", "\n");
    assertScanner(scanner, "#");
    assertFailure(scanner, "", 1, 1);
    assertFailure(scanner, "\n", 1, 1);
    assertFailure(scanner, "a", 1, 1);
  }

  @Test
  public void testJavaLineComment() {
    Parser<Void> scanner = Scanners.JAVA_LINE_COMMENT;
    assertScanner(scanner, "//hello");
  }

  @Test
  public void testSqlLineComment() {
    Parser<Void> scanner = Scanners.SQL_LINE_COMMENT;
    assertScanner(scanner, "--hello");
  }

  @Test
  public void testHaskellLineComment() {
    Parser<Void> scanner = Scanners.HASKELL_LINE_COMMENT;
    assertScanner(scanner, "--hello");
  }

  @Test
  public void testDoubleQuoteString() {
    Parser<String> scanner = Scanners.DOUBLE_QUOTE_STRING;
    assertStringScanner(scanner, "\"\"");
    assertStringScanner(scanner, "\"a b'c\"");
    assertStringScanner(scanner, "\"a\\\\\\\"1\"");
    assertFailure(scanner, "", 1, 1);
    assertFailure(scanner, "ab", 1, 1);
    assertFailure(scanner, "\"ab", 1, 4);
    assertFailure(scanner, "\"\\\"", 1, 4);
  }

  @Test
  public void testSingleQuoteString() {
    Parser<String> scanner = Scanners.SINGLE_QUOTE_STRING;
    assertStringScanner(scanner, "''");
    assertStringScanner(scanner, "'a'");
    assertStringScanner(scanner, "'foo'");
    assertStringScanner(scanner, "'foo''s day'");
  }

  @Test
  public void testSingleQuoteChar() {
    Parser<String> scanner = Scanners.SINGLE_QUOTE_CHAR;
    assertStringScanner(scanner, "'a'");
    assertStringScanner(scanner, "'\\a'");
    assertStringScanner(scanner, "'\\\\'");
    assertStringScanner(scanner, "'\\\"'");
    assertFailure(scanner, "", 1, 1);
    assertFailure(scanner, "ab", 1, 1);
    assertFailure(scanner, "''", 1, 2);
    assertFailure(scanner, "'\\'", 1, 4);
  }

  @Test
  public void testJavaDelimiter() {
    Parser<Void> scanner = Scanners.JAVA_DELIMITER;
    assertScanner(scanner, "");
    assertScanner(scanner, " ");
    assertScanner(scanner, "//comment");
    assertScanner(scanner, "/*comment*/");
    assertScanner(scanner, "  //line comment\n\t/*block comment*/ ");
    assertScanner(scanner, "a", "a");
  }

  @Test
  public void testSqlDelimiter() {
    Parser<Void> scanner = Scanners.SQL_DELIMITER;
    assertScanner(scanner, "");
    assertScanner(scanner, " ");
    assertScanner(scanner, "--comment");
    assertScanner(scanner, "/*comment*/");
    assertScanner(scanner, "  --line comment\n\t/*block comment*/ ");
    assertScanner(scanner, "a", "a");
  }

  @Test
  public void testHaskellDelimiter() {
    Parser<Void> scanner = Scanners.HASKELL_DELIMITER;
    assertScanner(scanner, "");
    assertScanner(scanner, " ");
    assertScanner(scanner, "--comment");
    assertScanner(scanner, "{-comment-}");
    assertScanner(scanner, "  --line comment\n\t{-block comment-} ");
    assertScanner(scanner, "a", "a");
  }

  @Test
  public void testJavaBlockComment() {
    Parser<Void> scanner = Scanners.JAVA_BLOCK_COMMENT;
    assertScanner(scanner, "/* this is a comment */");
    assertScanner(scanner, "/** another comment */");
    assertScanner(scanner, "/** \"comment\" again **/");
    assertScanner(scanner, "/*comment*/not comment*/", "not comment*/");
    assertFailure(scanner, "", 1, 1);
    assertFailure(scanner, "/*a *", 1, 6);
  }

  @Test
  public void testSqlBlockComment() {
    Parser<Void> scanner = Scanners.SQL_BLOCK_COMMENT;
    assertScanner(scanner, "/* this is a comment */");
    assertScanner(scanner, "/** another comment */");
    assertScanner(scanner, "/** \"comment\" again **/");
    assertScanner(scanner, "/*comment*/not comment*/", "not comment*/");
    assertFailure(scanner, "", 1, 1);
    assertFailure(scanner, "/*a *", 1, 6);
  }

  @Test
  public void testHaskellBlockComment() {
    Parser<Void> scanner = Scanners.HASKELL_BLOCK_COMMENT;
    assertScanner(scanner, "{- this is a comment -}");
    assertScanner(scanner, "{-- another comment -}");
    assertScanner(scanner, "{-- \"comment\" again --}");
    assertScanner(scanner, "{-comment-}not comment-}", "not comment-}");
    assertFailure(scanner, "", 1, 1);
    assertFailure(scanner, "{-a -", 1, 6);
  }

  @Test
  public void testBlockComment() {
    Parser<Void> scanner = Scanners.blockComment("<<", ">>");
    assertScanner(scanner, "<< this is a comment >>");
    assertScanner(scanner, "<<< another comment >>");
    assertScanner(scanner, "<<< \"comment\" again >>");
    assertScanner(scanner, "<<comment>>not comment>>", "not comment>>");
    assertFailure(scanner, "", 1, 1);
    assertFailure(scanner, "<<a >", 1, 6);
  }

  @Test
  public void testBlockComment_emptyQuotes() {
    Parser<Void> scanner = Scanners.blockComment("", "");
    assertScanner(scanner, "abc", "abc");
    assertScanner(scanner, "");
  }

  @Test
  public void testBlockComment_withQuotedPattern() {
    Parser<Void> scanner = Scanners.blockComment("<<", ">>", Patterns.hasAtLeast(1));
    assertScanner(scanner, "<<abc>>");
    assertScanner(scanner, "<<>>");
    assertScanner(scanner, "<<<>>");
    assertScanner(scanner, "<<a>>>\n", ">\n");
    assertFailure(scanner, "", 1, 1);
    assertFailure(scanner, "a", 1, 1);
  }

  @Test
  public void testBlockComment_withEmptyQuotedPattern() {
    Parser<Void> scanner = Scanners.blockComment("<<", ">>", Patterns.ALWAYS);
    assertScanner(scanner, "<<>>");
    assertFailure(scanner, "<<a>>", 1, 3);
    assertFailure(scanner, "", 1, 1);
    assertFailure(scanner, "a", 1, 1);
  }

  @Test
  public void testBlockComment_withQuotedPatternThatMismatches() {
    Parser<Void> scanner = Scanners.blockComment("<<", ">>", Patterns.NEVER);
    assertScanner(scanner, "<<>>");
    assertFailure(scanner, "<<a>>", 1, 3);
    assertFailure(scanner, "", 1, 1);
    assertFailure(scanner, "a", 1, 1);
  }

  @Test
  public void testBlockComment_withParsers() {
    Parser<Void> scanner = Scanners.blockComment(
        Scanners.string("<!--"), Scanners.string("-->"), Scanners.ANY_CHAR);
    assertScanner(scanner, "<!--abc-->");
    assertScanner(scanner, "<!---->");
    assertFailure(scanner, "", 1, 1);
    assertFailure(scanner, "a", 1, 1);
  }

  @Test
  public void testBlockComment_withQuotedParserThatMatchesEmpty() {
    Parser<Void> scanner = Scanners.blockComment(
        Scanners.string("<!--"), Scanners.string("-->"),
        Scanners.pattern(Patterns.ALWAYS, "nothing"));
    assertScanner(scanner, "<!---->");
    assertFailure(scanner, "", 1, 1);
    assertFailure(scanner, "<!-", 1, 1);
  }

  @Test
  public void testBlockComment_withQuotedParserThatMismatches() {
    Parser<Void> scanner = Scanners.blockComment(
        Scanners.string("<!--"), Scanners.string("-->"),
        Scanners.pattern(Patterns.NEVER, "nothing"));
    assertScanner(scanner, "<!---->");
    assertFailure(scanner, "", 1, 1);
    assertFailure(scanner, "<!-", 1, 1);
  }

  @Test
  public void testNestableBlockComment() {
    Parser<Void> scanner = Scanners.nestableBlockComment("/*", "*/");
    assertEquals("nestable block comment", scanner.toString());
    assertScanner(scanner, "/* not nested */");
    assertScanner(scanner, "/* this is /*nested*/ */");
    assertFailure(scanner, "", 1, 1);
    assertFailure(scanner, "/*", 1, 3);
    assertFailure(scanner, "/* /**/", 1, 8);
    assertFailure(scanner, "/* /**/*", 1, 9);
  }

  @Test
  public void testNestableBlockComment_withQuotedPattern() {
    Parser<Void> scanner = Scanners.nestableBlockComment("<!--", "-->", Patterns.ANY_CHAR);
    assertScanner(scanner, "<!-- not nested -->");
    assertScanner(scanner, "<!-- this is <!--nested--> -->");
    assertFailure(scanner, "", 1, 1);
    assertFailure(scanner, "<!--", 1, 5);
    assertFailure(scanner, "<!-- <!---->", 1, 13);
    assertFailure(scanner, "<!-- <!---->-", 1, 14);
  }

  @Test
  public void testNestableBlockComment_withQuotedParser() {
    Parser<Void> scanner = Scanners.nestableBlockComment(
        Scanners.string("<!--"), Scanners.string("-->"),
        Scanners.isChar(CharPredicates.not(CharPredicates.IS_DIGIT)));
    assertScanner(scanner, "<!-- not nested -->");
    assertScanner(scanner, "<!-- this is <!--nested--> -->");
    assertFailure(scanner, "", 1, 1);
    assertFailure(scanner, "<!-- 1-->", 1, 6);
    assertFailure(scanner, "<!--", 1, 5);
    assertFailure(scanner, "<!-- <!---->", 1, 13);
    assertFailure(scanner, "<!-- <!---->-", 1, 14);
  }

  @Test
  public void testNestedBlockComment_partialMatch() {
    Parser<Void> scanner = Scanners.nestableBlockComment(
        areChars("/*"), areChars("*/"), Scanners.isChar('a').many());
    assertScanner(scanner, "/*aaa*/");
    assertScanner(scanner, "/*a/*aa*/a*/");
    assertFailure(scanner, "/**a", 1, 4, "/ expected, a encountered.");
    assertFailure(scanner, "/*/a", 1, 4, "* expected, a encountered.");
    assertFailure(scanner, "/**a", 1, 4, "/ expected, a encountered.");
  }

  @Test
  public void testNestedBlockComment_notLogicalPartialMatch() {
    Parser<Void> scanner = Scanners.nestableBlockComment(
        Scanners.isChar('/').step(0).next(Scanners.isChar('*')),
        Scanners.isChar('*').step(0).next(Scanners.isChar('/')),
        Scanners.among("*/"));
    assertScanner(scanner, "/*****/");
    assertScanner(scanner, "/*//****/*/");
    assertFailure(scanner, "/***//*/", 1, 6);
  }

  @Test
  public void testNestableBlockComment_quotedConsumesNoChar() {
    Parser<Void> scanner = Scanners.nestableBlockComment("<!--", "-->", Patterns.ALWAYS);
    assertFailure(scanner, "<!-- -->", 1, 5, IllegalStateException.class);
  }

  @Test
  public void testNestableBlockComment_openQuoteConsumesNoChar() {
    Parser<Void> scanner = Scanners.nestableBlockComment(
        Parsers.always(), Scanners.string("*/"), Scanners.ANY_CHAR);
    assertFailure(scanner, "/**/", 1, 1, IllegalStateException.class);
  }

  @Test
  public void testNestableBlockComment_closeQuoteConsumesNoChar() {
    Parser<Void> scanner = Scanners.nestableBlockComment(
        Scanners.string("/*"), Parsers.always(), Scanners.ANY_CHAR);
    assertFailure(scanner, "/* */", 1, 3, IllegalStateException.class);
  }

  @Test
  public void testQuoted_byChar() {
    Parser<String> scanner = Scanners.quoted('<', '>');
    assertStringScanner(scanner, "<abc123>");
    assertFailure(scanner, "<a", 1, 3);
  }

  @Test
  public void testQuoted() {
    Parser<String> scanner = Scanners.quoted(Scanners.isChar('<'), Scanners.isChar('>'),
        Scanners.pattern(Patterns.INTEGER, "number"));
    assertStringScanner(scanner, "<>");
    assertStringScanner(scanner, "<123>");
    assertFailure(scanner, "", 1, 1);
    assertFailure(scanner, "<12", 1, 4);
    assertFailure(scanner, "<a>", 1, 2);
  }

  @Test
  public void testQuoted_quotedParserConsumeNoChar() {
    Parser<String> scanner =
        Scanners.quoted(Scanners.isChar('<'), Scanners.isChar('>'), Parsers.always());
    assertStringScanner(scanner, "<>");
    assertFailure(scanner, "", 1, 1);
    assertFailure(scanner, "<a>", 1, 2);
  }

  @Test
  public void testNestedScanner() {
    Parser<Void> scanner = Scanners.nestedScanner(
        Scanners.isChar(CharPredicates.IS_ALPHA).skipMany1(), Scanners.isChar('a').skipTimes(2));
    assertEquals("nested scanner", scanner.toString());
    assertScanner(scanner, "aa");
    assertScanner(scanner, "aabb");
    assertFailure(scanner, "ab", 1, 2);
    assertFailure(scanner, "01", 1, 1);
    assertScanner(Scanners.isChar(' ').next(scanner), " aa");
    assertScanner(Scanners.isChar(' ').next(scanner), " aab");
    assertScanner(Scanners.isChar(' ').next(scanner), " aab1", "1");
    assertScanner(Scanners.isChar(' ').next(scanner), " aa1", "1");
  }

}
