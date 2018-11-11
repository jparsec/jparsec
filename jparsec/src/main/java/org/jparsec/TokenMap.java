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
package org.jparsec;


/**
 * Maps a {@link Token} to a an object of type {@code T}, or null if the token isn't recognized.
 * 
 * @author Ben Yu
 */
@FunctionalInterface
public interface TokenMap<T> {
  
  /**
   * Transforms {@code token} to an instance of {@code T}.
   * {@code null} is returned if the token isn't recognized.
   */
  T map(Token token);
}
