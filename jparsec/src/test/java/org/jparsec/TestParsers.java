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
 * {@link Parser}s for testing purpose.
 * 
 * @author Ben Yu
 */
final class TestParsers {
  
  static Parser<Character> isChar(char c) {
    return Scanners.isChar(c).retn(c);
  }
  
  static Parser<Character> areChars(String chars) {
    Parser<Character> parser = Parsers.constant(null);
    for (int i = 0; i < chars.length(); i++) {
      parser = parser.next(isChar(chars.charAt(i)));
    }
    return parser;
  }
}
