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

import org.jparsec.Parser;
import org.jparsec.Parsers;
import org.jparsec.examples.java.ast.declaration.DefBody;
import org.jparsec.examples.java.ast.declaration.Member;
import org.jparsec.examples.java.ast.expression.*;
import org.junit.Test;

import java.util.function.UnaryOperator;

import static org.jparsec.examples.java.parser.TerminalParser.parse;

/**
 * Unit test for {@link ExpressionParser}.
 * 
 * @author Ben Yu
 */
public class ExpressionParserTest {
  
  private static final Parser<DefBody> EMPTY_BODY = DeclarationParser.body(Parsers.<Member>never());

  @Test
  public void testNull() {
    TerminalParserTest.assertResult(ExpressionParser.NULL, "null", NullExpression.class, "null");
  }

  @Test
  public void testIdentifier() {
    TerminalParserTest.assertResult(ExpressionParser.IDENTIFIER, "foo", Identifier.class, "foo");
  }

  @Test
  public void testSuper() {
    TerminalParserTest.assertResult(ExpressionParser.SUPER, "super", SuperExpression.class, "super");
  }

  @Test
  public void testThis() {
    Parser<Expression> parser = ExpressionParser.THIS;
    TerminalParserTest.assertResult(parser, "this", ThisExpression.class, "this");
    TerminalParserTest.assertResult(parser, "foo.this", ThisExpression.class, "foo.this");
    TerminalParserTest.assertResult(parser, "A.b.this", ThisExpression.class, "A.b.this");
  }

  @Test
  public void testCharLiteral() {
    Parser<Expression> parser = ExpressionParser.CHAR_LITERAL;
    TerminalParserTest.assertResult(parser, "'a'", CharLiteral.class, "a");
    TerminalParserTest.assertResult(parser, "'\\''", CharLiteral.class, "'");
  }

  @Test
  public void testStringLiteral() {
    Parser<Expression> parser = ExpressionParser.STRING_LITERAL;
    TerminalParserTest.assertResult(parser, "\"\"", StringLiteral.class, "");
    TerminalParserTest.assertResult(parser, "\"foo\"", StringLiteral.class, "foo");
    TerminalParserTest.assertResult(parser, "\"\\\"\"", StringLiteral.class, "\"");
  }

  @Test
  public void testBooleanLiteral() {
    Parser<Expression> parser = ExpressionParser.BOOLEAN_LITERAL;
    TerminalParserTest.assertResult(parser, "true", BooleanLiteral.class, "true");
    TerminalParserTest.assertResult(parser, "false", BooleanLiteral.class, "false");
  }

  @Test
  public void testClassLiteral() {
    Parser<Expression> parser = ExpressionParser.CLASS_LITERAL;
    TerminalParserTest.assertResult(parser, "int.class", ClassLiteral.class, "int.class");
    TerminalParserTest.assertResult(parser, "Integer.class", ClassLiteral.class, "Integer.class");
    TerminalParserTest.assertResult(parser, "java.lang.Integer.class", ClassLiteral.class, "java.lang.Integer.class");
    TerminalParserTest.assertResult(parser, "Map<Integer, String>.class",
        ClassLiteral.class, "Map<Integer, String>.class");
  }

  @Test
  public void testIntegerLiteral() {
    Parser<Expression> parser = ExpressionParser.INTEGER_LITERAL;
    TerminalParserTest.assertResult(parser, "123", IntegerLiteral.class, "123");
    TerminalParserTest.assertResult(parser, "0x123L", IntegerLiteral.class, "0X123L");
    TerminalParserTest.assertResult(parser, "0123f", IntegerLiteral.class, "0123F");
  }

  @Test
  public void testDecimalLiteral() {
    Parser<Expression> parser = ExpressionParser.DECIMAL_LITERAL;
    TerminalParserTest.assertResult(parser, "123.0", DecimalPointNumberLiteral.class, "123.0");
    TerminalParserTest.assertResult(parser, "123.0D", DecimalPointNumberLiteral.class, "123.0");
    TerminalParserTest.assertResult(parser, "0.123F", DecimalPointNumberLiteral.class, "0.123F");
  }

