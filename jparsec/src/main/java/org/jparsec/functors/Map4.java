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
 * Maps 4 objects of type {@code A}, {@code B}, {@code C} and {@code D} respectively
 * to an object of type {@code T}.
 * 
 * @author Ben Yu
 */
@FunctionalInterface
public interface Map4<A, B, C, D, T> {
  
  /** Maps {@code a}, {@code b}, {@code c} and {@code d} to the target object. */
  T map(A a, B b, C c, D d);
}
