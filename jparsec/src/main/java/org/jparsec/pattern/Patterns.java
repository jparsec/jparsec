/*****************************************************************************
 * Copyright 2013 (C) jparsec.org                                                *
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
package org.jparsec.pattern;

import org.jparsec.internal.util.Checks;

import java.util.regex.Matcher;

/**
 * Provides common {@link Pattern} implementations.
 *
 * @author Ben Yu
 */
public final class Patterns {

  private Patterns() {}

  /** A {@link Pattern} that always returns {@link Pattern#MISMATCH}. */
  public static final Pattern NEVER = new Pattern() {
    @Override
    public int match(CharSequence src, int begin, int end) {
      return MISMATCH;
    }

    @Override
    public String toString() {
      return "<>";
    }
  };

  /** A {@link Pattern} that always matches with match length {@code 0}. */
  public static final Pattern ALWAYS = new Pattern() {
    @Override
    public int match(CharSequence src, int begin, int end) {
      return 0;
    }
  };

  /** A {@link Pattern} that matches any character and only mismatches for an empty string. */
  public static final Pattern ANY_CHAR = hasAtLeast(1);

  /** A {@link Pattern} object that matches if the input has no character left. Match length is {@code 0} if succeed. */
  public static final Pattern EOF = hasExact(0);

  /**
   * A {@link Pattern} object that succeeds with match length {@code 2} if there are at least 2 characters in the input
   * and the first character is {@code '\'}. Mismatch otherwise.
   */
  public static final Pattern ESCAPED = new Pattern() {
    @Override public int match(CharSequence src, int begin, int end) {
      if (begin >= (end - 1))
        return MISMATCH;
      else if (src.charAt(begin) == '\\')
        return 2;
      else
        return MISMATCH;
    }
  };

  /** A {@link Pattern} object that matches an integer. */
  public static final Pattern INTEGER = many1(CharPredicates.IS_DIGIT);

  /**
   * A {@link Pattern} object that matches a decimal number that has at least one digit before the decimal point. The
   * decimal point and the numbers to the right are optional.
   *
   * <p> {@code 0, 11., 2.3} are all good candidates. While {@code .1, .} are not.
   */
  public static final Pattern STRICT_DECIMAL = INTEGER.next(isChar('.').next(many(CharPredicates.IS_DIGIT)).optional());

  /** A {@link Pattern} object that matches a decimal point and one or more digits after it. */
  public static final Pattern FRACTION = isChar('.').next(INTEGER);

  /** A {@link Pattern} object that matches a decimal number that could start with a decimal point or a digit. */
  public static final Pattern DECIMAL = STRICT_DECIMAL.or(FRACTION);

  /**
   * A {@link Pattern} object that matches a standard english word, which starts with either an underscore or an alpha
   * character, followed by 0 or more alphanumeric characters.
   */
  public static final Pattern WORD = isChar(CharPredicates.IS_ALPHA_).next(isChar(CharPredicates.IS_ALPHA_NUMERIC_).many());

  /**
   * A {@link Pattern} object that matches an octal integer that starts with a {@code 0} and is followed by 0 or more
   * {@code [0 - 7]} characters.
   */
  public static final Pattern OCT_INTEGER = isChar('0').next(many(CharPredicates.range('0', '7')));

  /**
   * A {@link Pattern} object that matches a decimal integer, which starts with a non-zero digit and is followed by 0 or
   * more digits.
   */
  public static final Pattern DEC_INTEGER = sequence(range('1', '9'), many(CharPredicates.IS_DIGIT));

  /**
   * A {@link Pattern} object that matches a hex integer, which starts with a {@code 0x} or {@code 0X}, and is followed
   * by one or more hex digits.
   */
  public static final Pattern HEX_INTEGER = string("0x").or(string("0X")).next(many1(CharPredicates.IS_HEX_DIGIT));

