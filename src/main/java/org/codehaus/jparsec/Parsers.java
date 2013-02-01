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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.codehaus.jparsec.annotations.Private;
import org.codehaus.jparsec.error.ParserException;
import org.codehaus.jparsec.functors.Map;
import org.codehaus.jparsec.functors.Map2;
import org.codehaus.jparsec.functors.Map3;
import org.codehaus.jparsec.functors.Map4;
import org.codehaus.jparsec.functors.Map5;
import org.codehaus.jparsec.functors.Maps;
import org.codehaus.jparsec.functors.Pair;
import org.codehaus.jparsec.functors.Tuple3;
import org.codehaus.jparsec.functors.Tuple4;
import org.codehaus.jparsec.functors.Tuple5;
import org.codehaus.jparsec.util.Lists;

/**
 * Provides common {@link Parser} implementations.
 * 
 * @author Ben Yu
 */
public final class Parsers {

  /** {@link Parser} that succeeds only if EOF is met. Fails otherwise. */
  public static final Parser<?> EOF = eof("EOF");

  /** A {@link Parser} that consumes a token. The token value is returned from the parser. */
  public static final Parser<Object> ANY_TOKEN = token(new TokenMap<Object>() {
      public Object map(Token tok) {
        return tok.value();
      }
      @Override public String toString() {
        return "any token";
      }
    });
  
  /** A {@link Parser} that retrieves the current index in the source. */
  public static final Parser<Integer> INDEX = new GetIndexParser();
  
  @SuppressWarnings("unchecked")
  private static final Parser ALWAYS = constant(null);
  
  @SuppressWarnings("unchecked")
  private static final Parser NEVER = new NeverParser<Object>();
  
  static final Parser<Boolean> TRUE = constant(true);
  static final Parser<Boolean> FALSE = constant(false);
  
  /** {@link Parser} that always succeeds. */
  @SuppressWarnings("unchecked")
  public static <T> Parser<T> always() {
    return ALWAYS;
  }
  
  /** {@link Parser} that always fails. */
  @SuppressWarnings("unchecked")
  public static <T> Parser<T> never() {
    return NEVER;
  }

  /** A {@link Parser} that succeeds only if EOF is met. Fails with {@code message} otherwise. */
  static Parser<?> eof(String message) {
    return new EofParser(message);
  }

  /** A {@link Parser} that always fails with {@code message}. */
  public static <T> Parser<T> fail(String message) {
    return new FailureParser<T>(message);
  }
  
  /**
   * Runs a character level {@code parser} against {@code src} using {@code locator} to locate
   * error location.
   */
  static <T> T parse(
      CharSequence src, Parser<T> parser, SourceLocator locator, String module) {
    ScannerState ctxt = new ScannerState(module, src, 0, locator);
    if (!parser.run(ctxt)) {
      throw new ParserException(
          ctxt.renderError(), ctxt.module, locator.locate(ctxt.errorIndex()));
    }
    return parser.getReturn(ctxt);
  }

  /**
   * A {@link Parser} that always succeeds and invokes {@link Runnable#run()} against
   * {@code runnable}.
   */
  public static Parser<?> runnable(Runnable runnable) {
    return new ActionParser(runnable);
  }

  /** Converts a parser of a collection of {@link Token} to a parser of an array of {@code Token}.*/
  static Parser<Token[]> tokens(final Parser<? extends Collection<Token>> parser) {
    return parser.map(new Map<Collection<Token>, Token[]>() {
      public Token[] map(Collection<Token> list) {
        return list.toArray(new Token[list.size()]);
      }
      @Override public String toString() {
        return parser.toString();
      }
    });
  }
  
  /**
   * A {@link Parser} that takes as input the array of {@link Token} returned from {@code lexer},
   * and feeds the tokens as input into {@code parser}.
   * 
   * <p> It fails if either {@code lexer} or {@code parser} fails.
   * 
   * @param lexer the lexer object that returns an array of Tok objects.
   * @param parser the token level parser object.
   * @return the new Parser object.
   */
  static <T> Parser<T> nested(Parser<Token[]> lexer, Parser<? extends T> parser) {
    return new NestedParser<T>(lexer, parser);
  }

