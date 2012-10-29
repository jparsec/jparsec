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

import static org.codehaus.jparsec.util.Checks.checkArgument;

import java.io.IOException;
import java.nio.CharBuffer;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.codehaus.jparsec.annotations.Private;
import org.codehaus.jparsec.error.ParserException;
import org.codehaus.jparsec.functors.Map;
import org.codehaus.jparsec.functors.Map2;
import org.codehaus.jparsec.functors.Maps;
import org.codehaus.jparsec.util.Checks;

/**
 * Defines grammar and encapsulates parsing logic.
 * A {@link Parser} takes as input a {@link CharSequence} source and parses it when the
 * {@link #parse(CharSequence)} method is called. A value of type {@code T} will be returned if
 * parsing succeeds, or a {@link ParserException} is thrown to indicate parsing error. For example:
 * <pre>
 * Parser&lt;String> scanner = Scanners.IDENTIFIER;
 * assertEquals("foo", scanner.parse("foo"));
 * </pre>
 * 
 * <p> {@code Parser}s are immutable and inherently covariant on the type parameter {@code T}.
 * Because Java generics has no native support for covariant type parameter, a workaround is to use
 * the {@link Parser#cast()} method to explicitly force covariance whenever needed.
 * 
 * <p> {@code Parser}s run either on character level to scan the source, or on token level to parse
 * a list of {@link Token} objects returned from another parser. This other parser that returns the
 * list of tokens for token level parsing is hooked up via the {@link #from(Parser)} or
 * {@link #from(Parser, Parser)} method.
 * 
 * <p> The following are important naming conventions used throughout the library:
 * <ul>
 * <li> A character level parser object that recognizes a single lexical word is called a scanner.
 * <li> A scanner that translates the recognized lexical word into a token is called a tokenizer.
 * <li > A character level parser object that does lexical analysis and returns a list of
 * {@link Token} is called a lexer.
 * <li> All {@code index} parameters are 0-based indexes in the original source.
 * </ul>
 * 
 * @author Ben Yu
 */
public abstract class Parser<T> {

  Parser() {}
  
  /**
   * An atomic mutable reference to {@link Parser}. Is useful to work around circular dependency
   * between parser objects.
   * 
   * <p> Example usage: <pre>
   * Parser.Reference&lt;Foo> ref = Parser.newReference();
   * ...
   * Parser&lt;Bar> barParser = barParser(ref.lazy());
   * Parser&lt;Foo> fooParser = fooParser(barParser);
   * ref.set(fooParser);
   * </pre>
   */
  public static final class Reference<T> extends AtomicReference<Parser<T>> {
    private static final long serialVersionUID = -8778697271614979497L;
    
    private final Parser<T> lazy = new LazyParser<T>(this);
    
    /**
     * A {@link Parser} that delegates to the parser object referenced by {@code this}
     * during parsing time.
     */
    public Parser<T> lazy() {
      return lazy;
    }
  }
  
  /** Creates a new instance of {@link Reference}. */
  public static <T> Reference<T> newReference() {
    return new Reference<T>();
  }
  
  /** A {@link Parser} that executes {@code this}, and returns {@code value} if succeeds. */
  public final <R> Parser<R> retn(R value) {
    return next(Parsers.constant(value));
  }
  
  /**
   * A {@link Parser} that sequentially executes {@code this} and then {@code parser}.
   * The return value of {@code parser} is preserved.
   */
  public final <R> Parser<R> next(Parser<R> parser) {
    return Parsers.sequence(this, parser);
  }
  
  /**
   * A {@link Parser} that executes {@code this}, maps the result using {@code map}
   * to another {@code Parser} object to be executed as the next step.
   */
  public final <To> Parser<To> next(Map<? super T, ? extends Parser<? extends To>> map) {
    return new BindNextParser<T, To>(this, map);
  }
  
