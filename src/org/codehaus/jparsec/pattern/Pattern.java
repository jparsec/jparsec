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
package org.codehaus.jparsec.pattern;

/**
 * Encapsulates algorithm to recognize certain string pattern. When fed with a character range,
 * a {@link Pattern} object either fails to match, or matches with the match length returned.
 * There is no error reported on where and what exactly failed.
 * 
 * @author Ben Yu
 */
public abstract class Pattern {
  
  /** Returned by {@link #match(CharSequence, int, int)} method when match fails. */
  public static final int MISMATCH = -1;
  
  /**
   * Matches character range against the pattern. The length of the range is {@code len - from}.
   * 
   * @param src the source string.
   * @param begin the beginning index in the sequence.
   * @param end the end index of the source string (exclusive).
   * NOTE: the range is {@code [begin, end)}.
   * @return the number of characters matched. MISMATCH otherwise.
   */
  public abstract int match(CharSequence src, int begin, int end);
  
  /**
   * Returns a {@link Pattern} object that sequentially matches the character range against
   * {@code this} and then {@code next}. If both succeeds, the entire match length is returned.
   * 
   * @param next the next pattern to match.
   * @return the new Pattern object.
   */
  public final Pattern next(Pattern next) {
    return Patterns.sequence(this, next);
  }
  
  /**
   * Returns a {@link Pattern} object that matches with 0 length even if {@code this} mismatches.
   */
  public final Pattern optional() {
    return Patterns.optional(this);
  }
  
  /**
   * Returns a {@link Pattern} object that matches this pattern for 0 or more times.
   * The total match length is returned.
   */
  public final Pattern many() {
    return Patterns.many(this);
  }
  
  /**
   * Returns {@link Pattern} object that matches this pattern for at least {@code min} times.
   * The total match length is returned.
   * 
   * @param min the minimal number of times to match.
   * @return the new Pattern object.
   */
  public final Pattern many(int min) {
    return Patterns.many(min, this);
  }
  
  /**
   * Returns a {@link Pattern} object that matches this pattern for 1 or more times.
   * The total match length is returned.
   */
  public final Pattern many1() {
    return many(1);
  }
  
  /**
   * Returns {@link Pattern} object that matches this pattern for up to {@code max} times.
   * The total match length is returned.
   * 
   * @param max the maximal number of times to match.
   * @return the new Pattern object.
   */
  public final Pattern some(int max) {
    return Patterns.some(max, this);
  }
  
  /**
   * Returns {@link Pattern} object that matches this pattern for at least {@code min} times
   * and up to {@code max} times. The total match length is returned.
   * 
   * @param min the minimal number of times to match.
   * @param max the maximal number of times to match.
   * @return the new Pattern object.
   */
  public final Pattern some(final int min, final int max) {
    return Patterns.some(min, max, this);
  }
  
  /**
   * Returns a {@link Pattern} object that only matches if this pattern mismatches, 0 is returned
   * otherwise.
   */
  public final Pattern not() {
    return Patterns.not(this);
  }
  
  /**
   * Returns {@link Pattern} object that matches with match length 0 if this Pattern object matches.
   */
  public final Pattern peek() {
    return Patterns.peek(this);
  }
  
  /**
   * Returns {@link Pattern} object that, if this pattern matches,
   * matches the remaining input against {@code consequence} pattern, or otherwise matches against
   * {@code alternative} pattern.
   */
  public final Pattern ifelse(Pattern consequence, Pattern alternative) {
    return Patterns.ifelse(this, consequence, alternative);
  }
  
  /**
   * Returns {@link Pattern} object that matches the input against this pattern for {@code n} times.
   */
  public final Pattern repeat(int n) {
    return Patterns.repeat(n, this);
  }
  
  /** Returns {@link Pattern} object that matches if either {@code this} or {@code p2} matches. */
  public final Pattern or(Pattern p2) {
    return Patterns.or(this, p2);
  }
}
