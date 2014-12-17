package org.codehaus.jparsec;

import static org.codehaus.jparsec.Asserts.assertFailure;
import static org.codehaus.jparsec.Asserts.assertParser;
import static org.codehaus.jparsec.Parsers.constant;
import static org.codehaus.jparsec.Scanners.string;
import static org.codehaus.jparsec.TestParsers.areChars;
import static org.codehaus.jparsec.TestParsers.isChar;
import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.StringReader;
import java.util.Arrays;
import java.util.List;

import org.codehaus.jparsec.easymock.BaseMockTest;
import org.codehaus.jparsec.error.ParserException;
import org.codehaus.jparsec.functors.Map;
import org.codehaus.jparsec.functors.Map2;
import org.codehaus.jparsec.functors.Maps;
import org.junit.Test;

/**
 * Unit test for {@link Parser}.
 * 
 * @author Ben Yu
 */
public class ParserTest extends BaseMockTest {
  
  private static final Parser<Integer> INTEGER = Scanners.INTEGER.source().map(Maps.TO_INTEGER);
  private static final Parser<String> FOO = constant("foo");
  private static final Parser<String> FAILURE = Parsers.fail("failure");
  private static final Parser<Void> COMMA = Scanners.isChar(',');

  @SuppressWarnings("deprecation")
  @Test
  public void testParse() throws Exception {
    assertEquals("foo", FOO.parse(""));
    assertFailure(FOO, "a", 1, 1, "EOF expected, a encountered.");
    assertFailure(FOO, "a", 1, 1, "test module", "EOF expected, a encountered.");
    assertEquals(new Integer(123), INTEGER.parse(new StringReader("123")));
    try {
      INTEGER.parse(new StringReader("x"), "test module");
      fail();
    } catch (ParserException e) {
      assertEquals(1, e.getLocation().line);
      assertEquals(1, e.getLocation().column);
      assertTrue(e.getMessage(), e.getMessage().contains("test module"));
      assertTrue(e.getMessage(), e.getMessage().contains("integer expected, x encountered."));
    }
  }

  @Test
  public void testSource() {
    assertEquals("source", FOO.source().toString());
    assertParser(FOO.source(), "", "");
    assertParser(COMMA.source(), ", ", ",", " ");
    assertParser(Terminals.IntegerLiteral.TOKENIZER.label("INTEGER").source(), "123 ", "123", " ");
    assertParser(
        Parsers.tokenType(Integer.class, "int").from(INTEGER, Scanners.WHITESPACES).source(),
        "123", "123");
  }

  @Test
  public void testToken() {
    assertEquals("foo", FOO.token().toString());
    assertParser(FOO.token(), "", new Token(0, 0, "foo"));
    assertParser(INTEGER.token(), "123", new Token(0, 3, 123));
    assertFailure(INTEGER.token(), "a", 1, 1);
  }

  @Test
  public void testWithSource() {
    assertEquals("foo", FOO.withSource().toString());
    assertParser(FOO.withSource(), "", new WithSource<String>("foo", ""));
    assertParser(INTEGER.withSource(), "123", new WithSource<Integer>(123, "123"));
    assertFailure(INTEGER.withSource(), "a", 1, 1);
  }
  
  @Mock Map<Object, Parser<String>> next;

  @Test
  public void testNext_withMap() {
    expect(next.map(1)).andReturn(FOO);
    replay();
    assertParser(INTEGER.next(next), "1", "foo");
    assertEquals(next.toString(), INTEGER.next(next).toString());
  }

  @Test
  public void testNext_firstParserFails() {
    replay();
    assertFailure(FAILURE.next(next), "", 1, 1, "failure");
  }

  @Test
  public void testNext_nextParserFails() {
    expect(next.map(123)).andReturn(FAILURE);
    replay();
    assertFailure(INTEGER.next(next), "123", 1, 4, "failure");
  }

  @Test
  public void testNext() {
    assertEquals("sequence", COMMA.next(INTEGER).toString());
    assertParser(COMMA.next(INTEGER), ",123", 123);
    assertFailure(FAILURE.next(FOO), "", 1, 1, "failure");
    assertFailure(INTEGER.next(COMMA), "123", 1, 4);
  }

