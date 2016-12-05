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
 * Immutable data holder for 2 values.
 *
 * @deprecated Prefer to using a lambda expression to convert to your own type.
 * @author Ben Yu
 */
@Deprecated
public class Pair<A, B> {
  
  public final A a;
  public final B b;

  public Pair(A a, B b) {
    this.a = a;
    this.b = b;
  }
  
  boolean equals(Pair<?, ?> other) {
    return Objects.equals(a, other.a) && Objects.equals(b, other.b);
  }
  
  @Override public boolean equals(Object obj) {
    if (obj instanceof Pair<?, ?>) {
      return equals((Pair<?, ?>) obj);
    }
    return false;
  }
  
  @Override public int hashCode() {
    return Objects.hashCode(a) * 31 + Objects.hashCode(b);
  }
  
  @Override public String toString() {
    return "(" + a + ", " + b + ")";
  }
}