  /**
   * A {@link Parser} that sequentially executes {@code this} and then {@code parser},
   * whose return value is ignored.
   */
  public final Parser<T> followedBy(Parser<?> parser) {
    return Parsers.sequence(this, parser, InternalFunctors.<T, Object>firstOfTwo());
  }
  
  /**
   * A {@link Parser} that succeeds if {@code this} succeeds and the pattern recognized by
   * {@code parser} isn't following.
   */
  public final Parser<T> notFollowedBy(Parser<?> parser) {
    return followedBy(parser.not());
  }
  
  /**
   * {@code p.many()} is equivalent to {@code p*} in EBNF. The return values are collected and
   * returned in a {@link List}.
   */
  public final Parser<List<T>> many() {
    return atLeast(0);
  }
  
  /** {@code p.skipMany()} is equivalent to {@code p*} in EBNF. The return values are discarded. */
  public final Parser<Void> skipMany() {
    return skipAtLeast(0);
  }
  
  /**
   * {@code p.many1()} is equivalent to {@code p+} in EBNF. The return values are collected and
   * returned in a {@link List}.
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
   * A {@link Parser} that runs {@code this} parser greedily for at least {@code min} times.
   * The return values are collected and returned in a {@link List}.
   */
  public final Parser<List<T>> atLeast(int min) {
    return new RepeatAtLeastParser<T>(this, Checks.checkMin(min));
  }
  
  /**
   * A {@link Parser} that runs {@code this} parser greedily for at least {@code min} times
   * and ignores the return values.
   */
  public final Parser<Void> skipAtLeast(int min) {
    return new SkipAtLeastParser(this, Checks.checkMin(min));
  }
  
  /**
   * A {@link Parser} that sequentially runs {@code this} for {@code n} times and ignores the
   * return values.
   */
  public final Parser<Void> skipTimes(int n) {
    return skipTimes(n, n);
  }
  
  /**
   * A {@link Parser} that runs {@code this} for {@code n} times and collects the return values in a
   * {@link List}.
   */
  public final Parser<List<T>> times(int n) {
    return times(n, n);
  }
  
  /**
   * A {@link Parser} that runs {@code this} parser for at least {@code min} times and up to
   * {@code max} times. The return values are collected and returned in {@link List}.
   */
  public final Parser<List<T>> times(int min, int max) {
    Checks.checkMinMax(min, max);
    return new RepeatTimesParser<T>(this, min, max);
  }
  
  /**
   * A {@link Parser} that runs {@code this} parser for at least {@code min} times and up to
   * {@code max} times, with all the return values ignored.
   */
  public final Parser<Void> skipTimes(int min, int max) {
    Checks.checkMinMax(min, max);
    return new SkipTimesParser(this, min, max);
  }
  
  /**
   * A {@link Parser} that runs {@code this} parser and transforms the return value using
   * {@code map}.
   */
  public final <R> Parser<R> map(Map<? super T, ? extends R> map) {
    return new MapParser<T, R>(this, map);
  }

  /**
   * {@code p1.or(p2)} is equivalent to {@code p1 | p2} in EBNF.
   * 
   * @param alternative the alternative parser to run if this fails.
   */
  @SuppressWarnings("unchecked")
  public final Parser<T> or(Parser<? extends T> alternative) {
    return Parsers.or(this, alternative);
  }
  
  /**
   * {@code p.optional()} is equivalent to {@code p?} in EBNF. {@code null} is the result when
   * {@code this} fails with no partial match.
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
  
  /** A {@link Parser} that fails if {@code this} succeeds. Any input consumption is undone. */
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
  
  /** A {@link Parser} that runs {@code this} and undoes any input consumption if succeeds. */
  public final Parser<T> peek() {
    return new PeekParser<T>(this);
  }
  
  /** A {@link Parser} that undoes any partial match if {@code this} fails. */
  public final Parser<T> atomic() {
    return new AtomicParser<T>(this);
  }
  
  /**
   * A {@link Parser} that runs {@code this} parser and sets the number of logical steps explicitly
   * to {@code n}.
   */
  final Parser<T> step(int n) {
    checkArgument(n >= 0, "step < 0");
    return new StepParser<T>(this, n);
  }
  
