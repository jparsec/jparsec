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

import java.util.ArrayList;
import java.util.List;

import org.codehaus.jparsec.util.Lists;

/**
 * Sequentially runs a list of {@link Parser} objects and collects the return values in a
 * {@link List}.
 * 
 * @author Ben Yu
 */
final class ListParser<T> extends Parser<List<T>> {
  private final Parser<? extends T>[] parsers;

  ListParser(Parser<? extends T>[] parsers) {
    this.parsers = parsers;
  }

  @Override boolean apply(ParseContext ctxt) {
    ArrayList<T> list = Lists.arrayList(parsers.length);
    for (Parser<? extends T> parser : parsers) {
      if (!parser.run(ctxt)) return false;
      list.add(parser.getReturn(ctxt));
    }
    ctxt.result = list;
    return true;
  }
  
  @Override public String toString() {
    return "list";
  }
}
