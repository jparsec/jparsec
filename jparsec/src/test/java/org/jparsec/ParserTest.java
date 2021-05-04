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

import org.jparsec.easymock.BaseMockTest;
import org.jparsec.error.ParserException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.StringReader;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

import static org.jparsec.Asserts.assertFailure;
import static org.jparsec.Asserts.assertParser;
import static org.jparsec.Parsers.constant;
import static org.jparsec.Scanners.string;
import static org.jparsec.TestParsers.areChars;
import static org.jparsec.TestParsers.isChar;
import static org.easymock.EasyMock.expect;
import static org.junit.Assert.*;

/**
 * Unit test for {@link Parser}.
 * 
 * @author Ben Yu
 */
@RunWith(Parameterized.class)
public class ParserTest extends BaseMockTest {
  
  private static final Parser<Integer> INTEGER =
      Scanners.INTEGER.source().map(Integer::valueOf).label("integer");
  private static final Parser<String> FOO = constant("foo");
  private static final Parser<String> FAILURE = Parsers.fail("failure");
  private static final Parser<Void> COMMA = Scanners.isChar(',');

  @Parameterized.Parameters
  public static Collection<Object[]> data() {
          return Arrays.asList(new Object[] {Parser.Mode.PRODUCTION}, new Object[] {Parser.Mode.DEBUG});
  }

  private final Parser.Mode mode;

  public ParserTest(Parser.Mode mode) {
    this.mode = mode;
  }

  @SuppressWarnings("deprecation")
  @Test
  public void testParse() throws Exception {
    assertEquals("foo", FOO.parse("", mode));
    assertFailure(mode, FOO, "a", 1, 1, "EOF expected, a encountered.");
    assertFailure(FOO, "a", 1, 1, "test module", "EOF expected, a encountered.");
    assertEquals(new Integer(123), INTEGER.parse(new StringReader("123")));
    try {
      INTEGER.parse(new StringReader("x"), "test module");
      fail();
    } catch (ParserException e) {
      assertEquals(1, e.getLine());
      assertEquals(1, e.getColumn());
      assertTrue(e.getMessage(), e.getMessage().contains("test module"));
      assertTrue(e.getMessage(), e.getMessage().contains("integer expected, x encountered."));
    }
  }

  @Test
  public void testSource() {
    assertEquals("source", FOO.source().toString());
    assertEquals("", FOO.source().parse("", mode));
    assertParser(mode, COMMA.source(), ", ", ",", " ");
    assertParser(mode, Terminals.IntegerLiteral.TOKENIZER.label("INTEGER").source(), "123 ", "123", " ");
    assertEquals("123",
        Parsers.tokenType(Integer.class, "int")
            .from(INTEGER, Scanners.WHITESPACES).source()
            .parse("123", mode));
  }

  @Test
  public void testToken() {
    assertEquals("foo", FOO.token().toString());
    assertEquals(new Token(0, 0, "foo"), FOO.token().parse("", mode));
    assertEquals(new Token(0, 3, 123), INTEGER.token().parse("123", mode));
    assertFailure(mode, INTEGER.token(), "a", 1, 1);
  }

  @Test
  public void testWithSource() {
    assertEquals("foo", FOO.withSource().toString());
    assertEquals(new WithSource<String>("foo", ""), FOO.withSource().parse("", mode));
    assertEquals(new WithSource<Integer>(123, "123"), INTEGER.withSource().parse("123", mode));
    assertFailure(mode, INTEGER.withSource(), "a", 1, 1);
  }
  
  @Mock Function<Object, Parser<String>> next;

  @Test
  public void testNext_withMap() {
    expect(next.apply(1)).andReturn(FOO);
    replay();
    assertEquals("foo", INTEGER.next(next).parse("1", mode));
    assertEquals(next.toString(), INTEGER.next(next).toString());
  }

  @Test
  public void testNext_firstParserFails() {
    replay();
    assertFailure(mode, FAILURE.next(next), "", 1, 1, "failure");
  }

  @Test
  public void testNext_nextParserFails() {
    expect(next.apply(123)).andReturn(FAILURE);
    replay();
    assertFailure(mode, INTEGER.next(next), "123", 1, 4, "failure");
  }

