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
package org.jparsec.internal.util;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit test for {@link IntList}.
 * 
 * @author Ben Yu
 */
public class IntListTest {

  @Test
  public void testCalcSize() {
    assertEquals(4, IntList.calcSize(4, 1));
    assertEquals(3, IntList.calcSize(1, 3));
    assertEquals(3, IntList.calcSize(2, 3));
    assertEquals(3, IntList.calcSize(3, 3));
    assertEquals(6, IntList.calcSize(4, 3));
  }

  @Test
  public void testConstructor() {
    IntList intList = new IntList();
    assertEquals(0, intList.size());
  }

  @Test
  public void testConstructor_withCapacity() {
    IntList intList = new IntList(1);
    assertEquals(0, intList.size());
  }

  @Test
  public void testToArray() {
    IntList intList = new IntList();
    assertEqualArray(intList.toArray());
    assertSame(intList, intList.add(1));
    assertSame(intList, intList.add(2));
    assertEqualArray(intList.toArray(), 1, 2);
  }

  @Test
  public void testGet() {
    IntList intList = new IntList();
    assertEquals(0, intList.size());
    intList.add(1);
    assertEquals(1, intList.get(0));
    assertEquals(1, intList.size());
    assertEqualArray(intList.toArray(), 1);
  }

  @Test
  public void testGet_throwsForNegativeIndex() {
    IntList intList = new IntList();
    try {
      intList.get(-1);
      fail();
    } catch (ArrayIndexOutOfBoundsException e) {}
  }

  @Test
  public void testGet_throwsForIndexOutOfBounds() {
    IntList intList = new IntList();
    try {
      intList.get(0);
      fail();
    } catch (ArrayIndexOutOfBoundsException e) {}
  }

  @Test
  public void testSet() {
    IntList intList = new IntList(0);
    intList.add(1);
    intList.set(0, 2);
    assertEquals(2, intList.get(0));
    assertEqualArray(intList.toArray(), 2);
  }

  @Test
  public void testEnsureCapacity() {
    IntList intList = new IntList(0);
    intList.add(1);
    intList.ensureCapacity(100);
    assertEquals(1, intList.size());
    assertEqualArray(intList.toArray(), 1);
  }

  @Test
  public void testSet_throwsForNegativeIndex() {
    IntList intList = new IntList();
    try {
      intList.set(-1, 0);
      fail();
    } catch (ArrayIndexOutOfBoundsException e) {}
  }

  @Test
  public void testSet_throwsForIndexOutOfBounds() {
    IntList intList = new IntList();
    try {
      intList.set(0, 0);
      fail();
    } catch (ArrayIndexOutOfBoundsException e) {}
  }
  
  private static void assertEqualArray(int[] array, int... values) {
    assertEquals(array.length, values.length);
    for (int i = 0; i < array.length; i++) {
      assertEquals(values[i], array[i]);
    }
  }
}
