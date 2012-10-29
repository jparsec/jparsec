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

import java.util.regex.Matcher;

import org.codehaus.jparsec.util.Checks;

/**
 * Provides common {@link Pattern} implementations.
 * 
 * @author Ben Yu
 */
public final class Patterns {

  private Patterns() {}
  
  /** A {@link Pattern} that always returns {@link Pattern#MISMATCH}. */
  public static final Pattern NEVER = new Pattern() {
    @Override public int match(CharSequence src, int begin, int end) {
      return Pattern.MISMATCH;
    }
  };

  /** A {@link Pattern} that always matches with match length {@code 0}. */
  public static final Pattern ALWAYS = new Pattern() {
    @Override public int match(CharSequence src, int begin, int end) {
      return 0;
    }
  };
  
  /** A {@link Pattern} that matches any character and only mismatches for an empty string. */
  public static final Pattern ANY_CHAR = hasAtLeast(1);
  
  /**
   * A {@link Pattern} object that matches if the input has no character left. Match
   * length is {@code 0} if succeed.
   */
  public static final Pattern EOF = hasExact(0);
  
  /**
   * A {@link Pattern} object that succeeds with match length {@code 2} if there are at least 2
   * characters in the input and the first character is {@code '\'}. Mismatch otherwise.
   */
  public static final Pattern ESCAPED = new Pattern() {
      @Override public int match(CharSequence src, int begin, int end) {
        if (begin >= end - 1) return Pattern.MISMATCH;
        else if (src.charAt(begin) == '\\') return 2;
        else return Pattern.MISMATCH;
      }
  };
  
  /** A {@link Pattern} object that matches an integer. */
  public static final Pattern INTEGER = many1(CharPredicates.IS_DIGIT);
  
  /**
   * A {@link Pattern} object that matches a decimal number that has at least one digit
   * before the decimal point. The decimal point and the numbers to the right are optional.
   * 
   * <p> {@code 0, 11., 2.3} are all good candidates. While {@code .1, .} are not.
   */
  public static final Pattern STRICT_DECIMAL =
      INTEGER.next(isChar('.').next(many(CharPredicates.IS_DIGIT)).optional());
  
  /** A {@link Pattern} object that matches a decimal point and one or more digits after it. */
  public static final Pattern FRACTION = isChar('.').next(INTEGER);
  
  /**
   * A {@link Pattern} object that matches a decimal number that could start with a decimal
   * point or a digit.
   */
  public static final Pattern DECIMAL = STRICT_DECIMAL.or(FRACTION);
  
  /**
   * A {@link Pattern} object that matches a standard english word, which starts with either
   * an underscore or an alpha character, followed by 0 or more alphanumeric characters.
   */
  public static final Pattern WORD = isChar(CharPredicates.IS_ALPHA_)
      .next(isChar(CharPredicates.IS_ALPHA_NUMERIC_).many());
  
  /**
   * A {@link Pattern} object that matches an octal integer that starts with a {@code 0} and
   * is followed by 0 or more {@code [0 - 7]} characters.
   */
  public static final Pattern OCT_INTEGER =
      isChar('0').next(many(CharPredicates.range('0','7')));
  
  /**
   * A {@link Pattern} object that matches a decimal integer, which starts with a non-zero
   * digit and is followed by 0 or more digits.
   */
  public static final Pattern DEC_INTEGER =
      sequence(range('1', '9'), many(CharPredicates.IS_DIGIT));
  
  /**
   * A {@link Pattern} object that matches a hex integer, which starts with a {@code 0x} or
   * {@code 0X}, and is followed by one or more hex digits.
   */
  public static final Pattern HEX_INTEGER =
      string("0x").or(string("0X")).next(many1(CharPredicates.IS_HEX_DIGIT));
  
  /**
   * A {@link Pattern} object that matches a scientific notation, such as {@code 1e12},
   * {@code 1.2E-1}, etc.
   */
  public static final Pattern SCIENTIFIC_NOTATION = sequence(
      DECIMAL, among("eE"), among("+-").optional(), INTEGER);

  
  /**
   * A {@link Pattern} object that matches any regular expression pattern string in the form
   * of {@code /some pattern here/}. {@code '\'} is used as escape character.
   */  
  public static final Pattern REGEXP_PATTERN = getRegularExpressionPattern();

  
  /**
   * A {@link Pattern} object that matches regular expression modifiers, which is a list of
   * alpha characters.
   */  
  public static final Pattern REGEXP_MODIFIERS = getModifiersPattern();
  