  @Test
  public void testCastOrExpression() {
    Parser<Expression> parser = ExpressionParser.castOrExpression(ExpressionParser.IDENTIFIER);
    TerminalParserTest.assertResult(parser, "(foo)", Identifier.class, "foo");
    TerminalParserTest.assertResult(parser, "(foo) bar", CastExpression.class, "((foo) bar)");
    TerminalParserTest.assertResult(parser, "(foo<int>) bar", CastExpression.class, "((foo<int>) bar)");
    TerminalParserTest.assertFailure(parser, "(foo<int>) ", 1, 12);
  }

  @Test
  public void testInstanceOf() {
    TerminalParserTest.assertToString(InstanceOfExpression.class, "(1 instanceof int)",
        parse(ExpressionParser.INSTANCE_OF, "instanceof int").apply(literal(1)));
    TerminalParserTest.assertToString(InstanceOfExpression.class, "(1 instanceof List<int>)",
        parse(ExpressionParser.INSTANCE_OF, "instanceof List<int>").apply(literal(1)));
  }

  @Test
  public void testQualifiedExpr() {
    TerminalParserTest.assertToString(QualifiedExpression.class, "(1.foo)",
        parse(ExpressionParser.QUALIFIED_EXPR, ".foo").apply(literal(1)));
  }

  @Test
  public void testSubscript() {
    TerminalParserTest.assertToString(ArraySubscriptExpression.class, "1[foo]",
        parse(ExpressionParser.subscript(ExpressionParser.IDENTIFIER), "[foo]").apply(literal(1)));
  }

  @Test
  public void testQualifiedMethodCall() {
    Parser<UnaryOperator<Expression>> parser = ExpressionParser.qualifiedMethodCall(ExpressionParser.IDENTIFIER);
    TerminalParserTest.assertToString(MethodCallExpression.class, "1.f(a, b)",
        parse(parser, ".f(a,b)").apply(literal(1)));
    TerminalParserTest.assertToString(MethodCallExpression.class, "1.f()",
        parse(parser, ".f()").apply(literal(1)));
  }

  @Test
  public void testQualifiedNew() {
    Parser<UnaryOperator<Expression>> parser = ExpressionParser.qualifiedNew(ExpressionParser.IDENTIFIER, EMPTY_BODY);
    TerminalParserTest.assertToString(NewExpression.class, "1.new int(a, b) {}",
        parse(parser, ".new int(a,b){}").apply(literal(1)));
    TerminalParserTest.assertToString(NewExpression.class, "1.new int(a)",
        parse(parser, ".new int(a)").apply(literal(1)));
  }

  @Test
  public void testSimpleMethodCall() {
    TerminalParserTest.assertResult(ExpressionParser.simpleMethodCall(ExpressionParser.IDENTIFIER), "f(a,b)",
        MethodCallExpression.class, "f(a, b)");
  }

  @Test
  public void testSimpleNewExpression() {
    Parser<Expression> parser = ExpressionParser.simpleNewExpression(ExpressionParser.IDENTIFIER, EMPTY_BODY);
    TerminalParserTest.assertResult(parser, "new Foo(a,b)", NewExpression.class, "new Foo(a, b)");
    TerminalParserTest.assertResult(parser, "new Foo(a,b){}", NewExpression.class, "new Foo(a, b) {}");
  }

  @Test
  public void testNewArrayWithExplicitLength() {
    Parser<Expression> parser = ExpressionParser.newArrayWithExplicitLength(ExpressionParser.IDENTIFIER);
    TerminalParserTest.assertResult(parser, "new int[n]", NewArrayExpression.class, "new int[n]");
    TerminalParserTest.assertResult(parser, "new int[][n]", NewArrayExpression.class, "new int[][n]");
    TerminalParserTest.assertResult(parser, "new int[n]{}", NewArrayExpression.class, "new int[n] {}");
    TerminalParserTest.assertResult(parser, "new int[n]{a,b,c}", NewArrayExpression.class, "new int[n] {a, b, c}");
  }

