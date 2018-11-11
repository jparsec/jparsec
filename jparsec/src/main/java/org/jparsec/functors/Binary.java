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

import java.util.function.BinaryOperator;

/**
 * Represents a binary operation on the same type {@code T}.
 * 
 * <p> Implement this interface for binary operator instead of {@link Map2} for brevity.
 *
 * @deprecated Use {@link java.util.function.BinaryOperator} instead.
 *
 * @author Ben Yu
 */
@Deprecated
public interface Binary<T> extends BinaryOperator<T>, Map2<T, T, T> {}
