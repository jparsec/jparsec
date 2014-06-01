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

class StringCaseInsensitivePattern extends Pattern {

  private final String string;

  public StringCaseInsensitivePattern(String string) {
    this.string = string;
  }

  static int matchStringCaseInsensitive(String str, CharSequence src, int begin, int end) {
    final int patternLength = str.length();
    if ((end - begin) < patternLength)
      return MISMATCH;
    for (int i = 0; i < patternLength; i++) {
      final char exp = str.charAt(i);
      final char enc = src.charAt(begin + i);
      if (!compareIgnoreCase(exp, enc)) {
        return MISMATCH;
      }
    }
    return patternLength;
  }

  static boolean compareIgnoreCase(char a, char b) {
    return Character.toLowerCase(a) == Character.toLowerCase(b);
  }

  @Override
  public Pattern derive(char c) {
    if ((string.length() > 0) && compareIgnoreCase(c, string.charAt(0)))
      return Patterns.string(string.substring(1));
    else
      return Patterns.NEVER;
  }

  @Override
  public int match(CharSequence src, int begin, int end) {
    return matchStringCaseInsensitive(string, src, begin, end);
  }

  @Override
  public String toString() {
    return string.toUpperCase();
  }
}
