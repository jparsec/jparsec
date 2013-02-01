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
 * Returns the source string of the part that matches the target parser.
 * 
 * @author Ben Yu
 */
final class ReturnSourceParser extends Parser<String> {
  private final Parser<?> parser;
  
  ReturnSourceParser(Parser<?> parser) {
    this.parser = parser;
  }

  @Override boolean apply(ParseContext ctxt) {
    int begin = ctxt.getIndex();
    if (!parser.apply(ctxt)) {
      return false;
    }
    ctxt.result = ctxt.source.subSequence(begin, ctxt.getIndex()).toString();
    return true;
  }
  
  @Override public String toString() {
    return "source";
  }
}
