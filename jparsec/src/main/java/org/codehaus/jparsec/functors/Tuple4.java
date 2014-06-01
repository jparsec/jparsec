/*****************************************************************************
 * Copyright (C) Codehaus.org                                                *
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
package org.codehaus.jparsec.functors;

import org.codehaus.jparsec.util.Objects;

/**
 * Immutable data holder for 4 values.
 * 
 * @author Ben Yu
 */
public class Tuple4<A, B, C, D> extends Tuple3<A, B, C> {
  
  public final D d;
  
  public Tuple4(A a, B b, C c, D d) {
    super(a, b, c);
    this.d = d;
  }
  
  boolean equals(Tuple4<?, ?, ?, ?> other) {
    return super.equals(other) && Objects.equals(d, other.d);
  }
  
  @Override public boolean equals(Object obj) {
    if (obj instanceof Tuple4<?, ?, ?, ?>) {
      return equals((Tuple4<?, ?, ?, ?>) obj);
    }
    return false;
  }
  
  @Override public int hashCode() {
    return super.hashCode() * 31 + Objects.hashCode(d);
  }
  
  @Override public String toString() {
    return "(" + a + ", " + b + ", " + c + ", " + d + ")";
  }
}