  @Test
  public void testRetn() {
    assertParser(COMMA.retn(1), ",", 1);
    assertFailure(FAILURE.retn(1), "", 1, 1, "failure");
  }

  @Test
  public void testUntil() {
    Parser<String> comma = Scanners.isChar(',').source();
    Parser<?> dot = Scanners.isChar('.');
    Parser<List<Object>> parser = INTEGER.cast().or(comma).until(dot);
    assertParser(parser, "123,456.", Arrays.<Object>asList(123, ",", 456), ".");
    assertFailure(parser, "", 1, 1);
    assertParser(parser, ".", Arrays.asList(), ".");
  }

  @Test
  public void testFollowedBy() {
    assertParser(INTEGER.followedBy(COMMA), "123,", 123);
    assertFailure(FAILURE.followedBy(FOO), "", 1, 1, "failure");
    assertFailure(INTEGER.followedBy(COMMA), "123", 1, 4, ", expected, EOF encountered.");
  }

  @Test
  public void testNotFollowedBy() {
    assertParser(INTEGER.notFollowedBy(COMMA), "123", 123);
    assertParser(INTEGER.notFollowedBy(COMMA.times(2)).followedBy(COMMA), "123,", 123);
    assertFailure(FAILURE.notFollowedBy(FOO), "", 1, 1, "failure");
    assertFailure(INTEGER.notFollowedBy(COMMA), "123,", 1, 4, "unexpected ,.");
  }

  @Test
  public void testSkipTimes() {
    assertParser(isChar('a').skipTimes(3), "aaa", null);
    assertFailure(isChar('a').skipTimes(3), "aa", 1, 3);
    assertParser(areChars("ab").skipTimes(3), "ababab", null);
    assertParser(FOO.skipTimes(3), "", null);
    assertFailure(areChars("ab").skipTimes(3), "aba", 1, 4);
    assertEquals("skipTimes", INTEGER.skipTimes(1).toString());
  }

  @Test
  public void testTimes() {
    assertListParser(isChar('a').times(3), "aaa", 'a', 'a', 'a');
    assertFailure(isChar('a').times(3), "aa", 1, 3);
    assertListParser(areChars("ab").times(3), "ababab", 'b', 'b', 'b');
    assertListParser(FOO.times(2), "", "foo", "foo");
    assertFailure(areChars("ab").times(3), "aba", 1, 4);
    assertEquals("times", INTEGER.times(1).toString());
  }

  @Test
  public void skipTimes_range() {
    assertParser(isChar('a').skipTimes(0, 1), "", null);
    assertFailure(isChar('a').skipTimes(1, 2), "", 1, 1);
    assertFailure(areChars("ab").skipTimes(1, 2), "aba", 1, 4);
    assertFailure(areChars("ab").skipTimes(1, 2), "aba", 1, 4);
    assertParser(FOO.skipTimes(0, 1), "", null);
    assertParser(FOO.skipTimes(1, 2), "", null);
    assertParser(isChar('a').step(0).next(isChar('b')).skipTimes(1, 2), "aba", null, "a");
    assertEquals("skipTimes", isChar('a').skipTimes(1, 2).toString());
  }

  @Test
  public void testTimes_range() {
    assertListParser(isChar('a').times(0, 1), "");
    assertFailure(isChar('a').times(1, 2), "", 1, 1);
    assertFailure(areChars("ab").times(1, 2), "aba", 1, 4);
    assertFailure(areChars("ab").times(1, 2), "aba", 1, 4);
    assertListParser(FOO.times(0, 1), "", "foo");
    assertListParser(FOO.times(2, 3), "", "foo", "foo", "foo");
    assertListParser(isChar('a').step(0).next(isChar('b')).times(1, 2).followedBy(isChar('a')),
        "aba", 'b');
    assertEquals("times", isChar('a').times(1, 2).toString());
  }

