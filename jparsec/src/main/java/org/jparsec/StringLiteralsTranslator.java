/*****************************************************************************
 * Copyright (C) jparsec.org                                                *
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
package org.jparsec;

/**
 * Translates the recognized string literal to a {@link String}.
 * 
 * @author Ben Yu
 */
final class StringLiteralsTranslator {
  
  private static final char escapedChar(char c) {
    switch (c) {
      case 'r':
        return '\r';
      case 'n':
        return '\n';
      case 't':
        return '\t';
      default:
        return c;
    }
  }
  
  static String tokenizeDoubleQuote(String text) {
    final int end = text.length() - 1;
    final StringBuilder buf = new StringBuilder();
    for (int i = 1; i < end; i++) {
      char c = text.charAt(i);
      if (c != '\\') {
        buf.append(c);
      }
      else {
        char c1 = text.charAt(++i);
        buf.append(escapedChar(c1));
      }
    }
    return buf.toString();
  }

  static String tokenizeSingleQuote(String text) {
    int end = text.length() - 1;
    StringBuilder buf = new StringBuilder();
    for (int i = 1; i < end; i++) {
      char c = text.charAt(i);
      if (c != '\'') {
        buf.append(c);
      }
      else {
        buf.append('\'');
        i++;
      }
    }
    return buf.toString();
  }
}
