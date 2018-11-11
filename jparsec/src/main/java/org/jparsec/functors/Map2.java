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

import java.util.function.BiFunction;

/**
 * Maps two objects of type {@code A} and {@code B} respectively to an object of type {@code T}.
 *
 * @deprecated Use {@link java.util.function.BiFunction} instead.
 * @author Ben Yu
 */
@Deprecated
@FunctionalInterface
public interface Map2<A, B, T> extends BiFunction<A, B, T> {
  
  /** Maps {@code a} and {@code b} to the target object. */
  T map(A a, B b);

  @Override default public T apply(A a, B b) {
    return map(a, b);
  }
}
