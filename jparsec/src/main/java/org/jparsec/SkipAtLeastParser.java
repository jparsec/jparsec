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

final class SkipAtLeastParser extends Parser<Void> {
  private final Parser<?> parser;
  private final int min;

  SkipAtLeastParser(Parser<?> parser, int min) {
    this.parser = parser;
    this.min = min;
  }

  @Override boolean apply(ParseContext ctxt) {
    if (!ctxt.repeat(parser, min)) return false;
    if (applyMany(ctxt)) {
      ctxt.result = null;
      return true;
    }
    return false;
  }
  
  @Override public String toString() {
    return "skipAtLeast";
  }

  private boolean applyMany(ParseContext ctxt) {
    int physical = ctxt.at;
    int logical = ctxt.step;
    for (;;logical = ctxt.step) {
      if (!parser.apply(ctxt)) {
        ctxt.setAt(logical, physical);
        return true;
      }
      int at2 = ctxt.at;
      if (physical == at2) return true;
      physical = at2;
    }
  }
}