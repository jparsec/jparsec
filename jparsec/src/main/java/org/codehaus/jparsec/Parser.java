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
package org.codehaus.jparsec;

import org.codehaus.jparsec.error.ParserException;
import org.codehaus.jparsec.functors.Map;
import org.codehaus.jparsec.functors.Map2;
import org.codehaus.jparsec.functors.Maps;
import org.codehaus.jparsec.internal.annotations.Private;
import org.codehaus.jparsec.internal.util.Checks;

import java.io.IOException;
import java.nio.CharBuffer;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.codehaus.jparsec.internal.util.Checks.checkArgument;

/**
 * Defines grammar and encapsulates parsing logic. A {@link Parser} takes as input a
 * {@link CharSequence} source and parses it when the {@link #parse(CharSequence)} method is called.
 * A value of type {@code T} will be returned if parsing succeeds, or a {@link ParserException}
 * is thrown to indicate parsing error. For example: <pre>   {@code
 *   Parser<String> scanner = Scanners.IDENTIFIER;
 *   assertEquals("foo", scanner.parse("foo"));
 * }</pre>
 *
 * <p> {@code Parser}s run either on character level to scan the source, or on token level to parse
 * a list of {@link Token} objects returned from another parser. This other parser that returns the
 * list of tokens for token level parsing is hooked up via the {@link #from(Parser, Parser)}
 * or {@link #from(Parser)} method.
 *
 * <p>The following are important naming conventions used throughout the library:
 *
 * <ul>
 * <li>A character level parser object that recognizes a single lexical word is called a scanner.
 * <li>A scanner that translates the recognized lexical word into a token is called a tokenizer.
 * <li>A character level parser object that does lexical analysis and returns a list of
 *     {@link Token} is called a lexer.
 * <li>All {@code index} parameters are 0-based indexes in the original source.
 * </ul>
 *
 * To debug a complex parser that fails in un-obvious way, pass {@link Mode#DEBUG} mode to
 * {@link #parse(CharSequence, Mode)} and inspect the result in
 * {@link ParserException#getParseTree()}. All {@link #label labeled} parsers will generate a node
 * in the exception's parse tree, with matched indices in the source.
 *
 * @author Ben Yu
 */
public abstract class Parser<T> {

  /**
   * An atomic mutable reference to {@link Parser} used in recursive grammars.
   *
   * <p>For example, the following is a recursive grammar for a simple calculator: <pre>   {@code
   *   Terminals terms = Terminals.operators("(", ")", "+", "-");
   *   Parser.Reference<Integer> ref = Parser.newReference();
   *   Parser<Integer> literal = Terminals.IntegerLiteral.PARSER.map(new Map<String, Integer>() {
   *      ...
   *      return Integer.parseInt(s);
   *   });
   *   Parser.Reference<Integer> parenthesized =  // recursion in rule E = (E)
   *       Parsers.between(terms.token("("), ref.lazy(), terms.token(")"));
   *   ref.set(new OperatorTable()
   *       .infixl(terms.token("+").retn(plus), 10)
   *       .infixl(terms.token("-").retn(minus), 10)
   *       .build(literal.or(parenthesized)));
   *   return ref.get();
   * }</pre>
   * Note that a left recursive grammar will result in {@code StackOverflowError}.
   * Use appropriate parser built-in parser combinators to avoid left-recursion.
   * For instance, many left recursive grammar rules can be thought as logically equivalent to
   * postfix operator rules. In such case, either {@link OperatorTable} or {@link Parser#postfix}
   * can be used to work around left recursion.
   * The following is a left recursive parser for array types in the form of "T[]" or "T[][]":
   * <pre>   {@code
   *   Terminals terms = Terminals.operators("[", "]");
   *   Parser.Reference<Type> ref = Parser.newReference();
   *   ref.set(Parsers.or(leafTypeParser,
   *       Parsers.sequence(ref.lazy(), terms.phrase("[", "]"), new Unary<Type>() {...})));
   *   return ref.get();
   * }</pre>
   * And it will fail. A correct implementation is:  <pre>   {@code
   *   Terminals terms = Terminals.operators("[", "]");
   *   return leafTypeParer.postfix(terms.phrase("[", "]").retn(new Unary<Type>() {...}));
   * }</pre>
   * A not-so-obvious example, is to parse the {@code expr ? a : b} ternary operator. It too is a
   * left recursive grammar. And un-intuitively it can also be thought as a postfix operator.
   * Basically, we can parse "? a : b" as a whole into a unary operator that accepts the condition
   * expression as input and outputs the full ternary expression: <pre>   {@code
   *   Parser<Expr> ternary(Parser<Expr> expr) {
   *     return expr.postfix(
   *       Parsers.sequence(terms.token("?"), expr, terms.token(":"), expr,
   *       new Map4<...>() {
   *         public Unary<Expr> map(unused, consequence, unused, alternative) {
   *           // (condition) -> Ternary(condition, consequence, alternative)
   *           return new Unary<Expr>() {
   *             ...
   *             return new TernaryExpr(condition, consequence, alternative);
   *           }
   *         }
   *       }));
   *   }
   * }</pre>
   */
  @SuppressWarnings("serial")
  public static final class Reference<T> extends AtomicReference<Parser<T>> {
    private final Parser<T> lazy = new Parser<T>() {
      @Override boolean apply(ParseContext ctxt) {
        return deref().apply(ctxt);
      }
      private Parser<T> deref() {
        Parser<T> p = get();
        Checks.checkNotNullState(p,
            "Uninitialized lazy parser reference. Did you forget to call set() on the reference?");
        return p;
      }
      @Override public String toString() {
        return "lazy";
      }
    };

