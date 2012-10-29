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

import org.codehaus.jparsec.functors.Map3;

final class Sequence3Parser<A, B, C, T> extends Parser<T> {
  private final Parser<A> p1;
  private final Parser<B> p2;
  private final Parser<C> p3;
  private final Map3<? super A, ? super B, ? super C, ? extends T> m3;

  Sequence3Parser(Parser<A> p1, Parser<B> p2, Parser<C> p3,
      Map3<? super A, ? super B, ? super C, ? extends T> m3) {
    this.p1 = p1;
    this.p2 = p2;
    this.p3 = p3;
    this.m3 = m3;
  }

  @Override boolean apply(ParseContext ctxt) {
    boolean r1 = p1.run(ctxt);
    if (!r1) return false;
    A o1 = p1.getReturn(ctxt);
    boolean r2 = p2.run(ctxt);
    if (!r2) return false;
    B o2 = p2.getReturn(ctxt);
    boolean r3 = p3.run(ctxt);
    if (!r3) return false;
    C o3 = p3.getReturn(ctxt);
    ctxt.result = m3.map(o1, o2, o3);
    return true;
  }
  
  @Override public String toString() {
    return m3.toString();
  }
}