  @Test
  public void testNext() {
    assertEquals("sequence", COMMA.next(INTEGER).toString());
    assertEquals((Object) 123, COMMA.next(INTEGER).parse(",123", mode));
    assertFailure(mode, FAILURE.next(FOO), "", 1, 1, "failure");
    assertFailure(mode, INTEGER.next(COMMA), "123", 1, 4);
  }

  @Test
  public void testRetn() {
    assertEquals((Object) 1, COMMA.retn(1).parse(",", mode));
    assertFailure(mode, FAILURE.retn(1), "", 1, 1, "failure");
  }

  @Test
  public void testUntil() {
    Parser<String> comma = Scanners.isChar(',').source();
    Parser<?> dot = Scanners.isChar('.');
    Parser<List<Object>> parser = INTEGER.cast().or(comma).until(dot);
    assertParser(mode, parser, "123,456.", Arrays.<Object>asList(123, ",", 456), ".");
    assertFailure(mode, parser, "", 1, 1);
    assertParser(mode, parser, ".", Arrays.asList(), ".");
  }

  @Test
  public void testFollowedBy() {
    assertEquals((Object) 123, INTEGER.followedBy(COMMA).parse("123,", mode));
    assertFailure(mode, FAILURE.followedBy(FOO), "", 1, 1, "failure");
    assertFailure(mode, INTEGER.followedBy(COMMA), "123", 1, 4, ", expected, EOF encountered.");
  }

  @Test
  public void testNotFollowedBy() {
    assertEquals((Object) 123, INTEGER.notFollowedBy(COMMA).parse("123", mode));
    assertEquals((Object) 123,
        INTEGER.notFollowedBy(COMMA.times(2)).followedBy(COMMA).parse("123,", mode));
    assertFailure(mode, FAILURE.notFollowedBy(FOO), "", 1, 1, "failure");
    assertFailure(mode, INTEGER.notFollowedBy(COMMA), "123,", 1, 4, "unexpected ,.");
  }

  @Test
  public void testSkipTimes() {
    assertEquals(null, isChar('a').skipTimes(3).parse("aaa", mode));
    assertFailure(mode, isChar('a').skipTimes(3), "aa", 1, 3);
    assertEquals(null, areChars("ab").skipTimes(3).parse("ababab", mode));
    assertEquals(null, FOO.skipTimes(3).parse("", mode));
    assertFailure(mode, areChars("ab").skipTimes(3), "aba", 1, 4);
    assertEquals("skipTimes", INTEGER.skipTimes(1).toString());
  }

  @Test
  public void testTimes() {
    assertListParser(isChar('a').times(3), "aaa", 'a', 'a', 'a');
    assertFailure(mode, isChar('a').times(3), "aa", 1, 3);
    assertListParser(areChars("ab").times(3), "ababab", 'b', 'b', 'b');
    assertListParser(FOO.times(2), "", "foo", "foo");
    assertFailure(mode, areChars("ab").times(3), "aba", 1, 4);
    assertEquals("times", INTEGER.times(1).toString());
  }

  @Test
  public void skipTimes_range() {
    assertEquals(null, isChar('a').skipTimes(0, 1).parse("", mode));
    assertFailure(mode, isChar('a').skipTimes(1, 2), "", 1, 1);
    assertFailure(mode, areChars("ab").skipTimes(1, 2), "aba", 1, 4);
    assertFailure(mode, areChars("ab").skipTimes(1, 2), "aba", 1, 4);
    assertEquals(null, FOO.skipTimes(0, 1).parse("", mode));
    assertEquals(null, FOO.skipTimes(1, 2).parse("", mode));
    assertParser(mode, isChar('a').asDelimiter().next(isChar('b')).skipTimes(1, 2), "aba", null, "a");
    assertEquals("skipTimes", isChar('a').skipTimes(1, 2).toString());
  }

