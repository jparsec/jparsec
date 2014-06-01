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

final class OrParser<T> extends Parser<T> {
  private final Parser<? extends T>[] alternatives;

  OrParser(Parser<? extends T>... alternatives) {
    this.alternatives = alternatives;
  }

  @Override boolean apply(ParseContext ctxt) {
    final Object result = ctxt.result;
    final int at = ctxt.at;
    final int step = ctxt.step;
    for(Parser<? extends T> p : alternatives) {
      if (p.run(ctxt)) {
        return true;
      }
      ctxt.set(step, at, result);
    }
    // set the index to the most relevant error so far.
    ctxt.setAt(step, at);
    return false;
  }
  
  @Override public String toString() {
    return "or";
  }
}
