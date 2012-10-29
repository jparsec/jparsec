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

import org.codehaus.jparsec.pattern.CharPredicate;
import org.codehaus.jparsec.pattern.CharPredicates;
import org.codehaus.jparsec.pattern.Pattern;
import org.codehaus.jparsec.pattern.Patterns;

/**
 * Provides common {@link Parser} implementations that scan the source and match certain string
 * patterns.
 * <p>
 * Some scanners like {@link #IDENTIFIER} and {@link #INTEGER} return the matched string,
 * while others like {@link #WHITESPACES} return nothing, as indicated by the {@link Void}
 * type parameter. In case the matched string is still needed nontheless,
 * use the {@link Parser#source()} method.
 * 
 * @author Ben Yu
 */
public final class Scanners {
  
  /** A scanner that scans greedily for 1 or more whitespace characters. */
  public static final Parser<Void> WHITESPACES = 
      pattern(Patterns.many1(CharPredicates.IS_WHITESPACE), "whitespaces");
  
  /**
   * Matches any character in the input. Different from {@link Parsers#always()},
   * it fails on EOF. Also it consumes the current character in the input.
   */
  public static final Parser<Void> ANY_CHAR = new AnyCharScanner("any character");
  
  /** Scanner for c++/java style line comment. */
  public static final Parser<Void> JAVA_LINE_COMMENT = lineComment("//");
  
  /** Scanner for SQL style line comment. */
  public static final Parser<Void> SQL_LINE_COMMENT = lineComment("--");
  
  /** Scanner for haskell style line comment. ({@code --}) */
  public static final Parser<Void> HASKELL_LINE_COMMENT = lineComment("--");
  
  private static final Parser<Void> JAVA_BLOCK_COMMENTED =
      pattern(notChar2('*', '/').many(), "commented block");
  
  /** Scanner for c++/java style block comment. */
  public static final Parser<Void> JAVA_BLOCK_COMMENT =
      Parsers.sequence(string("/*"), JAVA_BLOCK_COMMENTED, string("*/"));
  
  /** Scanner for SQL style block comment. */
  public static final Parser<Void> SQL_BLOCK_COMMENT =
      Parsers.sequence(string("/*"), JAVA_BLOCK_COMMENTED, string("*/"));
  
  /** Scanner for haskell style block comment. {- -} */
  public static final Parser<Void> HASKELL_BLOCK_COMMENT = Parsers.sequence(
      string("{-"), pattern(notChar2('-', '}').many(), "commented block"), string("-}"));
  
  /**
   * Scanner with a pattern for SQL style string literal. A SQL string literal
   * is a string quoted by single quote, a single quote character is escaped by
   * 2 single quotes.
   */
  public static final Parser<String> SINGLE_QUOTE_STRING = quotedBy(
      pattern(Patterns.regex("(('')|[^'])*"), "quoted string"), isChar('\'')).source();

  /**
   * Scanner with a pattern for double quoted string literal. Backslash '\' is
   * used as escape character. 
   */
  public static final Parser<String> DOUBLE_QUOTE_STRING = quotedBy(
      pattern(Patterns.regex("((\\\\.)|[^\"\\\\])*") ,"quoted string"), Scanners.isChar('"'))
      .source();
  
  /** Scanner for a c/c++/java style character literal. such as 'a' or '\\'. */
  public static final Parser<String> SINGLE_QUOTE_CHAR = quotedBy(
      pattern(Patterns.regex("(\\\\.)|[^'\\\\]") ,"quoted char"), Scanners.isChar('\''))
      .source();
  
  /**
   * Scanner for the c++/java style delimiter of tokens. For example,
   * whitespaces, line comment and block comment.
   */
  public static final Parser<Void> JAVA_DELIMITER =
      Parsers.plus(WHITESPACES, JAVA_LINE_COMMENT, JAVA_BLOCK_COMMENT).skipMany();
  
  /**
   * Scanner for the haskell style delimiter of tokens. For example,
   * whitespaces, line comment and block comment.
   */
  public static final Parser<Void> HASKELL_DELIMITER = 
      Parsers.plus(WHITESPACES, HASKELL_LINE_COMMENT, HASKELL_BLOCK_COMMENT).skipMany();
  
