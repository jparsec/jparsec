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
package org.jparsec.examples.sql.parser;

import org.jparsec.Parser;
import org.jparsec.functors.Tuples;
import org.jparsec.examples.sql.ast.*;
import org.junit.Test;

import java.util.Arrays;

import static org.jparsec.examples.sql.parser.ExpressionParser.NUMBER;
import static org.jparsec.examples.sql.parser.ExpressionParser.QUALIFIED_NAME;
import static org.jparsec.examples.sql.parser.RelationParserTest.table;
import static org.jparsec.examples.sql.parser.TerminalParserTest.assertFailure;

/**
 * Unit test for {@link ExpressionParser}.
 * 
 * @author Ben Yu
 */
public class ExpressionParserTest {

  @Test
  public void testNumber() {
    TerminalParserTest.assertParser(ExpressionParser.NUMBER, "1.2", new NumberExpression("1.2"));
  }

  @Test
  public void testQualifiedName() {
    Parser<Expression> parser = QUALIFIED_NAME;
    TerminalParserTest.assertParser(parser, "a", name("a"));
    TerminalParserTest.assertParser(parser, "a . bc", name("a", "bc"));
  }

  @Test
  public void testQualifiedWildcard() {
    TerminalParserTest.assertParser(ExpressionParser.QUALIFIED_WILDCARD, "a.b.*",
        new WildcardExpression(QualifiedName.of("a", "b")));
  }

  @Test
  public void testWildcard() {
    TerminalParserTest.assertParser(ExpressionParser.WILDCARD, "a.b.*",
        new WildcardExpression(QualifiedName.of("a", "b")));
    TerminalParserTest.assertParser(ExpressionParser.WILDCARD, "*",
        new WildcardExpression(QualifiedName.of()));
  }

  @Test
  public void testString() {
    TerminalParserTest.assertParser(ExpressionParser.STRING, "'foo'", new StringExpression("foo"));
  }

  @Test
  public void testFunctionCall() {
    Parser<Expression> parser = ExpressionParser.functionCall(NUMBER);
    TerminalParserTest.assertParser(parser, "f()", FunctionExpression.of(QualifiedName.of("f")));
    TerminalParserTest.assertParser(parser, "a.b(1)",
        FunctionExpression.of(QualifiedName.of("a", "b"), number(1)));
    TerminalParserTest.assertParser(parser, "a.b(1, 2)",
        FunctionExpression.of(QualifiedName.of("a", "b"), number(1), number(2)));
  }

  @Test
  public void testBetween() {
    Parser<Expression> parser = ExpressionParser.between(NUMBER);
    TerminalParserTest.assertParser(parser, "1 BETWEEN 0 and 2",
        new BetweenExpression(number(1), true, number(0), number(2)));
    TerminalParserTest.assertParser(parser, "1 not between 2 and 0",
        new BetweenExpression(number(1), false, number(2), number(0)));
  }

  @Test
  public void testTuple() {
    Parser<Expression> parser = ExpressionParser.tuple(NUMBER);
    TerminalParserTest.assertParser(parser, "()", TupleExpression.of());
    TerminalParserTest.assertParser(parser, "(1)", TupleExpression.of(number(1)));
    TerminalParserTest.assertParser(parser, "(1, 2)", TupleExpression.of(number(1), number(2)));
    TerminalParserTest.assertFailure(parser, "1", 1, 1, "( expected, 1 encountered.");
    TerminalParserTest.assertFailure(parser, "", 1, 1);
  }

  @Test
  public void testSimpleCase() {
    Parser<Expression> parser = ExpressionParser.simpleCase(NUMBER);
    TerminalParserTest.assertParser(parser, "case 1 when 1 then 2 else 3 end",
        simpleCase(number(1), number(1), number(2), number(3)));
    TerminalParserTest.assertParser(parser, "case 1 when 1 then 2 end",
        simpleCase(number(1), number(1), number(2), null));
  }

  @Test
  public void testFullCase() {
    Parser<Expression> parser =
        ExpressionParser.fullCase(ExpressionParser.QUALIFIED_NAME, NUMBER);
    TerminalParserTest.assertParser(parser, "case when a then 2 else 3 end",
        fullCase(name("a"), number(2), number(3)));
    TerminalParserTest.assertParser(parser, "case when a then 2 end", fullCase(name("a"), number(2), null));
  }

