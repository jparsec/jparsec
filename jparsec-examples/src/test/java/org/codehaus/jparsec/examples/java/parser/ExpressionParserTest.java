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
package org.codehaus.jparsec.examples.java.parser;

import org.codehaus.jparsec.Parser;
import org.codehaus.jparsec.Parsers;
import org.codehaus.jparsec.examples.java.ast.declaration.DefBody;
import org.codehaus.jparsec.examples.java.ast.declaration.Member;
import org.codehaus.jparsec.examples.java.ast.expression.*;
import org.codehaus.jparsec.examples.java.ast.expression.IntegerLiteral.Radix;
import org.junit.Test;

import java.util.function.UnaryOperator;

import static org.codehaus.jparsec.examples.java.parser.ExpressionParser.IDENTIFIER;
import static org.codehaus.jparsec.examples.java.parser.TerminalParser.parse;
import static org.codehaus.jparsec.examples.java.parser.TerminalParserTest.*;

/**
 * Unit test for {@link ExpressionParser}.
 * 
 * @author Ben Yu
 */
public class ExpressionParserTest {
  
  private static final Parser<DefBody> EMPTY_BODY = DeclarationParser.body(Parsers.<Member>never());

  @Test
  public void testNull() {
    assertResult(ExpressionParser.NULL, "null", NullExpression.class, "null");
  }

  @Test
  public void testIdentifier() {
    assertResult(IDENTIFIER, "foo", Identifier.class, "foo");
  }

  @Test
  public void testSuper() {
    assertResult(ExpressionParser.SUPER, "super", SuperExpression.class, "super");
  }

  @Test
  public void testThis() {
    Parser<Expression> parser = ExpressionParser.THIS;
    assertResult(parser, "this", ThisExpression.class, "this");
    assertResult(parser, "foo.this", ThisExpression.class, "foo.this");
    assertResult(parser, "A.b.this", ThisExpression.class, "A.b.this");
  }

  @Test
  public void testCharLiteral() {
    Parser<Expression> parser = ExpressionParser.CHAR_LITERAL;
    assertResult(parser, "'a'", CharLiteral.class, "a");
    assertResult(parser, "'\\''", CharLiteral.class, "'");
  }

  @Test
  public void testStringLiteral() {
    Parser<Expression> parser = ExpressionParser.STRING_LITERAL;
    assertResult(parser, "\"\"", StringLiteral.class, "");
    assertResult(parser, "\"foo\"", StringLiteral.class, "foo");
    assertResult(parser, "\"\\\"\"", StringLiteral.class, "\"");
  }

  @Test
  public void testBooleanLiteral() {
    Parser<Expression> parser = ExpressionParser.BOOLEAN_LITERAL;
    assertResult(parser, "true", BooleanLiteral.class, "true");
    assertResult(parser, "false", BooleanLiteral.class, "false");
  }

  @Test
  public void testClassLiteral() {
    Parser<Expression> parser = ExpressionParser.CLASS_LITERAL;
    assertResult(parser, "int.class", ClassLiteral.class, "int.class");
    assertResult(parser, "Integer.class", ClassLiteral.class, "Integer.class");
    assertResult(parser, "java.lang.Integer.class", ClassLiteral.class, "java.lang.Integer.class");
    assertResult(parser, "Map<Integer, String>.class",
        ClassLiteral.class, "Map<Integer, String>.class");
  }

  @Test
  public void testIntegerLiteral() {
    Parser<Expression> parser = ExpressionParser.INTEGER_LITERAL;
    assertResult(parser, "123", IntegerLiteral.class, "123");
    assertResult(parser, "0x123L", IntegerLiteral.class, "0X123L");
    assertResult(parser, "0123f", IntegerLiteral.class, "0123F");
  }

  @Test
  public void testDecimalLiteral() {
    Parser<Expression> parser = ExpressionParser.DECIMAL_LITERAL;
    assertResult(parser, "123.0", DecimalPointNumberLiteral.class, "123.0");
    assertResult(parser, "123.0D", DecimalPointNumberLiteral.class, "123.0");
    assertResult(parser, "0.123F", DecimalPointNumberLiteral.class, "0.123F");
  }