  /**
   * Scanner for the SQL style delimiter of tokens. For example, whitespaces and
   * line comment.
   */
  public static final Parser<Void> SQL_DELIMITER =
      Parsers.plus(WHITESPACES, SQL_LINE_COMMENT, SQL_BLOCK_COMMENT).skipMany();
  
  /**
   * Scanner for a regular identifier, that starts with either
   * an underscore or an alpha character, followed by 0 or more alphanumeric characters.
   */
  public static final Parser<String> IDENTIFIER = pattern(Patterns.WORD, "word").source();
  
  /** Scanner for an integer. */
  public static final Parser<String> INTEGER = pattern(Patterns.INTEGER, "integer").source();
  
  /** Scanner for a decimal number. */
  public static final Parser<String> DECIMAL = pattern(Patterns.DECIMAL, "decimal").source();
  
  /** Scanner for a decimal number. 0 is not allowed as the leading digit. */
  public static final Parser<String> DEC_INTEGER =
      pattern(Patterns.DEC_INTEGER, "decimal integer").source();
  
  /** Scanner for a octal number. 0 is the leading digit. */
  public static final Parser<String> OCT_INTEGER =
      pattern(Patterns.OCT_INTEGER, "octal integer").source();
  
  /** Scanner for a hexadecimal number. Has to start with {@code 0x} or {@code 0X}. */
  public static final Parser<String> HEX_INTEGER =
      pattern(Patterns.HEX_INTEGER, "hexadecimal integer").source();
  
  /** Scanner for a scientific notation. */
  public static final Parser<String> SCIENTIFIC_NOTATION =
      pattern(Patterns.SCIENTIFIC_NOTATION, "scientific notation").source();
  
  /**
   * A scanner that scans greedily for 0 or more characters that satisfies the given CharPredicate.
   * 
   * @param predicate the predicate object.
   * @return the Parser object.
   */
  public static Parser<Void> many(CharPredicate predicate) {
    return pattern(Patterns.isChar(predicate).many(), predicate + "*");
  }
  
  /**
   * A scanner that scans greedily for 1 or more characters that satisfies the given CharPredicate.
   * 
   * @param predicate the predicate object.
   * @return the Parser object.
   */
  public static Parser<Void> many1(CharPredicate predicate) {
    return pattern(Patterns.many1(predicate), predicate + "+");
  }
  
  /**
   * A scanner that scans greedily for 0 or more occurrences of the given pattern.
   * 
   * @param pattern the pattern object.
   * @param name the name of what's expected logically. Is used in error message.
   * @return the Parser object.
   */
  public static Parser<Void> many(Pattern pattern, String name) {
    return pattern(pattern.many(), name);
  }
  
  /**
   * A scanner that scans greedily for 1 or more occurrences of the given pattern.
   * 
   * @param pattern the pattern object.
   * @param name the name of what's expected logically. Is used in error message.
   * @return the Parser object.
   */
  public static Parser<Void> many1(Pattern pattern, String name) {
    return pattern(pattern.many1(), name);
  }

  /**
   * Matches the input against the specified string.
   * 
   * @param str the string to match
   * @return the scanner.
   */
  public static Parser<Void> string(String str) {
    return string(str, str);
  }
  
  /**
   * Matches the input against the specified string.
   * 
   * @param str the string to match
   * @param name the name of what's expected logically. Is used in error message.
   * @return the scanner.
   */
  public static Parser<Void> string(String str, String name) {
    return pattern(Patterns.string(str), name);
  }
  
  /**
   * A scanner that scans the input for an occurrence of a string pattern.
   * 
   * @param pattern the pattern object.
   * @param name the name of what's expected logically. Is used in error message.
   * @return the Parser object.
   */
  public static Parser<Void> pattern(Pattern pattern, String name) {
    return new PatternScanner(name, pattern);
  }
  
  /**
   * A scanner that matches the input against the specified string case insensitively.
   * 
   * @param str the string to match
   * @param name the name of what's expected logically. Is used in error message.
   * @return the scanner.
   */
  public static Parser<Void> stringCaseInsensitive(String str, String name) {
    return pattern(Patterns.stringCaseInsensitive(str), name);
  }
  
