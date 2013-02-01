package org.codehaus.jparsec;

import static org.codehaus.jparsec.Asserts.assertFailure;
import static org.codehaus.jparsec.Asserts.assertParser;
import static org.codehaus.jparsec.Parsers.always;
import static org.codehaus.jparsec.TestParsers.areChars;
import static org.codehaus.jparsec.TestParsers.isChar;
import static org.easymock.EasyMock.expect;

import java.util.Arrays;
import java.util.List;

import org.codehaus.jparsec.easymock.BaseMockTest;
import org.codehaus.jparsec.functors.Map2;
import org.codehaus.jparsec.functors.Map3;
import org.codehaus.jparsec.functors.Map4;
import org.codehaus.jparsec.functors.Map5;
import org.codehaus.jparsec.functors.Tuples;

/**
 * Unit test for {@link Parsers}.
 * 
 * @author Ben Yu
 */
public class ParsersTest extends BaseMockTest {
  
  public void testAlways() {
    assertParser(always(), "", null);
  }
  
  public void testNever() {
    assertFailure(Parsers.never(), "", 1, 1);
    assertEquals("never", Parsers.never().toString());
  }
  
  public void testBetween() {
    assertParser(Parsers.between(isChar('('), Scanners.string("foo"), isChar(')')), "(foo)", null);
  }
  
  public void testEof() {
    Parsers.EOF.parse("");
    assertFailure(Parsers.EOF, "a", 1, 1, "EOF");
    Parsers.eof("END").parse("");
    assertFailure(Parsers.eof("END"), "a", 1, 1, "END");
    assertEquals("EOF", Parsers.EOF.toString());
  }
  
  public void testConstant() {
    assertParser(Parsers.constant("foo").followedBy(Scanners.string("bar")), "bar", "foo");
    assertEquals("foo", Parsers.constant("foo").toString());
  }
  
  public void testRunnable() {
    Runnable runnable = mock(Runnable.class);
    runnable.run();
    replay();
    assertParser(Parsers.runnable(runnable), "", null);
    assertEquals(runnable.toString(), Parsers.runnable(runnable).toString());
  }
  
  public void testSequence_2Parsers() {
    Parser<Void> parser = Parsers.sequence(Scanners.isChar('a'), Scanners.isChar('b'));
    assertEquals("sequence", parser.toString());
    assertParser(parser, "ab", null);
    assertFailure(parser, "xb", 1, 1);
    assertFailure(parser, "ax", 1, 2);
  }
  
  public void testSequence_3Parsers() {
    Parser<Void> parser =
        Parsers.sequence(Scanners.isChar('a'), Scanners.isChar('b'), Scanners.isChar('c'));
    assertEquals("sequence", parser.toString());
    assertParser(parser, "abc", null);
    assertFailure(parser, "xbc", 1, 1);
    assertFailure(parser, "axc", 1, 2);
    assertFailure(parser, "abx", 1, 3);
  }
  
  public void testSequence_4Parsers() {
    Parser<Void> parser = Parsers.sequence(
        Scanners.isChar('a'), Scanners.isChar('b'), Scanners.isChar('c'), Scanners.isChar('d'));
    assertEquals("sequence", parser.toString());
    assertParser(parser, "abcd", null);
    assertFailure(parser, "xbcd", 1, 1);
    assertFailure(parser, "axcd", 1, 2);
    assertFailure(parser, "abxd", 1, 3);
    assertFailure(parser, "abcx", 1, 4);
  }
  
  public void testSequence_5Parsers() {
    Parser<Void> parser = Parsers.sequence(
        Scanners.isChar('a'), Scanners.isChar('b'), Scanners.isChar('c'),
        Scanners.isChar('d'), Scanners.isChar('e'));
    assertEquals("sequence", parser.toString());
    assertParser(parser, "abcde", null);
    assertFailure(parser, "bbcde", 1, 1);
    assertFailure(parser, "aacde", 1, 2);
    assertFailure(parser, "abbde", 1, 3);
    assertFailure(parser, "abcce", 1, 4);
    assertFailure(parser, "abcdd", 1, 5);
  }
  
  public void testSequence() {
    Parser<?> parser =
        Parsers.sequence(Scanners.isChar('a'), Scanners.isChar('b'), Scanners.isChar('c'));
    assertEquals("sequence", parser.toString());
    assertParser(parser, "abc", null);
    assertFailure(parser, "xbc", 1, 1);
    assertFailure(parser, "axc", 1, 2);
    assertFailure(parser, "abx", 1, 3);
  }
  