  /******************** monadic combinators ******************* */

  /** A {@link Parser} that always returns {@code v} regardless of input. */
  public static <T> Parser<T> constant(final T v) {
    return new Parser<T>() {
      @Override boolean apply(ParseContext ctxt) {
        ctxt.result = v;
        return true;
      }
      @Override public String toString() {
        return String.valueOf(v);
      }
    };
  }

  /**
   * A {@link Parser} that runs 2 parser objects sequentially. {@code p1} is executed,
   * if it succeeds, {@code p2} is executed.
   */
  public static <T> Parser<T> sequence(Parser<?> p1, Parser<T> p2) {
    return sequence(p1, p2, InternalFunctors.<Object, T>lastOfTwo());
  }

  /** A {@link Parser} that runs 3 parser objects sequentially. */
  public static <T> Parser<T> sequence(Parser<?> p1, Parser<?> p2, Parser<T> p3) {
    return sequence(p1, p2, p3, InternalFunctors.<Object, Object, T>lastOfThree());
  }

  /** A {@link Parser} that runs 4 parser objects sequentially. */
  public static <T> Parser<T> sequence(
      Parser<?> p1, Parser<?> p2, Parser<?> p3, Parser<T> p4) {
    return sequence(p1, p2, p3, p4, InternalFunctors.<Object, Object, Object, T>lastOfFour());
  }

  /** A {@link Parser} that runs 5 parser objects sequentially. */
  public static <T> Parser<T> sequence(
      Parser<?> p1, Parser<?> p2, Parser<?> p3, Parser<?> p4, Parser<T> p5) {
    return sequence(p1, p2, p3, p4, p5,
        InternalFunctors.<Object, Object, Object, Object, T>lastOfFive());
  }

  /**
   * A {@link Parser} that sequentially runs {@code p1} and {@code p2} and collects the results in a
   * {@link Pair} object. Is equivalent to {@link #tuple(Parser, Parser)}.
   */
  public static <A,B> Parser<Pair<A,B>> pair(Parser<? extends A> p1, Parser<? extends B> p2) {
    return sequence(p1, p2, Maps.<A, B > toPair());
  }

  /**
   * A {@link Parser} that sequentially runs {@code p1} and {@code p2} and collects the results in a
   * {@link Pair} object. Is equivalent to {@link #pair(Parser, Parser)}.
   */
  public static <A,B> Parser<Pair<A,B>> tuple(Parser<? extends A> p1, Parser<? extends B> p2) {
    return pair(p1, p2);
  }

  /**
   * A {@link Parser} that sequentially runs 3 parser objects and collects the results in a
   * {@link Tuple3} object.
   */
  public static <A,B,C> Parser<Tuple3<A,B,C>> tuple(
      Parser<? extends A> p1, Parser<? extends B> p2, Parser<? extends C> p3) {
    return sequence(p1, p2, p3, Maps.<A, B, C > toTuple3());
  }

  /**
   * A {@link Parser} that sequentially runs 4 parser objects and collects the results in a
   * {@link Tuple4} object.
   */
  public static <A,B,C,D> Parser<Tuple4<A,B,C,D>> tuple(
      Parser<? extends A> p1, Parser<? extends B> p2,
      Parser<? extends C> p3, Parser<? extends D> p4) {
    return sequence(p1, p2, p3, p4, Maps.<A, B, C, D > toTuple4());
  }

  /**
   * A {@link Parser} that sequentially runs 5 parser objects and collects the results in a
   * {@link Tuple5} object.
   */
  public static <A,B,C,D,E> Parser<Tuple5<A,B,C,D,E>> tuple(
      Parser<? extends A> p1, Parser<? extends B> p2, Parser<? extends C> p3,
      Parser<? extends D> p4, Parser<? extends E> p5) {
    return sequence(p1, p2, p3, p4, p5, Maps.<A, B, C, D, E > toTuple5());
  }
  
  /**
   * A {@link Parser} that sequentially runs {@code parsers} one by one and collects the return
   * values in an array.
   */
  public static Parser<Object[]> array(Parser<?>... parsers) {
    return new ArrayParser(parsers);
  }
  
