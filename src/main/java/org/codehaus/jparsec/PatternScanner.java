/*****************************************************************************
 * Copyright (C) Codehaus.org                                                *
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
package org.codehaus.jparsec;

import org.codehaus.jparsec.pattern.Pattern;
import org.codehaus.jparsec.pattern.Patterns;

/**
 * Parses a {@link Pattern}.
 *
 * @author Ben Yu
 */
final class PatternScanner extends Parser<Void> {
  private final String name;
  private final Pattern pattern;

  PatternScanner(String name, Pattern pattern) {
    this.name = name;
    this.pattern = pattern;
  }

  @Override
  boolean apply(final ParseContext ctxt) {
    int at = ctxt.at;
    CharSequence src = ctxt.characters();
    int matchLength = pattern.match(src, at, src.length());
    if (matchLength < 0) {
      ctxt.expected(name);
      return false;
    }
    ctxt.next(matchLength);
    ctxt.result = null;
    return true;
  }

  @Override
  public Incremental<Void> incrementally() {
    return new IncrementalPatternScanner(name, pattern);
  }

  @Override
  public String toString() {
    return name;
  }

  private static class IncrementalPatternScanner extends Incremental<Void> {

    private final String name;
    private final Pattern pattern;

    public IncrementalPatternScanner(String name, Pattern pattern) {
      this.name = name;
      this.pattern = pattern;
    }

    @Override
    Incremental<Void> parse(ParseContext ctxt) {
      CharSequence src = ctxt.characters();
      Pattern derived = pattern;
      for (int i = 0; i < src.length(); i++) {
        derived = derived.derive(src.charAt(i));
        if (derived == Patterns.ALWAYS) {
          ctxt.next(i);
          ctxt.result = null;
          return new Done<Void>(null);
        } else if (derived == Patterns.NEVER) {
          return new Failed<Void>();
        }
      }

      return new IncrementalPatternScanner(name, derived);
    }
  }
}