  @Test
  public void testSkipMany() {
    assertParser(isChar('a').skipMany(), "", null);
    assertParser(isChar('a').skipMany(), "a", null);
    assertParser(isChar('a').skipMany(), "aaa", null);
    assertFailure(areChars("ab").skipMany(), "aba", 1, 4);
    assertParser(FOO.skipMany(), "", null);
    assertParser(isChar('a').skipAtLeast(0), "", null);
    assertFailure(isChar('a').skipAtLeast(1), "", 1, 1);
    assertFailure(areChars("ab").skipAtLeast(1), "aba", 1, 4);
    assertFailure(areChars("ab").skipAtLeast(2), "aba", 1, 4);
    assertParser(FOO.skipAtLeast(0), "", null);
    assertParser(FOO.skipAtLeast(2), "", null);
    assertParser(isChar('a').step(0).next(isChar('b')).skipMany(), "a", null, "a");
    assertEquals("skipAtLeast", isChar('a').skipMany().toString());
    assertEquals("skipAtLeast", isChar('a').skipAtLeast(2).toString());
  }

  @Test
  public void testSkipMany1() {
    assertFailure(isChar('a').skipMany1(), "", 1, 1);
    assertParser(isChar('a').skipMany1(), "a", null);
    assertParser(isChar('a').skipMany1(), "aaa", null);
    assertFailure(areChars("ab").skipMany1(), "aba", 1, 4);
    assertParser(FOO.skipMany1(), "", null);
    assertParser(isChar('a').step(0).next(isChar('b')).skipMany1(), "aba", null, "a");
    assertEquals("skipAtLeast", isChar('a').skipMany1().toString());
  }

  @Test
  public void testMany1() {
    assertFailure(isChar('a').many1(), "", 1, 1);
    assertListParser(isChar('a').many1(), "a", 'a');
    assertListParser(isChar('a').many1(), "aaa", 'a', 'a', 'a');
    assertFailure(areChars("ab").many1(), "aba", 1, 4);
    assertListParser(FOO.many1(), "", "foo");
    assertListParser(isChar('a').step(0).next(isChar('b')).many1().followedBy(isChar('a')),
        "aba", 'b');
    assertEquals("atLeast", isChar('a').many1().toString());
  }

  @Test
  public void testMany() {
    assertListParser(isChar('a').many(), "");
    assertListParser(isChar('a').many(), "a", 'a');
    assertListParser(isChar('a').many(), "aaa", 'a', 'a', 'a');
    assertFailure(areChars("ab").many(), "aba", 1, 4);
    assertListParser(FOO.many(), "");
    assertListParser(isChar('a').atLeast(0), "");
    assertFailure(isChar('a').atLeast(1), "", 1, 1);
    assertFailure(areChars("ab").atLeast(1), "aba", 1, 4);
    assertFailure(areChars("ab").atLeast(2), "aba", 1, 4);
    assertListParser(FOO.atLeast(0), "");
    assertListParser(FOO.atLeast(2), "", "foo", "foo");
    assertListParser(isChar('a').step(0).next(isChar('b')).many().followedBy(isChar('a')), "a");
    assertEquals("atLeast", isChar('a').many().toString());
    assertEquals("atLeast", isChar('a').atLeast(1).toString());
  }

  @Test
  public void testOr() {
    assertEquals("or", INTEGER.or(INTEGER).toString());
    assertParser(INTEGER.or(constant(456)), "123", 123);
    assertParser(isChar('a').or(constant('b')), "", 'b');
    assertParser(areChars("ab").or(isChar('a')), "a", 'a');
    assertListParser(areChars("ab").or(isChar('a')).many(), "a", 'a');
    assertFailure(areChars("ab").or(isChar('a')), "x", 1, 1);
  }

  @Test
  public void testOptional() {
    assertParser(INTEGER.optional(), "12", 12);
    assertParser(INTEGER.optional(), "", null);
    assertFailure(areChars("ab").optional(), "a", 1, 2);
  }

  @Test
  public void testOptional_withDefaultValue() {
    assertParser(INTEGER.optional(0), "12", 12);
    assertParser(INTEGER.optional(0), "", 0);
    assertFailure(areChars("ab").optional('x'), "a", 1, 2);
  }

  @Test
  public void testNot() {
    assertParser(INTEGER.not(), "", null);
    assertFailure(INTEGER.not(), "12", 1, 1);
    assertParser(areChars("ab").not(), "a", null, "a");
    assertParser(INTEGER.not("num"), "", null);
    assertFailure(INTEGER.not("num"), "12", 1, 1, "unexpected num");
  }