  public void testSequence_withIterable() {
    @SuppressWarnings("unchecked")
    Parser<?> parser =
        Parsers.sequence(Arrays.asList(Scanners.isChar('a'), Scanners.isChar('b')));
    assertEquals("sequence", parser.toString());
    assertParser(parser, "ab", null);
    assertFailure(parser, "xb", 1, 1);
    assertFailure(parser, "ax", 1, 2);
  }
  
  public void testSequence_0Parser() {
    Parser<?> parser = Parsers.sequence();
    assertEquals("sequence", parser.toString());
    assertParser(parser, "", null);
  }
  
  public void testSequence_1Parser() {
    Parser<?> parser = Parsers.sequence(Scanners.isChar('a'));
    assertEquals("sequence", parser.toString());
    assertParser(parser, "a", null);
  }
  
  public void testPair() {
    Parser<?> parser = Parsers.pair(isChar('a'), isChar('b'));
    assertEquals("pair", parser.toString());
    assertParser(parser, "ab", Tuples.pair('a', 'b'));
    assertFailure(parser, "xb", 1, 1);
    assertFailure(parser, "ax", 1, 2);
  }
  
  public void testTuple_2Parsers() {
    Parser<?> parser = Parsers.tuple(isChar('a'), isChar('b'));
    assertEquals("pair", parser.toString());
    assertParser(parser, "ab", Tuples.pair('a', 'b'));
    assertFailure(parser, "xb", 1, 1);
    assertFailure(parser, "ax", 1, 2);
  }
  
  public void testTuple_3Parsers() {
    Parser<?> parser = Parsers.tuple(isChar('a'), isChar('b'), isChar('c'));
    assertEquals("tuple", parser.toString());
    assertParser(parser, "abc", Tuples.tuple('a', 'b', 'c'));
    assertFailure(parser, "xbc", 1, 1);
    assertFailure(parser, "axc", 1, 2);
    assertFailure(parser, "abx", 1, 3);
  }
  
  public void testTuple_4Parsers() {
    Parser<?> parser = Parsers.tuple(isChar('a'), isChar('b'), isChar('c'), isChar('d'));
    assertEquals("tuple", parser.toString());
    assertParser(parser, "abcd", Tuples.tuple('a', 'b', 'c', 'd'));
    assertFailure(parser, "xbcd", 1, 1);
    assertFailure(parser, "axcd", 1, 2);
    assertFailure(parser, "abxd", 1, 3);
    assertFailure(parser, "abcx", 1, 4);
  }
  
  public void testTuple_5Parsers() {
    Parser<?> parser =
        Parsers.tuple(isChar('a'), isChar('b'), isChar('c'), isChar('d'), isChar('e'));
    assertEquals("tuple", parser.toString());
    assertParser(parser, "abcde", Tuples.tuple('a', 'b', 'c', 'd', 'e'));
    assertFailure(parser, "xbcde", 1, 1);
    assertFailure(parser, "axcde", 1, 2);
    assertFailure(parser, "abxde", 1, 3);
    assertFailure(parser, "abcxe", 1, 4);
    assertFailure(parser, "abcdx", 1, 5);
  }
  
  public void testArray() {
    Parser<Object[]> parser = Parsers.array(isChar('a'), isChar('b'));
    assertEquals("array", parser.toString());
    assertEquals(Arrays.asList('a', 'b'), Arrays.asList(parser.parse("ab")));
    assertFailure(parser, "xb", 1, 1);
    assertFailure(parser, "ax", 1, 2);
  }
  
  public void testList() {
    @SuppressWarnings("unchecked")
    Parser<List<Character>> parser = Parsers.list(Arrays.asList(isChar('a'), isChar('b')));
    assertEquals("list", parser.toString());
    assertEquals(Arrays.asList('a', 'b'), parser.parse("ab"));
    assertFailure(parser, "xb", 1, 1);
    assertFailure(parser, "ax", 1, 2);
  }
  
  public void testFail() {
    assertFailure(Parsers.fail("foo"), "a", 1, 1, "foo");
    assertEquals("foo", Parsers.fail("foo").toString());
  }
  
  public void testPlus_0Parser() {
    assertSame(Parsers.never(), Parsers.plus());
  }
  
  @SuppressWarnings("unchecked")
  public void testPlus_1Parser() {
    Parser<?> parser = Parsers.constant(1);
    assertSame(parser, Parsers.plus(parser));
  }
  