  @Test
  public void testArithmetic() {
    Parser<Expression> parser = ExpressionParser.arithmetic(NUMBER);
    TerminalParserTest.assertParser(parser, "1", number(1));
    TerminalParserTest.assertParser(parser, "((1))", number(1));
    TerminalParserTest.assertParser(parser, "1 + 2", new BinaryExpression(number(1), Op.PLUS, number(2)));
    TerminalParserTest.assertParser(parser, "2 * (1 + (2))",
        new BinaryExpression(number(2), Op.MUL,
            new BinaryExpression(number(1), Op.PLUS, number(2))));
    TerminalParserTest.assertParser(parser, "2 - 1 / (2)",
        new BinaryExpression(number(2), Op.MINUS,
            new BinaryExpression(number(1), Op.DIV, number(2))));
    TerminalParserTest.assertParser(parser, "2 * 1 % -2",
        new BinaryExpression(
            new BinaryExpression(number(2), Op.MUL, number(1)),
                Op.MOD, new UnaryExpression(Op.NEG, number(2))));
    TerminalParserTest.assertParser(parser, "f(1)", FunctionExpression.of(QualifiedName.of("f"), number(1)));
    TerminalParserTest.assertParser(parser, "foo.bar(1, 2) + baz(foo.bar(1 / 2))",
        new BinaryExpression(
            FunctionExpression.of(QualifiedName.of("foo", "bar"), number(1), number(2)),
            Op.PLUS,
            FunctionExpression.of(QualifiedName.of("baz"),
                FunctionExpression.of(QualifiedName.of("foo", "bar"), new BinaryExpression(
                    number(1), Op.DIV, number(2))))));
  }

  @Test
  public void testExpression() {
    Parser<Expression> parser = ExpressionParser.expression(NUMBER);
    TerminalParserTest.assertParser(parser,
        "1 + case a when a then count(a.b.*) end - case when 1 then a * 2 else b end",
        new BinaryExpression(
            new BinaryExpression(
                number(1),
                Op.PLUS,
                simpleCase(name("a"), name("a"), function("count", wildcard("a", "b")), null)),
            Op.MINUS,
            fullCase(number(1), new BinaryExpression(name("a"), Op.MUL, number(2)), name("b"))
        )
    );
  }

  @Test
  public void testCompare() {
    Parser<Expression> parser = ExpressionParser.compare(NUMBER);
    TerminalParserTest.assertParser(parser, "1 = 1", new BinaryExpression(number(1), Op.EQ, number(1)));
    TerminalParserTest.assertParser(parser, "1 < 2", new BinaryExpression(number(1), Op.LT, number(2)));
    TerminalParserTest.assertParser(parser, "1 <= 2", new BinaryExpression(number(1), Op.LE, number(2)));
    TerminalParserTest.assertParser(parser, "1 <> 2", new BinaryExpression(number(1), Op.NE, number(2)));
    TerminalParserTest.assertParser(parser, "2 > 1", new BinaryExpression(number(2), Op.GT, number(1)));
    TerminalParserTest.assertParser(parser, "2 >= 1", new BinaryExpression(number(2), Op.GE, number(1)));
    TerminalParserTest.assertParser(parser, "1 is null",
        new BinaryExpression(number(1), Op.IS, NullExpression.instance));
    TerminalParserTest.assertParser(parser, "1 is not null",
        new BinaryExpression(number(1), Op.NOT, NullExpression.instance));
    TerminalParserTest.assertParser(parser, "1 like 2", new LikeExpression(number(1), true, number(2), null));
    TerminalParserTest.assertParser(parser, "1 BETWEEN 0 and 2",
        new BetweenExpression(number(1), true, number(0), number(2)));
    TerminalParserTest.assertParser(parser, "1 not between 2 and 0",
        new BetweenExpression(number(1), false, number(2), number(0)));
  }

  @Test
  public void testLike() {
    Parser<Expression> parser = ExpressionParser.like(NUMBER);
    TerminalParserTest.assertParser(parser, "1 like 2", new LikeExpression(number(1), true, number(2), null));
    TerminalParserTest.assertParser(parser, "1 not like 2", new LikeExpression(number(1), false, number(2), null));
    TerminalParserTest.assertParser(parser, "1 like 2 escape 3",
        new LikeExpression(number(1), true, number(2), number(3)));
    TerminalParserTest.assertParser(parser, "1 not like 2 escape 3",
        new LikeExpression(number(1), false, number(2), number(3)));
  }

