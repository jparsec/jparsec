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

import org.codehaus.jparsec.functors.Map2;

final class Sequence2Parser<A, B, T> extends Parser<T> {
  private final Parser<A> p1;
  private final Parser<B> p2;
  private final Map2<? super A, ? super B, ? extends T> m2;

  Sequence2Parser(Parser<A> p1, Parser<B> p2, Map2<? super A, ? super B, ? extends T> m2) {
    this.p1 = p1;
    this.p2 = p2;
    this.m2 = m2;
  }

  @Override boolean apply(ParseContext ctxt) {
    boolean r1 = p1.run(ctxt);
    if (!r1) return false;
    A o1 = p1.getReturn(ctxt);
    boolean r2 = p2.run(ctxt);
    if (!r2) return false;
    B o2 = p2.getReturn(ctxt);
    ctxt.result = m2.map(o1, o2);
    return true;
  }
  
  @Override public String toString() {
    return m2.toString();
  }
}