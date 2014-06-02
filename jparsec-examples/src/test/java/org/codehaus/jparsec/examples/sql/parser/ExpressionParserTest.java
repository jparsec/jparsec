package org.codehaus.jparsec.examples.sql.parser;

import static org.codehaus.jparsec.examples.sql.parser.ExpressionParser.NUMBER;
import static org.codehaus.jparsec.examples.sql.parser.ExpressionParser.QUALIFIED_NAME;
import static org.codehaus.jparsec.examples.sql.parser.RelationParserTest.table;
import static org.codehaus.jparsec.examples.sql.parser.TerminalParserTest.assertFailure;
import static org.codehaus.jparsec.examples.sql.parser.TerminalParserTest.assertParser;

import java.util.Arrays;

import org.codehaus.jparsec.Parser;
import org.codehaus.jparsec.examples.sql.ast.BetweenExpression;
import org.codehaus.jparsec.examples.sql.ast.BinaryExpression;
import org.codehaus.jparsec.examples.sql.ast.BinaryRelationalExpression;
import org.codehaus.jparsec.examples.sql.ast.Expression;
import org.codehaus.jparsec.examples.sql.ast.FullCaseExpression;
import org.codehaus.jparsec.examples.sql.ast.FunctionExpression;
import org.codehaus.jparsec.examples.sql.ast.LikeExpression;
import org.codehaus.jparsec.examples.sql.ast.NullExpression;
import org.codehaus.jparsec.examples.sql.ast.NumberExpression;
import org.codehaus.jparsec.examples.sql.ast.Op;
import org.codehaus.jparsec.examples.sql.ast.QualifiedName;
import org.codehaus.jparsec.examples.sql.ast.QualifiedNameExpression;
import org.codehaus.jparsec.examples.sql.ast.SimpleCaseExpression;
import org.codehaus.jparsec.examples.sql.ast.StringExpression;
import org.codehaus.jparsec.examples.sql.ast.TupleExpression;
import org.codehaus.jparsec.examples.sql.ast.UnaryExpression;
import org.codehaus.jparsec.examples.sql.ast.UnaryRelationalExpression;
import org.codehaus.jparsec.examples.sql.ast.WildcardExpression;
import org.codehaus.jparsec.functors.Tuples;
import org.junit.Test;

/**
 * Unit test for {@link ExpressionParser}.
 * 
 * @author Ben Yu
 */
public class ExpressionParserTest {

  @Test
  public void testNumber() {
    assertParser(ExpressionParser.NUMBER, "1.2", new NumberExpression("1.2"));
  }

  @Test
  public void testQualifiedName() {
    Parser<Expression> parser = QUALIFIED_NAME;
    assertParser(parser, "a", name("a"));
    assertParser(parser, "a . bc", name("a", "bc"));
  }

  @Test
  public void testQualifiedWildcard() {
    assertParser(ExpressionParser.QUALIFIED_WILDCARD, "a.b.*",
        new WildcardExpression(QualifiedName.of("a", "b")));
  }

  @Test
  public void testWildcard() {
    assertParser(ExpressionParser.WILDCARD, "a.b.*",
        new WildcardExpression(QualifiedName.of("a", "b")));
    assertParser(ExpressionParser.WILDCARD, "*",
        new WildcardExpression(QualifiedName.of()));
  }

  @Test
  public void testString() {
    assertParser(ExpressionParser.STRING, "'foo'", new StringExpression("foo"));
  }

  @Test
  public void testFunctionCall() {
    Parser<Expression> parser = ExpressionParser.functionCall(NUMBER);
    assertParser(parser, "f()", FunctionExpression.of(QualifiedName.of("f")));
    assertParser(parser, "a.b(1)",
        FunctionExpression.of(QualifiedName.of("a", "b"), number(1)));
    assertParser(parser, "a.b(1, 2)",
        FunctionExpression.of(QualifiedName.of("a", "b"), number(1), number(2)));
  }