  @Test
  public void testLogical() {
    Parser<Expression> parser = ExpressionParser.logical(NUMBER);
    TerminalParserTest.assertParser(parser, "1", number(1));
    TerminalParserTest.assertParser(parser, "(1)", number(1));
    TerminalParserTest.assertParser(parser, "((1))", number(1));
    TerminalParserTest.assertParser(parser, "not 1", new UnaryExpression(Op.NOT, number(1)));
    TerminalParserTest.assertParser(parser, "1 and 2", new BinaryExpression(number(1), Op.AND, number(2)));
    TerminalParserTest.assertParser(parser, "1 or 2", new BinaryExpression(number(1), Op.OR, number(2)));
    TerminalParserTest.assertParser(parser, "1 or 2 and 3", new BinaryExpression(number(1), Op.OR,
        new BinaryExpression(number(2), Op.AND, number(3))));
    TerminalParserTest.assertParser(parser, "1 or NOT 2", new BinaryExpression(number(1), Op.OR,
        new UnaryExpression(Op.NOT, number(2))));
    TerminalParserTest.assertParser(parser, "not 1 and 2", new BinaryExpression(
        new UnaryExpression(Op.NOT, number(1)), Op.AND, number(2)));
  }

  @Test
  public void testExists() {
    TerminalParserTest.assertParser(ExpressionParser.exists(RelationParser.TABLE), "exists t",
        new UnaryRelationalExpression(table("t"), Op.EXISTS));
  }

  @Test
  public void testNotExists() {
    TerminalParserTest.assertParser(ExpressionParser.notExists(RelationParser.TABLE), "not exists t",
        new UnaryRelationalExpression(table("t"), Op.NOT_EXISTS));
  }

  @Test
  public void testInRelation() {
    TerminalParserTest.assertParser(ExpressionParser.inRelation(NUMBER, RelationParser.TABLE),
        "1 in (table)",
        new BinaryRelationalExpression(number(1), Op.IN, table("table")));
  }

  @Test
  public void testNotInRelation() {
    TerminalParserTest.assertParser(ExpressionParser.notInRelation(NUMBER, RelationParser.TABLE),
        "1 not in (table)",
        new BinaryRelationalExpression(number(1), Op.NOT_IN, table("table")));
  }

  @Test
  public void testIn() {
    TerminalParserTest.assertParser(ExpressionParser.in(NUMBER), "1 in (2)",
        new BinaryExpression(number(1), Op.IN, TupleExpression.of(number(2))));
    TerminalParserTest.assertParser(ExpressionParser.in(NUMBER), "1 in (2, 3)",
        new BinaryExpression(number(1), Op.IN, TupleExpression.of(number(2), number(3))));
  }

  @Test
  public void testNotIn() {
    TerminalParserTest.assertParser(ExpressionParser.notIn(NUMBER), "1 not in (2)",
        new BinaryExpression(number(1), Op.NOT_IN, TupleExpression.of(number(2))));
    TerminalParserTest.assertParser(ExpressionParser.notIn(NUMBER), "1 not in (2, 3)",
        new BinaryExpression(number(1), Op.NOT_IN, TupleExpression.of(number(2), number(3))));
  }

  @Test
  public void testCondition() {
    Parser<Expression> parser =
        ExpressionParser.condition(NUMBER, RelationParser.TABLE);
    TerminalParserTest.assertParser(parser, "1 = 2", new BinaryExpression(number(1), Op.EQ, number(2)));
    TerminalParserTest.assertParser(parser, "1 is null",
        new BinaryExpression(number(1), Op.IS, NullExpression.instance));
    TerminalParserTest.assertParser(parser, "1 is not null",
        new BinaryExpression(number(1), Op.NOT, NullExpression.instance));
    TerminalParserTest.assertParser(parser, "1 like 2", new LikeExpression(number(1), true, number(2), null));
    TerminalParserTest.assertParser(parser, "(1 < 2 or not exists t)",
        new BinaryExpression(
            new BinaryExpression(number(1), Op.LT, number(2)),
            Op.OR,
            new UnaryExpression(Op.NOT, 
                new UnaryRelationalExpression(table("t"), Op.EXISTS))
        )
    );
  }
  
  static Expression number(int i) {
    return new NumberExpression(Integer.toString(i));
  }
  
  static Expression name(String... names) {
    return QualifiedNameExpression.of(names);
  }
  
  static Expression wildcard(String... owners) {
    return new WildcardExpression(QualifiedName.of(owners));
  }
  
  static Expression function(String name, Expression... args) {
    return new FunctionExpression(QualifiedName.of(name), Arrays.asList(args));
  }
  
  @SuppressWarnings("unchecked")
  static Expression fullCase(Expression when, Expression then, Expression defaultValue) {
    return new FullCaseExpression(Arrays.asList(Tuples.pair(when, then)), defaultValue);
  }
  
  @SuppressWarnings("unchecked")
  static Expression simpleCase(
      Expression expr, Expression when, Expression then, Expression defaultValue) {
    return new SimpleCaseExpression(expr, Arrays.asList(Tuples.pair(when, then)), defaultValue);
  }
}