  /** A {@link Pattern} object that matches a scientific notation, such as {@code 1e12}, {@code 1.2E-1}, etc. */
  public static final Pattern SCIENTIFIC_NOTATION = sequence(DECIMAL, among("eE"), among("+-").optional(), INTEGER);

  /**
   * A {@link Pattern} object that matches any regular expression pattern string in the form of {@code /some pattern
   * here/}. {@code '\'} is used as escape character.
   */
  public static final Pattern REGEXP_PATTERN = getRegularExpressionPattern();

  /** A {@link Pattern} object that matches regular expression modifiers, which is a list of alpha characters. */
  public static final Pattern REGEXP_MODIFIERS = getModifiersPattern();

  /**
   * Returns a {@link Pattern} object that matches if the input has at least {@code n} characters left. Match length is
   * {@code n} if succeed.
   */
  public static Pattern hasAtLeast(final int n) {
    return new Pattern() {
      @Override public int match(CharSequence src, int begin, int end) {
        if ((begin + n) > end) return MISMATCH;
        else return n;
      }
      @Override public String toString() {
        return ".{" + n + ",}";
      }
    };
  }

  /**
   * Returns a {@link Pattern} object that matches if the input has exactly {@code n} characters left. Match length is
   * {@code n} if succeed.
   */
  public static Pattern hasExact(final int n) {
    Checks.checkNonNegative(n, "n < 0");
    return new Pattern() {
      @Override public int match(CharSequence src, int begin, int end) {
        if ((begin + n) != end) return MISMATCH;
        else return n;
      }
      @Override public String toString() {
        return ".{" + n + "}";
      }
    };
  }

  /**
   * Returns a {@link Pattern} object that matches if the current character in the input is equal to character {@code
   * c}, in which case {@code 1} is returned as match length. Mismatches otherwise.
   */
  public static Pattern isChar(char c) {
    return isChar(CharPredicates.isChar(c));
  }

  /**
   * Returns a {@link Pattern} object that matches if the current character in the input is between character {@code c1}
   * and {@code c2}, in which case {@code 1} is returned as match length.
   */
  public static Pattern range(char c1, char c2) {
    return isChar(CharPredicates.range(c1, c2));
  }

  /**
   * Returns a {@link Pattern} object that matches if the current character in the input is equal to any character in
   * {@code chars}, in which case {@code 1} is returned as match length.
   */
  public static Pattern among(String chars) {
    return isChar(CharPredicates.among(chars));
  }

  /**
   * Returns a {@link Pattern} object that matches if the current character in the input satisfies {@code predicate}, in
   * which case {@code 1} is returned as match length.
   */
  public static Pattern isChar(final CharPredicate predicate) {
    return new Pattern() {
      @Override public int match(CharSequence src, int begin, int end) {
        if (begin >= end)
          return MISMATCH;
        else if (predicate.isChar(src.charAt(begin)))
          return 1;
        else
          return MISMATCH;
      }

      @Override public String toString() {
        return predicate.toString();
      }
    };
  }

  /**
   * Returns a {@link Pattern} object that matches a line comment started by {@code begin} and ended by {@code EOF} or
   * {@code LF} (the line feed character).
   */
  public static Pattern lineComment(String begin) {
    return string(begin).next(many(CharPredicates.notChar('\n')));
  }

  /** Returns a {@link Pattern} object that matches {@code string} literally. */
  public static Pattern string(final String string) {
    return new Pattern() {
      @Override public int match(CharSequence src, int begin, int end) {
        if ((end - begin) < string.length()) return MISMATCH;
        return matchString(string, src, begin, end);
      }
      @Override public String toString() {
        return string;
      }
    };
  }

  /** Returns a {@link Pattern} object that matches {@code string} case insensitively. */
  public static Pattern stringCaseInsensitive(final String string) {
    return new Pattern() {
      @Override public int match(CharSequence src, int begin, int end) {
        return matchStringCaseInsensitive(string, src, begin, end);
      }
      @Override public String toString() {
        return string.toUpperCase();
      }
    };
  }