  @Test
  public void testCastOrExpression() {
    Parser<Expression> parser = ExpressionParser.castOrExpression(IDENTIFIER);
    assertResult(parser, "(foo)", Identifier.class, "foo");
    assertResult(parser, "(foo) bar", CastExpression.class, "((foo) bar)");
    assertResult(parser, "(foo<int>) bar", CastExpression.class, "((foo<int>) bar)");
    assertFailure(parser, "(foo<int>) ", 1, 12);
  }

  @Test
  public void testInstanceOf() {
    assertToString(InstanceOfExpression.class, "(1 instanceof int)",
        parse(ExpressionParser.INSTANCE_OF, "instanceof int").apply(literal(1)));
    assertToString(InstanceOfExpression.class, "(1 instanceof List<int>)",
        parse(ExpressionParser.INSTANCE_OF, "instanceof List<int>").apply(literal(1)));
  }

  @Test
  public void testQualifiedExpr() {
    assertToString(QualifiedExpression.class, "(1.foo)",
        parse(ExpressionParser.QUALIFIED_EXPR, ".foo").apply(literal(1)));
  }

  @Test
  public void testSubscript() {
    assertToString(ArraySubscriptExpression.class, "1[foo]",
        parse(ExpressionParser.subscript(IDENTIFIER), "[foo]").apply(literal(1)));
  }

  @Test
  public void testQualifiedMethodCall() {
    Parser<UnaryOperator<Expression>> parser = ExpressionParser.qualifiedMethodCall(IDENTIFIER);
    assertToString(MethodCallExpression.class, "1.f(a, b)",
        parse(parser, ".f(a,b)").apply(literal(1)));
    assertToString(MethodCallExpression.class, "1.f()",
        parse(parser, ".f()").apply(literal(1)));
  }

  @Test
  public void testQualifiedNew() {
    Parser<UnaryOperator<Expression>> parser = ExpressionParser.qualifiedNew(IDENTIFIER, EMPTY_BODY);
    assertToString(NewExpression.class, "1.new int(a, b) {}",
        parse(parser, ".new int(a,b){}").apply(literal(1)));
    assertToString(NewExpression.class, "1.new int(a)",
        parse(parser, ".new int(a)").apply(literal(1)));
  }

  @Test
  public void testSimpleMethodCall() {
    assertResult(ExpressionParser.simpleMethodCall(IDENTIFIER), "f(a,b)",
        MethodCallExpression.class, "f(a, b)");
  }

  @Test
  public void testSimpleNewExpression() {
    Parser<Expression> parser = ExpressionParser.simpleNewExpression(IDENTIFIER, EMPTY_BODY);
    assertResult(parser, "new Foo(a,b)", NewExpression.class, "new Foo(a, b)");
    assertResult(parser, "new Foo(a,b){}", NewExpression.class, "new Foo(a, b) {}");
  }

  @Test
  public void testNewArrayWithExplicitLength() {
    Parser<Expression> parser = ExpressionParser.newArrayWithExplicitLength(IDENTIFIER);
    assertResult(parser, "new int[n]", NewArrayExpression.class, "new int[n]");
    assertResult(parser, "new int[][n]", NewArrayExpression.class, "new int[][n]");
    assertResult(parser, "new int[n]{}", NewArrayExpression.class, "new int[n] {}");
    assertResult(parser, "new int[n]{a,b,c}", NewArrayExpression.class, "new int[n] {a, b, c}");
  }

  @Test
  public void testNewArrayWithoutExplicitLength() {
    Parser<Expression> parser = ExpressionParser.newArrayWithoutExplicitLength(IDENTIFIER);
    assertResult(parser, "new int[]{}", NewArrayExpression.class, "new int[] {}");
    assertResult(parser, "new int[]{a,b,c}", NewArrayExpression.class, "new int[] {a, b, c}");
    assertFailure(parser, "new int[]", 1, 10);
  }

  @Test
  public void testConditional() {
    assertToString(ConditionalExpression.class, "(1 ? a : 2)",
        parse(ExpressionParser.conditional(IDENTIFIER), "?a:").apply(literal(1), literal(2)));
  }