  @Test
  public void testPeek() {
    assertParser(INTEGER.peek(), "12", 12, "12");
    assertFailure(INTEGER.peek(), "a", 1, 1);
    assertFailure(areChars("ab").peek(), "a", 1, 2);
    assertFailure(Parsers.plus(areChars("ab").peek(), isChar('a')), "a", 1, 2);
    assertEquals("peek", INTEGER.peek().toString());
  }

  @Test
  public void testAtomic() {
    assertEquals("integer", INTEGER.atomic().toString());
    assertParser(areChars("ab").atomic(), "ab", 'b');
    assertParser(Parsers.plus(areChars("ab").atomic(), isChar('a')), "a", 'a');
    assertFailure(areChars("ab").atomic(), "a", 1, 2);
  }

  @Test
  public void testStep() {
    assertEquals(INTEGER.toString(), INTEGER.step(0).toString());
    assertParser(Parsers.plus(areChars("ab").step(0).next(isChar('c')), areChars("ab")), "ab", 'b');
  }

  @Test
  public void testStep_negativeStep() {
    try {
      INTEGER.step(-1);
      fail();
    } catch (IllegalArgumentException e) {
      assertEquals(e.getMessage(), "step < 0", e.getMessage());
    }
  }

  @Test
  public void testSucceeds() {
    assertParser(isChar('a').succeeds(), "ab", true, "b");
    assertParser(isChar('a').succeeds(), "xb", false, "xb");
    assertParser(areChars("ab").succeeds(), "ax", false, "ax");
  }

  @Test
  public void testFails() {
    assertParser(isChar('a').fails(), "ab", false, "b");
    assertParser(isChar('a').fails(), "xb", true, "xb");
    assertParser(areChars("ab").fails(), "ax", true, "ax");
  }

  @Test
  public void testIfElse() {
    Parser<Integer> parser = areChars("ab").ifelse(INTEGER, constant(0));
    assertEquals("ifelse", parser.toString());
    assertParser(parser, "ab12", 12);
    assertParser(parser, "", 0);
    assertParser(parser, "a", 0, "a");
  }

  @Test
  public void testIfElse_withNext() {
    expect(next.map('b')).andReturn(FOO);
    replay();
    assertParser(areChars("ab").ifelse(next, constant("bar")), "ab", "foo");
  }

  @Test
  public void testLabel() {
    assertParser(FOO.label("the foo"), "", "foo");
    assertFailure(INTEGER.label("number"), "", 1, 1, "number");
  }

  @Test
  public void testCast() {
    Parser<String> parser = Parsers.<CharSequence>constant("chars").<String>cast();
    assertEquals("chars", parser.toString());
    assertParser(parser, "", "chars");
  }

  @Test
  public void testBetween() {
    assertParser(INTEGER.between(isChar('('), isChar(')')), "(123)", 123);
  }
  
  @Mock Map<Integer, String> map;

  @Test
  public void testMap() {
    expect(map.map(12)).andReturn("foo");
    replay();
    assertParser(INTEGER.map(map), "12", "foo");
    assertEquals(map.toString(), INTEGER.map(map).toString());
  }

  @Test
  public void testMap_fails() {
    replay();
    assertFailure(INTEGER.map(map), "", 1, 1, "integer expected, EOF encountered.");
  }

  @Test
  public void testSepBy1() {
    Parser<List<Integer>> parser = INTEGER.sepBy1(isChar(','));
    assertListParser(parser, "1", 1);
    assertListParser(parser, "123,45", 123, 45);
    assertListParser(parser.followedBy(Scanners.isChar(' ')), "1 ", 1);
    assertListParser(parser.followedBy(isChar(',')), "1,", 1);
    assertFailure(parser, "", 1, 1);
    assertFailure(areChars("ab").sepBy1(isChar(',')), "ab,a", 1, 5);
  }