  @Test
  public void testTimes_range() {
    assertListParser(isChar('a').times(0, 1), "");
    assertFailure(mode, isChar('a').times(1, 2), "", 1, 1);
    assertFailure(mode, areChars("ab").times(1, 2), "aba", 1, 4);
    assertFailure(mode, areChars("ab").times(1, 2), "aba", 1, 4);
    assertListParser(FOO.times(0, 1), "", "foo");
    assertListParser(FOO.times(2, 3), "", "foo", "foo", "foo");
    assertListParser(isChar('a').asDelimiter().next(isChar('b')).times(1, 2).followedBy(isChar('a')),
        "aba", 'b');
    assertEquals("times", isChar('a').times(1, 2).toString());
  }

  @Test
  public void testSkipMany() {
    assertEquals(null, isChar('a').skipMany().parse("", mode));
    assertEquals(null, isChar('a').skipMany().parse("a", mode));
    assertEquals(null, isChar('a').skipMany().parse("aaa", mode));
    assertFailure(mode, areChars("ab").skipMany(), "aba", 1, 4);
    assertEquals(null, FOO.skipMany().parse("", mode));
    assertEquals(null, isChar('a').skipAtLeast(0).parse("", mode));
    assertFailure(mode, isChar('a').skipAtLeast(1), "", 1, 1);
    assertFailure(mode, areChars("ab").skipAtLeast(1), "aba", 1, 4);
    assertFailure(mode, areChars("ab").skipAtLeast(2), "aba", 1, 4);
    assertEquals(null, FOO.skipAtLeast(0).parse("", mode));
    assertEquals(null, FOO.skipAtLeast(2).parse("", mode));
    assertParser(mode, isChar('a').asDelimiter().next(isChar('b')).skipMany(), "a", null, "a");
    assertEquals("skipAtLeast", isChar('a').skipMany().toString());
    assertEquals("skipAtLeast", isChar('a').skipAtLeast(2).toString());
  }

  @Test
  public void testSkipMany1() {
    assertFailure(mode, isChar('a').skipMany1(), "", 1, 1);
    assertEquals(null, isChar('a').skipMany1().parse("a", mode));
    assertEquals(null, isChar('a').skipMany1().parse("aaa", mode));
    assertFailure(mode, areChars("ab").skipMany1(), "aba", 1, 4);
    assertEquals(null, FOO.skipMany1().parse("", mode));
    assertParser(mode, isChar('a').asDelimiter().next(isChar('b')).skipMany1(), "aba", null, "a");
    assertEquals("skipAtLeast", isChar('a').skipMany1().toString());
  }

  @Test
  public void testMany1() {
    assertFailure(mode, isChar('a').many1(), "", 1, 1);
    assertListParser(isChar('a').many1(), "a", 'a');
    assertListParser(isChar('a').many1(), "aaa", 'a', 'a', 'a');
    assertFailure(mode, areChars("ab").many1(), "aba", 1, 4);
    assertListParser(areChars("ab").many1().followedBy(isChar('a')), "aba", 'b');
    assertListParser(FOO.many1(), "", "foo");
    assertListParser(isChar('a').asDelimiter().next(isChar('b')).many1().followedBy(isChar('a')),
        "aba", 'b');
    assertEquals("atLeast", isChar('a').many1().toString());
  }

  @Test
  public void testMany() {
    assertListParser(isChar('a').many(), "");
    assertListParser(isChar('a').many(), "a", 'a');
    assertListParser(isChar('a').many(), "aaa", 'a', 'a', 'a');
    assertFailure(mode, areChars("ab").many(), "aba", 1, 4);
    assertListParser(areChars("ab").many().followedBy(isChar('a')), "aba", 'b');
    assertListParser(FOO.many(), "");
    assertListParser(isChar('a').atLeast(0), "");
    assertFailure(mode, isChar('a').atLeast(1), "", 1, 1);
    assertFailure(mode, areChars("ab").atLeast(1), "aba", 1, 4);
    assertFailure(mode, areChars("ab").atLeast(2), "aba", 1, 4);
    assertListParser(FOO.atLeast(0), "");
    assertListParser(FOO.atLeast(2), "", "foo", "foo");
    assertListParser(isChar('a').asDelimiter().next(isChar('b')).many().followedBy(isChar('a')), "a");
    assertEquals("atLeast", isChar('a').many().toString());
    assertEquals("atLeast", isChar('a').atLeast(1).toString());
  }