  public void testPlus_2Parsers() {
    Parser<Character> parser = Parsers.plus(isChar('a'), isChar('b'));
    assertEquals("plus", parser.toString());
    assertParser(parser, "a", 'a');
    assertParser(parser, "b", 'b');
    assertFailure(Parsers.plus(areChars("ab"), isChar('b')), "a", 1, 2);
    assertFailure(Parsers.plus(areChars("ax"), areChars("abc")), "abx", 1, 2);
  }
  
  public void testPlus_3Parsers() {
    Parser<Character> parser = Parsers.plus(isChar('a'), isChar('b'), isChar('c'));
    assertEquals("plus", parser.toString());
    assertParser(parser, "a", 'a');
    assertParser(parser, "b", 'b');
    assertParser(parser, "c", 'c');
    assertFailure(Parsers.plus(areChars("ab"), isChar('b'), isChar('c')), "a", 1, 2);
  }
  
  public void testOr_0Parser() {
    assertSame(Parsers.never(), Parsers.or());
  }
  
  @SuppressWarnings("unchecked")
  public void testOr_1Parser() {
    Parser<?> parser = Parsers.constant(1);
    assertSame(parser, Parsers.or(parser));
  }
  
  public void testOr_withIterable() {
    @SuppressWarnings("unchecked")
    Parser<Character> parser = Parsers.or(Arrays.asList(isChar('a'), isChar('b')));
    assertEquals("or", parser.toString());
    assertParser(parser, "a", 'a');
    assertParser(parser, "b", 'b');
    assertParser(Parsers.or(areChars("ab"), isChar('a')), "a", 'a');
    assertFailure(Parsers.or(areChars("abc"), areChars("ax")), "abx", 1, 3);
    assertFailure(Parsers.or(areChars("ax"), areChars("abc")), "abx", 1, 3);
  }
  
  public void testOr_2Parsers() {
    Parser<Character> parser = Parsers.or(isChar('a'), isChar('b'));
    assertEquals("or", parser.toString());
    assertParser(parser, "a", 'a');
    assertParser(parser, "b", 'b');
    assertParser(Parsers.or(areChars("ab"), isChar('a')), "a", 'a');
    assertFailure(Parsers.or(areChars("abc"), areChars("ax")), "abx", 1, 3);
    assertFailure(Parsers.or(areChars("ax"), areChars("abc")), "abx", 1, 3);
  }
  
  public void testOr_3Parsers() {
    Parser<Character> parser = Parsers.or(isChar('a'), isChar('b'), isChar('c'));
    assertEquals("or", parser.toString());
    assertParser(parser, "a", 'a');
    assertParser(parser, "b", 'b');
    assertParser(parser, "c", 'c');
    assertParser(Parsers.or(areChars("ab"), isChar('b'), isChar('a')), "a", 'a');
  }
  
  public void testOr_4Parsers() {
    Parser<Character> parser = Parsers.or(isChar('a'), isChar('b'), isChar('c'), isChar('d'));
    assertEquals("or", parser.toString());
    assertParser(parser, "a", 'a');
    assertParser(parser, "b", 'b');
    assertParser(parser, "c", 'c');
    assertParser(parser, "d", 'd');
    assertParser(Parsers.or(areChars("ab"), isChar('b'), isChar('c'), isChar('a')),
        "a", 'a');
  }
  
  public void testOr_5Parsers() {
    Parser<Character> parser =
        Parsers.or(isChar('a'), isChar('b'), isChar('c'), isChar('d'), isChar('e'));
    assertEquals("or", parser.toString());
    assertParser(parser, "a", 'a');
    assertParser(parser, "b", 'b');
    assertParser(parser, "c", 'c');
    assertParser(parser, "d", 'd');
    assertParser(parser, "e", 'e');
    assertParser(Parsers.or(areChars("ab"), isChar('b'), isChar('c'),
            isChar('d'), isChar('a')),
        "a", 'a');
  }
  
  public void testOr_6Parsers() {
    Parser<Character> parser =
        Parsers.or(isChar('a'), isChar('b'), isChar('c'), isChar('d'), isChar('e'), isChar('f'));
    assertEquals("or", parser.toString());
    assertParser(parser, "a", 'a');
    assertParser(parser, "b", 'b');
    assertParser(parser, "c", 'c');
    assertParser(parser, "d", 'd');
    assertParser(parser, "e", 'e');
    assertParser(parser, "f", 'f');
    assertParser(Parsers.or(areChars("ab"), isChar('b'), isChar('c'),
            isChar('d'), isChar('e'), isChar('a')),
        "a", 'a');
  }
  