  @Test
  public void testSepBy() {
    Parser<List<Integer>> parser = INTEGER.sepBy(isChar(','));
    assertListParser(parser, "1", 1);
    assertListParser(parser, "123,45", 123, 45);
    assertListParser(parser.followedBy(isChar(' ')), "1 ", 1);
    assertListParser(parser, "");
    assertListParser(parser.followedBy(isChar(',')), "1,", 1);
    assertFailure(areChars("ab").sepBy(isChar(',')), "ab,a", 1, 5);
  }

  @Test
  public void testEndBy() {
    Parser<List<Integer>> parser = INTEGER.endBy(isChar(';'));
    assertListParser(parser, "");
    assertListParser(parser, "1;", 1);
    assertListParser(parser, "12;3;", 12, 3);
    assertListParser(parser.followedBy(isChar(';')), ";");
    assertFailure(parser, "1", 1, 2);
    assertFailure(areChars("ab").endBy(isChar(';')), "ab;a", 1, 5);
  }

  @Test
  public void testEndBy1() {
    Parser<List<Integer>> parser = INTEGER.endBy1(isChar(';'));
    assertListParser(parser, "1;", 1);
    assertListParser(parser, "12;3;", 12, 3);
    assertFailure(parser, "", 1, 1);
    assertFailure(parser, ";", 1, 1);
    assertFailure(parser, "1", 1, 2);
    assertFailure(areChars("ab").endBy1(isChar(';')), "ab;a", 1, 5);
  }

  @Test
  public void testSepEndBy1() {
    Parser<List<Integer>> parser = INTEGER.sepEndBy1(COMMA);
    assertListParser(parser, "1,2", 1, 2);
    assertListParser(parser, "1", 1);
    assertListParser(parser, "1,", 1);
    assertFailure(parser, ",", 1, 1);
    assertFailure(parser, "", 1, 1);
    assertFailure(parser.next(Parsers.EOF), "1,,", 1, 3);
    assertFailure(areChars("ab").sepEndBy1(isChar(';')), "ab;a", 1, 5);
    
    // atomize on delimiter
    assertListParser(INTEGER.sepEndBy1(COMMA.next(COMMA).atomic()).followedBy(COMMA),
        "1,", 1);
    
    // 0 step partial delimiter consumption
    assertListParser(INTEGER.sepEndBy1(COMMA.step(0).next(COMMA)).followedBy(COMMA),
        "1,", 1);
    
    // partial delimiter consumption
    assertFailure(INTEGER.sepEndBy1(COMMA.next(COMMA)), "1,", 1, 3, ", expected, EOF encountered.");
    
    // infinite loop.
    assertListParser(Parsers.always().sepEndBy1(Parsers.always()), "", (Integer) null);
    
    // partial consumption on delimited.
    assertFailure(INTEGER.followedBy(COMMA).sepEndBy1(COMMA),
        "1,,1", 1, 5, ", expected, EOF encountered.");
    
    // 0 step partial delimited consumption
    assertListParser(INTEGER.step(0).followedBy(COMMA).sepEndBy1(COMMA).followedBy(string("1"))
        , "1,,1", 1);
  }

  @Test
  public void testSepEndBy() {
    Parser<List<Integer>> parser = INTEGER.sepEndBy(COMMA);
    assertListParser(parser, "1,2", 1, 2);
    assertListParser(parser, "1", 1);
    assertListParser(parser, "1,", 1);
    assertListParser(parser.followedBy(isChar(',')), ",");
    assertListParser(parser, "");
    assertFailure(parser.next(Parsers.EOF), "1,,", 1, 3);
    assertFailure(areChars("ab").sepEndBy(isChar(';')), "ab;a", 1, 5);
    
    // atomize on delimiter
    assertListParser(INTEGER.sepEndBy(COMMA.next(COMMA).atomic()).followedBy(COMMA),
        "1,", 1);
    
    // 0 step partial delimiter consumption
    assertListParser(INTEGER.sepEndBy(COMMA.step(0).next(COMMA)).followedBy(COMMA),
        "1,", 1);
    
    // partial delimiter consumption
    assertFailure(INTEGER.sepEndBy(COMMA.next(COMMA)), "1,", 1, 3, ", expected, EOF encountered.");
    
    // infinite loop.
    assertListParser(Parsers.always().sepEndBy(Parsers.always()), "", (Integer) null);
    
    // partial consumption on delimited.
    assertFailure(INTEGER.followedBy(COMMA).sepEndBy(COMMA),
        "1,,1", 1, 5, ", expected, EOF encountered.");
    
    // 0 step partial delimited consumption
    assertListParser(INTEGER.step(0).followedBy(COMMA).sepEndBy(COMMA).followedBy(string("1"))
        , "1,,1", 1);
  }