  @Test
  public void testBetween() {
    Parser<Expression> parser = ExpressionParser.between(NUMBER);
    assertParser(parser, "1 BETWEEN 0 and 2",
        new BetweenExpression(number(1), true, number(0), number(2)));
    assertParser(parser, "1 not between 2 and 0",
        new BetweenExpression(number(1), false, number(2), number(0)));
  }

  @Test
  public void testTuple() {
    Parser<Expression> parser = ExpressionParser.tuple(NUMBER);
    assertParser(parser, "()", TupleExpression.of());
    assertParser(parser, "(1)", TupleExpression.of(number(1)));
    assertParser(parser, "(1, 2)", TupleExpression.of(number(1), number(2)));
    assertFailure(parser, "1", 1, 1, "( expected, 1 encountered.");
    assertFailure(parser, "", 1, 1);
  }

  @Test
  public void testSimpleCase() {
    Parser<Expression> parser = ExpressionParser.simpleCase(NUMBER);
    assertParser(parser, "case 1 when 1 then 2 else 3 end",
        simpleCase(number(1), number(1), number(2), number(3)));
    assertParser(parser, "case 1 when 1 then 2 end",
        simpleCase(number(1), number(1), number(2), null));
  }

  @Test
  public void testFullCase() {
    Parser<Expression> parser =
        ExpressionParser.fullCase(ExpressionParser.QUALIFIED_NAME, NUMBER);
    assertParser(parser, "case when a then 2 else 3 end",
        fullCase(name("a"), number(2), number(3)));
    assertParser(parser, "case when a then 2 end", fullCase(name("a"), number(2), null));
  }

