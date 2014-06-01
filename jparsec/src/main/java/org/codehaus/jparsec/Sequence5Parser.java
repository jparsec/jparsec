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

import org.codehaus.jparsec.functors.Map5;

final class Sequence5Parser<A, B, C, D, E, T> extends Parser<T> {
  private final Parser<A> p1;
  private final Parser<B> p2;
  private final Parser<C> p3;
  private final Parser<D> p4;
  private final Parser<E> p5;
  private final Map5<? super A, ? super B, ? super C, ? super D, ? super E, ? extends T> m5;

  Sequence5Parser(Parser<A> p1, Parser<B> p2, Parser<C> p3, Parser<D> p4, Parser<E> p5,
      Map5<? super A, ? super B, ? super C, ? super D, ? super E, ? extends T> m5) {
    this.p1 = p1;
    this.p2 = p2;
    this.p3 = p3;
    this.p4 = p4;
    this.p5 = p5;
    this.m5 = m5;
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
    boolean r5 = p5.run(ctxt);
    if (!r5) return false;
    E o5 = p5.getReturn(ctxt);
    ctxt.result = m5.map(o1, o2, o3, o4, o5);
    return true;
  }
  
  @Override public String toString() {
    return m5.toString();
  }
}