  @Test
  public void testEmptyListParser_toString() {
    assertEquals("[]", EmptyListParser.instance().toString());
  }
  
  @Mock Map<Integer, Integer> unaryOp;

  @Test
  public void testPrefix_noOperator() {
    replay();
    Parser<Integer> parser = INTEGER.prefix(isChar('-').retn(unaryOp));
    assertEquals("prefix", parser.toString());
    assertParser(parser, "123", 123);
  }

  @Test
  public void testPrefix() {
    expect(unaryOp.map(1)).andReturn(-1);
    expect(unaryOp.map(-1)).andReturn(1);
    expect(unaryOp.map(1)).andReturn(-1);
    replay();
    Parser<Integer> parser = INTEGER.prefix(isChar('-').retn(unaryOp));
    assertEquals("prefix", parser.toString());
    assertParser(parser, "---1", -1);
  }

  @Test
  public void testPostfix_noOperator() {
    replay();
    Parser<Integer> parser = INTEGER.postfix(isChar('^').retn(unaryOp));
    assertEquals("postfix", parser.toString());
    assertParser(parser, "123", 123);
  }

  @Test
  public void testPostfix() {
    expect(unaryOp.map(2)).andReturn(4);
    expect(unaryOp.map(4)).andReturn(256);
    replay();
    Parser<Integer> parser = INTEGER.postfix(isChar('^').retn(unaryOp));
    assertEquals("postfix", parser.toString());
    assertParser(parser, "2^^", 256);
  }
  
  @Mock Map2<Integer, Integer, Integer> binaryOp;

  @Test
  public void testInfixn_noOperator() {
    replay();
    Parser<Integer> parser = INTEGER.infixn(isChar('+').retn(binaryOp));
    assertEquals("infixn", parser.toString());
    assertParser(parser, "1", 1);
  }

  @Test
  public void testInfixn() {
    expect(binaryOp.map(1, 2)).andReturn(3);
    replay();
    Parser<Integer> parser = INTEGER.infixn(isChar('+').retn(binaryOp));
    assertEquals("infixn", parser.toString());
    assertParser(parser, "1+2+3", 3, "+3");
  }

  @Test
  public void testInfixl_noOperator() {
    replay();
    Parser<Integer> parser = INTEGER.infixl(isChar('+').retn(binaryOp));
    assertEquals("infixl", parser.toString());
    assertParser(parser, "1", 1);
  }

  @Test
  public void testInfixl() {
    expect(binaryOp.map(4, 1)).andReturn(3);
    expect(binaryOp.map(3, 2)).andReturn(1);
    replay();
    Parser<Integer> parser = INTEGER.infixl(isChar('-').retn(binaryOp));
    assertEquals("infixl", parser.toString());
    assertParser(parser, "4-1-2", 1);
  }

  @Test
  public void testInfixl_fails() {
    replay();
    assertFailure(INTEGER.infixl(isChar('-').retn(binaryOp)), "4-1-", 1, 5);
  }

  @Test
  public void testInfixr_noOperator() {
    replay();
    Parser<Integer> parser = INTEGER.infixr(isChar('+').retn(binaryOp));
    assertEquals("infixr", parser.toString());
    assertParser(parser, "1", 1);
  }

  @Test
  public void testInfixr() {
    expect(binaryOp.map(1, 2)).andReturn(12);
    expect(binaryOp.map(4, 12)).andReturn(412);
    replay();
    Parser<Integer> parser = INTEGER.infixr(string("->").retn(binaryOp));
    assertEquals("infixr", parser.toString());
    assertParser(parser, "4->1->2", 412);
  }

