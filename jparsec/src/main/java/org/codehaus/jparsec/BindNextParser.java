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

import org.codehaus.jparsec.functors.Map;

/**
 * Runs a {@link Parser} and then maps the parser result to another {@code Parser} object, the
 * returned {@code Parser} object is then executed as the next step.
 * 
 * @author Ben Yu
 */
final class BindNextParser<From, To> extends Parser<To> {
  private final Parser<? extends From> parser;
  private final Map<? super From, ? extends Parser<? extends To>> map;
  
  BindNextParser(
      Parser<? extends From> parser, Map<? super From, ? extends Parser<? extends To>> next) {
    this.map = next;
    this.parser = parser;
  }

  @Override boolean apply(ParseContext ctxt) {
    if (!parser.run(ctxt))
      return false;
    return Parsers.runNext(ctxt, map);
  }
  
  @Override public String toString() {
    return map.toString();
  }
}