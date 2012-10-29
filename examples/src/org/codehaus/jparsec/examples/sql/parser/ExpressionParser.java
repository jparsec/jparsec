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
package org.codehaus.jparsec.examples.sql.parser;

import static org.codehaus.jparsec.examples.sql.parser.TerminalParser.phrase;
import static org.codehaus.jparsec.examples.sql.parser.TerminalParser.term;

import java.util.List;

import org.codehaus.jparsec.OperatorTable;
import org.codehaus.jparsec.Parser;
import org.codehaus.jparsec.Parsers;
import org.codehaus.jparsec.Parser.Reference;
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
import org.codehaus.jparsec.examples.sql.ast.Relation;
import org.codehaus.jparsec.examples.sql.ast.SimpleCaseExpression;
import org.codehaus.jparsec.examples.sql.ast.StringExpression;
import org.codehaus.jparsec.examples.sql.ast.TupleExpression;
import org.codehaus.jparsec.examples.sql.ast.UnaryExpression;
import org.codehaus.jparsec.examples.sql.ast.UnaryRelationalExpression;
import org.codehaus.jparsec.examples.sql.ast.WildcardExpression;
import org.codehaus.jparsec.functors.Binary;
import org.codehaus.jparsec.functors.Pair;
import org.codehaus.jparsec.functors.Unary;
import org.codehaus.jparsec.misc.Mapper;

/**
 * Parser for expressions.
 * 
 * @author Ben Yu
 */
public final class ExpressionParser {
  
  static final Parser<Expression> NULL = term("null").<Expression>retn(NullExpression.instance);
  
  static final Parser<Expression> NUMBER =
      curry(NumberExpression.class).sequence(TerminalParser.NUMBER);
  
  static final Parser<Expression> QUALIFIED_NAME =
      curry(QualifiedNameExpression.class).sequence(TerminalParser.QUALIFIED_NAME);
  
  static final Parser<Expression> QUALIFIED_WILDCARD =
      curry(WildcardExpression.class)
      .sequence(TerminalParser.QUALIFIED_NAME, phrase(". *"));
  
  static final Parser<Expression> WILDCARD =
      term("*").<Expression>retn(new WildcardExpression(QualifiedName.of()))
      .or(QUALIFIED_WILDCARD);
  
  static final Parser<Expression> STRING =
       curry(StringExpression.class).sequence(TerminalParser.STRING);
  
  static Parser<Expression> functionCall(Parser<Expression> param) {
    return curry(FunctionExpression.class)
        .sequence(TerminalParser.QUALIFIED_NAME,
            term("("), param.sepBy(TerminalParser.term(",")), term(")"));
  }
  
  static Parser<Expression> tuple(Parser<Expression> expr) {
    return curry(TupleExpression.class)
        .sequence(term("("), expr.sepBy(term(",")), term(")"));
  }
  
  static Parser<Expression> simpleCase(Parser<Expression> expr) {
    return curry(SimpleCaseExpression.class).sequence(
        term("case"), expr, whenThens(expr, expr),
        term("else").next(expr).optional(), term("end"));
  }
  
  static Parser<Expression> fullCase(Parser<Expression> cond, Parser<Expression> expr) {
    return curry(FullCaseExpression.class).sequence(
        term("case"), whenThens(cond, expr),
        term("else").next(expr).optional(), term("end"));
  }

  private static Parser<List<Pair<Expression, Expression>>> whenThens(
      Parser<Expression> cond, Parser<Expression> expr) {
    return Parsers.pair(term("when").next(cond), term("then").next(expr)).many1();
  }
  
  static <T> Parser<T> paren(Parser<T> parser) {
    return parser.between(term("("), term(")"));
  }
  static Parser<Expression> arithmetic(Parser<Expression> atom) {
    Reference<Expression> reference = Parser.newReference();
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
    Reference<Expression> reference = Parser.newReference();
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
    return curry(LikeExpression.class)
        .sequence(expr, Parsers.or(
            term("like").retn(true), phrase("not like").retn(false)),
            expr, term("escape").next(expr).optional());
  }

  static Parser<Expression> nullCheck(Parser<Expression> expr) {
    return curry(BinaryExpression.class)
        .sequence(expr, phrase("is not").retn(Op.NOT).or(phrase("is").retn(Op.IS)), NULL);
  }
  
  static Parser<Expression> logical(Parser<Expression> expr) {
    Reference<Expression> ref = Parser.newReference();
    Parser<Expression> parser = new OperatorTable<Expression>()
      .prefix(unary("not", Op.NOT), 30)
      .infixl(binary("and", Op.AND), 20)
      .infixl(binary("or", Op.OR), 10)
      .build(paren(ref.lazy()).or(expr)).label("logical expression");
    ref.set(parser);
    return parser;
  }
  
  static Parser<Expression> between(Parser<Expression> expr) {
    return curry(BetweenExpression.class).sequence(
        expr, Parsers.or(term("between").retn(true), phrase("not between").retn(false)),
        expr, term("and"), expr);
  }
  
  static Parser<Expression> exists(Parser<Relation> relation) {
    return curry(UnaryRelationalExpression.class, Op.EXISTS)
        .sequence(term("exists"), relation);
  }
  
  static Parser<Expression> notExists(Parser<Relation> relation) {
    return curry(UnaryRelationalExpression.class, Op.NOT_EXISTS)
        .sequence(phrase("not exists"), relation);
  }
  
  static Parser<Expression> inRelation(Parser<Expression> expr, Parser<Relation> relation) {
    return curry(BinaryRelationalExpression.class, Op.IN)
        .sequence(expr, term("in"), term("("), relation, term(")"));
  }
  
  static Parser<Expression> notInRelation(Parser<Expression> expr, Parser<Relation> relation) {
    return curry(BinaryRelationalExpression.class, Op.NOT_IN)
        .sequence(expr, phrase("not in"), term("("), relation, term(")"));
  }
  
  static Parser<Expression> in(Parser<Expression> expr) {
    return binaryExpression(Op.IN).sequence(expr, term("in"), tuple(expr));
  }
  
  static Parser<Expression> notIn(Parser<Expression> expr) {
    return binaryExpression(Op.NOT_IN).sequence(expr, phrase("not in"), tuple(expr));
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
    return curry(BinaryExpression.class).sequence(operand, term(name).retn(op), operand);
  }
  
  private static Parser<Binary<Expression>> binary(String name, Op op) {
    return term(name).next(binaryExpression(op).binary());
  }
  
  private static Parser<Unary<Expression>> unary(String name, Op op) {
    return term(name).next(unaryExpression(op).unary());
  }
  
  private static Mapper<Expression> binaryExpression(Op op) {
    return curry(BinaryExpression.class, op);
  }
  
  private static Mapper<Expression> unaryExpression(Op op) {
    return curry(UnaryExpression.class, op);
  }
  
  private static Mapper<Expression> curry(Class<? extends Expression> clazz, Object... args) {
    return Mapper.curry(clazz, args);
  }
}
