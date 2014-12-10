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
package org.codehaus.jparsec.examples.sql.parser;

import static java.util.Arrays.asList;
import static org.codehaus.jparsec.Parsers.between;

import org.codehaus.jparsec.Parser;
import org.codehaus.jparsec.Parsers;
import org.codehaus.jparsec.Scanners;
import org.codehaus.jparsec.Terminals;
import org.codehaus.jparsec.Tokens.Tag;
import org.codehaus.jparsec.examples.sql.ast.QualifiedName;
import org.codehaus.jparsec.misc.Mapper;

/**
 * Lexers and terminal level parsers for SQL.
 * 
 * @author Ben Yu
 */
final class TerminalParser {
  
  private static final String[] OPERATORS = {
    "+", "-", "*", "/", "%", ">", "<", "=", ">=", "<=", "<>", ".", ",", "(", ")", "[", "]"
  };
  
  private static final String[] KEYWORDS = {
    "select", "distinct", "from", "as", "where", "group", "by", "having", "order", "asc", "desc", 
    "and", "or", "not", "in", "exists", "between", "is", "null", "like", "escape",
    "inner", "outer", "left", "right", "full", "cross", "join", "on",
    "union", "all", "case", "when", "then", "else", "end"
  };
  
  private static final Terminals TERMS =
      Terminals.operators(OPERATORS).words(Scanners.IDENTIFIER).caseInsensitiveKeywords(asList(KEYWORDS)).build();
  
  private static final Parser<?> TOKENIZER = Parsers.or(
      Terminals.DecimalLiteral.TOKENIZER, Terminals.StringLiteral.SINGLE_QUOTE_TOKENIZER,
      TERMS.tokenizer());
  
  static final Parser<String> NUMBER = Terminals.DecimalLiteral.PARSER;
  static final Parser<String> STRING = Terminals.StringLiteral.PARSER;
  
  static final Parser<String> NAME =
      between(term("["), Terminals.fragment(Tag.RESERVED, Tag.IDENTIFIER), term("]"))
      .or(Terminals.Identifier.PARSER);
  
  static final Parser<QualifiedName> QUALIFIED_NAME =
      Mapper.curry(QualifiedName.class).sequence(NAME.sepBy1(term(".")));
  
  static <T> T parse(Parser<T> parser, String source) {
    return parser.from(TOKENIZER, Scanners.SQL_DELIMITER).parse(source);
  }
  
  public static Parser<?> term(String term) {
    return Mapper._(TERMS.token(term));
  }
  
  public static Parser<?> phrase(String phrase) {
    return Mapper._(TERMS.phrase(phrase.split("\\s")));
  }
}
