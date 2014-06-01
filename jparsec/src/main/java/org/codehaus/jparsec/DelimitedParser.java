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
 * Parses a list of pattern started with a delimiter, separated and optionally
 * ended by the delimiter.
 * 
 * @author Ben Yu
 */
class DelimitedParser<T, R> extends Parser<R> {
  final Parser<T> parser;
  private final Parser<?> delim;

  DelimitedParser(Parser<T> p, Parser<?> delim) {
    this.parser = p;
    this.delim = delim;
  }

  @Override boolean apply(final ParseContext ctxt) {
    final R result = begin();
    for (;;) {
      final int step0 = ctxt.step;
      final int at0 = ctxt.at;
      boolean r = ParserInternals.greedyRun(delim, ctxt);
      if (!r) {
        if (!ParserInternals.stillThere(ctxt, at0, step0)) return false;
        ctxt.result = result;
        return true;
      }
      final int step1 = ctxt.step;
      final int at1 = ctxt.at;
      r = ParserInternals.greedyRun(parser, ctxt);
      if (!r) {
        if (!ParserInternals.stillThere(ctxt, at1, step1)) return false;
        ctxt.result = result;
        return true;
      }
      if (at0 == ctxt.at) { // infinite loop
        ctxt.result = result;
        return true;
      }
      element(ctxt, result);
    }
  }

  R begin() {
    return null;
  }

  void element(ParseContext ctxt, R result) {}
  
  @Override public String toString() {
    return "delimited";
  }
}
