package org.codehaus.jparsec.functors;

import java.util.HashMap;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

/**
 * Unit test for {@link Maps}.
 * 
 * @author Ben Yu
 */
public class MapsTest {

  @Test
  public void testToInteger() {
    assertEquals(new Integer(123), Maps.TO_INTEGER.map("123"));
    assertEquals("integer", Maps.TO_INTEGER.toString());
  }

  @Test
  public void testToLowerCase() {
    assertEquals("foo", Maps.TO_LOWER_CASE.map("Foo"));
    assertEquals("toLowerCase", Maps.TO_LOWER_CASE.toString());
  }

  @Test
  public void testToUpperCase() {
    assertEquals("FOO", Maps.TO_UPPER_CASE.map("Foo"));
    assertEquals("toUpperCase", Maps.TO_UPPER_CASE.toString());
  }
  
  private enum MyEnum {
    FOO, BAR
  }

  @Test
  public void testToEnum() {
    assertEquals(MyEnum.FOO, Maps.toEnum(MyEnum.class).map("FOO"));
    assertEquals("-> " + MyEnum.class.getName(), Maps.toEnum(MyEnum.class).toString());
  }

  @Test
  public void testIdentity() {
    String string = new String("test");
    assertSame(string, Maps.identity().map(string));
    assertEquals("identity", Maps.identity().toString());
  }

  @Test
  public void testConstant() {
    String string = new String("test");
    assertSame(string, Maps.constant(string).map(1));
    assertEquals("test", Maps.constant(string).toString());
  }

  @Test
  public void testJmap() {
    HashMap<String, Integer> hashmap = new HashMap<String, Integer>();
    hashmap.put("one", 1);
    Map<String, Integer> map = Maps.map(hashmap);
    assertEquals(hashmap.toString(), map.toString());
    assertEquals(1, map.map("one").intValue());
    assertNull(map.map("two"));
  }

  @Test
  public void testMapToString() {
    assertEquals("1", Maps.mapToString().map(1));
    assertEquals("toString", Maps.mapToString().toString());
    assertEquals(String.valueOf((Object) null), Maps.mapToString().map(null));
  }

  @Test
  public void testToPair() {
    assertEquals(Tuples.pair("one", 1), Maps.toPair().map("one", 1));
    assertEquals("pair", Maps.toPair().toString());
  }

  @Test
  public void testToTuple3() {
    assertEquals(Tuples.tuple("12", 1, 2), Maps.toTuple3().map("12", 1, 2));
    assertEquals("tuple", Maps.toTuple3().toString());
  }

  @Test
  public void testToTuple4() {
    assertEquals(Tuples.tuple("123", 1, 2, 3), Maps.toTuple4().map("123", 1, 2, 3));
    assertEquals("tuple", Maps.toTuple4().toString());
  }

  @Test
  public void testToTuple5() {
    assertEquals(Tuples.tuple("1234", 1, 2, 3, 4), Maps.toTuple5().map("1234", 1, 2, 3, 4));
    assertEquals("tuple", Maps.toTuple5().toString());
  }

}
