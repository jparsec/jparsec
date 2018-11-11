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
package org.jparsec.examples.calculator;

import static org.jparsec.Scanners.isChar;

import java.util.function.BinaryOperator;
import java.util.function.UnaryOperator;

import org.jparsec.OperatorTable;
import org.jparsec.Parser;
import org.jparsec.Scanners;

/**
 * The main calculator parser.
 * 
 * @author Ben Yu
 */
public final class Calculator {
  
  /** Parsers {@code source} and evaluates to an {@link Integer}. */
  public static int evaluate(String source) {
    return parser().parse(source);
  }
  
  static final Parser<Integer> NUMBER = Scanners.INTEGER.map(Integer::valueOf);
  
  static final BinaryOperator<Integer> PLUS = (a, b) -> a + b;
  
  static final BinaryOperator<Integer> MINUS = (a, b) -> a - b;
  
  static final BinaryOperator<Integer> MUL = (a, b) -> a * b;
  
  static final BinaryOperator<Integer> DIV = (a, b) -> a / b;
  
  static final BinaryOperator<Integer> MOD = (a, b) -> a % b;
  
  static final UnaryOperator<Integer> NEG = a -> -a;
  
  private static <T> Parser<T> op(char ch, T value) {
    return isChar(ch).retn(value);
  }
  
  static Parser<Integer> parser() {
    Parser.Reference<Integer> ref = Parser.newReference();
    Parser<Integer> term = ref.lazy().between(isChar('('), isChar(')')).or(NUMBER);
    Parser<Integer> parser = new OperatorTable<Integer>()
        .prefix(op('-', NEG), 100)
        .infixl(op('+', PLUS), 10)
        .infixl(op('-', MINUS), 10)
        .infixl(op('*', MUL), 20)
        .infixl(op('/', DIV), 20)
        .infixl(op('%', MOD), 20)
        .build(term);
    ref.set(parser);
    return parser;
  }
}