  @Test
  public void testNewArrayWithoutExplicitLength() {
    Parser<Expression> parser = ExpressionParser.newArrayWithoutExplicitLength(ExpressionParser.IDENTIFIER);
    TerminalParserTest.assertResult(parser, "new int[]{}", NewArrayExpression.class, "new int[] {}");
    TerminalParserTest.assertResult(parser, "new int[]{a,b,c}", NewArrayExpression.class, "new int[] {a, b, c}");
    TerminalParserTest.assertFailure(parser, "new int[]", 1, 10);
  }

  @Test
  public void testConditional() {
    TerminalParserTest.assertToString(ConditionalExpression.class, "(1 ? a : 2)",
        parse(ExpressionParser.conditional(ExpressionParser.IDENTIFIER), "?a:").apply(literal(1), literal(2)));
  }

  @Test
  public void testAtom() {
    Parser<Expression> parser = ExpressionParser.ATOM;
    TerminalParserTest.assertResult(parser, "null", NullExpression.class, "null");
    TerminalParserTest.assertResult(parser, "this", ThisExpression.class, "this");
    TerminalParserTest.assertResult(parser, "super", SuperExpression.class, "super");
    TerminalParserTest.assertResult(parser, "int.class", ClassLiteral.class, "int.class");
    TerminalParserTest.assertResult(parser, "true", BooleanLiteral.class, "true");
    TerminalParserTest.assertResult(parser, "false", BooleanLiteral.class, "false");
    TerminalParserTest.assertResult(parser, "'a'", CharLiteral.class, "a");
    TerminalParserTest.assertResult(parser, "\"foo\"", StringLiteral.class, "foo");
    TerminalParserTest.assertResult(parser, "123l", IntegerLiteral.class, "123L");
    TerminalParserTest.assertResult(parser, "1.2f", DecimalPointNumberLiteral.class, "1.2F");
    TerminalParserTest.assertResult(parser, "1.2e10f", ScientificNumberLiteral.class, "1.2e10F");
    TerminalParserTest.assertResult(parser, "1", IntegerLiteral.class, "1");
    TerminalParserTest.assertResult(parser, "1.0", DecimalPointNumberLiteral.class, "1.0");
    TerminalParserTest.assertResult(parser, "foo", Identifier.class, "foo");
  }

