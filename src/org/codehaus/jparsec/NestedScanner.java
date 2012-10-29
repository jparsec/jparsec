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
 * After a scanner succeeds, feeds the recognized character range to a nested scanner.
 * 
 * @author Ben Yu
 */
final class NestedScanner extends Parser<Void> {
  private final Parser<?> outer;
  private final Parser<Void> inner;
  
  NestedScanner(Parser<?> parser, Parser<Void> scanner) {
    this.outer = parser;
    this.inner = scanner;
  }

  @Override boolean apply(ParseContext ctxt) {
    int from = ctxt.at;
    if (!outer.run(ctxt)) return false;
    ScannerState scannerState = new ScannerState(
        ctxt.module, ctxt.characters(), from, ctxt.at, ctxt.locator, ctxt.result);
    return ParserInternals.runNestedParser(ctxt, scannerState, inner);
  }
  
  @Override public String toString() {
    return "nested scanner";
  }
}