  /**
   * A scanner that matches the input against the specified string case insensitively.
   * @param str the string to match
   * @return the scanner.
   */
  public static Parser<Void> stringCaseInsensitive(String str) {
    return stringCaseInsensitive(str, str);
  }

  /**
   * A scanner that succeeds and consumes the current character if it satisfies the given
   * {@link CharPredicate}.
   * 
   * @param predicate the predicate.
   * @return the scanner.
   */
  public static Parser<Void> isChar(CharPredicate predicate) {
    return isChar(predicate, predicate.toString());
  }
  
  /**
   * A scanner that succeeds and consumes the current character if it satisfies the given
   * {@link CharPredicate}.
   * 
   * @param predicate the predicate.
   * @param name the name of what's expected logically. Is used in error message.
   * @return the scanner.
   */
  public static Parser<Void> isChar(CharPredicate predicate, String name) {
    return new IsCharScanner(name, predicate);
  }
  
  /**
   * A scanner that succeeds and consumes the current character if it is equal to {@code ch}.
   * 
   * @param ch the expected character.
   * @param name the name of what's expected logically. Is used in error message.
   * @return the scanner.
   */
  public static Parser<Void> isChar(char ch, String name) {
    return isChar(CharPredicates.isChar(ch), name);
  }
  
  /**
   * A scanner that succeeds and consumes the current character if it is equal to {@code ch}.
   * 
   * @param ch the expected character.
   * @return the scanner.
   */
  public static Parser<Void> isChar(char ch) {
    return isChar(ch, Character.toString(ch));
  }
  
  /**
   * A scanner that succeeds and consumes the current character if it is equal to {@code ch}.
   * 
   * @param ch the expected character.
   * @param name the name of what's expected logically. Is used in error mesage.
   * @return the scanner.
   */
  public static Parser<Void> notChar(char ch, String name) {
    return isChar(CharPredicates.notChar(ch), name);
  }
  
  /**
   * A scanner that succeeds and consumes the current character if it is not equal to {@code ch}.
   * 
   * @param ch the expected character.
   * @return the scanner.
   */
  public static Parser<Void> notChar(char ch) {
    return notChar(ch, "^"+ch);
  }
  
  /**
   * A scanner that succeeds and consumes the current character if it equals to any character in
   * {@code chars}.
   * 
   * @param chars the characters.
   * @param name the name of what's expected logically. Is used in error message.
   * @return the scanner.
   */
  public static Parser<Void> among(String chars, String name) {
    return isChar(CharPredicates.among(chars), name);
  }
  
  /**
   * A scanner that succeeds and consumes the current character if it equals to any character in
   * {@code chars}.
   */
  public static Parser<Void> among(String chars) {
    if (chars.length() == 0) return isChar(CharPredicates.NEVER);
    if (chars.length() == 1) return isChar(chars.charAt(0));
    return isChar(CharPredicates.among(chars));
  }
  
  /**
   * A scanner that succeeds and consumes the current character if it is not equal to any character
   * in {@code chars}.
   * 
   * @param chars the characters.
   * @param name the name of what's expected logically. Is used in error message.
   * @return the scanner.
   */
  public static Parser<Void> notAmong(String chars, String name) {
    return isChar(CharPredicates.notAmong(chars), name);
  }
  
  /**
   * A scanner that succeeds and consumes the current character if it is not equal to any character
   * in {@code chars}.
   */
  public static Parser<Void> notAmong(String chars) {
    if (chars.length() == 0) return ANY_CHAR;
    if (chars.length() == 1) return notChar(chars.charAt(0));
    return isChar(CharPredicates.notAmong(chars));
  }
  
  /**
   * A scanner that succeeds and consumes all the characters until the {@code '\n'} character
   * if the current input starts with the string literal {@code begin}. The {@code '\n'} character
   * isn't consumed.
   */
  public static Parser<Void> lineComment(String begin) {
    return pattern(Patterns.lineComment(begin), begin);
  }
  
  /**
   * A scanner for non-nested block comment that starts with {@code begin} and ends with
   * {@code end}.
   */
  public static Parser<Void> blockComment(String begin, String end) {
    Pattern opening = Patterns.string(begin).next(Patterns.notString(end).many());
    return pattern(opening, begin).next(string(end));
  }
  