  /**
   * Returns a {@link Pattern} object that matches if the input has at least 1 character and doesn't match {@code
   * string}. {@code 1} is returned as match length if succeeds.
   */
  public static Pattern notString(final String string) {
    return new Pattern() {
      @Override public int match(CharSequence src, int begin, int end) {
        if (begin >= end) return MISMATCH;
        int matchedLength = matchString(string, src, begin, end);
        if ((matchedLength == MISMATCH) || (matchedLength < string.length()))
          return 1;
        else return MISMATCH;
      }
      @Override public String toString() {
        return "!(" + string + ")";
      }
    };
  }

  /**
   * Returns a {@link Pattern} object that matches if the input has at least 1 character and doesn't match {@code
   * string} case insensitively. {@code 1} is returned as match length if succeeds.
   */
  public static Pattern notStringCaseInsensitive(final String string) {
    return new Pattern() {
      @Override public int match(CharSequence src, int begin, int end) {
        if (begin >= end) return MISMATCH;
        if (matchStringCaseInsensitive(string, src, begin, end) == MISMATCH)
          return 1;
        else return MISMATCH;
      }
      @Override public String toString(){
        return "!(" + string.toUpperCase() + ")";
      }
    };
  }

  /**
   * 
   * @param pattern  
   * @return a {@link Pattern} that matches iff the input does not match nested {@code pattern}. 
   */
  public static Pattern not(Pattern pattern) {
    return pattern.not();
  }

  /**
   * Returns a {@link Pattern} that matches if all of {@code patterns} matches, in which case, the maximum match length
   * is returned. Mismatch if any one mismatches.
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

      @Override public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('(');
        for (Pattern pattern : patterns) {
          sb.append(pattern).append(" & ");
        }
        if (sb.length() > 1) {
          sb.delete(sb.length() - 3, sb.length());
        }
        return sb.append(')').toString();
      }
    };
  }

  /**
   * Returns a {@link Pattern} that matches if any of {@code patterns} matches, in which case, the first match length is
   * returned. Mismatch if any one mismatches.
   */
  public static Pattern or(Pattern... patterns) {
    return new OrPattern(patterns);
  }

  static Pattern orWithoutEmpty(Pattern left, Pattern right) {
    if (right == Patterns.NEVER)
      return left;
    if (left == Patterns.NEVER)
      return right;
    return left.or(right);
  }

  static Pattern nextWithEmpty(Pattern left, Pattern right) {
    if (right == Patterns.NEVER)
      return NEVER;
    if (left == Patterns.NEVER)
      return NEVER;
    return left.next(right);
  }

  /**
   * Returns a {@link Pattern} object that matches the input against {@code patterns} sequentially. Te total match
   * length is returned if all succeed.
   */
  public static Pattern sequence(Pattern... patterns) {
    return new SequencePattern(patterns);
  }

  /**
   * Returns a {@link Pattern} object that matches if the input has at least {@code n} characters and the first {@code
   * n} characters all satisfy {@code predicate}.
   */
  public static Pattern repeat(int n, CharPredicate predicate) {
    Checks.checkNonNegative(n, "n < 0");
    return new RepeatCharPredicatePattern(n, predicate);
  }

  /**
   * Returns a {@link Pattern} object that matches if the input starts with {@code min} or more characters and all
   * satisfy {@code predicate}.
   * @deprecated Use {@link #atLeast(int, CharPredicate)} instead.
   */
  @Deprecated
  public static Pattern many(int min, CharPredicate predicate) {
    return atLeast(min, predicate);
  }