  public void testOr_7Parsers() {
    Parser<Character> parser = Parsers.or(
        isChar('a'), isChar('b'), isChar('c'), isChar('d'), isChar('e'), isChar('f'), isChar('g'));
    assertEquals("or", parser.toString());
    assertParser(parser, "a", 'a');
    assertParser(parser, "b", 'b');
    assertParser(parser, "c", 'c');
    assertParser(parser, "d", 'd');
    assertParser(parser, "e", 'e');
    assertParser(parser, "f", 'f');
    assertParser(parser, "g", 'g');
    assertParser(Parsers.or(areChars("ab"), isChar('b'), isChar('c'),
            isChar('d'), isChar('e'), isChar('f'), isChar('a')),
        "a", 'a');
  }
  
  public void testOr_8Parsers() {
    Parser<Character> parser = Parsers.or(
        isChar('a'), isChar('b'), isChar('c'), isChar('d'), isChar('e'), isChar('f'), isChar('g'),
        isChar('h'));
    assertEquals("or", parser.toString());
    assertParser(parser, "a", 'a');
    assertParser(parser, "b", 'b');
    assertParser(parser, "c", 'c');
    assertParser(parser, "d", 'd');
    assertParser(parser, "e", 'e');
    assertParser(parser, "f", 'f');
    assertParser(parser, "g", 'g');
    assertParser(parser, "h", 'h');
    assertParser(Parsers.or(areChars("ab"), isChar('b'), isChar('c'),
            isChar('d'), isChar('e'), isChar('f'), isChar('g'), isChar('a')),
        "a", 'a');
  }
  
  public void testOr_9Parsers() {
    Parser<Character> parser = Parsers.or(
        isChar('a'), isChar('b'), isChar('c'), isChar('d'), isChar('e'), isChar('f'), isChar('g'),
        isChar('h'), isChar('i'));
    assertEquals("or", parser.toString());
    assertParser(parser, "a", 'a');
    assertParser(parser, "b", 'b');
    assertParser(parser, "c", 'c');
    assertParser(parser, "d", 'd');
    assertParser(parser, "e", 'e');
    assertParser(parser, "f", 'f');
    assertParser(parser, "g", 'g');
    assertParser(parser, "h", 'h');
    assertParser(parser, "i", 'i');
    assertParser(Parsers.or(areChars("ab"), isChar('b'), isChar('c'),
            isChar('d'), isChar('e'), isChar('f'), isChar('g'), isChar('h'), isChar('a')),
        "a", 'a');
  }
  
  @SuppressWarnings("unchecked")
  public void testOr_10Parsers() {
    Parser<Character> parser = Parsers.or(
        isChar('a'), isChar('b'), isChar('c'), isChar('d'), isChar('e'), isChar('f'), isChar('g'),
        isChar('h'), isChar('i'), isChar('j'));
    assertEquals("or", parser.toString());
    assertParser(parser, "a", 'a');
    assertParser(parser, "b", 'b');
    assertParser(parser, "c", 'c');
    assertParser(parser, "d", 'd');
    assertParser(parser, "e", 'e');
    assertParser(parser, "f", 'f');
    assertParser(parser, "g", 'g');
    assertParser(parser, "h", 'h');
    assertParser(parser, "i", 'i');
    assertParser(Parsers.or(areChars("ab"), isChar('b'), isChar('c'),
            isChar('d'), isChar('e'), isChar('f'), isChar('g'), isChar('h'), isChar('i'),
            isChar('a')),
        "a", 'a');
  }
  
  public void testLonger() {
    assertParser(Parsers.longer(isChar('a'), areChars("ab")), "ab", 'b');
    assertParser(Parsers.longer(areChars("ab"), isChar('a')), "ab", 'b');
    assertParser(Parsers.longer(areChars("ab"), areChars("abc")), "abc", 'c');
    assertParser(Parsers.longer(areChars("abc"), areChars("ab")), "abc", 'c');
    assertEquals("longest", Parsers.longer(isChar('a'), isChar('b')).toString());
  }
  
