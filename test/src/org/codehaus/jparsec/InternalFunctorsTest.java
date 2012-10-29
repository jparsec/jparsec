package org.codehaus.jparsec;

import static org.easymock.EasyMock.expect;
import junit.framework.TestCase;

import org.codehaus.jparsec.easymock.BaseMockTest;
import org.codehaus.jparsec.functors.Map;
import org.codehaus.jparsec.functors.Map2;
import org.codehaus.jparsec.functors.Map3;
import org.codehaus.jparsec.functors.Map4;
import org.codehaus.jparsec.functors.Map5;

/**
 * Unit test for {@link InternalFunctors}.
 * 
 * @author Ben Yu
 */
public class InternalFunctorsTest extends TestCase {
  
  public void testIsTokenType() {
    TokenMap<Integer> fromToken = InternalFunctors.isTokenType(Integer.class, "int");
    assertEquals("int", fromToken.toString());
    assertNull(fromToken.map(new Token(1, 1, "foo")));
    assertNull(fromToken.map(new Token(1, 1, null)));
    assertEquals(Integer.valueOf(1), fromToken.map(new Token(1, 1, 1)));
  }
  
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
  
  public void testFirstOfTwo() {
    Map2<String, Integer, String> map = InternalFunctors.firstOfTwo();
    assertEquals("followedBy", map.toString());
    assertEquals("one", map.map("one", 2));
  }
  
  public void testLastOfTwo() {
    Map2<Integer, String, String> map = InternalFunctors.lastOfTwo();
    assertEquals("sequence", map.toString());
    assertEquals("two", map.map(1, "two"));
  }
  
  public void testLastOfThree() {
    Map3<Integer, String, String, String> map = InternalFunctors.lastOfThree();
    assertEquals("sequence", map.toString());
    assertEquals("three", map.map(1, "two", "three"));
  }
  
  public void testLastOfFour() {
    Map4<Integer, String, String, String, String> map = InternalFunctors.lastOfFour();
    assertEquals("sequence", map.toString());
    assertEquals("four", map.map(1, "two", "three", "four"));
  }
  
  public void testLastOfFive() {
    Map5<Integer, String, String, String, String, String> map = InternalFunctors.lastOfFive();
    assertEquals("sequence", map.toString());
    assertEquals("five", map.map(1, "two", "three", "four", "five"));
  }
  
  public static class FallbackTest extends BaseMockTest {
    @Mock Map<String, Integer> map1;
    @Mock Map<String, Integer> map2;
     
    public void testFirstMapReturnsNonNull() {
      expect(map1.map("one")).andReturn(1);
      replay();
      assertEquals(Integer.valueOf(1), fallback().map("one"));
    }
    
    public void testFirstMapReturnsNull() {
      expect(map1.map("one")).andReturn(null);
      expect(map2.map("one")).andReturn(1);
      replay();
      assertEquals(Integer.valueOf(1), fallback().map("one"));
    }
  
    public void testBothMapsReturnNull() {
      expect(map1.map("null")).andReturn(null);
      expect(map2.map("null")).andReturn(null);
      replay();
      assertNull(fallback().map("null"));
    }
    
    public void testToString() {
      assertEquals("fallback", fallback().toString());
    }
    
    private Map<String, Integer> fallback() {
      return InternalFunctors.fallback(map1, map2);
    }
  }
}
