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

import java.util.function.Function;

/**
 * Maps object of type {@code From} to an object of type {@code To}.
 *
 * @deprecated Use {@link java.util.function.Function} instead.
 * @author Ben Yu
 */
@Deprecated
@FunctionalInterface
public interface Map<From, To> extends Function<From, To> {
  
  /** Maps {@code from} to the target object. */
  To map(From from);

  @Override default public To apply(From from) {
    return map(from);
  }
}
