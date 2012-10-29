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

final class NestedParser<T> extends Parser<T> {
  private final Parser<Token[]> lexer;
  private final Parser<? extends T> parser;

  NestedParser(Parser<Token[]> lexer, Parser<? extends T> p) {
    this.lexer = lexer;
    this.parser = p;
  }

  @Override boolean apply(ParseContext ctxt) {
    if (!lexer.run(ctxt)) return false;
    Token[] tokens = lexer.getReturn(ctxt);
    ParserState parserState = new ParserState(
        ctxt.module, ctxt.source, tokens, 0, ctxt.locator, ctxt.getIndex(), tokens);
    return ParserInternals.runNestedParser(ctxt, parserState, parser);
  }
  
  @Override public String toString() {
    return parser.toString();
  }
}