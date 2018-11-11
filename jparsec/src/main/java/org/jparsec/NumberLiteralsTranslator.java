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
 * Transforms the recognized character range to an integer within 64 bits. For bigger integer, use
 * {@link TokenizerMaps#DECIMAL_FRAGMENT} instead.
 * 
 * @author Ben Yu
 */
final class NumberLiteralsTranslator {
  
  private static int toDecDigit(char c) {
    return c - '0';
  }
  
  private static int toOctDigit(char c) {
    return c - '0';
  }
  
  private static int toHexDigit(char c) {
    if (c >= '0' && c <= '9') return c - '0';
    if (c >= 'a' && c <= 'h') return c - 'a' + 10;
    else return c - 'A' + 10;
  }

  static long tokenizeDecimalAsLong(String text) {
    long n = 0;
    int len = text.length();
    for(int i = 0; i < len; i++) {
      n = n * 10 + toDecDigit(text.charAt(i));
    }
    return n;
  }

  static long tokenizeOctalAsLong(String text) {
    long n = 0;
    int len = text.length();
    for(int i = 0; i < len; i++) {
      n = n * 8 + toOctDigit(text.charAt(i));
    }
    return n;
  }

  static long tokenizeHexAsLong(String text) {
    int len = text.length();
    if (len < 3) throw new IllegalStateException("illegal hex number");
    long n = 0;
    for(int i = 2; i < len; i++) {
      n = n * 16 + toHexDigit(text.charAt(i));
    }
    return n;
  }
}