  @Test
  public void testOr() {
    assertEquals("or", INTEGER.or(INTEGER).toString());
    assertEquals((Object) 123, INTEGER.or(constant(456)).parse("123", mode));
    assertEquals((Object) 'b', isChar('a').or(constant('b')).parse("", mode));
    assertEquals((Object) 'a', areChars("ab").or(isChar('a')).parse("a", mode));
    assertListParser(areChars("ab").or(isChar('a')).many(), "a", 'a');
    assertFailure(mode, areChars("ab").or(isChar('a')), "x", 1, 1);
  }

  @Test
  public void testOtherwise() {
    assertEquals((Object) 123, INTEGER.otherwise(constant(456)).parse("123", mode));
    assertEquals((Object) 'b', isChar('a').otherwise(constant('b')).parse("", mode));
    assertEquals((Object) 'c', isChar('a').next(isChar('b').otherwise(constant('c'))).parse("a", mode));
    assertFailure(mode, areChars("ab").otherwise(isChar('a')), "a", 1, 2);
    assertFailure(mode, areChars("ab").or(isChar('x')).otherwise(isChar('a')), "a", 1, 2);
    assertFailure(mode, areChars("ab").otherwise(isChar('a')), "x", 1, 1);
    assertEquals("otherwise", INTEGER.otherwise(INTEGER).toString());
  }

  @Test
  public void testOptional() {
    assertEquals((Object) 12, INTEGER.optional().parse("12", mode));
    assertEquals(null, INTEGER.optional().parse("", mode));
    assertFailure(mode, areChars("ab").optional(), "a", 1, 2);
  }

  @Test
  public void testAsOptional() {
    assertEquals(Optional.of(12), INTEGER.asOptional().parse("12", mode));
    assertEquals(Optional.empty(), INTEGER.asOptional().parse("", mode));
    assertFailure(mode, areChars("ab").asOptional(), "a", 1, 2);
  }

  @Test
  public void testOptional_withDefaultValue() {
    assertEquals((Object) 12, INTEGER.optional(0).parse("12", mode));
    assertEquals((Object) 0, INTEGER.optional(0).parse("", mode));
    assertFailure(mode, areChars("ab").optional('x'), "a", 1, 2);
  }

  @Test
  public void testNot() {
    assertEquals(null, INTEGER.not().parse("", mode));
    assertFailure(mode, INTEGER.not(), "12", 1, 1);
    assertParser(mode, areChars("ab").not(), "a", null, "a");
    assertEquals(null, INTEGER.not("num").parse("", mode));
    assertFailure(mode, INTEGER.not("num"), "12", 1, 1, "unexpected num");
  }

  @Test
  public void testPeek() {
    assertParser(mode, INTEGER.peek(), "12", 12, "12");
    assertFailure(mode, INTEGER.peek(), "a", 1, 1);
    assertFailure(mode, areChars("ab").peek(), "a", 1, 2);
    assertParser(mode, Parsers.or(areChars("ab").peek(), isChar('a')), "a", 'a', "");
    assertEquals("peek", INTEGER.peek().toString());
  }

  @Test
  public void testAtomic() {
    assertEquals("integer", INTEGER.atomic().toString());
    assertEquals((Object) 'b', areChars("ab").atomic().parse("ab", mode));
    assertEquals((Object) 'a',
        Parsers.or(areChars("ab").atomic(), isChar('a')).parse("a", mode));
    assertFailure(mode, areChars("ab").atomic(), "a", 1, 2);
  }

  @Test
  public void testStep() {
    assertEquals(INTEGER.toString(), INTEGER.asDelimiter().toString());
    assertEquals((Object) 'b',
        Parsers.or(areChars("ab").asDelimiter().next(isChar('c')), areChars("ab")).parse("ab", mode));
  }

  @Test
  public void testSucceeds() {
    assertParser(mode, isChar('a').succeeds(), "ab", true, "b");
    assertParser(mode, isChar('a').succeeds(), "xb", false, "xb");
    assertParser(mode, areChars("ab").succeeds(), "ax", false, "ax");
  }

