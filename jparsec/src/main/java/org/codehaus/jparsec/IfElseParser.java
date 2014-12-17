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

final class IfElseParser<T, C> extends Parser<T> {
  private final Parser<C> cond;
  private final Map<? super C, ? extends Parser<? extends T>> consequence;
  private final Parser<? extends T> alternative;

  IfElseParser(Parser<C> cond,
      Map<? super C, ? extends Parser<? extends T>> consequence, Parser<? extends T> alternative) {
    this.cond = cond;
    this.consequence = consequence;
    this.alternative = alternative;
  }

  @Override boolean apply(ParseContext ctxt) {
    final Object ret = ctxt.result;
    final int step = ctxt.step;
    final int at = ctxt.at;
    final TreeNode node = ctxt.trace.getLatestChild();
    if (evaluate(ctxt)) {
      Parser<? extends T> parser = consequence.map(cond.getReturn(ctxt));
      return parser.apply(ctxt);
    }
    ctxt.set(step, at, ret, node);
    return alternative.apply(ctxt);
  }
  
  @Override public String toString() {
    return "ifelse";
  }

  /** Runs {@code parser} with error recording suppressed. */
  private boolean evaluate(ParseContext ctxt) {
    boolean oldValue = ctxt.suppressError(true);
    boolean ok = cond.apply(ctxt);
    ctxt.suppressError(oldValue);
    return ok;
  }
}