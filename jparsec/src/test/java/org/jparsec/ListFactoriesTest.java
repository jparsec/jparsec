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

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

/**
 * Unit test for {@link ListFactories}.
 * 
 * @author Ben Yu
 */
public class ListFactoriesTest {

  @Test
  public void testArrayListFactory() {
    ListFactory<Integer> intListFactory = ListFactory.arrayListFactory();
    ListFactory<String> stringListFactory = ListFactory.arrayListFactory();
    ArrayList<Integer> intList = (ArrayList<Integer>) intListFactory.newList();
    ArrayList<String> stringList = (ArrayList<String>) stringListFactory.newList();
    assertNotSame(intList, stringList);
    assertEquals(0, intList.size());
    assertEquals(0, stringList.size());
  }

  @Test
  public void testArrayListFactoryWithFirstElement() {
    ListFactory<Integer> intListFactory = ListFactory.arrayListFactoryWithFirstElement(1);
    ArrayList<Integer> list = (ArrayList<Integer>) intListFactory.newList();
    assertEquals(Arrays.asList(1), list);
  }
}
