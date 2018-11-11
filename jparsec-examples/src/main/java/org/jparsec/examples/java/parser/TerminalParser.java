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
package org.jparsec.examples.java.parser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jparsec.Parser;
import org.jparsec.Parsers;
import org.jparsec.Scanners;
import org.jparsec.Terminals;
import org.jparsec.Token;

/**
 * Parser for terminals.
 * 
 * @author Ben Yu
 */
public final class TerminalParser {
  
  private static final Terminals TERMS = Terminals      
       // <<, >> and >>> are handled by {@link #adjacent(String)},
       // to avoid ambiguity with generics syntax.
      .operators("+", "-", "*", "/", "%", "&", "|", "~", "^",
          ">", "<", "==", ">=", "<=", "!=", "&&", "||", "!",
          ".", ",",  "?", ":", ";", "...", "@",
          "=", "+=", "-=", "*=", "/=", "%=", "^=", "&=", "|=", "<<=", ">>=", ">>>=", "++", "--",
          "(", ")", "[", "]", "{", "}", "::", "->")
      .words(JavaLexer.IDENTIFIER)
      .keywords("private", "protected", "public", "final", "abstract", "native", "static",
          "transient", "volatile", "throws", "class", "interface", "enum", "package", "import",
          "if", "else", "for", "while", "do", "continue", "break", "return",
          "switch", "case", "default", "throw", "try", "catch", "finally",
          "new", "this", "super", "synchronized", "instanceof", "extends", "implements", "assert",
          "byte", "short", "int", "long", "char", "float", "double", "boolean", "char", "void",
          "true", "false", "null",
          "goto", "const", "strictfp")
      .build();
  
  // hex, oct, int, decimal, scientific, string literal, char literal and the other terms.
  // Let's not worry about unicode escape in char and string literals for now.
  // They are no fun to write and this is just a demo.
  static final Parser<?> TOKENIZER = Parsers.or(
      JavaLexer.SCIENTIFIC_NUMBER_LITERAL,
      Terminals.StringLiteral.DOUBLE_QUOTE_TOKENIZER,
      Terminals.CharLiteral.SINGLE_QUOTE_TOKENIZER,
      TERMS.tokenizer(), JavaLexer.DECIMAL_POINT_NUMBER, JavaLexer.INTEGER);
  
  /**
   * A {@link Parser} that succeeds only if the {@link Token} objects in the {@link List} are
   * adjacent.
   */
  public static Parser<Token> adjacent(Parser<List<Token>> parser, final Parser<?> otherwise) {
    return parser.next(tokens -> {
        if (tokens.isEmpty()) return Parsers.always();
        int offset = tokens.get(0).index();
        for (Token token : tokens) {
          if (token.index() != offset) {
            return otherwise;
          }
          offset += token.length();
        }
        return Parsers.always();
      }).atomic().source().token();
  }
  
  /**
   * A {@link Parser} that parses all adjacent characters in {@code operator} as individual
   * {@link Token} and only succeeds if these tokens are adjacent. A {@code Token} representing
   * the entire {@code operator} is returned.
   */
  public static Parser<Token> adjacent(String operator) {
    List<Parser<Token>> parsers = new ArrayList<Parser<Token>>(operator.length());
    for (int i = 0; i < operator.length(); i++) {
      parsers.add(TERMS.token(Character.toString(operator.charAt(i))));
    }
    return adjacent(Parsers.list(parsers), Parsers.expect(operator));
  }
  
  public static Parser<?> term(String name) {
    if (name.equals(">>")) {
      // manually do the exclusion so that ">>>" never gets interpreted partially as ">>",
      // even if it can be interpreted as ">" followed by ">>" or three ">"s.
      return adjacent(">>>").not().next(adjacent(">>"));
    }
    if (name.equals("<<") || name.equals(">>>")) {
      return adjacent(name);
    }
    return TERMS.token(name);
  }
  
  static Parser<?> oneOf(String... names) {
    return TERMS.token(names);
  }
  
  static <T> T parse(Parser<T> parser, String source) {
    return parser.from(TOKENIZER, Scanners.JAVA_DELIMITER).parse(source);
  }
  
  static <T> T parse(Parser<T> parser, Readable readable, String module) throws IOException {
    return parser.from(TOKENIZER, Scanners.JAVA_DELIMITER).parse(readable, module);
  }
  
  public static Parser<?> phrase(String phrase) {
    return TERMS.phrase(phrase.split("\\s+"));
  }
}