    /**
     * A {@link Parser} that delegates to the parser object referenced by {@code this} during parsing time.
     */
    public Parser<T> lazy() {
      return lazy;
    }
  }

  Parser() {}

  /**
   * Creates a new instance of {@link Reference}.
   * Used when your grammar is recursive (many grammars are).
   */
  public static <T> Reference<T> newReference() {
    return new Reference<T>();
  }

  /**
   * A {@link Parser} that executes {@code this}, and returns {@code value} if succeeds.
   */
  public final <R> Parser<R> retn(R value) {
    return next(Parsers.constant(value));
  }

  /**
   * A {@link Parser} that sequentially executes {@code this} and then {@code parser}. The return value of {@code
   * parser} is preserved.
   */
  public final <R> Parser<R> next(Parser<R> parser) {
    return Parsers.sequence(this, parser);
  }

  /**
   * A {@link Parser} that executes {@code this}, maps the result using {@code map} to another {@code Parser} object
   * to be executed as the next step.
   */
  public final <To> Parser<To> next(Map<? super T, ? extends Parser<? extends To>> map) {
    return new BindNextParser<T, To>(this, map);
  }

  /**
   * A {@link Parser} that matches this parser zero or many times
   * until the given parser succeeds. The input that matches the given parser
   * will not be consumed. The input that matches this parser will
   * be collected in a list that will be returned by this function.
   */
  public final Parser<List<T>> until(Parser<?> parser) {
    return parser.not().next(this).many().followedBy(parser.peek());
  }

  /**
   * A {@link Parser} that sequentially executes {@code this} and then {@code parser}, whose return value is ignored.
   */
  public final Parser<T> followedBy(Parser<?> parser) {
    return Parsers.sequence(this, parser, InternalFunctors.<T, Object>firstOfTwo());
  }

  /**
   * A {@link Parser} that succeeds if {@code this} succeeds and the pattern recognized by {@code parser} isn't
   * following.
   */
  public final Parser<T> notFollowedBy(Parser<?> parser) {
    return followedBy(parser.not());
  }

  /**
   * {@code p.many()} is equivalent to {@code p*} in EBNF. The return values are collected and returned in a {@link
   * List}.
   */
  public final Parser<List<T>> many() {
    return atLeast(0);
  }

  /**
   * {@code p.skipMany()} is equivalent to {@code p*} in EBNF. The return values are discarded.
   */
  public final Parser<Void> skipMany() {
    return skipAtLeast(0);
  }

  /**
   * {@code p.many1()} is equivalent to {@code p+} in EBNF. The return values are collected and returned in a {@link
   * List}.
   */
  public final Parser<List<T>> many1() {
    return atLeast(1);
  }