  /**
   * Returns a {@link Pattern} object that matches if the input starts with {@code min} or more characters and all
   * satisfy {@code predicate}.
   * @since 2.2
   */
  public static Pattern atLeast(final int min, final CharPredicate predicate) {
    Checks.checkMin(min);
    return new Pattern() {
      @Override public int match(CharSequence src, int begin, int end) {
        int minLen = RepeatCharPredicatePattern.matchRepeat(min, predicate, src, end, begin, 0);
        if (minLen == MISMATCH) return MISMATCH;
        return matchMany(predicate, src, end, begin + minLen, minLen);
      }
      @Override public String toString() {
        return (min > 1) ? (predicate + "{" + min + ",}") : (predicate + "+");
      }
    };
  }

  /** Returns a {@link Pattern} that matches 0 or more characters satisfying {@code predicate}. */
  public static Pattern many(final CharPredicate predicate) {
    return new Pattern() {
      @Override public int match(CharSequence src, int begin, int end) {
        return matchMany(predicate, src, end, begin, 0);
      }
      @Override public String toString() {
        return predicate + "*";
      }
    };
  }

  /**
   * Returns a {@link Pattern} that matches at least {@code min} and up to {@code max} number of characters satisfying
   * {@code predicate},
   * @deprecated Use {@link #times(int, int, CharPredicate)} instead.
   */
  @Deprecated
  public static Pattern some(int min, int max, CharPredicate predicate) {
    return times(min, max, predicate);
  }

  /**
   * Returns a {@link Pattern} that matches at least {@code min} and up to {@code max} number of characters satisfying
   * {@code predicate},
   *
   * @since 2.2
   */
  public static Pattern times(final int min, final int max, final CharPredicate predicate) {
    Checks.checkMinMax(min, max);
    return new Pattern() {
      @Override
      public int match(CharSequence src, int begin, int end) {
        int minLen = RepeatCharPredicatePattern.matchRepeat(min, predicate, src, end, begin, 0);
        if (minLen == MISMATCH)
          return MISMATCH;
        return matchSome(max - min, predicate, src, end, begin + minLen, minLen);
      }
    };
  }

  /**
   * Returns a {@link Pattern} that matches up to {@code max} number of characters satisfying
   * {@code predicate}.
   * @deprecated Use {@link #atMost(int, CharPredicate)} instead.
   */
  @Deprecated
  public static Pattern some(final int max, final CharPredicate predicate) {
    return atMost(max, predicate);
  }

  /**
   * Returns a {@link Pattern} that matches up to {@code max} number of characters satisfying
   * {@code predicate}.
   * @since 2.2
   */
  public static Pattern atMost(final int max, final CharPredicate predicate) {
    Checks.checkMax(max);
    return new Pattern() {
      @Override
      public int match(CharSequence src, int begin, int end) {
        return matchSome(max, predicate, src, end, begin, 0);
      }
    };
  }

  /**
   * Returns a {@link Pattern} that tries both {@code p1} and {@code p2},
   * and picks the one with the longer match length.
   * If both have the same length, {@code p1} is favored.
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
      @Override
      public int match(CharSequence src, int begin, int end) {
        int r = MISMATCH;
        for (Pattern pattern : patterns) {
          int l = pattern.match(src, begin, end);
          if (l > r)
            r = l;
        }
        return r;
      }
    };
  }

  /**
   * Returns a {@link Pattern} that tries both {@code p1} and {@code p2}, and picks the one with the shorter match
   * length. If both have the same length, {@code p1} is favored.
   */
  public static Pattern shorter(Pattern p1, Pattern p2) {
    return shortest(p1, p2);
  }

  /**
   * Returns a {@link Pattern} that tries all of {@code patterns}, and picks the one with the shortest match length. If
   * two patterns have the same length, the first one is favored.
   */
  public static Pattern shortest(final Pattern... patterns) {
    return new Pattern() {
      @Override
      public int match(CharSequence src, int begin, int end) {
        int r = MISMATCH;
        for (Pattern pattern : patterns) {
          final int l = pattern.match(src, begin, end);
          if (l != MISMATCH) {
            if ((r == MISMATCH) || (l < r))
              r = l;
          }
        }
        return r;
      }
    };
  }