  /**
   * Returns a {@link Pattern} object that matches if the input has at least {@code n}
   * characters left. Match length is {@code n} if succeed.
   */
  public static Pattern hasAtLeast(final int n) {
    return new Pattern() {
      @Override public int match(CharSequence src, int begin, int end) {
        if (begin + n > end) return Pattern.MISMATCH;
        else return n;
      }
    };
  }
  
  /**
   * Returns a {@link Pattern} object that matches if the input has exactly {@code n}
   * characters left. Match length is {@code n} if succeed.
   */
  public static Pattern hasExact(final int n) {
    return new Pattern() {
      @Override public int match(CharSequence src, int begin, int end) {
        if (begin + n != end) return Pattern.MISMATCH;
        else return n;
      }
    };
  }
  
  /**
   * Returns a {@link Pattern} object that matches if the current character in the input is equal to
   * character {@code c}, in which case {@code 1} is returned as match length. Mismatches otherwise.
   */
  public static Pattern isChar(char c) {
    return isChar(CharPredicates.isChar(c));
  }
  
  /**
   * Returns a {@link Pattern} object that matches if the current character in the input is between
   * character {@code c1} and {@code c2}, in which case {@code 1} is returned as match length.
   */
  public static Pattern range(char c1, char c2) {
    return isChar(CharPredicates.range(c1, c2));
  }
  
  /**
   * Returns a {@link Pattern} object that matches if the current character in the input is equal to
   * any character in {@code chars}, in which case {@code 1} is returned as match length.
   */
  public static Pattern among(String chars) {
    return isChar(CharPredicates.among(chars));
  }
  
  /**
   * Returns a {@link Pattern} object that matches if the current character in the input satisfies
   * {@code predicate}, in which case {@code 1} is returned as match length.
   */
  public static Pattern isChar(final CharPredicate predicate) {
    return new Pattern() {
      @Override public int match(CharSequence src, int begin, int end) {
        if (begin >= end) return Pattern.MISMATCH;
        else if (predicate.isChar(src.charAt(begin))) return 1;
        else return Pattern.MISMATCH; 
      }
    };
  }

  /**
   * Returns a {@link Pattern} object that matches a line comment started by {@code begin}
   * and ended by {@code EOF} or {@code LF} (the line feed character).
   */
  public static Pattern lineComment(String begin) {
    return string(begin).next(many(CharPredicates.notChar('\n')));
  }
  
  /** Returns a {@link Pattern} object that matches {@code string} literally. */
  public static Pattern string(final String string) {
    return new Pattern() {
      @Override public int match(CharSequence src, int begin, int end) {
        return matchString(string, src, begin, end);
      }
    };
  }
  
  /** Returns a {@link Pattern} object that matches {@code string} case insensitively. */
  public static Pattern stringCaseInsensitive(final String string) {
    return new Pattern() {
      @Override public int match(CharSequence src, int begin, int end) {
        return matchStringCaseInsensitive(string, src, begin, end);
      }
    };
  }
  
  /**
   * Returns a {@link Pattern} object that matches if the input has at least 1 character and doesn't
   * match {@code string}. {@code 1} is returned as match length if succeeds.
   */
  public static Pattern notString(final String string) {
    return new Pattern() {
      @Override public int match(CharSequence src, int begin, int end) {
        if (begin >= end) return MISMATCH;
        if (matchString(string, src, begin, end) == Pattern.MISMATCH)
          return 1;
        else return MISMATCH;
      }
    };
  }
  
  /**
   * Returns a {@link Pattern} object that matches if the input has at least 1 character and doesn't
   * match {@code string} case insensitively. {@code 1} is returned as match length if succeeds.
   */
  public static Pattern notStringCaseInsensitive(final String string) {
    return new Pattern() {
      @Override public int match(CharSequence src, int begin, int end) {
        if (begin >= end) return MISMATCH;
        if (matchStringCaseInsensitive(string, src, begin, end) == Pattern.MISMATCH)
          return 1;
        else return MISMATCH;
      }
    };
  }

  private static boolean compareIgnoreCase(char a, char b) {
    return Character.toLowerCase(a) == Character.toLowerCase(b);
  }