  /**
   * {@code p.skipMany1()} is equivalent to {@code p+} in EBNF. The return values are discarded.
   */
  public final Parser<Void> skipMany1() {
    return skipAtLeast(1);
  }

  /**
   * A {@link Parser} that runs {@code this} parser greedily for at least {@code min} times. The return values are
   * collected and returned in a {@link List}.
   */
  public final Parser<List<T>> atLeast(int min) {
    return new RepeatAtLeastParser<T>(this, Checks.checkMin(min));
  }

  /**
   * A {@link Parser} that runs {@code this} parser greedily for at least {@code min} times and ignores the return
   * values.
   */
  public final Parser<Void> skipAtLeast(int min) {
    return new SkipAtLeastParser(this, Checks.checkMin(min));
  }

  /**
   * A {@link Parser} that sequentially runs {@code this} for {@code n} times and ignores the return values.
   */
  public final Parser<Void> skipTimes(int n) {
    return skipTimes(n, n);
  }

  /**
   * A {@link Parser} that runs {@code this} for {@code n} times and collects the return values in a {@link List}.
   */
  public final Parser<List<T>> times(int n) {
    return times(n, n);
  }

  /**
   * A {@link Parser} that runs {@code this} parser for at least {@code min} times and up to {@code max} times. The
   * return values are collected and returned in {@link List}.
   */
  public final Parser<List<T>> times(int min, int max) {
    Checks.checkMinMax(min, max);
    return new RepeatTimesParser<T>(this, min, max);
  }

  /**
   * A {@link Parser} that runs {@code this} parser for at least {@code min} times and up to {@code max} times, with
   * all the return values ignored.
   */
  public final Parser<Void> skipTimes(int min, int max) {
    Checks.checkMinMax(min, max);
    return new SkipTimesParser(this, min, max);
  }

  /**
   * A {@link Parser} that runs {@code this} parser and transforms the return value using {@code map}.
   */
  public final <R> Parser<R> map(Map<? super T, ? extends R> map) {
    return new MapParser<T, R>(this, map);
  }

  /**
   * {@code p1.or(p2)} is equivalent to {@code p1 | p2} in EBNF.
   *
   * @param alternative the alternative parser to run if this fails.
   */
  public final Parser<T> or(Parser<? extends T> alternative) {
    return Parsers.or(this, alternative);
  }

  /**
   * {@code p.optional()} is equivalent to {@code p?} in EBNF. {@code null} is the result when {@code this} fails with
   * no partial match.
   */
  public final Parser<T> optional() {
    return Parsers.plus(this, Parsers.<T>always());
  }

  /**
   * A {@link Parser} that returns {@code defaultValue} if {@code this} fails with no partial match.
   */
  public final Parser<T> optional(T defaultValue) {
    return Parsers.plus(this, Parsers.constant(defaultValue));
  }

  /**
   * A {@link Parser} that fails if {@code this} succeeds. Any input consumption is undone.
   */
  public final Parser<?> not() {
    return not(toString());
  }

  /**
   * A {@link Parser} that fails if {@code this} succeeds. Any input consumption is undone.
   *
   * @param unexpected the name of what we don't expect.
   */
  public final Parser<?> not(String unexpected) {
    return peek().ifelse(Parsers.unexpected(unexpected), Parsers.always());
  }

  /**
   * A {@link Parser} that runs {@code this} and undoes any input consumption if succeeds.
   */
  public final Parser<T> peek() {
    return new PeekParser<T>(this);
  }

  /**
   * A {@link Parser} that undoes any partial match if {@code this} fails.
   */
  public final Parser<T> atomic() {
    return new AtomicParser<T>(this);
  }

  /**
   * A {@link Parser} that returns {@code true} if {@code this} succeeds, {@code false} otherwise.
   */
  public final Parser<Boolean> succeeds() {
    return ifelse(Parsers.TRUE, Parsers.FALSE);
  }

  /**
   * A {@link Parser} that returns {@code true} if {@code this} fails, {@code false} otherwise.
   */
  public final Parser<Boolean> fails() {
    return ifelse(Parsers.FALSE, Parsers.TRUE);
  }

  /**
   * A {@link Parser} that runs {@code consequence} if {@code this} succeeds, or {@code alternative} otherwise.
   */
  public final <R> Parser<R> ifelse(Parser<? extends R> consequence, Parser<? extends R> alternative) {
    return ifelse(Maps.constant(consequence), alternative);
  }