  @Test
  public void testFails() {
    assertParser(mode, isChar('a').fails(), "ab", false, "b");
    assertParser(mode, isChar('a').fails(), "xb", true, "xb");
    assertParser(mode, areChars("ab").fails(), "ax", true, "ax");
  }

  @Test
  public void testIfElse() {
    Parser<Integer> parser = areChars("ab").ifelse(INTEGER, constant(0));
    assertEquals("ifelse", parser.toString());
    assertEquals((Object) 12, parser.parse("ab12", mode));
    assertEquals((Object) 0, parser.parse("", mode));
    assertParser(mode, parser, "a", 0, "a");
  }

  @Test
  public void testIfElse_withNext() {
    expect(next.apply('b')).andReturn(FOO);
    replay();
    assertEquals("foo", areChars("ab").ifelse(next, constant("bar")).parse("ab", mode));
  }

  @Test
  public void testLabel() {
    assertEquals("foo", FOO.label("the foo").parse("", mode));
    assertFailure(mode, INTEGER.label("number"), "", 1, 1, "number");
  }

  @Test
  public void labelShouldOverrideImplicitErrorMessage() {
    try {
      Scanners.string("foo").label("bar").parse("fo", mode);
      fail();
    } catch (ParserException e) {
      assertTrue(e.getMessage(), e.getMessage().contains("bar"));
      assertFalse(e.getMessage(), e.getMessage().contains("foo"));
    }
  }

  @Test
  public void labelShouldOverrideLabelMessage() {
    try {
      Scanners.string("foo").label("bar").label("override").parse("fo", mode);
      fail();
    } catch (ParserException e) {
      assertTrue(e.getMessage(), e.getMessage().contains("override"));
      assertFalse(e.getMessage(), e.getMessage().contains("foo"));
      assertFalse(e.getMessage(), e.getMessage().contains("bar"));
    }
  }

  @Test
  public void labelShouldOverrideFromAcrossAtomic() {
    try {
      Scanners.string("foo").label("bar").atomic().label("override").parse("fo", mode);
      fail();
    } catch (ParserException e) {
      assertTrue(e.getMessage(), e.getMessage().contains("override"));
      assertFalse(e.getMessage(), e.getMessage().contains("foo"));
      assertFalse(e.getMessage(), e.getMessage().contains("bar"));
    }
  }

  @Test
  public void labelShouldOverrideFromAcrossCast() {
    try {
      Scanners.string("foo").label("bar").cast().label("override").parse("fo", mode);
      fail();
    } catch (ParserException e) {
      assertTrue(e.getMessage(), e.getMessage().contains("override"));
      assertFalse(e.getMessage(), e.getMessage().contains("foo"));
      assertFalse(e.getMessage(), e.getMessage().contains("bar"));
    }
  }

  @Test
  public void labelShouldOverrideFromAcrossPeek() {
    try {
      Scanners.string("foo").label("bar").peek().label("override").parse("fo", mode);
      fail();
    } catch (ParserException e) {
      assertTrue(e.getMessage(), e.getMessage().contains("override"));
      assertFalse(e.getMessage(), e.getMessage().contains("foo"));
      assertFalse(e.getMessage(), e.getMessage().contains("bar"));
    }
  }

  @Test
  public void labelShouldOverrideFromAcrossAtomicAndPeek() {
    try {
      Scanners.string("foo").label("bar").atomic().peek().label("override").parse("fo", mode);
      fail();
    } catch (ParserException e) {
      assertTrue(e.getMessage(), e.getMessage().contains("override"));
      assertFalse(e.getMessage(), e.getMessage().contains("foo"));
      assertFalse(e.getMessage(), e.getMessage().contains("bar"));
    }
  }

  @Test
  public void labelShouldOverrideFromAcrossAsDelimiter() {
    try {
      Scanners.string("foo").label("bar").asDelimiter().label("override").parse("fo", mode);
      fail();
    } catch (ParserException e) {
      assertTrue(e.getMessage(), e.getMessage().contains("override"));
      assertFalse(e.getMessage(), e.getMessage().contains("foo"));
      assertFalse(e.getMessage(), e.getMessage().contains("bar"));
    }
  }