  @Test
  public void testInfixr_fails() {
    replay();
    assertFailure(INTEGER.infixr(isChar('-').retn(binaryOp)), "4-1-", 1, 5);
  }

  @Test
  public void testFrom() {
    List<Token> tokenList = Arrays.asList(new Token(0, 2, 'a'), new Token(2, 3, 4L));
    Parser<Long> parser = Terminals.CharLiteral.PARSER.next(Terminals.LongLiteral.PARSER);
    Parser<List<Token>> lexeme = constant(tokenList);
    assertParser(parser.from(lexeme), "", 4L);
    assertFailure(Terminals.CharLiteral.PARSER.from(constant(Arrays.<Token>asList())),
        "", 1, 1, "character literal expected, EOF encountered.");
    assertListParser(Parsers.ANY_TOKEN.many().from(lexeme), "", 'a', 4L);
    assertFailure(Parsers.ANY_TOKEN.from(lexeme), "abcde", 1, 3);
    assertFailure(Parsers.always().from(Parsers.<List<Token>>fail("foo")), "", 1, 1);
    Parser<String> badParser = Terminals.CharLiteral.PARSER.next(Terminals.Identifier.PARSER);
    assertFailure(badParser.from(lexeme), "aabbb", 1, 3);
  }

  @Test
  public void testFrom_throwsOnScanners() {
    assertFailure(string("foo").from(constant(Arrays.asList(new Token(0, 3, "foo")))),
        "foo", 1, 1, "Cannot scan characters on tokens.");
    assertFailure(isChar('f').from(constant(Arrays.asList(new Token(0, 1, 'f')))),
        "f", 1, 1, "Cannot scan characters on tokens.");
  }

  @Test
  public void testFrom_withDelimiter() {
    Parser<List<String>> integers = Terminals.IntegerLiteral.PARSER.many();
    Parser<List<String>> parser =
        integers.from(Terminals.IntegerLiteral.TOKENIZER, Scanners.WHITESPACES);
    assertEquals("followedBy", parser.toString());
    assertListParser(parser, "12 34   5 ", "12", "34", "5");
  }

  @Test
  public void testLexer() {
    Parser<List<Token>> parser = Terminals.LongLiteral.DEC_TOKENIZER.lexer(Scanners.WHITESPACES);
    assertParser(parser, "", Arrays.<Token>asList());
    assertParser(parser, "  ", Arrays.<Token>asList());
    assertParser(parser, " 12  ", Arrays.<Token>asList(new Token(1, 2, 12L)));
    assertParser(parser, "12 3  ", Arrays.<Token>asList(new Token(0, 2, 12L), new Token(3, 1, 3L)));
  }

  @Test
  public void testCopy() throws Exception {
    String content = "foo bar and baz";
    StringBuilder to = Parser.read(new StringReader(content));
    assertEquals(content, to.toString());
  }

  @Test
  public void emptyParseTreeInParserException() {
    try {
      Scanners.string("begin").parse("", Parser.Mode.DEBUG);
      fail();
    } catch (ParserException e) {
      ParseTree tree = e.getParseTree();
      assertEquals("root", tree.getName());
      assertEquals(0, tree.getBeginIndex());
      assertEquals(0, tree.getEndIndex());
      assertEquals(null, tree.getValue());
      assertEquals(tree.toString(), 0, tree.getChildren().size());
    }
  }

  @Test
  public void populatedParseTreeInParserException() {
    try {
      Scanners.string("begin").parse("beginx", Parser.Mode.DEBUG);
      fail();
    } catch (ParserException e) {
      ParseTree tree = e.getParseTree();
      assertEquals("root", tree.getName());
      assertEquals(0, tree.getBeginIndex());
      assertEquals(0, tree.getEndIndex());
      assertEquals(null, tree.getValue());
      assertEquals(tree.toString(), 1, tree.getChildren().size());
      ParseTree child = tree.getChildren().get(0);
      assertEquals("begin", child.getName());
    }
  }
  
  private static void assertListParser(
      Parser<? extends List<?>> parser, String source, Object... expected) {
    assertList(parser.parse(source), expected);
  }
  
  private static void assertList(Object actual, Object... expected) {
    assertEquals(Arrays.asList(expected), actual);
  }
}