  /**
   * A {@link Parser} that sequentially runs {@code parsers} one by one and collects the return
   * values in a {@link List}.
   */
  public static <T> Parser<List<T>> list(Iterable<? extends Parser<? extends T>> parsers) {
    return new ListParser<T>(toArray(parsers));
  }
  
  /**
   * Equivalent to {@link Parser#between(Parser, Parser)}. Use this to list the parsers in the
   * natural order.
   */
  public static <T> Parser<T> between(Parser<?> before, Parser<T> parser, Parser<?> after) {
    return parser.between(before, after);
  }

  /**
   * A {@link Parser} that runs {@code p1} and {@code p2} sequentially
   * and transforms the return values using {@code map}.
   */
  public static <A, B, T> Parser<T> sequence(
      Parser<A> p1, Parser<B> p2, Map2<? super A, ? super B, ? extends T> map) {
    return new Sequence2Parser<A, B, T>(p1, p2, map);
  }

  /**
   * A {@link Parser} that runs 3 parser objects sequentially and transforms the return values
   * using {@code map}.
   */
  public static <A, B, C, T> Parser<T> sequence(
      Parser<A> p1, Parser<B> p2, Parser<C> p3,
      Map3<? super A, ? super B, ? super C, ? extends T> map) {
    return new Sequence3Parser<A, B, C, T>(p1, p2, p3, map);
  }

  /**
   * A {@link Parser} that runs 4 parser objects sequentially and transforms the return values
   * using {@code map}.
   */
  public static <A, B, C, D, T> Parser<T> sequence(
      Parser<A> p1, Parser<B> p2, Parser<C> p3, Parser<D> p4,
      Map4<? super A, ? super B, ? super C, ? super D, ? extends T> map) {
    return new Sequence4Parser<A, B, C, D, T>(p1, p2, p3, p4, map);
  }

  /** 
   * A {@link Parser} that runs 5 parser objects sequentially and transforms the return values
   * using {@code map}.
   */
  public static <A, B, C, D, E, T> Parser<T> sequence(
      Parser<A> p1, Parser<B> p2, Parser<C> p3, Parser<D> p4, Parser<E> p5,
      Map5<? super A, ? super B, ? super C, ? super D, ? super E, ? extends T> map) {
    return new Sequence5Parser<A, B, C, D, E, T>(p1, p2, p3, p4, p5, map);
  }
  
  /** A {@link Parser} that runs {@code parsers} sequentially and discards the return values. */
  public static Parser<Object> sequence(Parser<?>... parsers) {
    return new SequenceParser(parsers);
  }
  
  /** A {@link Parser} that runs {@code parsers} sequentially and discards the return values. */
  public static Parser<Object> sequence(Iterable<? extends Parser<?>> parsers) {
    return sequence(toArray(parsers));
  }
  

  /**
   * Overload of {@link #plus(Parser[])} that takes 2 parser objects
   * to avoid unchecked compiler warning.
   */
  @SuppressWarnings("unchecked")
  static <T> Parser<T> plus(Parser<? extends T> p1, Parser<? extends T> p2) {
    return new SumParser<T>(p1, p2);
  }
  
  /**
   * Overload of {@link #plus(Parser[])} that takes 3 parser objects
   * to avoid unchecked compiler warning. 
   */
  @SuppressWarnings("unchecked")
  static <T> Parser<T> plus(
      Parser<? extends T> p1, Parser<? extends T> p2, Parser<? extends T> p3) {
    return new SumParser<T>(p1, p2, p3);
  }
  
  /**
   * A {@link Parser} that tries each alternative parser in {@code alternatives} and falls back
   * to the next parser if the previous parser fails <em>with no partial match</em>.
   */
  static <T> Parser<T> plus(Parser<? extends T>... alternatives) {
    if (alternatives.length == 0) return never();
    if (alternatives.length == 1) return alternatives[0].cast();
    return new SumParser<T>(alternatives);
  }
  
