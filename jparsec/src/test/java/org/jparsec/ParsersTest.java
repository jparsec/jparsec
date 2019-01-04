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
import org.jparsec.functors.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;

import static org.jparsec.Asserts.assertFailure;
import static org.jparsec.Parsers.always;
import static org.jparsec.TestParsers.areChars;
import static org.jparsec.TestParsers.isChar;
import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

/**
 * Unit test for {@link Parsers}.
 * 
 * @author Ben Yu
 */
@RunWith(Parameterized.class)
public class ParsersTest extends BaseMockTest {

  @Parameterized.Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(new Object[] {Parser.Mode.PRODUCTION}, new Object[] {Parser.Mode.DEBUG});
  }

  private final Parser.Mode mode;

  public ParsersTest(Parser.Mode mode) {
    this.mode = mode;
  }

  @Test
  public void testAlways() {
    assertEquals(null, always().parse("", mode));
  }

  @Test
  public void testNever() {
    assertFailure(mode, Parsers.never(), "", 1, 1);
    assertEquals("never", Parsers.never().toString());
  }

  @Test
  public void testBetween() {
    assertEquals(null,
        Parsers.between(isChar('('), Scanners.string("foo"), isChar(')')).parse("(foo)", mode));
  }

  @Test
  public void testEof() {
    Parsers.EOF.parse("", mode);
    assertFailure(mode, Parsers.EOF, "a", 1, 1, "EOF");
    Parsers.eof("END").parse("", mode);
    assertFailure(mode, Parsers.eof("END"), "a", 1, 1, "END");
    assertEquals("EOF", Parsers.EOF.toString());
  }

  @Test
  public void testConstant() {
    assertEquals("foo",
        Parsers.constant("foo").followedBy(Scanners.string("bar")).parse("bar", mode));
    assertEquals("foo", Parsers.constant("foo").toString());
  }

  @SuppressWarnings("deprecation")
  @Test
  public void testRunnable() {
    Runnable runnable = mock(Runnable.class);
    runnable.run();
    replay();
    assertEquals(null, Parsers.runnable(runnable).parse("", mode));
    assertEquals(runnable.toString(), Parsers.runnable(runnable).toString());
  }

  @Test
  public void testSequence_2Parsers() {
    Parser<Void> parser = Parsers.sequence(Scanners.isChar('a'), Scanners.isChar('b'));
    assertEquals("sequence", parser.toString());
    assertEquals(null, parser.parse("ab", mode));
    assertFailure(mode, parser, "xb", 1, 1);
    assertFailure(mode, parser, "ax", 1, 2);
  }

  @Test
  public void testSequence_3Parsers() {
    Parser<Void> parser =
        Parsers.sequence(Scanners.isChar('a'), Scanners.isChar('b'), Scanners.isChar('c'));
    assertEquals("sequence", parser.toString());
    assertEquals(null, parser.parse("abc", mode));
    assertFailure(mode, parser, "xbc", 1, 1);
    assertFailure(mode, parser, "axc", 1, 2);
    assertFailure(mode, parser, "abx", 1, 3);
  }

  @Test
  public void testSequence_4Parsers() {
    Parser<Void> parser = Parsers.sequence(
        Scanners.isChar('a'), Scanners.isChar('b'), Scanners.isChar('c'), Scanners.isChar('d'));
    assertEquals("sequence", parser.toString());
    assertEquals(null, parser.parse("abcd", mode));
    assertFailure(mode, parser, "xbcd", 1, 1);
    assertFailure(mode, parser, "axcd", 1, 2);
    assertFailure(mode, parser, "abxd", 1, 3);
    assertFailure(mode, parser, "abcx", 1, 4);
  }

  @Test
  public void testSequence_5Parsers() {
    Parser<Void> parser = Parsers.sequence(
        Scanners.isChar('a'), Scanners.isChar('b'), Scanners.isChar('c'),
        Scanners.isChar('d'), Scanners.isChar('e'));
    assertEquals("sequence", parser.toString());
    assertEquals(null, parser.parse("abcde", mode));
    assertFailure(mode, parser, "bbcde", 1, 1);
    assertFailure(mode, parser, "aacde", 1, 2);
    assertFailure(mode, parser, "abbde", 1, 3);
    assertFailure(mode, parser, "abcce", 1, 4);
    assertFailure(mode, parser, "abcdd", 1, 5);
  }

  @Test
  public void testSequence() {
    Parser<?> parser =
        Parsers.sequence(Scanners.isChar('a'), Scanners.isChar('b'), Scanners.isChar('c'));
    assertEquals("sequence", parser.toString());
    assertEquals(null, parser.parse("abc", mode));
    assertFailure(mode, parser, "xbc", 1, 1);
    assertFailure(mode, parser, "axc", 1, 2);
    assertFailure(mode, parser, "abx", 1, 3);
  }

  @Test
  public void testSequence_withIterable() {
    Parser<?> parser =
        Parsers.sequence(Arrays.asList(Scanners.isChar('a'), Scanners.isChar('b')));
    assertEquals("sequence", parser.toString());
    assertEquals(null, parser.parse("ab", mode));
    assertFailure(mode, parser, "xb", 1, 1);
    assertFailure(mode, parser, "ax", 1, 2);
  }

  @Test
  public void testSequence_0Parser() {
    Parser<?> parser = Parsers.sequence();
    assertEquals("sequence", parser.toString());
    assertEquals(null, parser.parse("", mode));
  }

  @Test
  public void testSequence_1Parser() {
    Parser<?> parser = Parsers.sequence(Scanners.isChar('a'));
    assertEquals("sequence", parser.toString());
    assertEquals(null, parser.parse("a", mode));
  }

  @Test
  public void testPair() {
    Parser<?> parser = Parsers.pair(isChar('a'), isChar('b'));
    assertEquals(Tuples.pair('a', 'b'), parser.parse("ab", mode));
    assertFailure(mode, parser, "xb", 1, 1);
    assertFailure(mode, parser, "ax", 1, 2);
  }

  @Test
  public void testTuple_2Parsers() {
    Parser<?> parser = Parsers.tuple(isChar('a'), isChar('b'));
    assertEquals(Tuples.pair('a', 'b'), parser.parse("ab", mode));
    assertFailure(mode, parser, "xb", 1, 1);
    assertFailure(mode, parser, "ax", 1, 2);
  }

  @Test
  public void testTuple_3Parsers() {
    Parser<?> parser = Parsers.tuple(isChar('a'), isChar('b'), isChar('c'));
    assertEquals(Tuples.tuple('a', 'b', 'c'), parser.parse("abc", mode));
    assertFailure(mode, parser, "xbc", 1, 1);
    assertFailure(mode, parser, "axc", 1, 2);
    assertFailure(mode, parser, "abx", 1, 3);
  }

  @Test
  public void testTuple_4Parsers() {
    Parser<?> parser = Parsers.tuple(isChar('a'), isChar('b'), isChar('c'), isChar('d'));
    assertEquals(Tuples.tuple('a', 'b', 'c', 'd'), parser.parse("abcd", mode));
    assertFailure(mode, parser, "xbcd", 1, 1);
    assertFailure(mode, parser, "axcd", 1, 2);
    assertFailure(mode, parser, "abxd", 1, 3);
    assertFailure(mode, parser, "abcx", 1, 4);
  }

  @Test
  public void testTuple_5Parsers() {
    Parser<?> parser =
        Parsers.tuple(isChar('a'), isChar('b'), isChar('c'), isChar('d'), isChar('e'));
    assertEquals(Tuples.tuple('a', 'b', 'c', 'd', 'e'), parser.parse("abcde", mode));
    assertFailure(mode, parser, "xbcde", 1, 1);
    assertFailure(mode, parser, "axcde", 1, 2);
    assertFailure(mode, parser, "abxde", 1, 3);
    assertFailure(mode, parser, "abcxe", 1, 4);
    assertFailure(mode, parser, "abcdx", 1, 5);
  }

  @Test
  public void testArray() {
    Parser<Object[]> parser = Parsers.array(isChar('a'), isChar('b'));
    assertEquals("array", parser.toString());
    assertEquals(Arrays.asList('a', 'b'), Arrays.asList(parser.parse("ab", mode)));
    assertFailure(mode, parser, "xb", 1, 1);
    assertFailure(mode, parser, "ax", 1, 2);
  }

  @Test
  public void testList() {
    Parser<List<Character>> parser = Parsers.list(Arrays.asList(isChar('a'), isChar('b')));
    assertEquals("list", parser.toString());
    assertEquals(Arrays.asList('a', 'b'), parser.parse("ab", mode));
    assertFailure(mode, parser, "xb", 1, 1);
    assertFailure(mode, parser, "ax", 1, 2);
  }

  @Test
  public void testFail() {
    assertFailure(mode, Parsers.fail("foo"), "a", 1, 1, "foo");
    assertEquals("foo", Parsers.fail("foo").toString());
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testOr_0Parser() {
    assertSame(Parsers.never(), Parsers.or());
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testOr_1Parser() {
    Parser<?> parser = Parsers.constant(1);
    assertSame(parser, Parsers.or(parser));
  }

  @Test
  public void testOr_withIterable() {
    Parser<Character> parser = Parsers.or(Arrays.asList(isChar('a'), isChar('b')));
    assertEquals("or", parser.toString());
    assertEquals((Object) 'a', parser.parse("a", mode));
    assertEquals((Object) 'b', parser.parse("b", mode));
    assertEquals((Object) 'a', Parsers.or(areChars("ab"), isChar('a')).parse("a", mode));
    assertFailure(mode, Parsers.or(areChars("abc"), areChars("ax")), "abx", 1, 3);
    assertFailure(mode, Parsers.or(areChars("ax"), areChars("abc")), "abx", 1, 3);
  }

  @Test
  public void testOr_2Parsers() {
    Parser<Character> parser = Parsers.or(isChar('a'), isChar('b'));
    assertEquals("or", parser.toString());
    assertEquals((Object) 'a', parser.parse("a", mode));
    assertEquals((Object) 'b', parser.parse("b", mode));
    assertEquals((Object) 'a', Parsers.or(areChars("ab"), isChar('a')).parse("a", mode));
    assertFailure(mode, Parsers.or(areChars("abc"), areChars("ax")), "abx", 1, 3);
    assertFailure(mode, Parsers.or(areChars("ax"), areChars("abc")), "abx", 1, 3);
  }

  @Test
  public void testOr_3Parsers() {
    Parser<Character> parser = Parsers.or(isChar('a'), isChar('b'), isChar('c'));
    assertEquals("or", parser.toString());
    assertEquals((Object) 'a', parser.parse("a", mode));
    assertEquals((Object) 'b', parser.parse("b", mode));
    assertEquals((Object) 'c', parser.parse("c", mode));
    assertEquals((Object) 'a', Parsers.or(areChars("ab"), isChar('b'), isChar('a')).parse("a", mode));
  }

  @Test
  public void testOr_4Parsers() {
    Parser<Character> parser = Parsers.or(isChar('a'), isChar('b'), isChar('c'), isChar('d'));
    assertEquals("or", parser.toString());
    assertEquals((Object) 'a', parser.parse("a", mode));
    assertEquals((Object) 'b', parser.parse("b", mode));
    assertEquals((Object) 'c', parser.parse("c", mode));
    assertEquals((Object) 'd', parser.parse("d", mode));
    assertEquals((Object) 'a', Parsers.or(areChars("ab"), isChar('b'), isChar('c'), isChar('a')).parse("a", mode));
  }

  @Test
  public void testOr_5Parsers() {
    Parser<Character> parser =
        Parsers.or(isChar('a'), isChar('b'), isChar('c'), isChar('d'), isChar('e'));
    assertEquals("or", parser.toString());
    assertEquals((Object) 'a', parser.parse("a", mode));
    assertEquals((Object) 'b', parser.parse("b", mode));
    assertEquals((Object) 'c', parser.parse("c", mode));
    assertEquals((Object) 'd', parser.parse("d", mode));
    assertEquals((Object) 'e', parser.parse("e", mode));
    assertEquals((Object) 'a', Parsers.or(areChars("ab"), isChar('b'), isChar('c'),
    isChar('d'), isChar('a')).parse("a", mode));
  }

  @Test
  public void testOr_6Parsers() {
    Parser<Character> parser =
        Parsers.or(isChar('a'), isChar('b'), isChar('c'), isChar('d'), isChar('e'), isChar('f'));
    assertEquals("or", parser.toString());
    assertEquals((Object) 'a', parser.parse("a", mode));
    assertEquals((Object) 'b', parser.parse("b", mode));
    assertEquals((Object) 'c', parser.parse("c", mode));
    assertEquals((Object) 'd', parser.parse("d", mode));
    assertEquals((Object) 'e', parser.parse("e", mode));
    assertEquals((Object) 'f', parser.parse("f", mode));
    assertEquals((Object) 'a', Parsers.or(areChars("ab"), isChar('b'), isChar('c'),
    isChar('d'), isChar('e'), isChar('a')).parse("a", mode));
  }

  @Test
  public void testOr_7Parsers() {
    Parser<Character> parser = Parsers.or(
        isChar('a'), isChar('b'), isChar('c'), isChar('d'), isChar('e'), isChar('f'), isChar('g'));
    assertEquals("or", parser.toString());
    assertEquals((Object) 'a', parser.parse("a", mode));
    assertEquals((Object) 'b', parser.parse("b", mode));
    assertEquals((Object) 'c', parser.parse("c", mode));
    assertEquals((Object) 'd', parser.parse("d", mode));
    assertEquals((Object) 'e', parser.parse("e", mode));
    assertEquals((Object) 'f', parser.parse("f", mode));
    assertEquals((Object) 'g', parser.parse("g", mode));
    assertEquals((Object) 'a', Parsers.or(areChars("ab"), isChar('b'), isChar('c'),
    isChar('d'), isChar('e'), isChar('f'), isChar('a')).parse("a", mode));
  }

  @Test
  public void testOr_8Parsers() {
    Parser<Character> parser = Parsers.or(
        isChar('a'), isChar('b'), isChar('c'), isChar('d'), isChar('e'), isChar('f'), isChar('g'),
        isChar('h'));
    assertEquals("or", parser.toString());
    assertEquals((Object) 'a', parser.parse("a", mode));
    assertEquals((Object) 'b', parser.parse("b", mode));
    assertEquals((Object) 'c', parser.parse("c", mode));
    assertEquals((Object) 'd', parser.parse("d", mode));
    assertEquals((Object) 'e', parser.parse("e", mode));
    assertEquals((Object) 'f', parser.parse("f", mode));
    assertEquals((Object) 'g', parser.parse("g", mode));
    assertEquals((Object) 'h', parser.parse("h", mode));
    assertEquals((Object) 'a', Parsers.or(areChars("ab"), isChar('b'), isChar('c'),
    isChar('d'), isChar('e'), isChar('f'), isChar('g'), isChar('a')).parse("a", mode));
  }

  @Test
  public void testOr_9Parsers() {
    Parser<Character> parser = Parsers.or(
        isChar('a'), isChar('b'), isChar('c'), isChar('d'), isChar('e'), isChar('f'), isChar('g'),
        isChar('h'), isChar('i'));
    assertEquals("or", parser.toString());
    assertEquals((Object) 'a', parser.parse("a", mode));
    assertEquals((Object) 'b', parser.parse("b", mode));
    assertEquals((Object) 'c', parser.parse("c", mode));
    assertEquals((Object) 'd', parser.parse("d", mode));
    assertEquals((Object) 'e', parser.parse("e", mode));
    assertEquals((Object) 'f', parser.parse("f", mode));
    assertEquals((Object) 'g', parser.parse("g", mode));
    assertEquals((Object) 'h', parser.parse("h", mode));
    assertEquals((Object) 'i', parser.parse("i", mode));
    assertEquals((Object) 'a', Parsers.or(areChars("ab"), isChar('b'), isChar('c'),
    isChar('d'), isChar('e'), isChar('f'), isChar('g'), isChar('h'), isChar('a')).parse("a", mode));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testOr_10Parsers() {
    Parser<Character> parser = Parsers.or(
        isChar('a'), isChar('b'), isChar('c'), isChar('d'), isChar('e'), isChar('f'), isChar('g'),
        isChar('h'), isChar('i'), isChar('j'));
    assertEquals("or", parser.toString());
    assertEquals((Object) 'a', parser.parse("a", mode));
    assertEquals((Object) 'b', parser.parse("b", mode));
    assertEquals((Object) 'c', parser.parse("c", mode));
    assertEquals((Object) 'd', parser.parse("d", mode));
    assertEquals((Object) 'e', parser.parse("e", mode));
    assertEquals((Object) 'f', parser.parse("f", mode));
    assertEquals((Object) 'g', parser.parse("g", mode));
    assertEquals((Object) 'h', parser.parse("h", mode));
    assertEquals((Object) 'i', parser.parse("i", mode));
    assertEquals((Object) 'a', Parsers.or(areChars("ab"), isChar('b'), isChar('c'),
    isChar('d'), isChar('e'), isChar('f'), isChar('g'), isChar('h'), isChar('i'),
    isChar('a')).parse("a", mode));
  }

  @Test
  public void testLonger() {
    assertEquals((Object) 'b', Parsers.longer(isChar('a'), areChars("ab")).parse("ab", mode));
    assertEquals((Object) 'b', Parsers.longer(areChars("ab"), isChar('a')).parse("ab", mode));
    assertEquals((Object) 'c', Parsers.longer(areChars("ab"), areChars("abc")).parse("abc", mode));
    assertEquals((Object) 'c', Parsers.longer(areChars("abc"), areChars("ab")).parse("abc", mode));
    assertEquals("longest", Parsers.longer(isChar('a'), isChar('b')).toString());
  }

  @Test
  public void testShorter() {
    assertEquals((Object) 'a',
        Parsers.shorter(isChar('a'), areChars("ab")).followedBy(Scanners.isChar('b')).parse("ab", mode));
    assertEquals((Object) 'a',
        Parsers.shorter(areChars("ab"), isChar('a')).followedBy(Scanners.isChar('b')).parse("ab", mode));
    assertEquals((Object) 'b',
        Parsers.shorter(areChars("ab"), areChars("abc")).followedBy(Scanners.isChar('c')).parse("abc", mode));
    assertEquals((Object) 'b',
        Parsers.shorter(areChars("abc"), areChars("ab")).followedBy(Scanners.isChar('c')).parse("abc", mode));
    assertEquals("shortest", Parsers.shorter(isChar('a'), isChar('b')).toString());
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testLongest_0Parser() {
    assertSame(Parsers.never(), Parsers.longest());
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testLongest_1Parser() {
    Parser<?> parser = Parsers.constant(1);
    assertSame(parser, Parsers.longest(parser));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testLongest() {
    assertEquals((Object) 'b', Parsers.longest(isChar('a'), isChar('b'), areChars("ab")).parse("ab", mode));
    assertEquals((Object) 'b', Parsers.longest(areChars("ab"), isChar('a')).parse("ab", mode));
    assertEquals((Object) 'c', Parsers.longest(areChars("ab"), areChars("abc")).parse("abc", mode));
    assertEquals((Object) 'c', Parsers.longest(areChars("abc"), areChars("ab")).parse("abc", mode));
    assertEquals((Object) 'c', Parsers.longest(Arrays.asList(areChars("abc"), areChars("ab"))).parse("abc", mode));
    assertEquals("longest", Parsers.longest(isChar('a'), isChar('b')).toString());
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testShortest_0Parser() {
    assertSame(Parsers.never(), Parsers.shortest());
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testShortest_1Parser() {
    Parser<?> parser = Parsers.constant(1);
    assertSame(parser, Parsers.shortest(parser));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testShortest() {
    assertEquals((Object) 'a', Parsers.shortest(isChar('a'), areChars("ab")).followedBy(Scanners.isChar('b')).parse("ab", mode));
    assertEquals((Object) 'a', Parsers.shortest(areChars("ab"), isChar('a')).followedBy(Scanners.isChar('b')).parse("ab", mode));
    assertEquals((Object) 'b', Parsers.shortest(areChars("ab"), areChars("abc")).followedBy(Scanners.isChar('c')).parse("abc", mode));
    assertEquals((Object) 'b', Parsers.shortest(areChars("abc"), areChars("ab")).followedBy(Scanners.isChar('c')).parse("abc", mode));
    assertEquals((Object) 'b', Parsers.shortest(Arrays.asList(areChars("abc"), areChars("ab")))
    .followedBy(Scanners.isChar('c')).parse("abc", mode));
    assertEquals("shortest", Parsers.shortest(isChar('a'), isChar('b')).toString());
  }

  @Test
  public void testExpect() {
    assertFailure(mode, Parsers.expect("foo"), "", 1, 1, "foo expected");
    assertEquals("foo", Parsers.expect("foo").toString());
  }

  @Test
  public void testUnexpected() {
    assertFailure(mode, Parsers.unexpected("foo"), "", 1, 1, "unexpected foo");
    assertEquals("foo", Parsers.unexpected("foo").toString());
  }
  
  @Mock BiFunction<Character, Character, Integer> map2;

  @Test
  public void testSequence_withMap2() {
    expect(map2.apply('a', 'b')).andReturn(1);
    replay();
    Parser<Integer> parser = Parsers.sequence(isChar('a'), isChar('b'), map2);
    assertEquals(map2.toString(), parser.toString());
    assertEquals((Object) 1, parser.parse("ab", mode));
  }

  @Test
  public void testSequence_withMap2_fails() {
    replay();
    Parser<Integer> parser = Parsers.sequence(isChar('a'), isChar('b'), map2);
    assertFailure(mode, parser, "xb", 1, 1);
    assertFailure(mode, parser, "ax", 1, 2);
  }
  
  @Mock
  Map3<Character, Character, Character, Integer> map3;

  @Test
  public void testSequence_withMap3() {
    expect(map3.map('a', 'b', 'c')).andReturn(1);
    replay();
    Parser<Integer> parser = Parsers.sequence(isChar('a'), isChar('b'), isChar('c'), map3);
    assertEquals(map3.toString(), parser.toString());
    assertEquals((Object) 1, parser.parse("abc", mode));
  }

  @Test
  public void testSequence_withMap3_fails() {
    replay();
    Parser<Integer> parser = Parsers.sequence(isChar('a'), isChar('b'),isChar('c'), map3);
    assertFailure(mode, parser, "xbc", 1, 1);
    assertFailure(mode, parser, "axc", 1, 2);
    assertFailure(mode, parser, "abx", 1, 3);
  }
  
  @Mock Map4<Character, Character, Character, Character, Integer> map4;

  @Test
  public void testSequence_withMap4() {
    expect(map4.map('a', 'b', 'c', 'd')).andReturn(1);
    replay();
    Parser<Integer> parser =
        Parsers.sequence(isChar('a'), isChar('b'), isChar('c'), isChar('d'), map4);
    assertEquals(map4.toString(), parser.toString());
    assertEquals((Object) 1, parser.parse("abcd", mode));
  }

  @Test
  public void testSequence_withMap4_fails() {
    replay();
    Parser<Integer> parser =
        Parsers.sequence(isChar('a'), isChar('b'),isChar('c'), isChar('d'), map4);
    assertFailure(mode, parser, "xbcd", 1, 1);
    assertFailure(mode, parser, "axcd", 1, 2);
    assertFailure(mode, parser, "abxd", 1, 3);
    assertFailure(mode, parser, "abcx", 1, 4);
  }
  
  @Mock
  Map5<Character, Character, Character, Character, Character, Integer> map5;

  @Test
  public void testSequence_withMap5() {
    expect(map5.map('a', 'b', 'c', 'd', 'e')).andReturn(1);
    replay();
    Parser<Integer> parser =
        Parsers.sequence(isChar('a'), isChar('b'), isChar('c'), isChar('d'), isChar('e'), map5);
    assertEquals(map5.toString(), parser.toString());
    assertEquals((Object) 1, parser.parse("abcde", mode));
  }
  
  @Mock
  Map6<Character, Character, Character, Character, Character, Character, Integer> map6;

  @Test
  public void testSequence_withMap6() {
    expect(map6.map('a', 'b', 'c', 'd', 'e', 'f')).andReturn(1);
    replay();
    Parser<Integer> parser =
        Parsers.sequence(isChar('a'), isChar('b'), isChar('c'), isChar('d'), isChar('e'), isChar('f'), map6);
    assertEquals(map6.toString(), parser.toString());
    assertEquals((Object) 1, parser.parse("abcdef", mode));
  }
  
  @Mock
  Map7<Character, Character, Character, Character, Character, Character, Character, Integer> map7;

  @Test
  public void testSequence_withMap7() {
    expect(map7.map('a', 'b', 'c', 'd', 'e', 'f', 'g')).andReturn(1);
    replay();
    Parser<Integer> parser = Parsers.sequence(
        isChar('a'), isChar('b'), isChar('c'), isChar('d'), isChar('e'), isChar('f'), isChar('g'), map7);
    assertEquals(map7.toString(), parser.toString());
    assertEquals((Object) 1, parser.parse("abcdefg", mode));
  }
  
  @Mock
  Map8<Character, Character, Character, Character, Character, Character, Character, Character, Integer> map8;

  @Test
  public void testSequence_withMap8() {
    expect(map8.map('a', 'b', 'c', 'd', 'e', 'f', 'g', 'h')).andReturn(1);
    replay();
    Parser<Integer> parser = Parsers.sequence(
        isChar('a'), isChar('b'), isChar('c'), isChar('d'), isChar('e'), isChar('f'), isChar('g'), isChar('h'), map8);
    assertEquals(map8.toString(), parser.toString());
    assertEquals((Object) 1, parser.parse("abcdefgh", mode));
  }

  @Test
  public void testSequence_withMap5_fails() {
    replay();
    Parser<Integer> parser =
        Parsers.sequence(isChar('a'), isChar('b'),isChar('c'), isChar('d'), isChar('e'), map5);
    assertFailure(mode, parser, "xbcde", 1, 1);
    assertFailure(mode, parser, "axcde", 1, 2);
    assertFailure(mode, parser, "abxde", 1, 3);
    assertFailure(mode, parser, "abcxe", 1, 4);
    assertFailure(mode, parser, "abcdx", 1, 5);
  }
  
  @Mock TokenMap<Integer> fromToken;

  @Test
  public void testToken() {
    Token token = new Token(1, 1, 'a');
    expect(fromToken.map(token)).andReturn(2);
    replay();
    Parser<Integer> parser = Parsers.token(fromToken);
    assertEquals(fromToken.toString(), parser.toString());
    assertEquals((Object) 2, parser.from(Parsers.constant(token).times(1)).parse("", mode));
  }

  @Test
  public void testToken_fails() {
    Token token = new Token(1, 1, 'a');
    expect(fromToken.map(token)).andReturn(null);
    replay();
    assertFailure(mode, 
        Parsers.token(fromToken).from(Parsers.constant(token).times(1)),
        "n", 1, 2);
  }

  @Test
  public void testTokenType() {
    Token token = new Token(0, 1, 'a');
    Parser<Character> parser = Parsers.tokenType(Character.class, "character");
    assertEquals("character", parser.toString());
    assertEquals((Object) 'a', parser.from(Parsers.constant(token).times(1)).parse("", mode));
  }

  @Test
  public void testAnyToken() {
    assertEquals("any token", Parsers.ANY_TOKEN.toString());
    Token token = new Token(0, 1, 'a');
    assertEquals(Character.valueOf('a'),
        Parsers.ANY_TOKEN.from(Parsers.constant(token).times(1)).parse("", mode));
    assertFailure(mode, 
        Parsers.ANY_TOKEN.from(Parsers.constant(token).times(0)), "", 1, 1);
  }

  @Test
  public void testIndex() {
    assertEquals((Object) 1, isChar('a').next(Parsers.INDEX).parse("a", mode));
    assertEquals("getIndex", Parsers.INDEX.toString());
  }

  @Test
  public void testSourceLocation() {
    assertEquals(1, isChar('a').next(Parsers.SOURCE_LOCATION).parse("a", mode).getIndex());
    assertEquals(1, isChar('a').next(Parsers.SOURCE_LOCATION).parse("a", mode).getLine());
    assertEquals(2, isChar('a').next(Parsers.SOURCE_LOCATION).parse("a", mode).getColumn());
  }

  @Test
  public void testSourceLocation_multipleLines() {
    SourceLocation location =
        Parsers.between(Scanners.string("ab\ncd\ne"), Parsers.SOURCE_LOCATION, Scanners.string("f\ngh"))
            .parse("ab\ncd\nef\ngh");
    assertEquals(3, location.getLine());
    assertEquals(2, location.getColumn());

    // Idempotent
    assertEquals(3, location.getLine());
    assertEquals(2, location.getColumn());
  }

  @Test
  public void testSourceLocation_nested() {
    Parser<SourceLocation> parser = Parsers.ANY_TOKEN.many().next(Parsers.SOURCE_LOCATION)
        .from(Scanners.IDENTIFIER.token().lexer(Scanners.WHITESPACES));
    SourceLocation location = parser.parse("ab\ncd\nef\ngh");
    assertEquals(4, location.getLine());
    assertEquals(3, location.getColumn());

    // Idempotent
    assertEquals(4, location.getLine());
    assertEquals(3, location.getColumn());
  }

  @Test
  public void testToArray() {
    Parser<Integer> p1 = Parsers.constant(1);
    Parser<Integer> p2 = Parsers.constant(2);
    Parser<Integer>[] array = Parsers.toArray(Arrays.asList(p1, p2));
    assertEquals(2, array.length);
    assertSame(p1, array[0]);
    assertSame(p2, array[1]);
  }

  @Test
  public void testToArrayWithIteration() {
    Parser<Integer> p1 = Parsers.constant(1);
    Parser<Integer> p2 = Parsers.constant(2);
    Parser<Integer>[] array = Parsers.toArrayWithIteration(Arrays.asList(p1, p2));
    assertEquals(2, array.length);
    assertSame(p1, array[0]);
    assertSame(p2, array[1]);
  }

}
