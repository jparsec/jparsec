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

import static org.jparsec.examples.sql.parser.TerminalParser.phrase;
import static org.jparsec.examples.sql.parser.TerminalParser.term;

import java.util.List;
import java.util.function.BinaryOperator;
import java.util.function.UnaryOperator;

import org.jparsec.OperatorTable;
import org.jparsec.Parser;
import org.jparsec.Parsers;
import org.jparsec.examples.sql.ast.BetweenExpression;
import org.jparsec.examples.sql.ast.BinaryExpression;
import org.jparsec.examples.sql.ast.BinaryRelationalExpression;
import org.jparsec.examples.sql.ast.Expression;
import org.jparsec.examples.sql.ast.FullCaseExpression;
import org.jparsec.examples.sql.ast.FunctionExpression;
import org.jparsec.examples.sql.ast.LikeExpression;
import org.jparsec.examples.sql.ast.NullExpression;
import org.jparsec.examples.sql.ast.NumberExpression;
import org.jparsec.examples.sql.ast.Op;
import org.jparsec.examples.sql.ast.QualifiedName;
import org.jparsec.examples.sql.ast.QualifiedNameExpression;
import org.jparsec.examples.sql.ast.Relation;
import org.jparsec.examples.sql.ast.SimpleCaseExpression;
import org.jparsec.examples.sql.ast.StringExpression;
import org.jparsec.examples.sql.ast.TupleExpression;
import org.jparsec.examples.sql.ast.UnaryExpression;
import org.jparsec.examples.sql.ast.UnaryRelationalExpression;
import org.jparsec.examples.sql.ast.WildcardExpression;
import org.jparsec.functors.Pair;

/**
 * Parser for expressions.
 * 
 * @author Ben Yu
 */
public final class ExpressionParser {
  
  static final Parser<Expression> NULL = term("null").<Expression>retn(NullExpression.instance);
  
  static final Parser<Expression> NUMBER = TerminalParser.NUMBER.map(NumberExpression::new);
  
  static final Parser<Expression> QUALIFIED_NAME = TerminalParser.QUALIFIED_NAME
      .map(QualifiedNameExpression::new);
  
  static final Parser<Expression> QUALIFIED_WILDCARD = TerminalParser.QUALIFIED_NAME
      .followedBy(phrase(". *"))
      .map(WildcardExpression::new);
  
  static final Parser<Expression> WILDCARD =
      term("*").<Expression>retn(new WildcardExpression(QualifiedName.of()))
      .or(QUALIFIED_WILDCARD);
  
  static final Parser<Expression> STRING = TerminalParser.STRING.map(StringExpression::new);
  
  static Parser<Expression> functionCall(Parser<Expression> param) {
    return Parsers.sequence(
        TerminalParser.QUALIFIED_NAME, paren(param.sepBy(TerminalParser.term(","))),
        FunctionExpression::new);
  }
  
  static Parser<Expression> tuple(Parser<Expression> expr) {
    return paren(expr.sepBy(term(","))).map(TupleExpression::new);
  }
  
  static Parser<Expression> simpleCase(Parser<Expression> expr) {
    return Parsers.sequence(
        term("case").next(expr),
        whenThens(expr, expr),
        term("else").next(expr).optional().followedBy(term("end")),
        SimpleCaseExpression::new);
  }
  
  static Parser<Expression> fullCase(Parser<Expression> cond, Parser<Expression> expr) {
    return Parsers.sequence(
        term("case").next(whenThens(cond, expr)),
        term("else").next(expr).optional().followedBy(term("end")),
        FullCaseExpression::new);
  }

  private static Parser<List<Pair<Expression, Expression>>> whenThens(
      Parser<Expression> cond, Parser<Expression> expr) {
    return Parsers.pair(term("when").next(cond), term("then").next(expr)).many1();
  }
  
  static <T> Parser<T> paren(Parser<T> parser) {
    return parser.between(term("("), term(")"));
  }
  static Parser<Expression> arithmetic(Parser<Expression> atom) {
    Parser.Reference<Expression> reference = Parser.newReference();
    Parser<Expression> operand =
        Parsers.or(paren(reference.lazy()), functionCall(reference.lazy()), atom);
    Parser<Expression> parser = new OperatorTable<Expression>()
        .infixl(binary("+", Op.PLUS), 10)
        .infixl(binary("-", Op.MINUS), 10)
        .infixl(binary("*", Op.MUL), 20)
        .infixl(binary("/", Op.DIV), 20)
        .infixl(binary("%", Op.MOD), 20)
        .prefix(unary("-", Op.NEG), 50)
        .build(operand);
    reference.set(parser);
    return parser;
  }
  