  /**
   * A {@link Parser} that returns {@code true} if {@code this} succeeds, {@code false} otherwise.
   */
  public final Parser<Boolean> succeeds() {
    return ifelse(Parsers.TRUE, Parsers.FALSE);
  }
  
  /** A {@link Parser} that returns {@code true} if {@code this} fails, {@code false} otherwise. */
  public final Parser<Boolean> fails() {
    return ifelse(Parsers.FALSE, Parsers.TRUE);
  }
  
  /**
   * A {@link Parser} that runs {@code consequence} if {@code this} succeeds, or
   * {@code alternative} otherwise.
   */
  public final <R> Parser<R> ifelse(
      Parser<? extends R> consequence, Parser<? extends R> alternative) {
    return ifelse(Maps.constant(consequence), alternative);
  }
  
  /**
   * A {@link Parser} that runs {@code consequence} if {@code this} succeeds, or
   * {@code alternative} otherwise.
   */
  public final <R> Parser<R> ifelse(
      Map<? super T, ? extends Parser<? extends R>> consequence, Parser<? extends R> alternative) {
    return new IfElseParser<R, T>(this, consequence, alternative);
  }
  
  /**
   * A {@link Parser} that reports reports an error about {@code name} expected, if {@code this}
   * fails with no partial match.
   */
  public final Parser<T> label(String name) {
    return Parsers.plus(this, Parsers.<T>expect(name));
  }
  
  /**
   * Casts {@code this} to a {@link Parser} of type {@code R}.
   * Use it only if you know the parser actually returns value of type {@code R}.
   */
  @SuppressWarnings("unchecked")
  public final <R> Parser<R> cast() {
    return (Parser<R>) this;
  } 
  
  /**
   * A {@link Parser} that runs {@code this} between {@code before} and {@code after}.
   * The return value of {@code this} is preserved.
   * 
   * <p> Equivalent to {@link Parsers#between(Parser, Parser, Parser)}, which preserves the
   * natural order of the parsers in the argument list, but is a bit more verbose.
   */
  public final Parser<T> between(Parser<?> before, Parser<?> after) {
    return before.next(followedBy(after));
  }

  /**
   * A {@link Parser} that runs {@code this} 1 or more times separated by {@code delim}.
   * 
   * <p> The return values are collected in a {@link List}.
   */
  public final Parser<List<T>> sepBy1(Parser<?> delim) {
    final Parser<T> afterFirst = delim.step(0).next(this);
    Map<T, Parser<List<T>>> binder = new Map<T, Parser<List<T>>>() {
      public Parser<List<T>> map(T firstValue) {
        return new RepeatAtLeastParser<T>(
            afterFirst, 0, ListFactories.arrayListFactoryWithFirstElement(firstValue));
      }
    };
    return next(binder);
  }
  
  /**
   * A {@link Parser} that runs {@code this} 0 or more times separated by {@code delim}.
   * 
   * <p> The return values are collected in a {@link List}.
   */
  public final Parser<List<T>> sepBy(Parser<?> delim) {
    return Parsers.plus(sepBy1(delim), EmptyListParser.<T>instance());
  }

  /**
   * A {@link Parser} that runs {@code this} for 0 or more times delimited and terminated by
   * {@code delim}.
   * 
   * <p> The return values are collected in a {@link List}.
   */
  public final Parser<List<T>> endBy(Parser<?> delim) {
    return followedBy(delim).many();
  }

  /**
   * A {@link Parser} that runs {@code this} for 1 or more times delimited and terminated by
   * {@code delim}.
   * 
   * <p> The return values are collected in a {@link List}.
   */
  public final Parser<List<T>> endBy1(Parser<?> delim) {
    return followedBy(delim).many1();
  }

