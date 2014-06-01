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

/**
 * Sequentially runs an array of {@link Parser} objects and collects the return values in an array.
 * 
 * @author Ben Yu
 */
final class ArrayParser extends Parser<Object[]> {
  private final Parser<?>[] parsers;

  ArrayParser(Parser<?>[] parsers) {
    this.parsers = parsers;
  }

  @Override boolean apply(ParseContext ctxt) {
    Object[] ret = new Object[parsers.length];
    for (int i = 0; i < parsers.length; i++) {
      Parser<?> parser = parsers[i];
      if (!parser.run(ctxt)) return false;
      ret[i] = parser.getReturn(ctxt);
    }
    ctxt.result = ret;
    return true;
  }
  
  @Override public String toString() {
    return "array";
  }
}