  private static int matchString(String str, CharSequence src, int begin, int end) {
    final int slen = str.length();
    if (end - begin < slen) return Pattern.MISMATCH;
    for (int i = 0; i < slen; i++) {
      final char exp = str.charAt(i);
      final char enc = src.charAt(begin + i);
      if (exp != enc) {
        return Pattern.MISMATCH;
      }
    }
    return slen;
  }

  private static int matchStringCaseInsensitive(String str, CharSequence src, int begin, int end) {
    final int slen = str.length();
    if (end - begin < slen) return Pattern.MISMATCH;
    for (int i = 0; i < slen; i++) {
      final char exp = str.charAt(i);
      final char enc = src.charAt(begin + i);
      if (!compareIgnoreCase(exp, enc)) {
        return Pattern.MISMATCH;
      }
    }
    return slen;
  }

  static Pattern not(final Pattern pp) {
    return new Pattern() {
      @Override public int match(CharSequence src, int begin, int end) {
        if (pp.match(src,begin,end) != Pattern.MISMATCH) return Pattern.MISMATCH;
        else return 0;
      }
    };
  }

  static Pattern peek(final Pattern pp) {
    return new Pattern() {
      @Override public int match(CharSequence src, int begin, int end) {
        if (pp.match(src,begin,end) == Pattern.MISMATCH) return Pattern.MISMATCH;
        else return 0;
      }
    };
  }

  /**
   * Returns a {@link Pattern} that matches if all of {@code patterns} matches,
   * in which case, the maximum match length is returned. Mismatch if any one mismatches.
   */
  public static Pattern and(final Pattern... patterns) {
    return new Pattern() {
      @Override public int match(CharSequence src, int begin, int end) {
        int ret = 0;
        for (Pattern pattern : patterns) {
          int l = pattern.match(src, begin, end);
          if (l == MISMATCH) return MISMATCH;
          if (l > ret) ret = l;
        }
        return ret;
      }
    };
  }

  /**
   * Returns a {@link Pattern} that matches if any of {@code patterns} matches, in which case, the
   * first match length is returned. Mismatch if any one mismatches.
   */
  public static Pattern or(final Pattern... patterns) {
    return new Pattern() {
      @Override public int match(CharSequence src, int begin, int end) {
        for (Pattern pattern : patterns) {
          int l = pattern.match(src, begin, end);
          if (l != MISMATCH) return l;
        }
        return MISMATCH;
      }
    };
  }

  /**
   * Returns a {@link Pattern} object that matches the input against {@code patterns} sequentially.
   * Te total match length is returned if all succeed.
   */
  public static Pattern sequence(final Pattern... patterns) {
    return new Pattern() {
      @Override public int match(final CharSequence src, final int begin, final int end) {
        int current = begin;
        for (Pattern pattern : patterns) {
          int l = pattern.match(src, current, end);
          if (l == Pattern.MISMATCH) return l;
          current += l;
        }
        return current - begin;
      }
    };
  }

  /**
   * Returns a {@link Pattern} object that matches if the input has at least {@code n} characters
   * and the first {@code n} characters all satisfy {@code predicate}.
   */
  public static Pattern repeat(final int n, final CharPredicate predicate) {
    Checks.checkNonNegative(n, "n < 0");
    return new Pattern() {
      @Override public int match(CharSequence src, int begin, int end) {
        return matchRepeat(n, predicate, src, end, begin, 0);
      }
    };
  }
  
  /**
   * Returns a {@link Pattern} object that matches if the input has {@code n} occurrences of
   * {@code pattern}.
   */
  static Pattern repeat(final int n, final Pattern pattern) {
    Checks.checkNonNegative(n, "n < 0");
    return new Pattern() {
      @Override public int match(CharSequence src, int begin, int end) {
        return matchRepeat(n, pattern, src, end, begin, 0);
      }
    };
  }
  
  /**
   * Returns a {@link Pattern} object that matches if the input starts with {@code min} or more
   * characters and all satisfy {@code predicate}.
   */
  public static Pattern many(final int min, final CharPredicate predicate) {
    Checks.checkMin(min);
    return new Pattern() {
      @Override public int match(CharSequence src, int begin, int end) {
        int minlen = matchRepeat(min, predicate, src, end, begin, 0);
        if (minlen == MISMATCH) return MISMATCH;
        return matchMany(predicate, src, end, begin + minlen, minlen);
      }
    };
  }
  
