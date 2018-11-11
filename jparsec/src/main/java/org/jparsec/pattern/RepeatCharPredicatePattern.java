/*****************************************************************************
 * Copyright 2013 (C) jparsec.org                                                *
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
package org.jparsec.pattern;

class RepeatCharPredicatePattern extends Pattern {

  private final int n;
  private final CharPredicate predicate;

  RepeatCharPredicatePattern(int n, CharPredicate predicate) {
    this.n = n;
    this.predicate = predicate;
  }

  @Override public int match(CharSequence src, int begin, int end) {
    return matchRepeat(n, predicate, src, end, begin, 0);
  }

  @Override public String toString() {
    return predicate.toString() + '{' + n + '}';
  }

  static int matchRepeat(int n, CharPredicate predicate, CharSequence src, int length, int begin, int acc) {
    int end = begin + n;
    if (end > length) return MISMATCH;
    for (int i = begin; i < end; i++) {
      if (!predicate.isChar(src.charAt(i))) return MISMATCH;
    }
    return n + acc;
  }
}