  /**
   * A {@link Parser} that tries 2 alternative parser objects.
   * Fallback happens regardless of partial match.
   */
  public static <T> Parser<T> or(Parser<? extends T> p1, Parser<? extends T> p2) {
    return alt(p1, p2).cast();
  }
  
  /**
   * A {@link Parser} that tries 3 alternative parser objects.
   * Fallback happens regardless of partial match.
   */
  public static <T> Parser<T> or(
      Parser<? extends T> p1, Parser<? extends T> p2, Parser<? extends T> p3) {
    return alt(p1, p2, p3).cast();
  }
  
  /**
   * A {@link Parser} that tries 4 alternative parser objects.
   * Fallback happens regardless of partial match.
   */
  public static <T> Parser<T> or(
      Parser<? extends T> p1, Parser<? extends T> p2,
      Parser<? extends T> p3, Parser<? extends T> p4) {
    return alt(p1, p2, p3, p4).cast();
  }
  
  /**
   * A {@link Parser} that tries 5 alternative parser objects.
   * Fallback happens regardless of partial match.
   */
  public static <T> Parser<T> or(
      Parser<? extends T> p1, Parser<? extends T> p2, Parser<? extends T> p3, 
      Parser<? extends T> p4, Parser<? extends T> p5) {
    return alt(p1, p2, p3, p4, p5).cast();
  }
  
  /**
   * A {@link Parser} that tries 6 alternative parser objects.
   * Fallback happens regardless of partial match.
   */
  public static <T> Parser<T> or(
      Parser<? extends T> p1, Parser<? extends T> p2, Parser<? extends T> p3, 
      Parser<? extends T> p4, Parser<? extends T> p5, Parser<? extends T> p6) {
    return alt(p1, p2, p3, p4, p5, p6).cast();
  }
  
  /**
   * A {@link Parser} that tries 7 alternative parser objects.
   * Fallback happens regardless of partial match.
   */
  public static <T> Parser<T> or(
      Parser<? extends T> p1, Parser<? extends T> p2, Parser<? extends T> p3, 
      Parser<? extends T> p4, Parser<? extends T> p5, Parser<? extends T> p6,
      Parser<? extends T> p7) {
    return alt(p1, p2, p3, p4, p5, p6, p7).cast();
  }
  
  /**
   * A {@link Parser} that tries 8 alternative parser objects.
   * Fallback happens regardless of partial match.
   */
  public static <T> Parser<T> or(
      Parser<? extends T> p1, Parser<? extends T> p2, Parser<? extends T> p3, 
      Parser<? extends T> p4, Parser<? extends T> p5, Parser<? extends T> p6,
      Parser<? extends T> p7, Parser<? extends T> p8) {
    return alt(p1, p2, p3, p4, p5, p6, p7, p8).cast();
  }
  
  /**
   * A {@link Parser} that tries 9 alternative parser objects.
   * Fallback happens regardless of partial match.
   */
  public static <T> Parser<T> or(
      Parser<? extends T> p1, Parser<? extends T> p2, Parser<? extends T> p3, 
      Parser<? extends T> p4, Parser<? extends T> p5, Parser<? extends T> p6,
      Parser<? extends T> p7, Parser<? extends T> p8, Parser<? extends T> p9) {
    return alt(p1, p2, p3, p4, p5, p6, p7, p8, p9).cast();
  }
  
  /**
   * A {@link Parser} that tries each alternative parser in {@code alternatives}.
   * 
   * <p> Different than {@link #alt(Parser[])}, it requires all alternative parsers to have
   * type {@code T}.
   */
  public static <T> Parser<T> or(Parser<? extends T>... alternatives) {
    if (alternatives.length == 0) return never();
    if (alternatives.length == 1) return alternatives[0].cast();
    return new OrParser<T>(alternatives);
  }
  
  /**
   * A {@link Parser} that tries each alternative parser in {@code alternatives}.
   */
  public static <T> Parser<T> or(Iterable<? extends Parser<? extends T>> alternatives) {
    return or(toArray(alternatives));
  }
  
  /** Allows the overloads of "or()" to call the varargs version of "or" with no ambiguity. */
  private static Parser<Object> alt(Parser<?>... alternatives) {
    return or(alternatives);
  }