  /**
   * A {@link Parser} that runs {@code consequence} if {@code this} succeeds, or {@code alternative} otherwise.
   */
  public final <R> Parser<R> ifelse(
      Map<? super T, ? extends Parser<? extends R>> consequence, Parser<? extends R> alternative) {
    return new IfElseParser<R, T>(this, consequence, alternative);
  }

  /**
   * A {@link Parser} that reports reports an error about {@code name} expected, if {@code this} fails with no partial
   * match.
   */
  public Parser<T> label(String name) {
    return new LabeledParser<T>(this, name);
  }

  /**
   * Casts {@code this} to a {@link Parser} of type {@code R}. Use it only if you know the parser actually returns
   * value of type {@code R}.
   */
  @SuppressWarnings("unchecked")
  public final <R> Parser<R> cast() {
    return (Parser<R>) this;
  }

  /**
   * A {@link Parser} that runs {@code this} between {@code before} and {@code after}. The return value of {@code
   * this} is preserved.
   *
   * <p>Equivalent to {@link Parsers#between(Parser, Parser, Parser)}, which preserves the natural order of the
   * parsers in the argument list, but is a bit more verbose.
   */
  public final Parser<T> between(Parser<?> before, Parser<?> after) {
    return before.next(followedBy(after));
  }
  
  /**
   * A {@link Parser} that first runs {@code before} from the input start, 
   * then runs {@code after} from the input's end, and only
   * then runs {@code this} on what's left from the input.
   * In effect, {@code this} behaves reluctantly, giving
   * {@code after} a chance to grab input that would have been consumed by {@code this}
   * otherwise.
   * @deprecated This method probably only works in the simplest cases. And it's a character-level
   * parser only. Use it at your own risk. It may be deleted later when we find a better way.
   */
  @Deprecated
  public final Parser<T> reluctantBetween(Parser<?> before, Parser<?> after) {
    return new ReluctantBetweenParser<T>(before, this, after);
  }

  /**
   * A {@link Parser} that runs {@code this} 1 or more times separated by {@code delim}.
   *
   * <p>The return values are collected in a {@link List}.
   */
  public final Parser<List<T>> sepBy1(Parser<?> delim) {
    final Parser<T> afterFirst = delim.step(0).next(this);
    Map<T, Parser<List<T>>> binder = new Map<T, Parser<List<T>>>() {
      @Override public Parser<List<T>> map(T firstValue) {
        return new RepeatAtLeastParser<T>(
            afterFirst, 0, ListFactory.arrayListFactoryWithFirstElement(firstValue));
      }
    };
    return next(binder);
  }

  /**
   * A {@link Parser} that runs {@code this} 0 or more times separated by {@code delim}.
   *
   * <p>The return values are collected in a {@link List}.
   */
  public final Parser<List<T>> sepBy(Parser<?> delim) {
    return Parsers.plus(sepBy1(delim), EmptyListParser.<T>instance());
  }

  /**
   * A {@link Parser} that runs {@code this} for 0 or more times delimited and terminated by {@code delim}.
   *
   * <p>The return values are collected in a {@link List}.
   */
  public final Parser<List<T>> endBy(Parser<?> delim) {
    return followedBy(delim).many();
  }

  /**
   * A {@link Parser} that runs {@code this} for 1 or more times delimited and terminated by {@code delim}.
   *
   * <p>The return values are collected in a {@link List}.
   */
  public final Parser<List<T>> endBy1(Parser<?> delim) {
    return followedBy(delim).many1();
  }

  /**
   * A {@link Parser} that runs {@code this} for 1 ore more times separated and optionally terminated by {@code
   * delim}. For example: {@code "foo;foo;foo"} and {@code "foo;foo;"} both matches {@code foo.sepEndBy1(semicolon)}.
   *
   * <p>The return values are collected in a {@link List}.
   */
  public final Parser<List<T>> sepEndBy1(final Parser<?> delim) {
    return next(new Map<T, Parser<List<T>>>() {
      @Override public Parser<List<T>> map(T first) {
        return new DelimitedListParser<T>(
            Parser.this, delim, ListFactory.arrayListFactoryWithFirstElement(first));
      }
    });
  }

