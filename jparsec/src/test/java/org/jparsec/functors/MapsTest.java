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
package org.jparsec.functors;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

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
  }

  @Test
  public void testToTuple3() {
    assertEquals(Tuples.tuple("12", 1, 2), Maps.toTuple3().map("12", 1, 2));
  }

  @Test
  public void testToTuple4() {
    assertEquals(Tuples.tuple("123", 1, 2, 3), Maps.toTuple4().map("123", 1, 2, 3));
  }

  @Test
  public void testToTuple5() {
    assertEquals(Tuples.tuple("1234", 1, 2, 3, 4), Maps.toTuple5().map("1234", 1, 2, 3, 4));
  }
}
