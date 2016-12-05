/*****************************************************************************
 * Copyright (C) jparsec.org                                                *
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
package org.jparsec;

final class BestParser<T> extends Parser<T> {
  private final Parser<? extends T>[] parsers;
  private final IntOrder order;

  BestParser(Parser<? extends T>[] parsers, IntOrder order) {
    this.parsers = parsers;
    this.order = order;
  }

  @Override boolean apply(ParseContext ctxt) {
    final Object result = ctxt.result;
    final int step = ctxt.step;
    final int at = ctxt.at;
    final TreeNode latestChild = ctxt.getTrace().getLatestChild();
    for (int i = 0; i < parsers.length; i++) {
      Parser<? extends T> parser = parsers[i];
      if (parser.apply(ctxt)) {
        applyForBestFit(i + 1, ctxt, result, step, at, latestChild);
        return true;
      }
      // in alternate, we do not care partial match.
      ctxt.set(step, at, result);
    }
    return false;
  }
  
  @Override public String toString() {
    return order.toString();
  }

  private void applyForBestFit(
      int from, ParseContext ctxt,
      Object originalResult, int originalStep, int originalAt, TreeNode originalLatestChild) {
    int bestAt = ctxt.at;
    int bestStep = ctxt.step;
    Object bestResult = ctxt.result;
    TreeNode bestChild = ctxt.getTrace().getLatestChild();
    for (int i = from; i < parsers.length; i++) {
      ctxt.set(originalStep, originalAt, originalResult);
      ctxt.getTrace().setLatestChild(originalLatestChild);
      Parser<?> parser = parsers[i];
      boolean ok = parser.apply(ctxt);
      if (!ok) continue;
      int at2 = ctxt.at;
      if (order.compare(at2, bestAt)) {
        bestAt = at2;
        bestStep = ctxt.step;
        bestResult = ctxt.result;
        bestChild = ctxt.getTrace().getLatestChild();
      }
    }
    ctxt.set(bestStep, bestAt, bestResult);
    ctxt.getTrace().setLatestChild(bestChild);
  }
}