  /**
   * A scanner for a non-nestable block comment that starts with {@code begin} and ends with
   * {@code end}.
   * 
   * @param begin begins a block comment
   * @param end ends a block comment
   * @param commented the commented pattern.
   * @return the Scanner for the block comment.
   */
  public static Parser<Void> blockComment(String begin, String end, Pattern commented) {
    Pattern opening = Patterns.string(begin)
        .next(Patterns.string(end).not().next(commented).many());
    return pattern(opening, begin).next(string(end));
  }
  
  /**
   * A scanner for a non-nestable block comment that starts with {@code begin} and ends with
   * {@code end}.
   * 
   * @param begin begins a block comment
   * @param end ends a block comment
   * @param commented the commented pattern.
   * @return the Scanner for the block comment.
   */
  public static Parser<Void> blockComment(Parser<Void> begin, Parser<Void> end, Parser<?> commented) {
    return Parsers.sequence(begin, end.not().next(commented).skipMany(), end);
  }
  
  /**
   * A scanner for a nestable block comment that starts with {@code begin} and ends with
   * {@code end}.
   * 
   * @param begin begins a block comment
   * @param end ends a block comment
   * @return the block comment scanner.
   */
  public static Parser<Void> nestableBlockComment(String begin, String end) {
    return nestableBlockComment(begin, end, Patterns.isChar(CharPredicates.ALWAYS));
  }
  
  /**
   * A scanner for a nestable block comment that starts with {@code begin} and ends with
   * {@code end}.
   * 
   * @param begin begins a block comment
   * @param end ends a block comment
   * @param commented the commented pattern except for nested comments.
   * @return the block comment scanner.
   */
  public static Parser<Void> nestableBlockComment(String begin, String end, Pattern commented) {
    return nestableBlockComment(
        string(begin), string(end), pattern(commented, "commented"));
  }
  
  /**
   * A scanner for a nestable block comment that starts with {@code begin} and ends with
   * {@code end}.
   * 
   * @param begin starts a block comment
   * @param end ends a block comment
   * @param commented the commented pattern except for nested comments.
   * @return the block comment scanner.
   */
  public static Parser<Void> nestableBlockComment(
      Parser<?> begin, Parser<?> end, Parser<?> commented) {
    return new NestableBlockCommentScanner(begin, end, commented);
  }
  
  /**
   * A scanner for a quoted string that starts with character {@code begin} and ends with character
   * {@code end}.
   */
  public static Parser<String> quoted(char begin, char end) {
    Pattern beforeClosingQuote =
        Patterns.isChar(begin).next(Patterns.many(CharPredicates.notChar(end)));
    return pattern(beforeClosingQuote, Character.toString(begin)).next(isChar(end)).source();
  }
  
  /**
   * A scanner for a quoted string that starts with {@code begin} and ends with {@code end}.
   * 
   * @param begin begins a quote
   * @param end ends a quote
   * @param quoted the parser that recognizes the quoted pattern.
   * @return the scanner.
   */
  public static Parser<String> quoted(Parser<Void> begin, Parser<Void> end, Parser<?> quoted) {
    return Parsers.sequence(begin, quoted.skipMany(), end).source();
  }
  
  /**
   * A scanner that after character level {@code outer} succeeds,
   * subsequently feeds the recognized characters to {@code inner} for a nested scanning.
   * 
   * <p> Is useful for scenaios like parsing string interpolation grammar.
   */
  public static Parser<Void> nestedScanner(Parser<?> outer, Parser<Void> inner) {
    return new NestedScanner(outer, inner);
  }
  
  /**
   * Matches a character if the input has at least 1 character, or if the input has at least 2
   * characters with the first 2 characters not being {@code c1} and {@code c2}.
   * 
   * @return the Pattern object.
   */
  private static Pattern notChar2(final char c1, final char c2) {
    return new Pattern() {
      @Override public int match(CharSequence src, int begin, int end) {
        if (begin == end - 1) return 1;
        if (begin >= end) return MISMATCH;
        if (src.charAt(begin) == c1 && src.charAt(begin + 1) == c2) return Pattern.MISMATCH;
        return 1;
      }
    };
  }
  
  private static Parser<Void> quotedBy(Parser<Void> parser, Parser<?> quote) {
    return parser.between(quote, quote);
  }
  
  private Scanners() {}
}