  /**
   * Returns a {@link Pattern} that matches 0 or more characters satisfying {@code predicate}.
   */
  public static Pattern many(final CharPredicate predicate) {
    return new Pattern() {
      @Override public int match(CharSequence src, int begin, int end) {
        return matchMany(predicate, src, end, begin, 0);
      }
    };
  }
  
  static Pattern many(final int min, final Pattern pattern) {
    Checks.checkMin(min);
    return new Pattern() {
      @Override public int match(CharSequence src, int begin, int end) {
        int minlen = matchRepeat(min, pattern, src, end, begin, 0);
        if (MISMATCH == minlen) return MISMATCH;
        return matchMany(pattern, src, end, begin + minlen, minlen);
      }
    };
  }

  static Pattern many(final Pattern pattern) {
    return new Pattern() {
      @Override public int match(CharSequence src, int begin, int end) {
        return matchMany(pattern, src, end, begin, 0);
      }
    };
  }
  
  /**
   * Returns a {@link Pattern} that matches at least {@code min} and up to {@code max} number of
   * characters satisfying {@code predicate},
   */
  public static Pattern some(final int min, final int max, final CharPredicate predicate) {
    Checks.checkMinMax(min, max);
    return new Pattern() {
      @Override public int match(CharSequence src, int begin, int end) {
        int minlen = matchRepeat(min, predicate, src, end, begin, 0);
        if (minlen == MISMATCH) return MISMATCH;
        return matchSome(max - min, predicate, src, end, begin + minlen, minlen);
      }
    };
  }
  
  /**
   * Returns a {@link Pattern} that matches up to {@code max} number of characters
   * satisfying {@code predicate}.
   */
  public static Pattern some(final int max, final CharPredicate predicate) {
    Checks.checkMax(max);
    return new Pattern() {
      @Override public int match(CharSequence src, int begin, int end) {
        return matchSome(max, predicate, src, end, begin, 0);
      }
    };
  }

  static Pattern some(final int min, final int max, final Pattern pp) {
    Checks.checkMinMax(min, max);
    return new Pattern() {
      @Override public int match(CharSequence src, int begin, int end) {
        int minlen = matchRepeat(min, pp, src, end, begin, 0);
        if (MISMATCH == minlen) return MISMATCH;
        return matchSome(max - min, pp, src, end, begin + minlen, minlen);
      }
    };
  }

  static Pattern some(final int max, final Pattern pp) {
    Checks.checkMax(max);
    return new Pattern() {
      @Override public int match(CharSequence src, int begin, int end) {
        return matchSome(max, pp, src, end, begin, 0);
      }
    };
  }
  
  /**
   * Returns a {@link Pattern} that tries both {@code p1} and {@code p2}, and picks the one with the
   * longer match length. If both have the same length, {@code p1} is favored.
   */
  public static Pattern longer(Pattern p1, Pattern p2) {
    return longest(p1, p2);
  }
  
  /**
   * Returns a {@link Pattern} that tries all of {@code patterns}, and picks the one with the
   * longest match length. If two patterns have the same length, the first one is favored.
   */
  public static Pattern longest(final Pattern... patterns) {
    return new Pattern() {
      @Override public int match(CharSequence src, int begin, int end) {
        int r = MISMATCH;
        for (Pattern pattern : patterns) {
          int l = pattern.match(src, begin, end);
          if (l > r) r = l;
        }
        return r;
      }
    };
  }
  
  /**
   * Returns a {@link Pattern} that tries both {@code p1} and {@code p2}, and picks the one with the
   * shorter match length. If both have the same length, {@code p1} is favored.
   */
  public static Pattern shorter(Pattern p1, Pattern p2) {
    return shortest(p1, p2);
  }
  
  /**
   * Returns a {@link Pattern} that tries all of {@code patterns}, and picks the one with the
   * shortest match length. If two patterns have the same length, the first one is favored.
   */
  public static Pattern shortest(final Pattern... patterns) {
    return new Pattern() {
      @Override public int match(CharSequence src, int begin, int end) {
        int r = MISMATCH;
        for (int i = 0; i < patterns.length; i++) {
          final int l = patterns[i].match(src,begin,end);
          if (l != MISMATCH) {
            if (r == MISMATCH || l < r)
              r = l;
          }
        }
        return r;
      }
    };
  }

