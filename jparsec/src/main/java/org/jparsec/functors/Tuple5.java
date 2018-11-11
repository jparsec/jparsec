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
 * Immutable data holder for 5 values.
 *
 * @deprecated Prefer to using a lambda expression to convert to your own type.
 * @author Ben Yu
 */
@Deprecated
public class Tuple5<A, B, C, D, E> extends Tuple4<A, B, C, D>{
  
  public final E e;
  
  public Tuple5(A a, B b, C c, D d, E e) {
    super(a, b, c, d);
    this.e = e;
  }
  
  boolean equals(Tuple5<?, ?, ?, ?, ?> other) {
    return super.equals(other) && Objects.equals(e, other.e);
  }
  
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof Tuple5<?, ?, ?, ?, ?>) {
      return equals((Tuple5<?, ?, ?, ?, ?>) obj);
    }
    return false;
  }
  
  @Override
  public int hashCode() {
    return super.hashCode() * 31 + Objects.hashCode(e);
  }
  
  @Override
  public String toString() {
    return "(" + a + ", " + b + ", " + c + ", " + d + ", " + e + ")";
  }
}
