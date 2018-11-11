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

import org.jparsec.util.ObjectTester;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Unit test for {@link Tuples}.
 * 
 * @author Ben Yu
 */
public class TuplesTest {

  @Test
  public void testPair() {
    Pair<String, Integer> pair = Tuples.pair("one", 1);
    assertEquals("(one, 1)", pair.toString());
    assertEquals("one", pair.a);
    assertEquals(1, pair.b.intValue());
    ObjectTester.assertEqual(pair, pair, Tuples.pair("one", 1));
    ObjectTester.assertNotEqual(pair, Tuples.pair("one", 2), Tuples.pair("two", 1), "abc");
  }

  @Test
  public void testTuple2() {
    Pair<String, Integer> pair = Tuples.tuple("one", 1);
    assertEquals("(one, 1)", pair.toString());
    assertEquals("one", pair.a);
    assertEquals(1, pair.b.intValue());
    ObjectTester.assertEqual(pair, pair, Tuples.pair("one", 1));
    ObjectTester.assertNotEqual(pair, Tuples.pair("one", 2), Tuples.pair("two", 1), "abc");
  }

  @Test
  public void testTuple3() {
    Tuple3<String, Integer, Integer> tuple = Tuples.tuple("12", 1, 2);
    assertEquals("(12, 1, 2)", tuple.toString());
    assertEquals("12", tuple.a);
    assertEquals(1, tuple.b.intValue());
    assertEquals(2, tuple.c.intValue());
    ObjectTester.assertEqual(tuple, tuple, Tuples.tuple("12", 1, 2));
    ObjectTester.assertNotEqual(tuple,
        Tuples.tuple("21", 1, 2), Tuples.tuple("12", 2, 2), Tuples.tuple("12", 1, 1), "abc");
  }

  @Test
  public void testTuple4() {
    Tuple4<String, Integer, Integer, Integer> tuple = Tuples.tuple("123", 1, 2, 3);
    assertEquals("(123, 1, 2, 3)", tuple.toString());
    assertEquals("123", tuple.a);
    assertEquals(1, tuple.b.intValue());
    assertEquals(2, tuple.c.intValue());
    assertEquals(3, tuple.d.intValue());
    ObjectTester.assertEqual(tuple, tuple, Tuples.tuple("123", 1, 2, 3));
    ObjectTester.assertNotEqual(tuple,
        Tuples.tuple("21", 1, 2, 3), Tuples.tuple("123", 2, 2, 3),
        Tuples.tuple("123", 1, 1, 3), Tuples.tuple("123", 1, 2, 2), "abc");
  }

  @Test
  public void testTuple5() {
    Tuple5<String, Integer, Integer, Integer, Integer> tuple = Tuples.tuple("1234", 1, 2, 3, 4);
    assertEquals("(1234, 1, 2, 3, 4)", tuple.toString());
    assertEquals("1234", tuple.a);
    assertEquals(1, tuple.b.intValue());
    assertEquals(2, tuple.c.intValue());
    assertEquals(3, tuple.d.intValue());
    assertEquals(4, tuple.e.intValue());
    ObjectTester.assertEqual(tuple, tuple, Tuples.tuple("1234", 1, 2, 3, 4));
    ObjectTester.assertNotEqual(tuple,
        Tuples.tuple("21", 1, 2, 3, 4), Tuples.tuple("1234", 2, 2, 3, 4),
        Tuples.tuple("1234", 1, 1, 3, 4), Tuples.tuple("1234", 1, 2, 2, 4), 
        Tuples.tuple("1234", 1, 2, 3, 3), "abc");
  }

}