  /**
   * A {@link Parser} that runs both {@code p1} and {@code p2} and selects the longer match.
   * If both matches the same length, the first one is favored.
   */
  @SuppressWarnings("unchecked")
  public static <T> Parser<T> longer(Parser<? extends T> p1, Parser<? extends T> p2) {
    return longest(p1, p2);
  }

  /**
   * A {@link Parser} that runs every element of {@code parsers} and selects the longest match.
   * If two matches have the same length, the first one is favored.
   */
  public static <T> Parser<T> longest(Parser<? extends T>... parsers) {
    if (parsers.length == 0) return never();
    if (parsers.length == 1) return parsers[0].cast();
    return new BestParser<T>(parsers, IntOrders.GT);
  }

  /**
   * A {@link Parser} that runs every element of {@code parsers} and selects the longest match.
   * If two matches have the same length, the first one is favored.
   */
  public static <T> Parser<T> longest(Iterable<? extends Parser<? extends T>> parsers) {
    return longest(toArray(parsers));
  }

  /**
   * A {@link Parser} that runs both {@code p1} and {@code p2} and selects the shorter match.
   * If both matches the same length, the first one is favored.
   */
  @SuppressWarnings("unchecked")
  public static <T> Parser<T> shorter(Parser<? extends T> p1, Parser<? extends T> p2) {
    return shortest(p1, p2);
  }

  /**
   * A {@link Parser} that runs every element of {@code parsers} and selects the shortest match.
   * If two matches have the same length, the first one is favored.
   */
  public static <T> Parser<T> shortest(Parser<? extends T>... parsers) {
    if (parsers.length == 0) return never();
    if (parsers.length == 1) return parsers[0].cast();
    return new BestParser<T>(parsers, IntOrders.LT);
  }

  /**
   * A {@link Parser} that runs every element of {@code parsers} and selects the shortest match.
   * If two matches have the same length, the first one is favored.
   */
  public static <T> Parser<T> shortest(Iterable<? extends Parser<? extends T>> parsers) {
    return shortest(toArray(parsers));
  }
  
  /** A {@link Parser} that fails and reports that {@code name} is logically expected. */
  public static <T> Parser<T> expect(String name) {
    return new ExpectParser<T>(name);    
  }

  /** A {@link Parser} that fails and reports that {@code name} is logically unexpected. */
  public static <T> Parser<T> unexpected(String name) {
    return new UnexpectedParser<T>(name);
  }

  /**
   * Checks the current token with the {@code fromToken} object. If the
   * {@link TokenMap#map(Token)} method returns null, an unexpected token error occurs;
   * if the method returns a non-null value, the value is returned and the parser succeeds.
   * 
   * @param fromToken the {@code FromToken} object.
   * @return the new Parser object.
   */
  public static <T> Parser<T> token(TokenMap<? extends T> fromToken) {
    return new IsTokenParser<T>(fromToken);
  }

  /**
   * Checks whether the current token value is of {@code type}, in which case, the token value is
   * returned and parse succeeds.
   * 
   * @param type the expected token value type.
   * @param name the name of what's logically expected.
   * @return the new Parser object.
   */
  public static <T> Parser<T> tokenType(Class<? extends T> type, String name) {
    return token(InternalFunctors.isTokenType(type, name));
  }

  /**
   * We always convert {@link Iterable} to an array to avoid the cost of creating
   * a new {@Link java.util.Iterator} object each time the parser runs.
   */
  @SuppressWarnings("unchecked")
  static <T> Parser<T>[] toArray(Iterable<? extends Parser<? extends T>> parsers) {
    if (parsers instanceof Collection<?>) {
      return toArray((Collection) parsers);
    }
    return toArrayWithIteration(parsers);
  }

  @Private static <T> Parser<T>[] toArrayWithIteration(
      Iterable<? extends Parser<? extends T>> parsers) {
    ArrayList<Parser<? extends T>> list = Lists.arrayList();
    for (Parser<? extends T> parser : parsers) {
      list.add(parser);
    }
    return toArray(list);
  }

