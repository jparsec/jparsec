package org.codehaus.jparsec.functors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.HashMap;

import org.junit.Test;

/**
 * Unit test for {@link Maps}.
 * 
 * @author Ben Yu
 */
public class MapsTest {

  @Test
  public void testToLowerCase() {
    assertEquals("foo", Maps.TO_LOWER_CASE.apply("Foo"));
    assertEquals("toLowerCase", Maps.TO_LOWER_CASE.toString());
  }

  @Test
  public void testToUpperCase() {
    assertEquals("FOO", Maps.TO_UPPER_CASE.apply("Foo"));
    assertEquals("toUpperCase", Maps.TO_UPPER_CASE.toString());
  }
  
  private enum MyEnum {
    FOO, BAR
  }

  @Test
  public void testToEnum() {
    assertEquals(MyEnum.FOO, Maps.toEnum(MyEnum.class).apply("FOO"));
    assertEquals("-> " + MyEnum.class.getName(), Maps.toEnum(MyEnum.class).toString());
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