  public void testShorter() {
    assertParser(Parsers.shorter(isChar('a'), areChars("ab")).followedBy(Scanners.isChar('b')),
        "ab", 'a');
    assertParser(Parsers.shorter(areChars("ab"), isChar('a')).followedBy(Scanners.isChar('b')),
        "ab", 'a');
    assertParser(Parsers.shorter(areChars("ab"), areChars("abc")).followedBy(Scanners.isChar('c')),
        "abc", 'b');
    assertParser(Parsers.shorter(areChars("abc"), areChars("ab")).followedBy(Scanners.isChar('c')),
        "abc", 'b');
    assertEquals("shortest", Parsers.shorter(isChar('a'), isChar('b')).toString());
  }
  
  public void testLongest_0Parser() {
    assertSame(Parsers.never(), Parsers.longest());
  }
  
  @SuppressWarnings("unchecked")
  public void testLongest_1Parser() {
    Parser<?> parser = Parsers.constant(1);
    assertSame(parser, Parsers.longest(parser));
  }
  
  @SuppressWarnings("unchecked")
  public void testLongest() {
    assertParser(Parsers.longest(isChar('a'), isChar('b'), areChars("ab")), "ab", 'b');
    assertParser(Parsers.longest(areChars("ab"), isChar('a')), "ab", 'b');
    assertParser(Parsers.longest(areChars("ab"), areChars("abc")), "abc", 'c');
    assertParser(Parsers.longest(areChars("abc"), areChars("ab")), "abc", 'c');
    assertParser(Parsers.longest(Arrays.asList(areChars("abc"), areChars("ab"))), "abc", 'c');
    assertEquals("longest", Parsers.longest(isChar('a'), isChar('b')).toString());
  }
  
  public void testShortest_0Parser() {
    assertSame(Parsers.never(), Parsers.shortest());
  }
  
  @SuppressWarnings("unchecked")
  public void testShortest_1Parser() {
    Parser<?> parser = Parsers.constant(1);
    assertSame(parser, Parsers.shortest(parser));
  }
  
  @SuppressWarnings("unchecked")
  public void testShortest() {
    assertParser(Parsers.shortest(isChar('a'), areChars("ab")).followedBy(Scanners.isChar('b')),
        "ab", 'a');
    assertParser(Parsers.shortest(areChars("ab"), isChar('a')).followedBy(Scanners.isChar('b')),
        "ab", 'a');
    assertParser(Parsers.shortest(areChars("ab"), areChars("abc")).followedBy(Scanners.isChar('c')),
        "abc", 'b');
    assertParser(Parsers.shortest(areChars("abc"), areChars("ab")).followedBy(Scanners.isChar('c')),
        "abc", 'b');
    assertParser(Parsers.shortest(Arrays.asList(areChars("abc"), areChars("ab")))
            .followedBy(Scanners.isChar('c')),
        "abc", 'b');
    assertEquals("shortest", Parsers.shortest(isChar('a'), isChar('b')).toString());
  }
  
  public void testExpect() {
    assertFailure(Parsers.expect("foo"), "", 1, 1, "foo expected");
    assertEquals("foo", Parsers.expect("foo").toString());
  }
  
  public void testUnexpected() {
    assertFailure(Parsers.unexpected("foo"), "", 1, 1, "unexpected foo");
    assertEquals("foo", Parsers.unexpected("foo").toString());
  }
  
  @Mock Map2<Character, Character, Integer> map2;
  public void testSequence_withMap2() {
    expect(map2.map('a', 'b')).andReturn(1);
    replay();
    Parser<Integer> parser = Parsers.sequence(isChar('a'), isChar('b'), map2);
    assertEquals(map2.toString(), parser.toString());
    assertParser(parser, "ab", 1);
  }
  
  public void testSequence_withMap2_fails() {
    replay();
    Parser<Integer> parser = Parsers.sequence(isChar('a'), isChar('b'), map2);
    assertFailure(parser, "xb", 1, 1);
    assertFailure(parser, "ax", 1, 2);
  }
  
  @Mock Map3<Character, Character, Character, Integer> map3;
  public void testSequence_withMap3() {
    expect(map3.map('a', 'b', 'c')).andReturn(1);
    replay();
    Parser<Integer> parser = Parsers.sequence(isChar('a'), isChar('b'), isChar('c'), map3);
    assertEquals(map3.toString(), parser.toString());
    assertParser(parser, "abc", 1);
  }
  
  public void testSequence_withMap3_fails() {
    replay();
    Parser<Integer> parser = Parsers.sequence(isChar('a'), isChar('b'),isChar('c'), map3);
    assertFailure(parser, "xbc", 1, 1);
    assertFailure(parser, "axc", 1, 2);
    assertFailure(parser, "abx", 1, 3);
  }
  
