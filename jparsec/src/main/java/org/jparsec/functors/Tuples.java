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

/**
 * Creates {@link Pair} and tuple instances.
 * 
 * <p> These data holders can be used to hold temporary results during parsing so you don't have to
 * create your own data types.
 *
 * @deprecated Prefer to using a lambda expression to convert to your own type.
 * @author Ben Yu
 */
@Deprecated
public final class Tuples {
  
  /** Returns a {@link Pair} of 2 objects. Is equivalent to {@link #tuple(Object, Object)}. */
  public static <A, B> Pair<A, B> pair(A a, B b) {
    return new Pair<A, B>(a, b);
  }
  
  /** Returns a {@link Pair} of 2 objects. Is equivalent to {@link #pair(Object, Object)}. */
  public static <A, B> Pair<A, B> tuple(A a, B b) {
    return pair(a, b);
  }
  
  /** Returns a {@link Tuple3} of 3 objects. */
  public static <A, B, C> Tuple3<A, B, C> tuple(A a, B b, C c) {
    return new Tuple3<A, B, C>(a, b, c);
  }
  
  /** Returns a {@link Tuple4} of 4 objects. */
  public static <A, B, C, D> Tuple4<A, B, C, D> tuple(A a, B b, C c, D d) {
    return new Tuple4<A, B, C, D>(a, b, c, d);
  }
  
  /** Returns a {@link Tuple5} of 5 objects. */
  public static <A, B, C, D, E> Tuple5<A, B, C, D, E> tuple(A a, B b, C c, D d, E e) {
    return new Tuple5<A, B, C, D, E>(a, b, c, d, e);
  }
}
