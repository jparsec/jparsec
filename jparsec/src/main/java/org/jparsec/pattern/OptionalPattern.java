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

class OptionalPattern extends Pattern {
  private final Pattern pattern;

  OptionalPattern(Pattern pattern) {
    this.pattern = pattern;
  }

  @Override public int match(CharSequence src, int begin, int end) {
    int l = pattern.match(src, begin, end);
    return (l == MISMATCH) ? 0 : l;
  }

  @Override public String toString() {
    return pattern + "?";
  }
}
