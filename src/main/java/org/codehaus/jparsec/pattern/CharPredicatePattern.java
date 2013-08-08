/*****************************************************************************
 * Copyright 2013 (C) Codehaus.org                                                *
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
package org.codehaus.jparsec.pattern;

class CharPredicatePattern extends Pattern {
  private final CharPredicate predicate;

  public CharPredicatePattern(CharPredicate predicate) {
    this.predicate = predicate;
  }

  @Override
  public int match(CharSequence src, int begin, int end) {
    if (begin >= end)
      return Pattern.MISMATCH;
    else if (predicate.isChar(src.charAt(begin)))
      return 1;
    else
      return Pattern.MISMATCH;
  }

  @Override
  public Pattern derive(char c) {
    if (predicate.isChar(c))
      return Patterns.ALWAYS;
    else
      return Patterns.NEVER;
  }

  @Override
  public String toString() {
    return predicate.toString();
  }
}
