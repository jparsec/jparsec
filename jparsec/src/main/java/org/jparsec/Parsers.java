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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;

import org.jparsec.functors.Map3;
import org.jparsec.functors.Map4;
import org.jparsec.functors.Map5;
import org.jparsec.functors.Map6;
import org.jparsec.functors.Map7;
import org.jparsec.functors.Map8;
import org.jparsec.functors.Pair;
import org.jparsec.functors.Tuple3;
import org.jparsec.functors.Tuple4;
import org.jparsec.functors.Tuple5;
import org.jparsec.internal.annotations.Private;
import org.jparsec.internal.util.Lists;

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
      @Override public Object map(Token tok) {
        return tok.value();
      }
      @Override public String toString() {
        return "any token";
      }
    });
  
  /**
   * A {@link Parser} that retrieves the current index in the source.
   *
   * @deprecated Use {@link #SOURCE_LOCATION} instead.
   */
  @Deprecated
  public static final Parser<Integer> INDEX = new Parser<Integer>() {
    @Override boolean apply(ParseContext ctxt) {
      ctxt.result = ctxt.getIndex();
      return true;
    }
    @Override public String toString() {
      return "getIndex";
    }
  };
  
  /**
   * A {@link Parser} that returns the current location in the source.
   *
   * <p>Because {@link SourceLocation#getLine} and {@link SourceLocation#getColumn} take amortized
   * {@code log(n)} time, it's more efficient to avoid calling them until the entire source has been
   * parsed successfully. In other words, avoid {@code SOURCE_LOCATION.map(SourceLocation::getLine)} or
   * anything similar.
   *
   * <p>{@code SourceLocation#getIndex} can be called any time.
   *
   * @since 3.1
   */
  public static final Parser<SourceLocation> SOURCE_LOCATION = new Parser<SourceLocation>() {
    @Override boolean apply(ParseContext ctxt) {
      ctxt.result = new SourceLocation(ctxt.getIndex(), ctxt.locator);
      return true;
    }
    @Override public String toString() {
      return "SOURCE_LOCATION";
    }
  };
  
  @SuppressWarnings("rawtypes")
  private static final Parser ALWAYS = constant(null);
  
  @SuppressWarnings("rawtypes")
  private static final Parser NEVER = new Parser<Object>() {
    @Override boolean apply(ParseContext ctxt) {
      return false;
    }
    @Override public String toString() {
      return "never";
    }
  };
  
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
  static Parser<?> eof(final String message) {
    return new Parser<Object>() {
      @Override boolean apply(ParseContext ctxt) {
        if (ctxt.isEof()) return true;
        ctxt.missing(message);
        return false;
      }
      
      @Override public String toString() {
        return message;
      }
    };
  }

  /** A {@link Parser} that always fails with {@code message}. */
  public static <T> Parser<T> fail(final String message) {
    return new Parser<T>() {
      @Override boolean apply(ParseContext ctxt) {
        ctxt.fail(message);
        return false;
      }
      
      @Override public String toString() {
        return message;
      }
    };
  }
  
  /**
   * A {@link Parser} that always succeeds and invokes {@code runnable}.
   */
  @Deprecated
  public static Parser<?> runnable(final Runnable runnable) {
    return new Parser<Object>() {      
      @Override boolean apply(ParseContext ctxt) {
        runnable.run();
        return true;
      }
      
      @Override public String toString() {
        return runnable.toString();
      }
    };
  }

  /** Converts a parser of a collection of {@link Token} to a parser of an array of {@code Token}.*/
  static Parser<Token[]> tokens(final Parser<? extends Collection<Token>> parser) {
    return parser.map(list -> list.toArray(new Token[list.size()]));
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
  static <T> Parser<T> nested(final Parser<Token[]> lexer, final Parser<? extends T> parser) {
    return new Parser<T>() {
      @Override boolean apply(ParseContext ctxt) {
        if (!lexer.apply(ctxt)) return false;
        Token[] tokens = lexer.getReturn(ctxt);
        ParserState parserState = new ParserState(
            ctxt.module, ctxt.source, tokens, 0, ctxt.locator, ctxt.getIndex(), tokens);
        ctxt.getTrace().startFresh(parserState);
        return ctxt.applyNested(parser, parserState);
      }
      
      @Override public String toString() {
        return parser.toString();
      }
    };
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
   *
   * @deprecated Prefer to converting to your own object with a lambda.
   */
  @Deprecated
  public static <A,B> Parser<Pair<A,B>> pair(Parser<? extends A> p1, Parser<? extends B> p2) {
    return sequence(p1, p2, Pair::new);
  }

  /**
   * A {@link Parser} that sequentially runs {@code p1} and {@code p2} and collects the results in a
   * {@link Pair} object. Is equivalent to {@link #pair(Parser, Parser)}.
   *
   * @deprecated Prefer to converting to your own object with a lambda.
   */
  @Deprecated
  public static <A,B> Parser<Pair<A,B>> tuple(Parser<? extends A> p1, Parser<? extends B> p2) {
    return pair(p1, p2);
  }

  /**
   * A {@link Parser} that sequentially runs 3 parser objects and collects the results in a
   * {@link Tuple3} object.
   *
   * @deprecated Prefer to converting to your own object with a lambda.
   */
  @Deprecated
  public static <A,B,C> Parser<Tuple3<A,B,C>> tuple(
      Parser<? extends A> p1, Parser<? extends B> p2, Parser<? extends C> p3) {
    return sequence(p1, p2, p3, Tuple3::new);
  }

  /**
   * A {@link Parser} that sequentially runs 4 parser objects and collects the results in a
   * {@link Tuple4} object.
   *
   * @deprecated Prefer to converting to your own object with a lambda.
   */
  @Deprecated
  public static <A,B,C,D> Parser<Tuple4<A,B,C,D>> tuple(
      Parser<? extends A> p1, Parser<? extends B> p2,
      Parser<? extends C> p3, Parser<? extends D> p4) {
    return sequence(p1, p2, p3, p4, Tuple4::new);
  }

  /**
   * A {@link Parser} that sequentially runs 5 parser objects and collects the results in a
   * {@link Tuple5} object.
   *
   * @deprecated Prefer to converting to your own object with a lambda.
   */
  @Deprecated
  public static <A,B,C,D,E> Parser<Tuple5<A,B,C,D,E>> tuple(
      Parser<? extends A> p1, Parser<? extends B> p2, Parser<? extends C> p3,
      Parser<? extends D> p4, Parser<? extends E> p5) {
    return sequence(p1, p2, p3, p4, p5, Tuple5::new);
  }
  
  /**
   * A {@link Parser} that sequentially runs {@code parsers} one by one and collects the return
   * values in an array.
   */
  public static Parser<Object[]> array(final Parser<?>... parsers) {
    return new Parser<Object[]>() {
      @Override boolean apply(ParseContext ctxt) {
        Object[] ret = new Object[parsers.length];
        for (int i = 0; i < parsers.length; i++) {
          Parser<?> parser = parsers[i];
          if (!parser.apply(ctxt)) return false;
          ret[i] = parser.getReturn(ctxt);
        }
        ctxt.result = ret;
        return true;
      }
      
      @Override public String toString() {
        return "array";
      }
    };
  }
  
  /**
   * A {@link Parser} that sequentially runs {@code parsers} one by one and collects the return
   * values in a {@link List}.
   */
  public static <T> Parser<List<T>> list(Iterable<? extends Parser<? extends T>> parsers) {
    final Parser<? extends T>[] array = toArray(parsers);
    return new Parser<List<T>>() {
      @Override boolean apply(ParseContext ctxt) {
        ArrayList<T> list = Lists.arrayList(array.length);
        for (Parser<? extends T> parser : array) {
          if (!parser.apply(ctxt)) return false;
          list.add(parser.getReturn(ctxt));
        }
        ctxt.result = list;
        return true;
      }
      
      @Override public String toString() {
        return "list";
      }
    };
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
      final Parser<A> p1, final Parser<B> p2, final BiFunction<? super A, ? super B, ? extends T> map) {
    return new Parser<T>() {
      @Override boolean apply(ParseContext ctxt) {
        boolean r1 = p1.apply(ctxt);
        if (!r1) return false;
        A o1 = p1.getReturn(ctxt);
        boolean r2 = p2.apply(ctxt);
        if (!r2) return false;
        B o2 = p2.getReturn(ctxt);
        ctxt.result = map.apply(o1, o2);
        return true;
      }
      @Override public String toString() {
        return map.toString();
      }
    };
  }

  /**
   * A {@link Parser} that runs 3 parser objects sequentially and transforms the return values
   * using {@code map}.
   */
  public static <A, B, C, T> Parser<T> sequence(
      final Parser<A> p1, final Parser<B> p2, final Parser<C> p3,
      final Map3<? super A, ? super B, ? super C, ? extends T> map) {
    return new Parser<T>() {
      @Override boolean apply(ParseContext ctxt) {
        boolean r1 = p1.apply(ctxt);
        if (!r1) return false;
        A o1 = p1.getReturn(ctxt);
        boolean r2 = p2.apply(ctxt);
        if (!r2) return false;
        B o2 = p2.getReturn(ctxt);
        boolean r3 = p3.apply(ctxt);
        if (!r3) return false;
        C o3 = p3.getReturn(ctxt);
        ctxt.result = map.map(o1, o2, o3);
        return true;
      }
      @Override public String toString() {
        return map.toString();
      }
    };
  }

  /**
   * A {@link Parser} that runs 4 parser objects sequentially and transforms the return values
   * using {@code map}.
   */
  public static <A, B, C, D, T> Parser<T> sequence(
      final Parser<A> p1, final Parser<B> p2, final Parser<C> p3, final Parser<D> p4,
      final Map4<? super A, ? super B, ? super C, ? super D, ? extends T> map) {
    return new Parser<T>() {
      @Override boolean apply(ParseContext ctxt) {
        boolean r1 = p1.apply(ctxt);
        if (!r1) return false;
        A o1 = p1.getReturn(ctxt);
        boolean r2 = p2.apply(ctxt);
        if (!r2) return false;
        B o2 = p2.getReturn(ctxt);
        boolean r3 = p3.apply(ctxt);
        if (!r3) return false;
        C o3 = p3.getReturn(ctxt);
        boolean r4 = p4.apply(ctxt);
        if (!r4) return false;
        D o4 = p4.getReturn(ctxt);
        ctxt.result = map.map(o1, o2, o3, o4);
        return true;
      }
      @Override public String toString() {
        return map.toString();
      }
    };
  }

  /** 
   * A {@link Parser} that runs 5 parser objects sequentially and transforms the return values
   * using {@code map}.
   */
  public static <A, B, C, D, E, T> Parser<T> sequence(
      final Parser<A> p1, final Parser<B> p2, final Parser<C> p3, final Parser<D> p4, final Parser<E> p5,
      final Map5<? super A, ? super B, ? super C, ? super D, ? super E, ? extends T> map) {
    return new Parser<T>() {
      @Override boolean apply(ParseContext ctxt) {
        boolean r1 = p1.apply(ctxt);
        if (!r1) return false;
        A o1 = p1.getReturn(ctxt);
        boolean r2 = p2.apply(ctxt);
        if (!r2) return false;
        B o2 = p2.getReturn(ctxt);
        boolean r3 = p3.apply(ctxt);
        if (!r3) return false;
        C o3 = p3.getReturn(ctxt);
        boolean r4 = p4.apply(ctxt);
        if (!r4) return false;
        D o4 = p4.getReturn(ctxt);
        boolean r5 = p5.apply(ctxt);
        if (!r5) return false;
        E o5 = p5.getReturn(ctxt);
        ctxt.result = map.map(o1, o2, o3, o4, o5);
        return true;
      }
      @Override public String toString() {
        return map.toString();
      }
    };
  }

  /** 
   * A {@link Parser} that runs 6 parser objects sequentially and transforms the return values
   * using {@code map}.
   *
   * @since 3.0
   */
  public static <A, B, C, D, E, F, T> Parser<T> sequence(
      final Parser<A> p1, final Parser<B> p2, final Parser<C> p3,
      final Parser<D> p4, final Parser<E> p5, final Parser<F> p6,
      final Map6<? super A, ? super B, ? super C, ? super D, ? super E, ? super F, ? extends T> map) {
    return new Parser<T>() {
      @Override boolean apply(ParseContext ctxt) {
        boolean r1 = p1.apply(ctxt);
        if (!r1) return false;
        A o1 = p1.getReturn(ctxt);
        boolean r2 = p2.apply(ctxt);
        if (!r2) return false;
        B o2 = p2.getReturn(ctxt);
        boolean r3 = p3.apply(ctxt);
        if (!r3) return false;
        C o3 = p3.getReturn(ctxt);
        boolean r4 = p4.apply(ctxt);
        if (!r4) return false;
        D o4 = p4.getReturn(ctxt);
        boolean r5 = p5.apply(ctxt);
        if (!r5) return false;
        E o5 = p5.getReturn(ctxt);
        boolean r6 = p6.apply(ctxt);
        if (!r6) return false;
        F o6 = p6.getReturn(ctxt);
        ctxt.result = map.map(o1, o2, o3, o4, o5, o6);
        return true;
      }
      @Override public String toString() {
        return map.toString();
      }
    };
  }

  /** 
   * A {@link Parser} that runs 7 parser objects sequentially and transforms the return values
   * using {@code map}.
   *
   * @since 3.0
   */
  public static <A, B, C, D, E, F, G, T> Parser<T> sequence(
      final Parser<A> p1, final Parser<B> p2, final Parser<C> p3,
      final Parser<D> p4, final Parser<E> p5, final Parser<F> p6, final Parser<G> p7,
      final Map7<? super A, ? super B, ? super C, ? super D, ? super E, ? super F, ? super G, ? extends T> map) {
    return new Parser<T>() {
      @Override boolean apply(ParseContext ctxt) {
        boolean r1 = p1.apply(ctxt);
        if (!r1) return false;
        A o1 = p1.getReturn(ctxt);
        boolean r2 = p2.apply(ctxt);
        if (!r2) return false;
        B o2 = p2.getReturn(ctxt);
        boolean r3 = p3.apply(ctxt);
        if (!r3) return false;
        C o3 = p3.getReturn(ctxt);
        boolean r4 = p4.apply(ctxt);
        if (!r4) return false;
        D o4 = p4.getReturn(ctxt);
        boolean r5 = p5.apply(ctxt);
        if (!r5) return false;
        E o5 = p5.getReturn(ctxt);
        boolean r6 = p6.apply(ctxt);
        if (!r6) return false;
        F o6 = p6.getReturn(ctxt);
        boolean r7 = p7.apply(ctxt);
        if (!r7) return false;
        G o7 = p7.getReturn(ctxt);
        ctxt.result = map.map(o1, o2, o3, o4, o5, o6, o7);
        return true;
      }
      @Override public String toString() {
        return map.toString();
      }
    };
  }

  /** 
   * A {@link Parser} that runs 7 parser objects sequentially and transforms the return values
   * using {@code map}.
   *
   * @since 3.0
   */
  public static <A, B, C, D, E, F, G, H, T> Parser<T> sequence(
      final Parser<A> p1, final Parser<B> p2, final Parser<C> p3, final Parser<D> p4,
      final Parser<E> p5, final Parser<F> p6, final Parser<G> p7, final Parser<H> p8,
      final Map8<? super A, ? super B, ? super C, ? super D, ? super E, ? super F, ? super G, ? super H, ? extends T> map) {
    return new Parser<T>() {
      @Override boolean apply(ParseContext ctxt) {
        boolean r1 = p1.apply(ctxt);
        if (!r1) return false;
        A o1 = p1.getReturn(ctxt);
        boolean r2 = p2.apply(ctxt);
        if (!r2) return false;
        B o2 = p2.getReturn(ctxt);
        boolean r3 = p3.apply(ctxt);
        if (!r3) return false;
        C o3 = p3.getReturn(ctxt);
        boolean r4 = p4.apply(ctxt);
        if (!r4) return false;
        D o4 = p4.getReturn(ctxt);
        boolean r5 = p5.apply(ctxt);
        if (!r5) return false;
        E o5 = p5.getReturn(ctxt);
        boolean r6 = p6.apply(ctxt);
        if (!r6) return false;
        F o6 = p6.getReturn(ctxt);
        boolean r7 = p7.apply(ctxt);
        if (!r7) return false;
        G o7 = p7.getReturn(ctxt);
        boolean r8 = p8.apply(ctxt);
        if (!r8) return false;
        H o8 = p8.getReturn(ctxt);
        ctxt.result = map.map(o1, o2, o3, o4, o5, o6, o7, o8);
        return true;
      }
      @Override public String toString() {
        return map.toString();
      }
    };
  }
  
  /** A {@link Parser} that runs {@code parsers} sequentially and discards the return values. */
  public static Parser<Object> sequence(final Parser<?>... parsers) {
    return new Parser<Object>() {
      @Override boolean apply(ParseContext ctxt) {
        for (Parser<?> p : parsers) {
          if (!p.apply(ctxt)) return false;
        }
        return true;
      }
      @Override public String toString() {
        return "sequence";
      }
    };
  }
  
  /** A {@link Parser} that runs {@code parsers} sequentially and discards the return values. */
  public static Parser<Object> sequence(Iterable<? extends Parser<?>> parsers) {
    return sequence(toArray(parsers));
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
  public static <T> Parser<T> or(final Parser<? extends T>... alternatives) {
    if (alternatives.length == 0) return never();
    if (alternatives.length == 1) return alternatives[0].cast();
    return new Parser<T>() {
      @Override boolean apply(ParseContext ctxt) {
        final Object result = ctxt.result;
        final int at = ctxt.at;
        final int step = ctxt.step;
        for(Parser<? extends T> p : alternatives) {
          if (p.apply(ctxt)) {
            return true;
          }
          ctxt.set(step, at, result);
        }
        return false;
      }
      @Override public String toString() {
        return "or";
      }
    };
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
    return new BestParser<T>(parsers, IntOrder.GT);
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
    return new BestParser<T>(parsers, IntOrder.LT);
  }

  /**
   * A {@link Parser} that runs every element of {@code parsers} and selects the shortest match.
   * If two matches have the same length, the first one is favored.
   */
  public static <T> Parser<T> shortest(Iterable<? extends Parser<? extends T>> parsers) {
    return shortest(toArray(parsers));
  }
  
  /** A {@link Parser} that fails and reports that {@code name} is logically expected. */
  public static <T> Parser<T> expect(final String name) {
    return new Parser<T>() {
      @Override boolean apply(ParseContext ctxt) {
        ctxt.expected(name);
        return false;
      }
      
      @Override public String toString() {
        return name;
      }
    };
  }

  /** A {@link Parser} that fails and reports that {@code name} is logically unexpected. */
  public static <T> Parser<T> unexpected(final String name) {
    return new Parser<T>() {
      @Override boolean apply(final ParseContext ctxt) {
        ctxt.unexpected(name);
        return false;
      }
      @Override public String toString() {
        return name;
      }
    };
  }

  /**
   * Checks the current token with the {@code fromToken} object. If the
   * {@link TokenMap#map(Token)} method returns null, an unexpected token error occurs;
   * if the method returns a non-null value, the value is returned and the parser succeeds.
   * 
   * @param fromToken the {@code FromToken} object.
   * @return the new Parser object.
   */
  public static <T> Parser<T> token(final TokenMap<? extends T> fromToken) {
    return new Parser<T>() {
      @Override boolean apply(final ParseContext ctxt) {
        if (ctxt.isEof()) {
          ctxt.missing(fromToken);
          return false;
        }
        Token token = ctxt.getToken();
        Object v = fromToken.map(token);
        if (v == null) {
          ctxt.missing(fromToken);
          return false;
        }
        ctxt.result = v;
        ctxt.next();
        return true;
      }
      
      @Override public String toString() {
        return fromToken.toString();
      }
    };
  }

  /**
   * Checks whether the current token value is of {@code type}, in which case, the token value is
   * returned and parse succeeds.
   * 
   * @param type the expected token value type.
   * @param name the name of what's logically expected.
   * @return the new Parser object.
   */
  public static <T> Parser<T> tokenType(final Class<? extends T> type, final String name) {
    return token(new TokenMap<T>() {
      @Override public T map(Token token) {
        if (type.isInstance(token.value())) {
          return type.cast(token.value());
        }
        return null;
      }
      @Override public String toString() {
        return name;
      }
    });
  }

  @Private static <T> Parser<T>[] toArrayWithIteration(
      Iterable<? extends Parser<? extends T>> parsers) {
    ArrayList<Parser<? extends T>> list = Lists.arrayList();
    for (Parser<? extends T> parser : parsers) {
      list.add(parser);
    }
    return toArray(list);
  }

  /**
   * We always convert {@link Iterable} to an array to avoid the cost of creating
   * a new {@Link java.util.Iterator} object each time the parser runs.
   */
  @SuppressWarnings({"unchecked", "rawtypes"})
  @Private static <T> Parser<T>[] toArray(Iterable<? extends Parser<? extends T>> parsers) {
    if (parsers instanceof Collection<?>) {
      return toArray((Collection) parsers);
    }
    return toArrayWithIteration(parsers);
  }

  @SuppressWarnings("unchecked")
  private static <T> Parser<T>[] toArray(Collection<? extends Parser<? extends T>> parsers) {
    return parsers.toArray(new Parser[parsers.size()]);
  }

  private Parsers() {}
}