  @Test
  public void testArithmetic() {
    Parser<Expression> parser = ExpressionParser.arithmetic(NUMBER);
    assertParser(parser, "1", number(1));
    assertParser(parser, "((1))", number(1));
    assertParser(parser, "1 + 2", new BinaryExpression(number(1), Op.PLUS, number(2)));
    assertParser(parser, "2 * (1 + (2))",
        new BinaryExpression(number(2), Op.MUL,
            new BinaryExpression(number(1), Op.PLUS, number(2))));
    assertParser(parser, "2 - 1 / (2)",
        new BinaryExpression(number(2), Op.MINUS,
            new BinaryExpression(number(1), Op.DIV, number(2))));
    assertParser(parser, "2 * 1 % -2",
        new BinaryExpression(
            new BinaryExpression(number(2), Op.MUL, number(1)),
                Op.MOD, new UnaryExpression(Op.NEG, number(2))));
    assertParser(parser, "f(1)", FunctionExpression.of(QualifiedName.of("f"), number(1)));
    assertParser(parser, "foo.bar(1, 2) + baz(foo.bar(1 / 2))",
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
    assertParser(parser,
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
    assertParser(parser, "1 = 1", new BinaryExpression(number(1), Op.EQ, number(1)));
    assertParser(parser, "1 < 2", new BinaryExpression(number(1), Op.LT, number(2)));
    assertParser(parser, "1 <= 2", new BinaryExpression(number(1), Op.LE, number(2)));
    assertParser(parser, "1 <> 2", new BinaryExpression(number(1), Op.NE, number(2)));
    assertParser(parser, "2 > 1", new BinaryExpression(number(2), Op.GT, number(1)));
    assertParser(parser, "2 >= 1", new BinaryExpression(number(2), Op.GE, number(1)));
    assertParser(parser, "1 is null",
        new BinaryExpression(number(1), Op.IS, NullExpression.instance));
    assertParser(parser, "1 is not null",
        new BinaryExpression(number(1), Op.NOT, NullExpression.instance));
    assertParser(parser, "1 like 2", new LikeExpression(number(1), true, number(2), null));
    assertParser(parser, "1 BETWEEN 0 and 2",
        new BetweenExpression(number(1), true, number(0), number(2)));
    assertParser(parser, "1 not between 2 and 0",
        new BetweenExpression(number(1), false, number(2), number(0)));
  }

  @Test
  public void testLike() {
    Parser<Expression> parser = ExpressionParser.like(NUMBER);
    assertParser(parser, "1 like 2", new LikeExpression(number(1), true, number(2), null));
    assertParser(parser, "1 not like 2", new LikeExpression(number(1), false, number(2), null));
    assertParser(parser, "1 like 2 escape 3",
        new LikeExpression(number(1), true, number(2), number(3)));
    assertParser(parser, "1 not like 2 escape 3",
        new LikeExpression(number(1), false, number(2), number(3)));
  }

  @Test
  public void testLogical() {
    Parser<Expression> parser = ExpressionParser.logical(NUMBER);
    assertParser(parser, "1", number(1));
    assertParser(parser, "(1)", number(1));
    assertParser(parser, "((1))", number(1));
    assertParser(parser, "not 1", new UnaryExpression(Op.NOT, number(1)));
    assertParser(parser, "1 and 2", new BinaryExpression(number(1), Op.AND, number(2)));
    assertParser(parser, "1 or 2", new BinaryExpression(number(1), Op.OR, number(2)));
    assertParser(parser, "1 or 2 and 3", new BinaryExpression(number(1), Op.OR,
        new BinaryExpression(number(2), Op.AND, number(3))));
    assertParser(parser, "1 or NOT 2", new BinaryExpression(number(1), Op.OR,
        new UnaryExpression(Op.NOT, number(2))));
    assertParser(parser, "not 1 and 2", new BinaryExpression(
        new UnaryExpression(Op.NOT, number(1)), Op.AND, number(2)));
  }

  @Test
  public void testExists() {
    assertParser(ExpressionParser.exists(RelationParser.TABLE), "exists t",
        new UnaryRelationalExpression(table("t"), Op.EXISTS));
  }

  @Test
  public void testNotExists() {
    assertParser(ExpressionParser.notExists(RelationParser.TABLE), "not exists t",
        new UnaryRelationalExpression(table("t"), Op.NOT_EXISTS));
  }

  @Test
  public void testInRelation() {
    assertParser(ExpressionParser.inRelation(NUMBER, RelationParser.TABLE),
        "1 in (table)",
        new BinaryRelationalExpression(number(1), Op.IN, table("table")));
  }

  @Test
  public void testNotInRelation() {
    assertParser(ExpressionParser.notInRelation(NUMBER, RelationParser.TABLE),
        "1 not in (table)",
        new BinaryRelationalExpression(number(1), Op.NOT_IN, table("table")));
  }

  @Test
  public void testIn() {
    assertParser(ExpressionParser.in(NUMBER), "1 in (2)",
        new BinaryExpression(number(1), Op.IN, TupleExpression.of(number(2))));
    assertParser(ExpressionParser.in(NUMBER), "1 in (2, 3)",
        new BinaryExpression(number(1), Op.IN, TupleExpression.of(number(2), number(3))));
  }

  @Test
  public void testNotIn() {
    assertParser(ExpressionParser.notIn(NUMBER), "1 not in (2)",
        new BinaryExpression(number(1), Op.NOT_IN, TupleExpression.of(number(2))));
    assertParser(ExpressionParser.notIn(NUMBER), "1 not in (2, 3)",
        new BinaryExpression(number(1), Op.NOT_IN, TupleExpression.of(number(2), number(3))));
  }

  @Test
  public void testCondition() {
    Parser<Expression> parser =
        ExpressionParser.condition(NUMBER, RelationParser.TABLE);
    assertParser(parser, "1 = 2", new BinaryExpression(number(1), Op.EQ, number(2)));
    assertParser(parser, "1 is null",
        new BinaryExpression(number(1), Op.IS, NullExpression.instance));
    assertParser(parser, "1 is not null",
        new BinaryExpression(number(1), Op.NOT, NullExpression.instance));
    assertParser(parser, "1 like 2", new LikeExpression(number(1), true, number(2), null));
    assertParser(parser, "(1 < 2 or not exists t)",
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