  @SuppressWarnings("unchecked")
  static <T> Parser<T>[] toArray(Collection<? extends Parser<? extends T>> parsers) {
    return parsers.toArray(new Parser[parsers.size()]);
  }
  
  @SuppressWarnings("unchecked")
  static <From> boolean runNext(ParseContext state, Map<? super From, ? extends Parser<?>> next) {
    Parser<?> parser = next.map((From) state.result);
    return parser.run(state);
  }

  @SuppressWarnings("unchecked")
  static final Map2 PREFIX_OPERATOR_MAP2 = prefixOperatorMap2("prefix");

  @SuppressWarnings("unchecked")
  static final Map2 POSTFIX_OPERATOR_MAP2 = postfixOperatorMap2("postfix");

  /**
   * Non-associative infix operator. Runs {@code p} and then runs {@code op}
   * and {@code p} optionally. The {@link Map2} objects returned from {@code op}
   * is applied to the return values of the two {@code this} pattern, if any.
   * <p>
   * {@code infixn(p, op)} is equivalent to {@code p (op p)?} in EBNF.
   * 
   * @param op the operator
   * @return the new Parser object
   */
  static <T> Parser<T> infixn(
      final Parser<T> p, final Parser<? extends Map2<? super T, ? super T, ? extends T>> op) {
    return p.next(new Map<T, Parser<T>>() {
      public Parser<T> map(final T a) {
        final Parser<T> shift = sequence(op, p,
            new Map2<Map2<? super T, ? super T, ? extends T>, T, T>() {
              public T map(Map2<? super T, ? super T, ? extends T> m2, T b) {
                return m2.map(a, b);
              }
              @Override public String toString() {
                return "shift right operand";
              }
            });
        return plus(shift, constant(a));
      }
      @Override public String toString() {
        return "infixn";
      }
    });
  }

  /**
   * Left associative infix operator. Runs Parser {@code p} and then runs
   * {@code op} and {@code p} for 0 or more times greedily. The Map objects
   * returned from op are applied from left to right to the return values of
   * {@code p}. For example: {@code a + b+c + d} is evaluated as {@code (((a + b)+c)+d)}.
   * 
   * <p> {@code infixl(p, op)} is equivalent to {@code p (op p)*} in EBNF.
   * 
   * @param p the operand
   * @param op the operator
   * @return the new Parser object
   */
   static <T> Parser<T> infixl(
       Parser<T> p, Parser<? extends Map2<? super T, ? super T, ? extends T>> op) {
    @SuppressWarnings("unchecked")
    Parser<Map<T, T>> opAndRhs = sequence(op, p, MAP_OPERATOR_AND_RHS_TO_CLOSURE);
    final Parser<List<Map<T, T>>> afterFirstOperand = opAndRhs.many();
    Map<T, Parser<T>> next = new Map<T, Parser<T>>() {
      public Parser<T> map(final T first) {
        return afterFirstOperand.map(new Map<List<Map<T, T>>, T>() {
          public T map(List<Map<T, T>> maps) {
            return applyInfixOperators(first, maps);
          }
          @Override public String toString() {
            return "reduce";
          }
        });
      }
      @Override public String toString() {
        return "infixl";
      }
    };
    return p.next(next);
  }

  /**
   * Right associative infix operator. Runs Parser {@code p} and then runs
   * {@code op} and {@code p} for 0 or more times greedily. The {@link Map}
   * objects returned from {@code op} are applied from right to left to the
   * return values of {@code p}. For example: {@code a + b+c + d} is evaluated as
   * {@code a+(b+(c + d))}.
   * 
   * <p> {@code infixr(p, op)} is equivalent to {@code p (op p)*} in EBNF.
   * 
   * @param p the operand.
   * @param op the operator.
   * @return the new Parser object.
   */
  @SuppressWarnings("unchecked")
  static <T> Parser<T> infixr(
      Parser<T> p, Parser<? extends Map2<? super T, ? super T, ? extends T>> op) {
    Parser<Rhs<T>> rhs = sequence(op, p, INFIXR_OPERATOR_MAP2);
    return sequence(p, rhs.many(), APPLY_INFIXR_OPERATORS);
  }

