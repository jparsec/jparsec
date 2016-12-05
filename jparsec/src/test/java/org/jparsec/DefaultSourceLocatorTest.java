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

import org.jparsec.error.Location;
import org.jparsec.internal.util.IntList;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Unit test for {@link SourceLocator}.
 * 
 * @author Ben Yu
 */
public class DefaultSourceLocatorTest {

  @Test
  public void testLocate_onlyOneLineBreakCharacter() {
    SourceLocator locator = new SourceLocator("\n");
    Location location = locator.locate(0);
    assertEquals(new Location(1, 1), location);
    assertEquals(location, locator.locate(0));
    assertEquals(new Location(2, 1), locator.locate(1));
  }

  @Test
  public void testLocate_emptySource() {
    SourceLocator locator = new SourceLocator("");
    Location location = locator.locate(0);
    assertEquals(new Location(1, 1), location);
    assertEquals(location, locator.locate(0));
  }

  @Test
  public void testBinarySearch_firstElementIsEqual() {
    assertEquals(0, SourceLocator.binarySearch(intList(1, 2, 3), 1));
  }

  @Test
  public void testBinarySearch_firstElementIsBigger() {
    assertEquals(0, SourceLocator.binarySearch(intList(1, 2, 3), 0));
  }

  @Test
  public void testBinarySearch_secondElementIsEqual() {
    assertEquals(1, SourceLocator.binarySearch(intList(1, 2, 3), 2));
  }

  @Test
  public void testBinarySearch_secondElementIsBigger() {
    assertEquals(1, SourceLocator.binarySearch(intList(1, 3, 5), 2));
  }

  @Test
  public void testBinarySearch_lastElementIsEqual() {
    assertEquals(2, SourceLocator.binarySearch(intList(1, 3, 5), 5));
  }

  @Test
  public void testBinarySearch_lastElementIsBigger() {
    assertEquals(2, SourceLocator.binarySearch(intList(1, 3, 5), 4));
  }

  @Test
  public void testBinarySearch_allSmaller() {
    assertEquals(3, SourceLocator.binarySearch(intList(1, 3, 5), 10));
  }

  @Test
  public void testBinarySearch_oneEqualElement() {
    assertEquals(0, SourceLocator.binarySearch(intList(1), 1));
  }

  @Test
  public void testBinarySearch_oneBiggerElement() {
    assertEquals(0, SourceLocator.binarySearch(intList(2), 1));
  }

  @Test
  public void testBinarySearch_oneSmallerElement() {
    assertEquals(1, SourceLocator.binarySearch(intList(0), 1));
  }

  @Test
  public void testBinarySearch_noElement() {
    assertEquals(0, SourceLocator.binarySearch(intList(), 1));
  }

  @Test
  public void testLookup_noLineBreaksScanned() {
    SourceLocator locator = new SourceLocator("whatever", 2, 3);
    assertEquals(new Location(2, 4), locator.lookup(1));
  }

  @Test
  public void testLookup_inFirstLine() {
    SourceLocator locator = new SourceLocator("whatever", 2, 3);
    addLineBreaks(locator, 3, 5, 7);
    assertEquals(new Location(2, 4), locator.lookup(1));
  }

  @Test
  public void testLookup_firstLineBreak() {
    SourceLocator locator = new SourceLocator("whatever", 2, 3);
    addLineBreaks(locator, 3, 5, 7);
    assertEquals(new Location(2, 6), locator.lookup(3));
  }

  @Test
  public void testLookup_firstCharInSecondLine() {
    SourceLocator locator = new SourceLocator("whatever", 2, 3);
    addLineBreaks(locator, 3, 5, 7);
    assertEquals(new Location(3, 1), locator.lookup(4));
  }

  @Test
  public void testLookup_lastCharInSecondLine() {
    SourceLocator locator = new SourceLocator("whatever", 2, 3);
    addLineBreaks(locator, 3, 5, 7);
    assertEquals(new Location(3, 2), locator.lookup(5));
  }

  @Test
  public void testLookup_firstCharInThirdLine() {
    SourceLocator locator = new SourceLocator("whatever", 2, 3);
    addLineBreaks(locator, 3, 5, 7);
    assertEquals(new Location(4, 1), locator.lookup(6));
  }

  @Test
  public void testLookup_lastCharInThirdLine() {
    SourceLocator locator = new SourceLocator("whatever", 2, 3);
    addLineBreaks(locator, 3, 5, 7);
    assertEquals(new Location(4, 2), locator.lookup(7));
  }

  @Test
  public void testLookup_firstCharInLastLine() {
    SourceLocator locator = new SourceLocator("whatever", 2, 3);
    addLineBreaks(locator, 3, 5, 7);
    assertEquals(new Location(5, 1), locator.lookup(8));
  }

  @Test
  public void testLookup_secondCharInLastLine() {
    SourceLocator locator = new SourceLocator("whatever", 2, 3);
    addLineBreaks(locator, 3, 5, 7);
    assertEquals(new Location(5, 2), locator.lookup(9));
  }

  @Test
  public void testScanTo_indexOutOfBounds() {
    SourceLocator locator = new SourceLocator("whatever", 2, 3);
    try {
      locator.scanTo(100);
      fail();
    } catch (StringIndexOutOfBoundsException e) {}
  }

  @Test
  public void testScanTo_indexOnEof() {
    SourceLocator locator = new SourceLocator("foo", 2, 3);
    assertEquals(new Location(2, 6), locator.scanTo(3));
    assertEquals(3, locator.nextIndex);
    assertEquals(3, locator.nextColumnIndex);
  }

  @Test
  public void testScanTo_spansLines() {
    SourceLocator locator = new SourceLocator("foo\nbar\n", 2, 3);
    assertEquals(new Location(3, 1), locator.scanTo(4));
    assertEquals(5, locator.nextIndex);
    assertEquals(1, locator.nextColumnIndex);
  }

  @Test
  public void testScanTo_lastCharOfLine() {
    SourceLocator locator = new SourceLocator("foo\nbar\n", 2, 3);
    assertEquals(new Location(3, 4), locator.scanTo(7));
    assertEquals(8, locator.nextIndex);
    assertEquals(0, locator.nextColumnIndex);
  }

  @Test
  public void testLocate() {
    SourceLocator locator = new SourceLocator("foo\nbar\n", 2, 3);
    assertEquals(new Location(3, 4), locator.locate(7));
    assertEquals(new Location(2, 5), locator.locate(2)); // this will call lookup()
  }
  
  private static void addLineBreaks(SourceLocator locator, int... indices) {
    for (int i : indices) {
      locator.lineBreakIndices.add(i);
    }
  }
  
  private static IntList intList(int... ints) {
    IntList intList = new IntList();
    for (int i : ints) {
      intList.add(i);
    }
    return intList;
  }
}