  /**
   * A {@link Parser} that runs {@code this} for 0 ore more times separated and optionally terminated by {@code
   * delim}. For example: {@code "foo;foo;foo"} and {@code "foo;foo;"} both matches {@code foo.sepEndBy(semicolon)}.
   *
   * <p>The return values are collected in a {@link List}.
   */
  public final Parser<List<T>> sepEndBy(Parser<?> delim) {
    return Parsers.plus(sepEndBy1(delim), EmptyListParser.<T>instance());
  }

  /**
   * A {@link Parser} that runs {@code op} for 0 or more times greedily, then runs {@code this}. The {@link Map}
   * objects returned from {@code op} are applied from right to left to the return value of {@code p}.
   *
   * <p> {@code p.prefix(op)} is equivalent to {@code op* p} in EBNF.
   */
  @SuppressWarnings("unchecked")
  public final Parser<T> prefix(Parser<? extends Map<? super T, ? extends T>> op) {
    return Parsers.sequence(op.many(), this, Parsers.PREFIX_OPERATOR_MAP2);
  }

  /**
   * A {@link Parser} that runs {@code this} and then runs {@code op} for 0 or more times greedily.
   * The {@link Map} objects returned from {@code op} are applied from left to right to the return
   * value of p.
   *
   * <p>This is the preferred API to avoid {@code StackOverflowError} in left-recursive parsers.
   * For example, to parse array types in the form of "T[]" or "T[][]", the following
   * left recursive grammar will fail: <pre>   {@code
   *   Terminals terms = Terminals.operators("[", "]");
   *   Parser.Reference<Type> ref = Parser.newReference();
   *   ref.set(Parsers.or(leafTypeParser,
   *       Parsers.sequence(ref.lazy(), terms.phrase("[", "]"), new Unary<Type>() {...})));
   *   return ref.get();
   * }</pre>
   * A correct implementation is:  <pre>   {@code
   *   Terminals terms = Terminals.operators("[", "]");
   *   return leafTypeParer.postfix(terms.phrase("[", "]").retn(new Unary<Type>() {...}));
   * }</pre>
   * A not-so-obvious example, is to parse the {@code expr ? a : b} ternary operator. It too is a
   * left recursive grammar. And un-intuitively it can also be thought as a postfix operator.
   * Basically, we can parse "? a : b" as a whole into a unary operator that accepts the condition
   * expression as input and outputs the full ternary expression: <pre>   {@code
   *   Parser<Expr> ternary(Parser<Expr> expr) {
   *     return expr.postfix(
   *       Parsers.sequence(terms.token("?"), expr, terms.token(":"), expr,
   *       new Map4<...>() {
   *         public Unary<Expr> map(unused, consequence, unused, alternative) {
   *           // (condition) -> Ternary(condition, consequence, alternative)
   *           return new Unary<Expr>() {
   *             ...
   *             return new TernaryExpr(condition, consequence, alternative);
   *           }
   *         }
   *       }));
   *   }
   * }</pre>
   * {@link OperatorTable} also handles left recursion transparently.
   *
   * <p> {@code p.postfix(op)} is equivalent to {@code p op*} in EBNF.
   */
  @SuppressWarnings("unchecked")
  public final Parser<T> postfix(Parser<? extends Map<? super T, ? extends T>> op) {
    return Parsers.sequence(this, op.many(), Parsers.POSTFIX_OPERATOR_MAP2);
  }

  /**
   * A {@link Parser} that parses non-associative infix operator. Runs {@code this} for the left operand, and then
   * runs {@code op} and {@code this} for the operator and the right operand optionally. The {@link Map2} objects
   * returned from {@code op} are applied to the return values of the two operands, if any.
   *
   * <p> {@code p.infixn(op)} is equivalent to {@code p (op p)?} in EBNF.
   */
  public final Parser<T> infixn(Parser<? extends Map2<? super T, ? super T, ? extends T>> op) {
    return Parsers.infixn(this, op);
  }