  private static <T> Map2<List<? extends Map<? super T, ? extends T>>, T, T> prefixOperatorMap2(
      final String name) {
    return new Map2<List<? extends Map<? super T, ? extends T>>, T, T>() {
      public T map(List<? extends Map<? super T, ? extends T>> ops, T a) {
        return applyPrefixOperators(a, ops);
      }
      @Override public String toString() {
        return name;
      }
    };
  }

  private static <T> T applyInfixOperators(T initialValue, List<Map<T, T>> maps) {
    T result = initialValue;
    for (Map<T, T> map : maps) {
      result = map.map(result);
    }
    return result;
  }

  private static <T> T applyPrefixOperators(
      T a, final List<? extends Map<? super T, ? extends T>> ms) {
    for (int i = ms.size() - 1; i >= 0; i--) {
      Map<? super T, ? extends T> m = ms.get(i);
      a = m.map(a);
    }
    return a;
  }

  private static <T> Map2<T, List<? extends Map<? super T, ? extends T>>, T> postfixOperatorMap2(
      final String name) {
    return new Map2<T, List<? extends Map<? super T, ? extends T>>, T>() {
      public T map(T a, List<? extends Map<? super T, ? extends T>> ops) {
        return applyPostfixOperators(a, ops);
      }
      @Override public String toString() {
        return name;
      }
    };
  }

  private static <T> T applyPostfixOperators(
      T a, final Iterable<? extends Map<? super T, ? extends T>> ms) {
    for (Map<? super T, ? extends T> m : ms) {
      a = m.map(a);
    }
    return a;
  }

  // 1+ 1+ 1+ ..... 1
  private static final class Rhs<T> {
    final Map2<? super T, ? super T, ? extends T> op;
    final T rhs;

    Rhs(Map2<? super T, ? super T, ? extends T> op, T rhs) {
      this.op = op;
      this.rhs = rhs;
    }
    
    @Override public String toString() {
      return op + " " + rhs;
    }
  }

  @SuppressWarnings("unchecked")
  private static final Map2 INFIXR_OPERATOR_MAP2 = toInfixRhs();

  private static <T> Map2<Map2<? super T, ? super T, ? extends T>, T, Rhs<T>> toInfixRhs() {
    return new Map2<Map2<? super T, ? super T, ? extends T>, T, Rhs<T>>() {
      public Rhs<T> map(Map2<? super T, ? super T, ? extends T> m2, T b) {
        return new Rhs<T>(m2, b);
      }
      @Override public String toString() {
        return "operator and right operand";
      }
    };
  }

  @SuppressWarnings("unchecked")
  private static final Map2 APPLY_INFIXR_OPERATORS = applyInfixrOperators();

  private static final <T> Map2<T, List<Rhs<T>>, T> applyInfixrOperators() {
    return new Map2<T, List<Rhs<T>>, T>() {
      public T map(final T first, final List<Rhs<T>> rhss) {
        if (rhss.isEmpty())
          return first;
        int lastIndex = rhss.size() - 1;
        T o2 = rhss.get(lastIndex).rhs;
        for (int i = lastIndex; i > 0; i--) {
          T o1 = rhss.get(i - 1).rhs;
          o2 = rhss.get(i).op.map(o1, o2);
        }
        return rhss.get(0).op.map(first, o2);
      }
      @Override public String toString() {
        return "infixr";
      }
    };
  }

  @SuppressWarnings("unchecked")
  static final Map2 MAP_OPERATOR_AND_RHS_TO_CLOSURE = fromOperatorAndRhsToClosure();

  private static <A, B, R> Map2<Map2<A, B, R>, B, Map<A, R>> fromOperatorAndRhsToClosure() {
    return new Map2<Map2<A, B, R>, B, Map<A, R>>() {
      public Map<A, R> map(final Map2<A, B, R> op, final B b) {
        return new Map<A, R>() {
          public R map(A a) {
            return op.map(a, b);
          }
          @Override public String toString() {
            return "reduce left operand";
          }
        };
      }
      @Override public String toString() {
        return "operator and right operand";
      }
    };
  }
  
  private Parsers() {}
}
