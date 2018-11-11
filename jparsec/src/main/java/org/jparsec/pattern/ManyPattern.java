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

class ManyPattern extends Pattern {

  private final Pattern pattern;

  ManyPattern(Pattern pattern) {
    this.pattern = pattern;
  }

  static int matchMany(Pattern pattern, CharSequence src, int len, int from, int acc) {
    for (int i = from;;) {
      int l = pattern.match(src, i, len);
      if (MISMATCH == l)
        return i - from + acc;
      //we simply stop the loop when infinity is found. this may make the parser more user-friendly.
      if (l == 0)
        return i - from + acc;
      i += l;
    }
  }

  @Override
  public int match(CharSequence src, int begin, int end) {
    return matchMany(pattern, src, end, begin, 0);
  }

  @Override
  public String toString() {
    return pattern + "*";
  }

}