  static Parser<Expression> expression(Parser<Expression> cond) {
    Parser.Reference<Expression> reference = Parser.newReference();
    Parser<Expression> lazyExpr = reference.lazy();
    Parser<Expression> atom = Parsers.or(
        NUMBER, WILDCARD, QUALIFIED_NAME, simpleCase(lazyExpr), fullCase(cond, lazyExpr));
    Parser<Expression> expression = arithmetic(atom).label("expression");
    reference.set(expression);
    return expression;
  }
  
  /************************** boolean expressions ****************************/
  
  static Parser<Expression> compare(Parser<Expression> expr) {
    return Parsers.or(
        compare(expr, ">", Op.GT), compare(expr, ">=", Op.GE),
        compare(expr, "<", Op.LT), compare(expr, "<=", Op.LE),
        compare(expr, "=", Op.EQ), compare(expr, "<>", Op.NE),
        nullCheck(expr), like(expr), between(expr));
  }

  static Parser<Expression> like(Parser<Expression> expr) {
    return Parsers.sequence(
        expr, Parsers.or(term("like").retn(true), phrase("not like").retn(false)),
        expr, term("escape").next(expr).optional(),
        LikeExpression::new);
  }

  static Parser<Expression> nullCheck(Parser<Expression> expr) {
    return Parsers.sequence(
        expr, phrase("is not").retn(Op.NOT).or(phrase("is").retn(Op.IS)), NULL,
        BinaryExpression::new);
  }
  
  static Parser<Expression> logical(Parser<Expression> expr) {
    Parser.Reference<Expression> ref = Parser.newReference();
    Parser<Expression> parser = new OperatorTable<Expression>()
      .prefix(unary("not", Op.NOT), 30)
      .infixl(binary("and", Op.AND), 20)
      .infixl(binary("or", Op.OR), 10)
      .build(paren(ref.lazy()).or(expr)).label("logical expression");
    ref.set(parser);
    return parser;
  }
  
  static Parser<Expression> between(Parser<Expression> expr) {
    return Parsers.sequence(
        expr, Parsers.or(term("between").retn(true), phrase("not between").retn(false)),
        expr, term("and").next(expr),
        BetweenExpression::new);
  }
  
  static Parser<Expression> exists(Parser<Relation> relation) {
    return term("exists").next(relation).map(e -> new UnaryRelationalExpression(e, Op.EXISTS));
  }
  
  static Parser<Expression> notExists(Parser<Relation> relation) {
    return phrase("not exists").next(relation)
        .map(e -> new UnaryRelationalExpression(e, Op.NOT_EXISTS));
  }
  
  static Parser<Expression> inRelation(Parser<Expression> expr, Parser<Relation> relation) {
    return Parsers.sequence(
        expr, Parsers.between(phrase("in ("), relation, term(")")),
        (e, r) -> new BinaryRelationalExpression(e, Op.IN, r));
  }
  
  static Parser<Expression> notInRelation(Parser<Expression> expr, Parser<Relation> relation) {
    return Parsers.sequence(
        expr, Parsers.between(phrase("not in ("), relation, term(")")),
        (e, r) -> new BinaryRelationalExpression(e, Op.NOT_IN, r));
  }
  
  static Parser<Expression> in(Parser<Expression> expr) {
    return Parsers.sequence(
        expr, term("in").next(tuple(expr)),
        (e, t) -> new BinaryExpression(e, Op.IN, t));
  }
  
  static Parser<Expression> notIn(Parser<Expression> expr) {
    return Parsers.sequence(
        expr, phrase("not in").next(tuple(expr)),
        (e, t) -> new BinaryExpression(e, Op.NOT_IN, t));
  }
  
  static Parser<Expression> condition(Parser<Expression> expr, Parser<Relation> rel) {
    Parser<Expression> atom = Parsers.or(
        compare(expr), in(expr), notIn(expr),
        exists(rel), notExists(rel), inRelation(expr, rel), notInRelation(expr, rel));
    return logical(atom);
  }
  
  /************************** utility methods ****************************/
  
  private static Parser<Expression> compare(
      Parser<Expression> operand, String name, Op op) {
    return Parsers.sequence(
        operand, term(name).retn(op), operand,
        BinaryExpression::new);
  }
  
  private static Parser<BinaryOperator<Expression>> binary(String name, Op op) {
    return term(name).retn((l, r) -> new BinaryExpression(l, op, r));
  }
  
  private static Parser<UnaryOperator<Expression>> unary(String name, Op op) {
    return term(name).retn(e -> new UnaryExpression(op, e));
  }
}
