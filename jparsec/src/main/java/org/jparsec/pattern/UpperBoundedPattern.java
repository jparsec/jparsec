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

class UpperBoundedPattern extends Pattern {
  private final int max;
  private final Pattern pattern;

  UpperBoundedPattern(int max, Pattern pattern) {
    this.max = max;
    this.pattern = pattern;
  }

  @Override public int match(CharSequence src, int begin, int end) {
    return matchSome(max, pattern, src, end, begin, 0);
  }

  @Override public String toString() {
    return pattern.toString() + "{0," + max + '}';
  }

  static int matchSome(int max, Pattern pattern, CharSequence src, int len, int from, int acc) {
    int begin = from;
    for (int i = 0; i < max; i++) {
      int l = pattern.match(src, begin, len);
      if (MISMATCH == l) return begin - from + acc;
      begin += l;
    }
    return begin - from + acc;
  }
}