  static Pattern ifelse(final Pattern cond, final Pattern consequence, final Pattern alternative) {
    return new Pattern() {
      @Override public int match(CharSequence src, int begin, int end) {
        final int conditionResult = cond.match(src, begin, end);
        if (conditionResult == MISMATCH) {
          return alternative.match(src, begin, end);
        }
        else {
          final int consequenceResult = consequence.match(src, begin + conditionResult, end);
          if (consequenceResult == MISMATCH) return MISMATCH;
          else return conditionResult + consequenceResult;
        }
      }
    };
  }
  
  /** Returns a {@link Pattern} that matches 1 or more characters satisfying {@code predicate}. */
  public static Pattern many1(CharPredicate predicate) {
    return many(1, predicate);
  }
  
  /** Adapts a regular expression pattern to a {@link Pattern}. */
  public static Pattern regex(final java.util.regex.Pattern p) {
    return new Pattern() {
      @Override public int match(CharSequence src, int begin, int end) {
        if (begin > end) return Pattern.MISMATCH;
        Matcher matcher = p.matcher(src.subSequence(begin, end));
        if (matcher.lookingAt()) return matcher.end();
        return Pattern.MISMATCH;
      }
    };
  }
  
  /** Adapts a regular expression pattern string to a {@link Pattern}. */
  public static Pattern regex(String s) {
    return regex(java.util.regex.Pattern.compile(s));
  }

  static Pattern optional(final Pattern pp) {
    return new Pattern() {
      @Override public int match(CharSequence src, int begin, int end) {
        final int l= pp.match(src, begin, end);
        return (l == Pattern.MISMATCH)?0:l;
      }
    };
  }
  
  private static int matchRepeat(
      int n, CharPredicate predicate, CharSequence src, int len, int from, int acc) {
    int tail = from + n;
    if (tail > len) return Pattern.MISMATCH;
    for (int i = from; i < tail; i++) {
      if (!predicate.isChar(src.charAt(i))) return Pattern.MISMATCH;
    }
    return n + acc;
  }
  
  private static int matchRepeat(
      int n, Pattern pattern, CharSequence src, int len, int from, int acc) {
    int end = from;
    for (int i = 0; i < n; i++) {
      int l = pattern.match(src,end,len);
      if (l == Pattern.MISMATCH) return Pattern.MISMATCH;
      end += l;
    }
    return end - from + acc;
  }
  
  private static int matchSome(
      int max, CharPredicate predicate, CharSequence src, int len, int from, int acc) {
    int k = Math.min(max + from, len);
    for (int i = from; i < k; i++) {
      if (!predicate.isChar(src.charAt(i))) return i - from + acc;
    }
    return k - from + acc;
  }
  
  private static int matchSome(
      int max, Pattern pattern, CharSequence src, int len, int from, int acc) {
    int begin = from;
    for (int i = 0; i < max; i++) {
      int l = pattern.match(src, begin, len);
      if (Pattern.MISMATCH == l) return begin - from + acc;
      begin += l;
    }
    return begin - from + acc;
  }
  
  private static int matchMany(
      CharPredicate predicate, CharSequence src, int len, int from, int acc) {
    for (int i = from; i < len; i++) {
      if (!predicate.isChar(src.charAt(i))) return i - from + acc;
    }
    return len - from + acc;
  }
  
  private static int matchMany(Pattern pattern, CharSequence src, int len, int from, int acc) {
    for (int i = from; ;) {
      int l = pattern.match(src,i,len);
      if (Pattern.MISMATCH == l) return i - from + acc;
      //we simply stop the loop when infinity is found. this may make the parser more user-friendly.
      if (l == 0) return i - from + acc;
      i += l;
    }
  }
  
  private static final Pattern getRegularExpressionPattern() {
    Pattern quote = isChar('/');
    Pattern escape = isChar('\\').next(hasAtLeast(1));
    Pattern content = or(escape,  isChar(CharPredicates.notAmong("/\r\n\\")));
    return quote.next(content.many()).next(quote);
  }
  
  private static final Pattern getModifiersPattern() {
    return isChar(CharPredicates.IS_ALPHA).many();
  }
}
