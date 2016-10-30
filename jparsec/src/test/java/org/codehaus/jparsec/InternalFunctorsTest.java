package org.codehaus.jparsec;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import java.util.function.BiFunction;

import org.codehaus.jparsec.functors.Map3;
import org.codehaus.jparsec.functors.Map4;
import org.codehaus.jparsec.functors.Map5;
import org.junit.Test;

/**
 * Unit test for {@link InternalFunctors}.
 * 
 * @author Ben Yu
 */
public class InternalFunctorsTest {

  @Test
  public void testTokenWithSameValue() {
    Integer i = new Integer(10);
    TokenMap<Token> fromToken = InternalFunctors.tokenWithSameValue(i);
    assertEquals("10", fromToken.toString());
    assertNull(fromToken.map(new Token(1, 1, "foo")));
    assertNull(fromToken.map(new Token(1, 1, 2)));
    assertNull(fromToken.map(new Token(1, 1, null)));
    Token token = new Token(1, 1, i);
    assertSame(token, fromToken.map(token));
  }

  @Test
  public void testFirstOfTwo() {
    BiFunction<String, Integer, String> map = InternalFunctors.firstOfTwo();
    assertEquals("followedBy", map.toString());
    assertEquals("one", map.apply("one", 2));
  }

  @Test
  public void testLastOfTwo() {
    BiFunction<Integer, String, String> map = InternalFunctors.lastOfTwo();
    assertEquals("sequence", map.toString());
    assertEquals("two", map.apply(1, "two"));
  }

  @Test
  public void testLastOfThree() {
    Map3<Integer, String, String, String> map = InternalFunctors.lastOfThree();
    assertEquals("sequence", map.toString());
    assertEquals("three", map.map(1, "two", "three"));
  }

  @Test
  public void testLastOfFour() {
    Map4<Integer, String, String, String, String> map = InternalFunctors.lastOfFour();
    assertEquals("sequence", map.toString());
    assertEquals("four", map.map(1, "two", "three", "four"));
  }

  @Test
  public void testLastOfFive() {
    Map5<Integer, String, String, String, String, String> map = InternalFunctors.lastOfFive();
    assertEquals("sequence", map.toString());
    assertEquals("five", map.map(1, "two", "three", "four", "five"));
  }

}
