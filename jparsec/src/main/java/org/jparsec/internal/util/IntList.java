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

import org.jparsec.internal.annotations.Private;

/**
 * A simple, efficient and dynamic int list.
 * 
 * <p> Not thread-safe.
 * 
 * @author Ben Yu.
 */
public final class IntList {
  private int[] buf;
  private int len = 0;
  
  /** Creates a {@code int[]} object with all the elements. */
  public int[] toArray() {
    int[] ret = new int[len];
    for(int i = 0; i < len; i++) {
      ret[i] = buf[i];
    }
    return ret;
  }
  
  /** Creates an {@link IntList} object with initial capacity equal to {@code capacity}. */
  public IntList(int capacity) {
    this.buf = new int[capacity];
  }
  
  /** Creates an empty {@link IntList} object. */
  public IntList() {
    this(10);
  }
  
  /** Gets the number of int values stored. */
  public int size() {
    return len;
  }
  
  private void checkIndex(int i) {
    if (i < 0 || i >= len)
      throw new ArrayIndexOutOfBoundsException(i);
  }
  
  /**
   * Gets the int value at a index {@code i}.
   * @param i the 0 - based index of the value. 
   * @return the int value.
   * @throws ArrayIndexOutOfBoundsException if {@code i &lt; 0 or i >= size()}.
   */
  public int get(int i) {
    checkIndex(i);
    return buf[i];
  }
  
  /**
   * Sets the value at index {@code i} to {@code val}.
   * 
   * @param i the 0 - based index.
   * @param val the new value.
   * @return the old value.
   * @throws ArrayIndexOutOfBoundsException if {@code i &lt; 0 or i >= size()}.
   */
  public int set(int i, int val) {
    checkIndex(i);
    int old = buf[i];
    buf[i] = val;
    return old;
  }
  
  @Private static int calcSize(int expectedSize, int factor) {
    int rem = expectedSize % factor;
    return expectedSize / factor * factor + (rem > 0 ? factor : 0);
  }
  
  /**
   * Ensures that there is at least {@code l} capacity.
   * 
   * @param capacity the minimal capacity.
   */
  public void ensureCapacity(int capacity) {
    if (capacity > buf.length) {
      int factor = buf.length / 2 + 1;
      grow(calcSize(capacity - buf.length, factor));
    }
  }
  
  private void grow(int l) {
    int[] nbuf = new int[buf.length + l];
    System.arraycopy(buf, 0, nbuf, 0, buf.length);
    buf = nbuf;
  }
  
  /**
   * Adds {@code i} into the array.
   * 
   * @param i the int value.
   * @return this object.
   */
  public IntList add(int i) {
    ensureCapacity(len + 1);
    buf[len++] = i;
    return this;
  }
}