  @Test
  public void succeedsShouldNotLeaveErrorBehind() {
    try {
      Scanners.string("foo").succeeds().parse("fo", mode);
      fail();
    } catch (ParserException e) {
      assertTrue(e.getMessage(), e.getMessage().contains("EOF"));
      assertFalse(e.getMessage(), e.getMessage().contains("foo"));
    }
  }

  @Test
  public void testCast() {
    Parser<String> parser = Parsers.<CharSequence>constant("chars").<String>cast();
    assertEquals("chars", parser.toString());
    assertEquals("chars", parser.parse("", mode));
  }

  @Test
  public void testBetween() {
    assertEquals((Object) 123, INTEGER.between(isChar('('), isChar(')')).parse("(123)", mode));
  }
  
  @Mock Function<Integer, String> map;

  @Test
  public void testMap() {
    expect(map.apply(12)).andReturn("foo");
    replay();
    assertEquals("foo", INTEGER.map(map).parse("12", mode));
    assertEquals(map.toString(), INTEGER.map(map).toString());
  }

  @Test
  public void testMap_fails() {
    replay();
    assertFailure(mode, INTEGER.map(map), "", 1, 1, "integer expected, EOF encountered.");
  }

  @Test
  public void testSepBy1() {
    Parser<List<Integer>> parser = INTEGER.sepBy1(isChar(','));
    assertListParser(parser, "1", 1);
    assertListParser(parser, "123,45", 123, 45);
    assertListParser(parser.followedBy(Scanners.isChar(' ')), "1 ", 1);
    assertListParser(parser.followedBy(isChar(',')), "1,", 1);
    assertFailure(mode, parser, "", 1, 1);
    assertFailure(mode, areChars("ab").sepBy1(isChar(',')), "ab,a", 1, 5);
  }

  @Test
  public void testSepBy() {
    Parser<List<Integer>> parser = INTEGER.sepBy(isChar(','));
    assertListParser(parser, "1", 1);
    assertListParser(parser, "123,45", 123, 45);
    assertListParser(parser.followedBy(isChar(' ')), "1 ", 1);
    assertListParser(parser, "");
    assertListParser(parser.followedBy(isChar(',')), "1,", 1);
    assertFailure(mode, areChars("ab").sepBy(isChar(',')), "ab,a", 1, 5);
  }

  @Test
  public void testEndBy() {
    Parser<List<Integer>> parser = INTEGER.endBy(isChar(';'));
    assertListParser(parser, "");
    assertListParser(parser, "1;", 1);
    assertListParser(parser, "12;3;", 12, 3);
    assertListParser(parser.followedBy(isChar(';')), ";");
    assertFailure(mode, parser, "1", 1, 2);
    assertFailure(mode, areChars("ab").endBy(isChar(';')), "ab;a", 1, 5);
  }

  @Test
  public void testEndBy1() {
    Parser<List<Integer>> parser = INTEGER.endBy1(isChar(';'));
    assertListParser(parser, "1;", 1);
    assertListParser(parser, "12;3;", 12, 3);
    assertFailure(mode, parser, "", 1, 1);
    assertFailure(mode, parser, ";", 1, 1);
    assertFailure(mode, parser, "1", 1, 2);
    assertFailure(mode, areChars("ab").endBy1(isChar(';')), "ab;a", 1, 5);
  }