  /**
   * A {@link Parser} for left-associative infix operator. Runs {@code this} for the left operand, and then runs
   * {@code op} and {@code this} for the operator and the right operand for 0 or more times greedily. The {@link Map2}
   * objects returned from {@code op} are applied from left to right to the return values of {@code this}, if any. For
   * example: {@code a + b + c + d} is evaluated as {@code (((a + b)+c)+d)}.
   *
   * <p> {@code p.infixl(op)} is equivalent to {@code p (op p)*} in EBNF.
   */
  public final Parser<T> infixl(Parser<? extends Map2<? super T, ? super T, ? extends T>> op) {
    // somehow generics doesn't work if we inline the code here.
    return Parsers.infixl(this, op);
  }

  /**
   * A {@link Parser} for right-associative infix operator. Runs {@code this} for the left operand, and then runs
   * {@code op} and {@code this} for the operator and the right operand for 0 or more times greedily. The {@link Map2}
   * objects returned from {@code op} are applied from right to left to the return values of {@code this}, if any. For
   * example: {@code a + b + c + d} is evaluated as {@code a + (b + (c + d))}.
   *
   * <p> {@code p.infixr(op)} is equivalent to {@code p (op p)*} in EBNF.
   */
  public final Parser<T> infixr(Parser<? extends Map2<? super T, ? super T, ? extends T>> op) {
    return Parsers.infixr(this, op);
  }

  /**
   * A {@link Parser} that runs {@code this} and wraps the return value in a {@link Token}.
   *
   * <p>It is normally not necessary to call this method explicitly. {@link #lexer(Parser)} and {@link #from(Parser,
   * Parser)} both do the conversion automatically.
   */
  public final Parser<Token> token() {
    return new ToTokenParser(this);
  }

  /**
   * A {@link Parser} that returns the matched string in the original source.
   */
  public final Parser<String> source() {
    return new ReturnSourceParser(this);
  }

  /**
   * A {@link Parser} that returns both parsed object and matched string.
   */
  public final Parser<WithSource<T>> withSource() {
    return new WithSourceParser<T>(this);
  }

  /**
   * A {@link Parser} that takes as input the {@link Token} collection returned by {@code lexer},
   * and runs {@code this} to parse the tokens. Most parsers should use the simpler
   * {@link #from(Parser, Parser)} instead.
   *
   * <p> {@code this} must be a token level parser.
   */
  public final Parser<T> from(Parser<? extends Collection<Token>> lexer) {
    return Parsers.nested(Parsers.tokens(lexer), followedBy(Parsers.EOF));
  }

  /**
   * A {@link Parser} that takes as input the tokens returned by {@code tokenizer} delimited by
   * {@code delim}, and runs {@code this} to parse the tokens. A common misunderstanding is that
   * {@code tokenizer} has to be a parser of {@link Token}. It doesn't need to be because
   * {@code Terminals} already takes care of wrapping your logical token objects into physical
   * {@code Token} with correct source location information tacked on for free. Your token object
   * can literally be anything, as long as your token level parser can recognize it later.
   *
   * <p>The following example uses {@code Terminals.tokenizer()}: <pre class="code">
   * Terminals terminals = ...;
   * return parser.from(terminals.tokenizer(), Scanners.WHITESPACES.optional()).parse(str);
   * </pre>
   * And tokens are optionally delimited by whitespaces.
   * <p>Optionally, you can skip comments using an alternative scanner than {@code WHITESPACES}:
   * <pre class="code">   {@code
   *   Terminals terminals = ...;
   *   Parser<?> delim = Parsers.or(
   *       Scanners.WHITESPACE,
   *       Scanners.JAVA_LINE_COMMENT,
   *       Scanners.JAVA_BLOCK_COMMENT).skipMany();
   *   return parser.from(terminals.tokenizer(), delim).parse(str);
   * }</pre>
   *
   * <p>In both examples, it's important to make sure the delimiter scanner can accept empty string
   * (either through {@link #optional} or {@link #skipMany}), unless adjacent operator
   * characters shouldn't be parsed as separate operators.
   * i.e. "((" as two left parenthesis operators.
   *
   * <p> {@code this} must be a token level parser.
   */
  public final Parser<T> from(Parser<?> tokenizer, Parser<Void> delim) {
    return from(tokenizer.lexer(delim));
  }

