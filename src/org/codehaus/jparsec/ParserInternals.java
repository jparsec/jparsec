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

import java.util.Collection;

/**
 * Common internal utilities.
 *
 * @author benyu
 */
final class ParserInternals {

  static void runForBestFit(
      IntOrder order, Parser<?>[] parsers, int from,
      ParseContext state,
      Object originalResult, int originalStep, int originalAt) {
    int bestAt = state.at;
    int bestStep = state.step;
    Object bestResult = state.result;
    for (int i = from; i < parsers.length; i++) {
      state.set(originalStep, originalAt, originalResult);
      Parser<?> parser = parsers[i];
      boolean ok = parser.run(state);
      if (!ok) continue;
      int at2 = state.at;
      if (order.compare(at2, bestAt)) {
        bestAt = at2;
        bestStep = state.step;
        bestResult = state.result;
      }
    }
    state.set(bestStep, bestAt, bestResult);
  }

  static boolean repeat(Parser<?> parser, int n, ParseContext ctxt) {
    for (int i = 0; i < n; i++) {
      if (!parser.run(ctxt)) return false;
    }
    return true;
  }

  static boolean many(final Parser<?> parser, final ParseContext ctxt) {
    for (int at = ctxt.at, step = ctxt.step;;step = ctxt.step) {
      if (!greedyRun(parser, ctxt)) return stillThere(ctxt, at, step);
      int at2 = ctxt.at;
      if (at == at2) return true;
      at = at2;
    }
  }

  static boolean repeatAtMost(Parser<?> parser, int max, ParseContext ctxt) {
    for (int i = 0; i < max; i++) {
      int at = ctxt.at;
      int step = ctxt.step;
      if (!greedyRun(parser, ctxt)) return stillThere(ctxt, at, step);
    }
    return true;
  }

  static <T> boolean repeat(
      Parser<? extends T> parser, int n, Collection<T> collection, ParseContext ctxt) {
    for (int i = 0; i < n; i++) {
      if (!parser.run(ctxt)) return false;
      collection.add(parser.getReturn(ctxt));
    }
    return true;
  }

  static <T> boolean repeatAtMost(
      Parser<? extends T> parser, int max, Collection<T> collection, ParseContext ctxt) {
    for (int i = 0; i < max; i++) {
      int at = ctxt.at;
      int step = ctxt.step;
      if (!greedyRun(parser, ctxt)) return stillThere(ctxt, at, step);
      collection.add(parser.getReturn(ctxt));
    }
    return true;
  }

  static <T> boolean many(
      Parser<? extends T> parser, Collection<T> collection, ParseContext ctxt) {
    for (int at = ctxt.at, step = ctxt.step;;step = ctxt.step) {
      if (!greedyRun(parser, ctxt)) return stillThere(ctxt, at, step);
      int at2 = ctxt.at;
      if (at == at2) return true;
      at = at2;
      collection.add(parser.getReturn(ctxt));
    }
  }

  static boolean stillThere(ParseContext ctxt, int wasAt, int originalStep) {
    if (ctxt.step == originalStep) {
      // logical step didn't change, so logically we are still there, undo any physical offset
      ctxt.setAt(originalStep, wasAt);
      return true;
    }
    return false;
  }

  static boolean runNestedParser(
      ParseContext ctxt, ParseContext freshInitState, Parser<?> parser) {
    if (parser.run(freshInitState))  {
      ctxt.set(freshInitState.step, ctxt.at, freshInitState.result);
      return true;
    }
    // index on token level is the "at" on character level
    ctxt.set(ctxt.step, freshInitState.getIndex(), null);
    
    // always copy error because there could be false alarms in the character level.
    // For example, a "or" parser nested in a "many" failed in one of its branches.
    copyError(ctxt, freshInitState);
    return false;
  }

  private static void copyError(ParseContext ctxt, ParseContext nestedState) {
    int errorIndex = nestedState.errorIndex();
    ctxt.setErrorState(
        errorIndex, errorIndex, nestedState.errorType(), nestedState.errors());
    if (!nestedState.isEof()) {
      ctxt.setEncountered(nestedState.getEncountered());
    }
  }

  /**
   * Runs {@code parser} in greedy mode. Currently it does nothing special.
   * May want to suppress irrelevant errors (such the 'x expected' in x*).
   */
  static boolean greedyRun(Parser<?> parser, ParseContext ctxt) {
    return parser.run(ctxt);
  }

  /** Runs {@code parser} with error recording suppressed. */
  static boolean runWithoutRecordingError(Parser<?> parser, ParseContext ctxt) {
    boolean oldValue = ctxt.suppressError(true);
    boolean ok = parser.run(ctxt);
    ctxt.suppressError(oldValue);
    return ok;
  }
}