  @Test
  public void testExpression() {
    Parser<Expression> parser =
        ExpressionParser.expression(ExpressionParser.IDENTIFIER, EMPTY_BODY, StatementParser.expression(ExpressionParser.IDENTIFIER));
    TerminalParserTest.assertResult(parser, "foo", Identifier.class, "foo");
    TerminalParserTest.assertResult(parser, "(foo)", Identifier.class, "foo");
    TerminalParserTest.assertResult(parser, "((foo))", Identifier.class, "foo");
    TerminalParserTest.assertResult(parser, "foo[bar[baz]]", ArraySubscriptExpression.class, "foo[bar[baz]]");
    TerminalParserTest.assertResult(parser, "(foo) (bar)", CastExpression.class, "((foo) bar)");
    TerminalParserTest.assertResult(parser, "(foo) (bar) baz", CastExpression.class, "((foo) ((bar) baz))");
    TerminalParserTest.assertResult(parser, "new Foo(a,b)", NewExpression.class, "new Foo(a, b)");
    TerminalParserTest.assertResult(parser, "new int[n]", NewArrayExpression.class, "new int[n]");
    TerminalParserTest.assertResult(parser, "new int[n]{}", NewArrayExpression.class, "new int[n] {}");
    TerminalParserTest.assertResult(parser, "new int[]{a,b,c}", NewArrayExpression.class, "new int[] {a, b, c}");
    TerminalParserTest.assertResult(parser, "foo(a)", MethodCallExpression.class, "foo(a)");
    TerminalParserTest.assertResult(parser, "foo.f()", MethodCallExpression.class, "foo.f()");
    TerminalParserTest.assertResult(parser, "foo().bar().baz()", MethodCallExpression.class, "foo().bar().baz()");
    TerminalParserTest.assertResult(parser, "foo.new Foo()", NewExpression.class, "foo.new Foo()");
    TerminalParserTest.assertResult(parser, "foo.bar.baz", QualifiedExpression.class, "((foo.bar).baz)");
    TerminalParserTest.assertResult(parser, "foo.bar.new Foo()", NewExpression.class, "(foo.bar).new Foo()");
    TerminalParserTest.assertResult(parser, "foo++", PostfixUnaryExpression.class, "(foo++)");
    TerminalParserTest.assertResult(parser, "foo++--", PostfixUnaryExpression.class, "((foo++)--)");
    TerminalParserTest.assertResult(parser, "++foo", PrefixUnaryExpression.class, "(++foo)");
    TerminalParserTest.assertResult(parser, "++--foo", PrefixUnaryExpression.class, "(++(--foo))");
    TerminalParserTest.assertResult(parser, "++foo--", PrefixUnaryExpression.class, "(++(foo--))");
    TerminalParserTest.assertResult(parser, "+foo", PrefixUnaryExpression.class, "(+foo)");
    TerminalParserTest.assertResult(parser, "+-foo", PrefixUnaryExpression.class, "(+(-foo))");
    TerminalParserTest.assertResult(parser, "!foo", PrefixUnaryExpression.class, "(!foo)");
    TerminalParserTest.assertResult(parser, "!!foo", PrefixUnaryExpression.class, "(!(!foo))");
    TerminalParserTest.assertResult(parser, "~foo", PrefixUnaryExpression.class, "(~foo)");
    TerminalParserTest.assertResult(parser, "~~foo", PrefixUnaryExpression.class, "(~(~foo))");
    TerminalParserTest.assertResult(parser, "foo+bar", BinaryExpression.class, "(foo + bar)");
    TerminalParserTest.assertResult(parser, "a+b*c/d-e%f", BinaryExpression.class, "((a + ((b * c) / d)) - (e % f))");
    TerminalParserTest.assertResult(parser, "a<<b", BinaryExpression.class, "(a << b)");
    TerminalParserTest.assertResult(parser, "a>>b", BinaryExpression.class, "(a >> b)");
    TerminalParserTest.assertResult(parser, "a>>>b", BinaryExpression.class, "(a >>> b)");
    TerminalParserTest.assertResult(parser, "a>b", BinaryExpression.class, "(a > b)");
    TerminalParserTest.assertResult(parser, "a>b<c", BinaryExpression.class, "((a > b) < c)");
    TerminalParserTest.assertResult(parser, "a>=b", BinaryExpression.class, "(a >= b)");
    TerminalParserTest.assertResult(parser, "a>=b<=c", BinaryExpression.class, "((a >= b) <= c)");
    TerminalParserTest.assertResult(parser, "a instanceof int", InstanceOfExpression.class, "(a instanceof int)");
    TerminalParserTest.assertResult(parser, "a instanceof int instanceof boolean",
        InstanceOfExpression.class, "((a instanceof int) instanceof boolean)");
    TerminalParserTest.assertResult(parser, "a==b", BinaryExpression.class, "(a == b)");
    TerminalParserTest.assertResult(parser, "a==b!=c", BinaryExpression.class, "((a == b) != c)");
    TerminalParserTest.assertResult(parser, "a&b&c", BinaryExpression.class, "((a & b) & c)");
    TerminalParserTest.assertResult(parser, "a|b&c", BinaryExpression.class, "(a | (b & c))");
    TerminalParserTest.assertResult(parser, "a&&b|c", BinaryExpression.class, "(a && (b | c))");
    TerminalParserTest.assertResult(parser, "!a||b&&c", BinaryExpression.class, "((!a) || (b && c))");
    TerminalParserTest.assertResult(parser, "x?c:d", ConditionalExpression.class, "(x ? c : d)");
    TerminalParserTest.assertResult(parser, "a==b?b==c?x:y:z+n ? m: n",
        ConditionalExpression.class, "((a == b) ? ((b == c) ? x : y) : ((z + n) ? m : n))");
    TerminalParserTest.assertResult(parser, "a=b=c", BinaryExpression.class, "(a = (b = c))");
    TerminalParserTest.assertResult(parser, "a+=b+=c", BinaryExpression.class, "(a += (b += c))");
    TerminalParserTest.assertResult(parser, "a-=b-=c", BinaryExpression.class, "(a -= (b -= c))");
    TerminalParserTest.assertResult(parser, "a*=b*=c", BinaryExpression.class, "(a *= (b *= c))");
    TerminalParserTest.assertResult(parser, "a/=b/=c", BinaryExpression.class, "(a /= (b /= c))");
    TerminalParserTest.assertResult(parser, "a%=b%=c", BinaryExpression.class, "(a %= (b %= c))");
    TerminalParserTest.assertResult(parser, "a&=b&=c", BinaryExpression.class, "(a &= (b &= c))");
    TerminalParserTest.assertResult(parser, "a|=b|=c", BinaryExpression.class, "(a |= (b |= c))");
    TerminalParserTest.assertResult(parser, "a>>=b>>=c", BinaryExpression.class, "(a >>= (b >>= c))");
    TerminalParserTest.assertResult(parser, "a>>>=b>>>=c", BinaryExpression.class, "(a >>>= (b >>>= c))");
    TerminalParserTest.assertResult(parser, "a<<=b<<=c", BinaryExpression.class, "(a <<= (b <<= c))");
    TerminalParserTest.assertResult(parser, "a^=b^=c", BinaryExpression.class, "(a ^= (b ^= c))");
    TerminalParserTest.assertResult(parser, "Foo::new", ConstructorReference.class, "Foo::new");
    TerminalParserTest.assertResult(parser, "Foo.Bar::new", ConstructorReference.class, "(Foo.Bar)::new");
    TerminalParserTest.assertResult(parser, "x::new", ConstructorReference.class, "x::new");
    TerminalParserTest.assertResult(parser, "Foo::create", MethodReference.class, "Foo::create");
    TerminalParserTest.assertResult(parser, "Foo.Bar::create", MethodReference.class, "(Foo.Bar)::create");
    TerminalParserTest.assertResult(parser, "x::create", MethodReference.class, "x::create");
    TerminalParserTest.assertResult(parser, "Foo::<T>create", MethodReference.class, "Foo::<T>create");
    TerminalParserTest.assertResult(parser, "Foo.Bar::<T>create", MethodReference.class, "(Foo.Bar)::<T>create");
    TerminalParserTest.assertResult(parser, "x::<String>create", MethodReference.class, "x::<String>create");
    TerminalParserTest.assertResult(parser, "() -> a", LambdaExpression.class, "() -> a;");
    TerminalParserTest.assertResult(parser, "() -> {a;}", LambdaExpression.class, "() -> {a;}");
    TerminalParserTest.assertResult(parser, "x -> {}", LambdaExpression.class, "(x) -> {}");
    TerminalParserTest.assertResult(parser, "(x, y) -> {}", LambdaExpression.class, "(x, y) -> {}");
    TerminalParserTest.assertResult(parser, "(String x, int y) -> {}", LambdaExpression.class, "(String x, int y) -> {}");
    TerminalParserTest.assertResult(parser, "(String x, int y) -> {xyz;}", LambdaExpression.class, "(String x, int y) -> {xyz;}");
  }

  @Test
  public void testArrayInitializer() {
    Parser<Expression> parser = ExpressionParser.arrayInitializer(ExpressionParser.IDENTIFIER);
    TerminalParserTest.assertResult(parser, "{}", ArrayInitializer.class, "{}");
    TerminalParserTest.assertResult(parser, "{foo,bar}", ArrayInitializer.class, "{foo, bar}");
    TerminalParserTest.assertResult(parser, "{foo,bar,}", ArrayInitializer.class, "{foo, bar}");
  }

  static IntegerLiteral literal(int i) {
    return new IntegerLiteral(IntegerLiteral.Radix.DEC, Integer.toString(i), NumberType.INT);
  }
}