  @Test
  public void testSepEndBy1() {
    Parser<List<Integer>> parser = INTEGER.sepEndBy1(COMMA);
    assertListParser(parser, "1,2", 1, 2);
    assertListParser(parser, "1", 1);
    assertListParser(parser, "1,", 1);
    assertFailure(mode, parser, ",", 1, 1);
    assertFailure(mode, parser, "", 1, 1);
    assertFailure(mode, parser.next(Parsers.EOF), "1,,", 1, 3);
    assertFailure(mode, areChars("ab").sepEndBy1(isChar(';')), "ab;a", 1, 5);
    
    // atomize on delimiter
    assertListParser(INTEGER.sepEndBy1(COMMA.next(COMMA).atomic()).followedBy(COMMA),
        "1,", 1);
    
    // 0 step partial delimiter consumption
    assertListParser(INTEGER.sepEndBy1(COMMA.asDelimiter().next(COMMA)).followedBy(COMMA),
        "1,", 1);
    
    // partial delimiter consumption
    assertFailure(mode, INTEGER.sepEndBy1(COMMA.next(COMMA)), "1,", 1, 3, ", expected, EOF encountered.");
    
    // infinite loop.
    assertListParser(Parsers.always().sepEndBy1(Parsers.always()), "", (Integer) null);
    
    // partial consumption on delimited.
    assertFailure(mode, INTEGER.followedBy(COMMA).sepEndBy1(COMMA),
        "1,,1", 1, 5, ", expected, EOF encountered.");
    
    // 0 step partial delimited consumption
    assertListParser(INTEGER.asDelimiter().followedBy(COMMA).sepEndBy1(COMMA).followedBy(string("1"))
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
    assertFailure(mode, parser.next(Parsers.EOF), "1,,", 1, 3);
    assertFailure(mode, areChars("ab").sepEndBy(isChar(';')), "ab;a", 1, 5);
    
    // atomize on delimiter
    assertListParser(INTEGER.sepEndBy(COMMA.next(COMMA).atomic()).followedBy(COMMA),
        "1,", 1);
    
    // 0 step partial delimiter consumption
    assertListParser(INTEGER.sepEndBy(COMMA.asDelimiter().next(COMMA)).followedBy(COMMA),
        "1,", 1);
    
    // partial delimiter consumption
    assertFailure(mode, INTEGER.sepEndBy(COMMA.next(COMMA)), "1,", 1, 3, ", expected, EOF encountered.");
    
    // infinite loop.
    assertListParser(Parsers.always().sepEndBy(Parsers.always()), "", (Integer) null);
    
    // partial consumption on delimited.
    assertFailure(mode, INTEGER.followedBy(COMMA).sepEndBy(COMMA),
        "1,,1", 1, 5, ", expected, EOF encountered.");
    
    // 0 step partial delimited consumption
    assertListParser(INTEGER.asDelimiter().followedBy(COMMA).sepEndBy(COMMA).followedBy(string("1"))
        , "1,,1", 1);
  }

  @Test
  public void testEmptyListParser_toString() {
    assertEquals("[]", EmptyListParser.instance().toString());
  }
  
  @Mock Function<Integer, Integer> unaryOp;

  @Test
  public void testPrefix_noOperator() {
    replay();
    Parser<Integer> parser = INTEGER.prefix(isChar('-').retn(unaryOp));
    assertEquals((Object) 123, parser.parse("123", mode));
  }

  @Test
  public void testPrefix() {
    expect(unaryOp.apply(1)).andReturn(-1);
    expect(unaryOp.apply(-1)).andReturn(1);
    expect(unaryOp.apply(1)).andReturn(-1);
    replay();
    Parser<Integer> parser = INTEGER.prefix(isChar('-').retn(unaryOp));
    assertEquals(Integer.valueOf(-1), parser.parse("---1", mode));
  }

  @Test
  public void testPostfix_noOperator() {
    replay();
    Parser<Integer> parser = INTEGER.postfix(isChar('^').retn(unaryOp));
    assertEquals((Object) 123, parser.parse("123", mode));
  }

  @Test
  public void testPostfix() {
    expect(unaryOp.apply(2)).andReturn(4);
    expect(unaryOp.apply(4)).andReturn(256);
    replay();
    Parser<Integer> parser = INTEGER.postfix(isChar('^').retn(unaryOp));
    assertEquals((Object) 256, parser.parse("2^^", mode));
  }
  
  @Mock BiFunction<Integer, Integer, Integer> binaryOp;

  @Test
  public void testInfixn_noOperator() {
    replay();
    Parser<Integer> parser = INTEGER.infixn(isChar('+').retn(binaryOp));
    assertEquals((Object) 1, parser.parse("1", mode));
  }

  @Test
  public void testInfixn() {
    expect(binaryOp.apply(1, 2)).andReturn(3);
    replay();
    Parser<Integer> parser = INTEGER.infixn(isChar('+').retn(binaryOp));
    assertParser(mode, parser, "1+2+3", 3, "+3");
  }

  @Test
  public void testInfixl_noOperator() {
    replay();
    Parser<Integer> parser = INTEGER.infixl(isChar('+').retn(binaryOp));
    assertEquals((Object) 1, parser.parse("1", mode));
  }

  @Test
  public void testInfixl() {
    expect(binaryOp.apply(4, 1)).andReturn(3);
    expect(binaryOp.apply(3, 2)).andReturn(1);
    replay();
    Parser<Integer> parser = INTEGER.infixl(isChar('-').retn(binaryOp));
    assertEquals((Object) 1, parser.parse("4-1-2", mode));
  }

  @Test
  public void testInfixl_fails() {
    assertFailure(mode, INTEGER.infixl(isChar('-').retn(binaryOp)), "4-1-", 1, 5);
  }

  @Test
  public void testInfixr_noOperator() {
    replay();
    Parser<Integer> parser = INTEGER.infixr(isChar('+').retn(binaryOp));
    assertEquals((Object) 1, parser.parse("1", mode));
  }

  @Test
  public void testInfixr() {
    expect(binaryOp.apply(1, 2)).andReturn(12);
    expect(binaryOp.apply(4, 12)).andReturn(412);
    replay();
    Parser<Integer> parser = INTEGER.infixr(string("->").retn(binaryOp));
    assertEquals((Object) 412, parser.parse("4->1->2", mode));
  }

  @Test
  public void testInfixr_fails() {
    assertFailure(mode, INTEGER.infixr(isChar('-').retn(binaryOp)), "4-1-", 1, 5);
  }

  @Test
  public void testFrom() {
    List<Token> tokenList = Arrays.asList(new Token(0, 2, 'a'), new Token(2, 3, 4L));
    Parser<Long> parser = Terminals.CharLiteral.PARSER.next(Terminals.LongLiteral.PARSER);
    Parser<List<Token>> lexeme = constant(tokenList);
    assertEquals((Object) 4L, parser.from(lexeme).parse("", mode));
    assertFailure(mode, Terminals.CharLiteral.PARSER.from(constant(Arrays.<Token>asList())),
        "", 1, 1, "character literal expected, EOF encountered.");
    assertListParser(Parsers.ANY_TOKEN.many().from(lexeme), "", 'a', 4L);
    assertFailure(mode, Parsers.ANY_TOKEN.from(lexeme), "abcde", 1, 3);
    assertFailure(mode, Parsers.always().from(Parsers.<List<Token>>fail("foo")), "", 1, 1);
    Parser<String> badParser = Terminals.CharLiteral.PARSER.next(Terminals.Identifier.PARSER);
    assertFailure(mode, badParser.from(lexeme), "aabbb", 1, 3);
  }

  @Test
  public void testFrom_throwsOnScanners() {
    assertFailure(mode, string("foo").from(constant(Arrays.asList(new Token(0, 3, "foo")))),
        "foo", 1, 1, "Cannot scan characters on tokens.");
    assertFailure(mode, isChar('f').from(constant(Arrays.asList(new Token(0, 1, 'f')))),
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
    assertEquals(Arrays.<Token>asList(), parser.parse("", mode));
    assertEquals(Arrays.<Token>asList(), parser.parse("  ", mode));
    assertEquals(Arrays.<Token>asList(new Token(1, 2, 12L)), parser.parse(" 12  ", mode));
    assertEquals(Arrays.<Token>asList(new Token(0, 2, 12L), new Token(3, 1, 3L)),
        parser.parse("12 3  ", mode));
  }

  @Test
  public void testCopy() throws Exception {
    String content = "foo bar and baz";
    StringBuilder to = Parser.read(new StringReader(content));
    assertEquals(content, to.toString());
  }

  private void assertListParser(
      Parser<? extends List<?>> parser, String source, Object... expected) {
    assertList(parser.parse(source, mode), expected);
  }
  
  private static void assertList(Object actual, Object... expected) {
    assertEquals(Arrays.asList(expected), actual);
  }
}
