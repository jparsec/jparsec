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

final class PeekParser<T> extends Parser<T> {
  private final Parser<T> parser;

  PeekParser(Parser<T> parser) {
    this.parser = parser;
  }

  @Override boolean apply(ParseContext ctxt) {
    int step = ctxt.step;
    int at = ctxt.at;
    boolean ok = parser.run(ctxt);
    if (ok) ctxt.setAt(step, at);
    return ok;
  }
  
  @Override public String toString() {
    return "peek";
  }
}