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

import java.util.ArrayList;
import static java.util.Arrays.asList;
import java.util.Collections;

import org.codehaus.jparsec.util.Lists;


class SequencePattern extends Pattern {
  private final Pattern[] patterns;

  public SequencePattern(Pattern... patterns) {
    this.patterns = patterns;
  }

  @Override
  public Pattern derive(char c) {
    Pattern current = null;
    for (Pattern pattern : reverse(patterns)) {
      if (current == null)
        current = pattern;
      else {
        Pattern derivedPrefix = pattern.derive(c);
        Pattern derivedSuffix = current.derive(c);
        current = Patterns.orWithoutEmpty(Patterns.nextWithEmpty(derivedPrefix, current), //
          Patterns.nextWithEmpty(Patterns.nullable(pattern), derivedSuffix));
      }
    }
    return current;
  }

  private static ArrayList<Pattern> reverse(Pattern[] patterns1) {
    ArrayList<Pattern> reversed = Lists.arrayList();
    reversed.addAll(asList(patterns1));
    Collections.reverse(reversed);
    return reversed;
  }

  @Override
  public int match(final CharSequence src, final int begin, final int end) {
    int current = begin;
    for (Pattern pattern : patterns) {
      int l = pattern.match(src, current, end);
      if (l == Pattern.MISMATCH)
        return l;
      current += l;
    }
    return current - begin;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    for (Pattern pattern : patterns) {
      sb.append(pattern);
    }
    return sb.toString();
  }

}
