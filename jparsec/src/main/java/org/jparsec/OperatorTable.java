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

import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.jparsec.internal.annotations.Private;
import org.jparsec.internal.util.Lists;

/**
 * Builds {@link Parser} to parse expressions with operator-precedence grammar. The operators
 * and precedences are declared in this table.
 * 
 * <p>Operators have precedences. The higher the precedence number, the higher the precedence. For
 * the same precedence, prefix &gt; postfix &gt; left-associative &gt; non-associative &gt; right-associative.
 *
 * <p>For example: 
 * {@code
 *   Unary<Integer> negate = new Unary<Integer>() {... return -n; };
 *   Binary<Integer> plus = new Binary<Integer>() {... return a + b; };
 *   Binary<Integer> minus = new Binary<Integer>() {... return a - b; };
 *   ...
 *   Terminals terms = Terminals.operators("+", "-", "*", "/");
 *   Parser<Integer> calculator = new OperatorTable()
 *       .prefix(terms.token("-").retn(negate), 100)
 *       .infixl(terms.token("+").retn(plus), 10)
 *       .infixl(terms.token("-").retn(minus), 10)
 *       .infixl(terms.token("*").retn(multiply), 20)
 *       .infixl(terms.token("/").retn(divide), 20)
 *       .build(Terminals.IntegerLiteral.PARSER.map(stringToInteger));
 *   Parser<Integer> parser = calculator.from(
 *       terms.tokenizer().or(Terminals.IntegerLiteral.TOKENIZER), Scanners.WHITESPACES.optional());
 *   return parser.parse(text);
 * }
 * 
 * @author Ben Yu
 */
public final class OperatorTable<T> {
  
  /** Describes operator associativity, in order of precedence. */
  enum Associativity {
    PREFIX, POSTFIX, LASSOC, NASSOC, RASSOC
  }
  
  private final List<Operator> ops = Lists.arrayList();
  
  static final class Operator implements Comparable<Operator>{
    final Parser<?> op;
    final int precedence;
    final Associativity associativity;
    
    Operator(Parser<?> op, int precedence, Associativity associativity) {
      this.op = op;
      this.precedence = precedence;
      this.associativity = associativity;
    }
    
    /** Higher precedence first. For tie, compares associativity. */
    @Override public int compareTo(Operator that) {
      if (precedence > that.precedence) return -1;
      if (precedence < that.precedence) return 1;
      return associativity.compareTo(that.associativity);
    }
  }
  
  /**
   * Adds a prefix unary operator.
   * 
   * @param parser the parser for the operator.
   * @param precedence the precedence number.
   * @return this.
   */
  public OperatorTable<T> prefix(
      Parser<? extends Function<? super T, ? extends T>> parser, int precedence) {
    ops.add(new Operator(parser, precedence, Associativity.PREFIX));
    return this;
  }
  
  /**
   * Adds a postfix unary operator.
   * 
   * @param parser the parser for the operator.
   * @param precedence the precedence number.
   * @return this.
   */
  public OperatorTable<T> postfix(
      Parser<? extends Function<? super T, ? extends T>> parser, int precedence) {
    ops.add(new Operator(parser, precedence, Associativity.POSTFIX));
    return this;
  }
  
  /**
   * Adds an infix left-associative binary operator.
   * 
   * @param parser the parser for the operator.
   * @param precedence the precedence number.
   * @return this.
   */
  public OperatorTable<T> infixl(
      Parser<? extends BiFunction<? super T, ? super T, ? extends T>> parser, int precedence) {
    ops.add(new Operator(parser, precedence, Associativity.LASSOC));
    return this;
  }
  
  /**
   * Adds an infix right-associative binary operator.
   * 
   * @param parser the parser for the operator.
   * @param precedence the precedence number.
   * @return this.
   */
  public OperatorTable<T> infixr(
      Parser<? extends BiFunction<? super T, ? super T, ? extends T>> parser, int precedence) {
    ops.add(new Operator(parser, precedence, Associativity.RASSOC));
    return this;
  }
  /**
   * Adds an infix non-associative binary operator.
   * 
   * @param parser the parser for the operator.
   * @param precedence the precedence number.
   * @return this.
   */
  public OperatorTable<T> infixn(
      Parser<? extends BiFunction<? super T, ? super T, ? extends T>> parser, int precedence) {
    ops.add(new Operator(parser, precedence, Associativity.NASSOC));
    return this;
  }

  
  /**
   * Builds a {@link Parser} based on information in this {@link OperatorTable}.
   * 
   * @param operand parser for the operands.
   * @return the expression parser.
   */
  public Parser<T> build(Parser<? extends T> operand) {
    return buildExpressionParser(operand, operators());
  }
  
  @Private Operator[] operators() {
    Collections.sort(ops);
    return ops.toArray(new Operator[ops.size()]);
  }
  
  /**
   * Builds a {@link Parser} based on information described by {@link OperatorTable}.
   * 
   * @param term parser for the terminals.
   * @param ops the operators.
   * @return the expression parser.
   */
  static <T> Parser<T> buildExpressionParser(
      final Parser<? extends T> term, final Operator... ops) {
    if (ops.length == 0) return term.cast();
    int begin = 0;
    int precedence = ops[0].precedence;
    Associativity associativity = ops[0].associativity;
    int end = 0;
    Parser<T> ret = term.cast();
    for (int i = 1; i < ops.length; i++) {
      Operator op = ops[i];
      end = i;
      if (op.precedence == precedence && op.associativity == associativity) {
        continue;
      }
      end = i;
      Parser<?> p = slice(ops, begin, end);
      ret = build(p, associativity, ret);
      begin = i;
      precedence = ops[i].precedence;
      associativity = ops[i].associativity;
    }
    if (end != ops.length) {
      end = ops.length;
      associativity = ops[begin].associativity;
      Parser<?> p = slice(ops, begin, end);
      ret = build(p, associativity, ret);
    }
    return ret;
  }
  
  private static Parser<?> slice(Operator[] ops, int begin, int end) {
    Parser<?>[] ps = new Parser<?>[end - begin];
    for (int i = 0; i < ps.length; i++) {
      ps[i] = ops[i + begin].op;
    }
    return Parsers.or(ps);
  }
  
  @SuppressWarnings({"rawtypes", "unchecked"})
  private static <T> Parser<T> build(
      Parser op, Associativity associativity, Parser<T> operand) {
    switch (associativity) {
      case PREFIX:
        return operand.prefix(op);
      case POSTFIX:
        return operand.postfix(op);
      case LASSOC:
        return operand.infixl(op);
      case RASSOC:
        return operand.infixr(op);
      case NASSOC:
        return operand.infixn(op);
      default:
        throw new AssertionError();
    }
  }
}
