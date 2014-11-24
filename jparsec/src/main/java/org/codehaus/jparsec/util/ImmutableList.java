/*****************************************************************************
 * Copyright 2013 (C) Codehaus.org                                                *
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
package org.codehaus.jparsec.util;

import java.util.Iterator;

/**
 * A immutable list that implements the concept in LISP. Null element is allowed.
 * The traverse order of this list is reversed, meaning last in first visited.
 *
 * @author Winter Young
 * @since 3.0
 */
public final class ImmutableList<T> implements Iterable<T> {
  private static class ImmutableListIterator<T> implements Iterator<T> {
    private ImmutableList<T> lst;

    public ImmutableListIterator(ImmutableList<T> lst) {
      this.lst = lst;
    }

    @Override
    public boolean hasNext() {
      return lst != empty();
    }

    @Override
    public T next() {
      T head = lst.head();
      lst = lst.tail();
      return head;
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException();
    }
  };

  private static final ImmutableList<?> empty = new ImmutableList<Object>();
  private int size;

  @SuppressWarnings("unchecked")
  public static <T> ImmutableList<T> empty() {
    return (ImmutableList<T>) empty;
  }

  private T head;
  private ImmutableList<T> tail;

  private ImmutableList() {
    this(null, null, 0);
  }

  private ImmutableList(T head, ImmutableList<T> tail, int size) {
    this.head = head;
    this.tail = tail;
    this.size = size;
  }

  public ImmutableList<T> insert(T head) {
    return new ImmutableList<T>(head, this, size + 1);
  }

  public T head() {
    return head;
  }

  public ImmutableList<T> tail() {
    return tail;
  }

  public boolean isEmpty() {
    return tail == null;
  }

  public ImmutableList<T> reverse() {
    ImmutableList<T> rev = ImmutableList.empty();
    for (T elem : this) {
      rev = rev.insert(elem);
    }
    return rev;
  }

  public int size() {
    return size;
  }

  @Override
  public Iterator<T> iterator() {
    return new ImmutableListIterator<T>(this);
  }
}