  /** Returns a {@link Pattern} that matches 1 or more characters satisfying {@code predicate}. */
  public static Pattern many1(CharPredicate predicate) {
    return atLeast(1, predicate);
  }

  /**
   * Adapts a regular expression pattern to a {@link Pattern}.
   * 
   * <p><em>WARNING</em>: in addition to regular expression cost, the returned {@code Pattern} object needs
   * to make a substring copy every time it's evaluated. This can incur excessive copying and memory overhead
   * when parsing large strings. Consider implementing {@code Pattern} manually for large input.
   */
  public static Pattern regex(final java.util.regex.Pattern p) {
    return new Pattern() {
      @Override
      public int match(CharSequence src, int begin, int end) {
        if (begin > end)
          return MISMATCH;
        Matcher matcher = p.matcher(src.subSequence(begin, end));
        if (matcher.lookingAt())
          return matcher.end();
        return MISMATCH;
      }
    };
  }

  /**
   * Adapts a regular expression pattern string to a {@link Pattern}.
   * 
   * <p><em>WARNING</em>: in addition to regular expression cost, the returned {@code Pattern} object needs
   * to make a substring copy every time it's evaluated. This can incur excessive copying and memory overhead
   * when parsing large strings. Consider implementing {@code Pattern} manually for large input.
   */
  public static Pattern regex(String s) {
    return regex(java.util.regex.Pattern.compile(s));
  }

  static Pattern optional(Pattern pp) {
    return new OptionalPattern(pp);
  }

  private static int matchSome(int max, CharPredicate predicate, CharSequence src, int len, int from, int acc) {
    int k = Math.min(max + from, len);
    for (int i = from; i < k; i++) {
      if (!predicate.isChar(src.charAt(i)))
        return i - from + acc;
    }
    return k - from + acc;
  }

  private static Pattern getRegularExpressionPattern() {
    Pattern quote = isChar('/');
    Pattern escape = isChar('\\').next(hasAtLeast(1));
    Pattern content = or(escape, isChar(CharPredicates.notAmong("/\r\n\\")));
    return quote.next(content.many()).next(quote);
  }

  private static Pattern getModifiersPattern() {
    return isChar(CharPredicates.IS_ALPHA).many();
  }

  private static int matchMany(
      CharPredicate predicate, CharSequence src, int len, int from, int acc) {
    for (int i = from; i < len; i++) {
      if (!predicate.isChar(src.charAt(i)))
        return i - from + acc;
    }
    return len - from + acc;
  }

  private  static int matchStringCaseInsensitive(String str, CharSequence src, int begin, int end) {
    final int patternLength = str.length();
    if ((end - begin) < patternLength) return Pattern.MISMATCH;
    for (int i = 0; i < patternLength; i++) {
      final char exp = str.charAt(i);
      final char enc = src.charAt(begin + i);
      if (Character.toLowerCase(exp) != Character.toLowerCase(enc))
        return Pattern.MISMATCH;
    }
    return patternLength;
  }

  /**
   * Matches (part of) a character sequence against a pattern string.
   *
   * @param  str   the pattern string.
   * @param  src   the input sequence. Must not be null.
   * @param  begin start of index to scan characters from <code>src</code>.
   * @param  end   end of index to scan characters from <code>src</code>.
   *
   * @return the number of characters matched, or {@link Pattern#MISMATCH} if an unexpected character is encountered.
   */
  private static int matchString(String str, CharSequence src, int begin, int end) {
    final int patternLength = str.length();
    int i = 0;
    for (; (i < patternLength) && ((begin + i) < end); i++) {
      final char exp = str.charAt(i);
      final char enc = src.charAt(begin + i);
      if (exp != enc) return Pattern.MISMATCH;
    }
    return i;
  }
}