  @Test
  public void testAtom() {
    Parser<Expression> parser = ExpressionParser.ATOM;
    assertResult(parser, "null", NullExpression.class, "null");
    assertResult(parser, "this", ThisExpression.class, "this");
    assertResult(parser, "super", SuperExpression.class, "super");
    assertResult(parser, "int.class", ClassLiteral.class, "int.class");
    assertResult(parser, "true", BooleanLiteral.class, "true");
    assertResult(parser, "false", BooleanLiteral.class, "false");
    assertResult(parser, "'a'", CharLiteral.class, "a");
    assertResult(parser, "\"foo\"", StringLiteral.class, "foo");
    assertResult(parser, "123l", IntegerLiteral.class, "123L");
    assertResult(parser, "1.2f", DecimalPointNumberLiteral.class, "1.2F");
    assertResult(parser, "1.2e10f", ScientificNumberLiteral.class, "1.2e10F");
    assertResult(parser, "1", IntegerLiteral.class, "1");
    assertResult(parser, "1.0", DecimalPointNumberLiteral.class, "1.0");
    assertResult(parser, "foo", Identifier.class, "foo");
  }

  @Test
  public void testExpression() {
    Parser<Expression> parser =
        ExpressionParser.expression(IDENTIFIER, EMPTY_BODY, StatementParser.expression(IDENTIFIER));
    assertResult(parser, "foo", Identifier.class, "foo");
    assertResult(parser, "(foo)", Identifier.class, "foo");
    assertResult(parser, "((foo))", Identifier.class, "foo");
    assertResult(parser, "foo[bar[baz]]", ArraySubscriptExpression.class, "foo[bar[baz]]");
    assertResult(parser, "(foo) (bar)", CastExpression.class, "((foo) bar)");
    assertResult(parser, "(foo) (bar) baz", CastExpression.class, "((foo) ((bar) baz))");
    assertResult(parser, "new Foo(a,b)", NewExpression.class, "new Foo(a, b)");
    assertResult(parser, "new int[n]", NewArrayExpression.class, "new int[n]");
    assertResult(parser, "new int[n]{}", NewArrayExpression.class, "new int[n] {}");
    assertResult(parser, "new int[]{a,b,c}", NewArrayExpression.class, "new int[] {a, b, c}");
    assertResult(parser, "foo(a)", MethodCallExpression.class, "foo(a)");
    assertResult(parser, "foo.f()", MethodCallExpression.class, "foo.f()");
    assertResult(parser, "foo().bar().baz()", MethodCallExpression.class, "foo().bar().baz()");
    assertResult(parser, "foo.new Foo()", NewExpression.class, "foo.new Foo()");
    assertResult(parser, "foo.bar.baz", QualifiedExpression.class, "((foo.bar).baz)");
    assertResult(parser, "foo.bar.new Foo()", NewExpression.class, "(foo.bar).new Foo()");
    assertResult(parser, "foo++", PostfixUnaryExpression.class, "(foo++)");
    assertResult(parser, "foo++--", PostfixUnaryExpression.class, "((foo++)--)");
    assertResult(parser, "++foo", PrefixUnaryExpression.class, "(++foo)");
    assertResult(parser, "++--foo", PrefixUnaryExpression.class, "(++(--foo))");
    assertResult(parser, "++foo--", PrefixUnaryExpression.class, "(++(foo--))");
    assertResult(parser, "+foo", PrefixUnaryExpression.class, "(+foo)");
    assertResult(parser, "+-foo", PrefixUnaryExpression.class, "(+(-foo))");
    assertResult(parser, "!foo", PrefixUnaryExpression.class, "(!foo)");
    assertResult(parser, "!!foo", PrefixUnaryExpression.class, "(!(!foo))");
    assertResult(parser, "~foo", PrefixUnaryExpression.class, "(~foo)");
    assertResult(parser, "~~foo", PrefixUnaryExpression.class, "(~(~foo))");
    assertResult(parser, "foo+bar", BinaryExpression.class, "(foo + bar)");
    assertResult(parser, "a+b*c/d-e%f", BinaryExpression.class, "((a + ((b * c) / d)) - (e % f))");
    assertResult(parser, "a<<b", BinaryExpression.class, "(a << b)");
    assertResult(parser, "a>>b", BinaryExpression.class, "(a >> b)");
    assertResult(parser, "a>>>b", BinaryExpression.class, "(a >>> b)");
    assertResult(parser, "a>b", BinaryExpression.class, "(a > b)");
    assertResult(parser, "a>b<c", BinaryExpression.class, "((a > b) < c)");
    assertResult(parser, "a>=b", BinaryExpression.class, "(a >= b)");
    assertResult(parser, "a>=b<=c", BinaryExpression.class, "((a >= b) <= c)");
    assertResult(parser, "a instanceof int", InstanceOfExpression.class, "(a instanceof int)");
    assertResult(parser, "a instanceof int instanceof boolean",
        InstanceOfExpression.class, "((a instanceof int) instanceof boolean)");
    assertResult(parser, "a==b", BinaryExpression.class, "(a == b)");
    assertResult(parser, "a==b!=c", BinaryExpression.class, "((a == b) != c)");
    assertResult(parser, "a&b&c", BinaryExpression.class, "((a & b) & c)");
    assertResult(parser, "a|b&c", BinaryExpression.class, "(a | (b & c))");
    assertResult(parser, "a&&b|c", BinaryExpression.class, "(a && (b | c))");
    assertResult(parser, "!a||b&&c", BinaryExpression.class, "((!a) || (b && c))");
    assertResult(parser, "x?c:d", ConditionalExpression.class, "(x ? c : d)");
    assertResult(parser, "a==b?b==c?x:y:z+n ? m: n",
        ConditionalExpression.class, "((a == b) ? ((b == c) ? x : y) : ((z + n) ? m : n))");
    assertResult(parser, "a=b=c", BinaryExpression.class, "(a = (b = c))");
    assertResult(parser, "a+=b+=c", BinaryExpression.class, "(a += (b += c))");
    assertResult(parser, "a-=b-=c", BinaryExpression.class, "(a -= (b -= c))");
    assertResult(parser, "a*=b*=c", BinaryExpression.class, "(a *= (b *= c))");
    assertResult(parser, "a/=b/=c", BinaryExpression.class, "(a /= (b /= c))");
    assertResult(parser, "a%=b%=c", BinaryExpression.class, "(a %= (b %= c))");
    assertResult(parser, "a&=b&=c", BinaryExpression.class, "(a &= (b &= c))");
    assertResult(parser, "a|=b|=c", BinaryExpression.class, "(a |= (b |= c))");
    assertResult(parser, "a>>=b>>=c", BinaryExpression.class, "(a >>= (b >>= c))");
    assertResult(parser, "a>>>=b>>>=c", BinaryExpression.class, "(a >>>= (b >>>= c))");
    assertResult(parser, "a<<=b<<=c", BinaryExpression.class, "(a <<= (b <<= c))");
    assertResult(parser, "a^=b^=c", BinaryExpression.class, "(a ^= (b ^= c))");
    assertResult(parser, "Foo::new", ConstructorReference.class, "Foo::new");
    assertResult(parser, "Foo.Bar::new", ConstructorReference.class, "(Foo.Bar)::new");
    assertResult(parser, "x::new", ConstructorReference.class, "x::new");
    assertResult(parser, "Foo::create", MethodReference.class, "Foo::create");
    assertResult(parser, "Foo.Bar::create", MethodReference.class, "(Foo.Bar)::create");
    assertResult(parser, "x::create", MethodReference.class, "x::create");
    assertResult(parser, "Foo::<T>create", MethodReference.class, "Foo::<T>create");
    assertResult(parser, "Foo.Bar::<T>create", MethodReference.class, "(Foo.Bar)::<T>create");
    assertResult(parser, "x::<String>create", MethodReference.class, "x::<String>create");
    assertResult(parser, "() -> a", LambdaExpression.class, "() -> a;");
    assertResult(parser, "() -> {a;}", LambdaExpression.class, "() -> {a;}");
    assertResult(parser, "x -> {}", LambdaExpression.class, "(x) -> {}");
    assertResult(parser, "(x, y) -> {}", LambdaExpression.class, "(x, y) -> {}");
    assertResult(parser, "(String x, int y) -> {}", LambdaExpression.class, "(String x, int y) -> {}");
    assertResult(parser, "(String x, int y) -> {xyz;}", LambdaExpression.class, "(String x, int y) -> {xyz;}");
  }

  @Test
  public void testArrayInitializer() {
    Parser<Expression> parser = ExpressionParser.arrayInitializer(IDENTIFIER);
    assertResult(parser, "{}", ArrayInitializer.class, "{}");
    assertResult(parser, "{foo,bar}", ArrayInitializer.class, "{foo, bar}");
    assertResult(parser, "{foo,bar,}", ArrayInitializer.class, "{foo, bar}");
  }

  static IntegerLiteral literal(int i) {
    return new IntegerLiteral(Radix.DEC, Integer.toString(i), NumberType.INT);
  }
}