  /**
   * A {@link Parser} that greedily runs {@code this} repeatedly, and ignores the pattern recognized by {@code delim}
   * before and after each occurrence. The result tokens are wrapped in {@link Token} and are collected and returned
   * in a {@link List}.
   *
   * <p>It is normally not necessary to call this method explicitly. {@link #from(Parser, Parser)} is more convenient
   * for simple uses that just need to connect a token level parser with a lexer that produces the tokens. When more
   * flexible control over the token list is needed, for example, to parse indentation sensitive language, a
   * pre-processor of the token list may be needed.
   *
   * <p> {@code this} must be a tokenizer that returns a token value.
   */
  public Parser<List<Token>> lexer(Parser<?> delim) {
    return delim.optional().next(token().sepEndBy(delim));
  }

  /**
   * Parses {@code source}.
   */
  public final T parse(CharSequence source) {
    return parse(source, Mode.PRODUCTION);
  }

  /**
   * Parses source read from {@code readable}.
   */
  public final T parse(Readable readable) throws IOException {
    return parse(read(readable));
  }

  /**
   * Parses {@code source} under the given {@code mode}. For example: <pre>
   *   parser.parse(text, Mode.DEBUG);
   * </pre>
   *
   * @since 2.3
   */
  public final T parse(CharSequence source, Mode mode) {
    return mode.run(this, source);
  }

  /**
   * Parses {@code source} and returns a {@link ParseTree} corresponding to the syntactical
   * structure of the input. Only {@link #label labeled} parser nodes are represented in the parse
   * tree.
   *
   * <p>If the parse failed, {@link ParserException#getParseTree()} can be inspected for the parse
   * tree at error location.
   *
   * @since 2.3
   */
  public final ParseTree parseToTree(CharSequence source) {
    ScannerState state = new ScannerState(source);
    state.enableTrace("root");
    state.run(this.followedBy(Parsers.EOF));
    return state.buildParseTree();
  }

  /**
   * Defines the mode that a parser should be run in.
   *
   * @since 2.3
   */
  public enum Mode {
    /** Default mode. Used for production. */
    PRODUCTION {
      @Override <T> T run(Parser<T> parser, CharSequence source) {
        return new ScannerState(source).run(parser.followedBy(Parsers.EOF));
      }
    },

    /**
     * Debug mode. {@link ParserException#getParseTree} can be used to inspect partial parse result.
     */
    DEBUG {
      @Override <T> T run(Parser<T> parser, CharSequence source) {
        ScannerState state = new ScannerState(source);
        state.enableTrace("root");
        return state.run(parser.followedBy(Parsers.EOF));
      }
    }
    ;
    abstract <T> T run(Parser<T> parser, CharSequence source);
  }

  /**
   * Parses {@code source}.
   *
   * @param source     the source string
   * @param moduleName the name of the module, this name appears in error message
   * @return the result
   * @deprecated Please use {@link #parse(CharSequence)} instead.
   */
  @Deprecated
  public final T parse(CharSequence source, String moduleName) {
    return new ScannerState(moduleName, source, 0, new SourceLocator(source))
        .run(followedBy(Parsers.EOF));
  }

  /**
   * Parses source read from {@code readable}.
   *
   * @param readable   where the source is read from
   * @param moduleName the name of the module, this name appears in error message
   * @return the result
   * @deprecated Please use {@link #parse(Readable)} instead.
   */
  @Deprecated
  public final T parse(Readable readable, String moduleName) throws IOException {
    return parse(read(readable), moduleName);
  }
  
  abstract boolean apply(ParseContext ctxt);

  /**
   * Copies all content from {@code from} to {@code to}.
   */
  @Private
  static StringBuilder read(Readable from) throws IOException {
    StringBuilder builder = new StringBuilder();
    CharBuffer buf = CharBuffer.allocate(2048);
    for (; ; ) {
      int r = from.read(buf);
      if (r == -1) break;
      buf.flip();
      builder.append(buf, 0, r);
    }
    return builder;
  }

  /**
   * A {@link Parser} that runs {@code this} parser and sets the number of logical steps explicitly to {@code n}.
   */
  final Parser<T> step(int n) {
    checkArgument(n >= 0, "step < 0");
    return new StepParser<T>(this, n);
  }

  @SuppressWarnings("unchecked")
  final T getReturn(ParseContext ctxt) {
    return (T) ctxt.result;
  }
}
