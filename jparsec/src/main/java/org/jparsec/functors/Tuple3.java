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

import org.jparsec.internal.util.Objects;

/**
 * Immutable data holder for 3 values.
 *
 * @deprecated Prefer to using a lambda expression to convert to your own type.
 * @author Ben Yu
 */
@Deprecated
public class Tuple3<A, B, C> extends Pair<A, B> {
  
  public final C c;
  
  public Tuple3(A a, B b, C c) {
    super(a, b);
    this.c = c;
  }
  
  boolean equals(Tuple3<?, ?, ?> other) {
    return super.equals(other) && Objects.equals(c, other.c);
  }
  
  @Override public boolean equals(Object obj) {
    if (obj instanceof Tuple3<?, ?, ?>) {
      return equals((Tuple3<?, ?, ?>) obj);
    }
    return false;
  }
  
  @Override public int hashCode() {
    return super.hashCode() * 31 + Objects.hashCode(c);
  }
  
  @Override public String toString() {
    return "(" + a + ", " + b + ", " + c + ")";
  }
}
