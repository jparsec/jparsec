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

import org.codehaus.jparsec.functors.Map4;

final class Sequence4Parser<A, B, C, D, T> extends Parser<T> {
  private final Parser<A> p1;
  private final Parser<B> p2;
  private final Parser<C> p3;
  private final Parser<D> p4;
  private final Map4<? super A, ? super B, ? super C, ? super D, ? extends T> m4;

  Sequence4Parser(Parser<A> p1, Parser<B> p2, Parser<C> p3, Parser<D> p4,
      Map4<? super A, ? super B, ? super C, ? super D, ? extends T> m4) {
    this.p1 = p1;
    this.p2 = p2;
    this.p3 = p3;
    this.p4 = p4;
    this.m4 = m4;
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
    boolean r4 = p4.run(ctxt);
    if (!r4) return false;
    D o4 = p4.getReturn(ctxt);
    ctxt.result = m4.map(o1, o2, o3, o4);
    return true;
  }
  
  @Override public String toString() {
    return m4.toString();
  }
}