  @Mock Map4<Character, Character, Character, Character, Integer> map4;
  public void testSequence_withMap4() {
    expect(map4.map('a', 'b', 'c', 'd')).andReturn(1);
    replay();
    Parser<Integer> parser =
        Parsers.sequence(isChar('a'), isChar('b'), isChar('c'), isChar('d'), map4);
    assertEquals(map4.toString(), parser.toString());
    assertParser(parser, "abcd", 1);
  }
  
  public void testSequence_withMap4_fails() {
    replay();
    Parser<Integer> parser =
        Parsers.sequence(isChar('a'), isChar('b'),isChar('c'), isChar('d'), map4);
    assertFailure(parser, "xbcd", 1, 1);
    assertFailure(parser, "axcd", 1, 2);
    assertFailure(parser, "abxd", 1, 3);
    assertFailure(parser, "abcx", 1, 4);
  }
  
  @Mock Map5<Character, Character, Character, Character, Character, Integer> map5;
  public void testSequence_withMap5() {
    expect(map5.map('a', 'b', 'c', 'd', 'e')).andReturn(1);
    replay();
    Parser<Integer> parser =
        Parsers.sequence(isChar('a'), isChar('b'), isChar('c'), isChar('d'), isChar('e'), map5);
    assertEquals(map5.toString(), parser.toString());
    assertParser(parser, "abcde", 1);
  }
  
  public void testSequence_withMap5_fails() {
    replay();
    Parser<Integer> parser =
        Parsers.sequence(isChar('a'), isChar('b'),isChar('c'), isChar('d'), isChar('e'), map5);
    assertFailure(parser, "xbcde", 1, 1);
    assertFailure(parser, "axcde", 1, 2);
    assertFailure(parser, "abxde", 1, 3);
    assertFailure(parser, "abcxe", 1, 4);
    assertFailure(parser, "abcdx", 1, 5);
  }
  
  @Mock TokenMap<Integer> fromToken;
  public void testToken() {
    Token token = new Token(1, 1, 'a');
    expect(fromToken.map(token)).andReturn(2);
    replay();
    Parser<Integer> parser = Parsers.token(fromToken);
    assertEquals(fromToken.toString(), parser.toString());
    assertParser(parser.from(Parsers.constant(token).times(1)), "", 2);
  }
  public void testToken_fails() {
    Token token = new Token(1, 1, 'a');
    expect(fromToken.map(token)).andReturn(null);
    replay();
    assertFailure(
        Parsers.token(fromToken).from(Parsers.constant(token).times(1)),
        "n", 1, 2);
  }
  
  public void testTokenType() {
    Token token = new Token(0, 1, 'a');
    Parser<Character> parser = Parsers.tokenType(Character.class, "character");
    assertEquals("character", parser.toString());
    assertParser(parser.from(Parsers.constant(token).times(1)), "", 'a');
  }
  
  public void testAnyToken() {
    assertEquals("any token", Parsers.ANY_TOKEN.toString());
    Token token = new Token(0, 1, 'a');
    assertParser(
        Parsers.ANY_TOKEN.from(Parsers.constant(token).times(1)),
        "", 'a');
    assertFailure(
        Parsers.ANY_TOKEN.from(Parsers.constant(token).times(0)), "", 1, 1);
  }

  public void testIndex() {
    assertParser(isChar('a').next(Parsers.INDEX), "a", 1);
    assertEquals("getIndex", Parsers.INDEX.toString());
  }
  
  public void testToArray() {
    Parser<Integer> p1 = Parsers.constant(1);
    Parser<Integer> p2 = Parsers.constant(2);
    @SuppressWarnings("unchecked")
    Parser<Integer>[] array = Parsers.toArray(Arrays.asList(p1, p2));
    assertEquals(2, array.length);
    assertSame(p1, array[0]);
    assertSame(p2, array[1]);
  }
  
  public void testToArrayWithIteration() {
    Parser<Integer> p1 = Parsers.constant(1);
    Parser<Integer> p2 = Parsers.constant(2);
    @SuppressWarnings("unchecked")
    Parser<Integer>[] array = Parsers.toArrayWithIteration(Arrays.asList(p1, p2));
    assertEquals(2, array.length);
    assertSame(p1, array[0]);
    assertSame(p2, array[1]);
  }
}