  /**
   * A {@link Parser} that runs {@code this} for 1 ore more times separated and optionally
   * terminated by {@code delim}. For example: {@code "foo;foo;foo"} and {@code "foo;foo;"} both
   * matches {@code foo.sepEndBy1(semicolon)}.
   * 
   * <p> The return values are collected in a {@link List}.
   */
  public final Parser<List<T>> sepEndBy1(final Parser<?> delim) {
    return next(new Map<T, Parser<List<T>>>() {
      public Parser<List<T>> map(T first) {
        return new DelimitedListParser<T>(
            Parser.this, delim, ListFactories.arrayListFactoryWithFirstElement(first));
      }
    });
  }

  /**
   * A {@link Parser} that runs {@code this} for 0 ore more times separated and optionally
   * terminated by {@code delim}. For example: {@code "foo;foo;foo"} and {@code "foo;foo;"} both
   * matches {@code foo.sepEndBy(semicolon)}.
   * 
   * <p> The return values are collected in a {@link List}.
   */
  public final Parser<List<T>> sepEndBy(Parser<?> delim) {
    return Parsers.plus(sepEndBy1(delim), EmptyListParser.<T>instance());
  }

  /**
   * A {@link Parser} that runs {@code op} for 0 or more times greedily, then runs {@code this}.
   * The {@link Map} objects returned from {@code op} are applied from right to left to the return
   * value of {@code p}.
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
   * <p> {@code p.postfix(op)} is equivalent to {@code p op*} in EBNF.
   */
  @SuppressWarnings("unchecked")
  public final Parser<T> postfix(Parser<? extends Map<? super T, ? extends T>> op) {
    return Parsers.sequence(this, op.many(), Parsers.POSTFIX_OPERATOR_MAP2);
  }

  /**
   * A {@link Parser} that parses non-associative infix operator. Runs {@code this} for the left
   * operand, and then runs {@code op} and {@code this} for the operator and the right operand
   * optionally. The {@link Map2} objects returned from {@code op} are applied to the return values
   * of the two operands, if any.
   * 
   * <p> {@code p.infixn(op)} is equivalent to {@code p (op p)?} in EBNF.
   */
  public final Parser<T> infixn(Parser<? extends Map2<? super T, ? super T, ? extends T>> op) {
    return Parsers.infixn(this, op);
  }

 /**
  * A {@link Parser} for left-associative infix operator. Runs {@code this} for the left operand,
  * and then runs {@code op} and {@code this} for the operator and the right operand for 0 or more
  * times greedily. The {@link Map2} objects returned from {@code op} are applied from left to right
  * to the return values of {@code this}, if any.
  * For example: {@code a + b + c + d} is evaluated as {@code (((a + b)+c)+d)}.
  * 
  * <p> {@code p.infixl(op)} is equivalent to {@code p (op p)*} in EBNF.
  */
  public final Parser<T> infixl(
      Parser<? extends Map2<? super T, ? super T, ? extends T>> op) {
    // somehow generics doesn't work if we inline the code here.
    return Parsers.infixl(this, op);
  }

  /**
   * A {@link Parser} for right-associative infix operator. Runs {@code this} for the left operand,
   * and then runs {@code op} and {@code this} for the operator and the right operand for 0 or more
   * times greedily. The {@link Map2} objects returned from {@code op} are applied from right to
   * left to the return values of {@code this}, if any.
   * For example: {@code a + b + c + d} is evaluated as {@code a + (b + (c + d))}.
   * 
   * <p> {@code p.infixr(op)} is equivalent to {@code p (op p)*} in EBNF.
   */
  public final Parser<T> infixr(Parser<? extends Map2<? super T, ? super T, ? extends T>> op) {
    return Parsers.infixr(this, op);
  }
  
  /**
   * A {@link Parser} that runs {@code this} and wraps the return value in a {@link Token}.
   * 
   * <p> It is normally not necessary to call this method explicitly. {@link #lexer(Parser)} and
   * {@link #from(Parser, Parser)} both do the conversion automatically.
   */
  public final Parser<Token> token() {
    return new ToTokenParser(this);
  }
  
