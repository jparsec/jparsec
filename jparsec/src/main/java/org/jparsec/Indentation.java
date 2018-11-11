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

import java.util.List;
import java.util.Stack;

import org.jparsec.internal.annotations.Private;
import org.jparsec.internal.util.Lists;
import org.jparsec.internal.util.Objects;
import org.jparsec.pattern.CharPredicate;
import org.jparsec.pattern.Pattern;
import org.jparsec.pattern.Patterns;

/**
 * Processes indentation based lexical structure according to the
 * <a href="http://en.wikipedia.org/wiki/Off-side_rule">Off-side rule</a>.
 * 
 * @author Ben Yu
 */
public final class Indentation {
  
  /**
   * A {@link CharPredicate} that returns true only if the character isn't line feed
   * and {@link Character#isWhitespace(char)} returns true.
   */
  static final CharPredicate INLINE_WHITESPACE = new CharPredicate() {
    @Override public boolean isChar(char c) {
      return c != '\n' && Character.isWhitespace(c);
    }
    @Override public String toString() {
      return "whitespace";
    }
  };
  
  /**
   * A {@link Pattern} object that matches a line continuation. i.e. a backslash character
   * ({@code '\'}) followed by some whitespaces and ended by a line feed character ({@code '\n'}).
   * Is useful if the line feed character plays a role in the syntax (as in
   * indentation-sensitive languages) and line continuation is supported.
   */
  static final Pattern LINE_CONTINUATION = Patterns.sequence(
      Patterns.isChar('\\'), Patterns.many(INLINE_WHITESPACE), Patterns.isChar('\n'));
  
  /**
   * A {@link Pattern} object that matches one or more whitespace characters or line continuations,
   * where the line feed character ({@code '\n'}) is escaped by the backslash character
   * ({@code '\'}).
   */
  static final Pattern INLINE_WHITESPACES = Patterns.many1(INLINE_WHITESPACE);
  
  /**
   * A {@link Parser} that recognizes 1 or more whitespace characters on the same line.
   * Line continutation (escaped by a backslash character {@code '\'}) is considered the same line.
   */
  public static final Parser<Void> WHITESPACES =
      INLINE_WHITESPACES.or(LINE_CONTINUATION).many1().toScanner("whitespaces");
  
  @Private static enum Punctuation {
    INDENT, OUTDENT, LF
  }
  
  private final Object indent;
  private final Object outdent;
  
  /**
   * Creates an {@link Indentation} object that uses {@code indent} and {@code outdent} as the
   * token values for indentation and outdentation.
   */
  public Indentation(Object indent, Object outdent) {
    this.indent = indent;
    this.outdent = outdent;
  }
  
  /** Creates a {@link Indentation} object that generates default indent and outdent tokens. */
  public Indentation() {
    this(Punctuation.INDENT, Punctuation.OUTDENT);
  }
  
  /** A {@link Parser} that recognizes the generated {@code indent} token. */
  public Parser<Token> indent() {
    return token(indent);
  }
  
  /** A {@link Parser} that recognizes the generated {@code outdent} token. */
  public Parser<Token> outdent() {
    return token(outdent);
  }
  
  /**
   * A {@link Parser} that greedily runs {@code tokenizer}, and translates line feed characters
   * ({@code '\n'}) to {@code indent} and {@code outdent} tokens.
   * Return values are wrapped in {@link Token} objects and collected in a {@link List}.
   * Patterns recognized by {@code delim} are ignored. 
   */
  public Parser<List<Token>> lexer(Parser<?> tokenizer, Parser<?> delim) {
    Parser<?> lf = Scanners.isChar('\n').retn(Punctuation.LF);
    return Parsers.or(tokenizer, lf).lexer(delim)
        .map(tokens -> analyzeIndentations(tokens, Punctuation.LF));
  }
  
  private static Parser<Token> token(Object value) {
    return Parsers.token(InternalFunctors.tokenWithSameValue(value));
  }

  /**
   * Analyzes indentation by looking at the first token after each {@code lf} and inserting
   * {@code indent} and {@code outdent} tokens properly.
   */
  List<Token> analyzeIndentations(List<Token> tokens, Object lf) {
    if (tokens.isEmpty()) {
      return tokens;
    }
    int size = tokens.size();
    List<Token> result = Lists.arrayList(size + size / 16);
    Stack<Integer> indentations = new Stack<Integer>();
    boolean freshLine = true;
    int lfIndex = 0;
    for (Token token : tokens) {
      if (freshLine) {
        int indentation = token.index() - lfIndex;
        if (Objects.equals(token.value(), lf)) {
          // if first token on a line is lf, indentation is ignored.
          indentation = 0;
        }
        newLine(token, indentations, indentation, result);
      }
      if (Objects.equals(token.value(), lf)) {
        freshLine = true;
        lfIndex = token.index() + token.length();
      }
      else {
        freshLine = false;
        result.add(token);
      }
    }
    Token lastToken = tokens.get(tokens.size() - 1);
    int endIndex = lastToken.index() + lastToken.length();
    Token outdentToken = pseudoToken(endIndex, outdent);
    for (int i = 0; i < indentations.size() - 1; i++) {
      // add outdent for every remaining indentation except the first one
      result.add(outdentToken);
    }
    return result;
  }
  
  private void newLine(
      Token token, Stack<Integer> indentations, int indentation, List<Token> result) {
    for (;;) {
      if (indentations.isEmpty()) {
        indentations.add(indentation);
        return;
      }
      int previousIndentation = indentations.peek();
      if (previousIndentation < indentation) {
        // indent
        indentations.push(indentation);
        result.add(pseudoToken(token.index(), indent));
        return;
      }
      else if (previousIndentation > indentation) {
        // outdent
        indentations.pop();
        if (indentations.isEmpty()) {
          return;
        }
        result.add(pseudoToken(token.index(), outdent));
        continue;
      }
      return;
    }
  }
  
  private static Token pseudoToken(int index, Object value) {
    return new Token(index, 0, value);
  }
}
