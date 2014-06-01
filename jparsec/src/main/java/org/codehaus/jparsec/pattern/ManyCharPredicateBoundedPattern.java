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

class ManyCharPredicateBoundedPattern extends Pattern {
  private final CharPredicate predicate;
  private final int min;

  public ManyCharPredicateBoundedPattern(CharPredicate predicate, int min) {
    this.predicate = predicate;
    this.min = min;
  }

  @Override
  public Pattern derive(char c) {
    if (predicate.isChar(c)) {
      if (min > 0)
        return Patterns.many(min - 1, predicate);
      else
        return Patterns.many(predicate);
    }

    return Patterns.NEVER;
  }

  @Override
  public int match(CharSequence src, int begin, int end) {
    int minLen = RepeatCharPredicatePattern.matchRepeat(min, predicate, src, end, begin, 0);
    if (minLen == MISMATCH)
      return MISMATCH;
    return ManyCharPredicatePattern.matchMany(predicate, src, end, begin + minLen, minLen);
  }

  @Override
  public String toString() {
    return (min > 1) ? (predicate + "{" + min + ",}") : (predicate + "+");
  }

}