  /** A {@link Parser} that returns the matched string in the original source. */
  public final Parser<String> source() {
    return new ReturnSourceParser(this);
  }
  
  /**
   * A {@link Parser} that takes as input the {@link Token} collection returned by {@code lexer},
   * and runs {@code this} to parse the tokens.
   * 
   * <p> {@code this} must be a token level parser.
   */
  public final Parser<T> from(Parser<? extends Collection<Token>> lexer) {
    return Parsers.nested(Parsers.tokens(lexer), followedBy(Parsers.EOF));
  }
  
  /**
   * A {@link Parser} that takes as input the tokens returned by {@code tokenizer}
   * delimited by {@code delim}, and runs {@code this} to parse the tokens.
   * 
   * <p> {@code this} must be a token level parser.
   */
  public final Parser<T> from(Parser<?> tokenizer, Parser<Void> delim) {
    return from(tokenizer.lexer(delim));
  }
  
  /**
   * A {@link Parser} that greedily runs {@code this} repeatedly,
   * and ignores the pattern recognized by {@code delim} before and after each occurrence.
   * The result tokens are wrapped in {@link Token} and are collected and returned in a
   * {@link List}.
   * 
   * <p> It is normally not necessary to call this method explicitly.
   * {@link #from(Parser, Parser)} is more convenient for simple uses that just need to connect a
   * token level parser with a lexer that produces the tokens.
   * When more flexible control over the token list is needed, for example,
   * to parse indentation sensitive language, a pre-processor of the token list may be needed.
   * 
   * <p> {@code this} must be a tokenizer that returns a token value.
   */
  public Parser<List<Token>> lexer(Parser<?> delim) {
    return delim.optional().next(token().sepEndBy(delim));
  }
  
  /**
   * Parses a source string. 
   * 
   * @param source the source string
   * @param moduleName the name of the module, this name appears in error message
   * @param sourceLocator maps an index of char into line and column numbers
   * @return the result
   */
  final T parse(CharSequence source, String moduleName, SourceLocator sourceLocator) {
    return Parsers.parse(source, followedBy(Parsers.EOF), sourceLocator, moduleName);
  }
  
  /**
   * Parses {@code source}.
   * 
   * @param source the source string
   * @param moduleName the name of the module, this name appears in error message
   * @return the result
   */
  public final T parse(CharSequence source, String moduleName) {
    return parse(source, moduleName, new DefaultSourceLocator(source));
  }
  
  /** Parses {@code source}. */
  public final T parse(CharSequence source) {
    return parse(source, null);
  }

  
  /** Parses source read from {@code readable}.  */
  public final T parse(Readable readable) throws IOException {
    return parse(readable, null);
  }

  /**
   * Parses source read from {@code readable}.
   * 
   * @param readable where the source is read from
   * @param moduleName the name of the module, this name appears in error message
   * @return the result
   */
  public final T parse(Readable readable, String moduleName) throws IOException {
    StringBuilder builder = new StringBuilder();
    copy(readable, builder);
    return parse(builder, moduleName);
  }
  
  /** Copies all content from {@code from} to {@code to}. */
  @Private static void copy(Readable from, Appendable to) throws IOException {
     CharBuffer buf = CharBuffer.allocate(2048);
     for (;;) {
       int r = from.read(buf);
       if (r == -1) break;
       buf.flip();
       to.append(buf, 0, r);
     }
  }
  
  @SuppressWarnings("unchecked")
  final T getReturn(ParseContext ctxt) {
    return (T) ctxt.result;
  }
  
  private ParserException asParserException(Throwable e, ParseContext ctxt) {
    if (e instanceof ParserException) return (ParserException) e;
    return new ParserException(
        e, null, ctxt.module, ctxt.locator.locate(ctxt.getIndex()));
  }
  
  final boolean run(ParseContext ctxt) {
    try {
      return apply(ctxt);
    }
    catch(RuntimeException e) {
      throw asParserException(e, ctxt);
    }
  }
  
  abstract boolean apply(ParseContext ctxt);
}
