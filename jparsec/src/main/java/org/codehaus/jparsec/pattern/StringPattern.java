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

class StringPattern extends Pattern {

  private final String string;

  public StringPattern(String string) {
    this.string = string;
  }

  /**
   * Matches (part of) a character sequence against a pattern string.
   *
   * @param  str   the pattern string.
   * @param  src   the input sequence. Must not be null.
   * @param  begin start of index to scan characters from <code>src</code>.
   * @param  end   end of index to scan characters from <code>src</code>.
   *
   * @return the number of characters matched, or {@link Pattern#MISMATCH} if an unexpected character is encountered.
   */
  static int matchString(String str, CharSequence src, int begin, int end) {
    final int patternLength = str.length();

    int i;
    for (i = 0; (i < patternLength) && ((begin + i) < end); i++) {
      final char exp = str.charAt(i);
      final char enc = src.charAt(begin + i);
      if (exp != enc) {
        return MISMATCH;
      }
    }
    return i;
  }

  @Override
  public int match(CharSequence src, int begin, int end) {
    if ((end - begin) < string.length())
      return Pattern.MISMATCH;

    return matchString(string, src, begin, end);
  }

  @Override
  public Pattern derive(char c) {
    if ((string.length() > 0) && (c == string.charAt(0)))
      return Patterns.string(string.substring(1));
    else
      return Patterns.NEVER;
  }

  @Override
  public String toString() {
    return string;
  }
}
