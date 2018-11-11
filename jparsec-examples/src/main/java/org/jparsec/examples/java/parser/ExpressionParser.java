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

import static org.jparsec.examples.java.parser.TerminalParser.phrase;
import static org.jparsec.examples.java.parser.TerminalParser.term;

import java.util.Collections;
import java.util.List;
import java.util.function.BinaryOperator;
import java.util.function.UnaryOperator;

import org.jparsec.OperatorTable;
import org.jparsec.Parser;
import org.jparsec.Parsers;
import org.jparsec.Terminals;
import org.jparsec.examples.java.ast.declaration.DefBody;
import org.jparsec.examples.java.ast.expression.ArrayInitializer;
import org.jparsec.examples.java.ast.expression.ArraySubscriptExpression;
import org.jparsec.examples.java.ast.expression.BinaryExpression;
import org.jparsec.examples.java.ast.expression.BooleanLiteral;
import org.jparsec.examples.java.ast.expression.CastExpression;
import org.jparsec.examples.java.ast.expression.CharLiteral;
import org.jparsec.examples.java.ast.expression.ClassLiteral;
import org.jparsec.examples.java.ast.expression.ConditionalExpression;
import org.jparsec.examples.java.ast.expression.ConstructorReference;
import org.jparsec.examples.java.ast.expression.DecimalPointNumberLiteral;
import org.jparsec.examples.java.ast.expression.Expression;
import org.jparsec.examples.java.ast.expression.Identifier;
import org.jparsec.examples.java.ast.expression.InstanceOfExpression;
import org.jparsec.examples.java.ast.expression.IntegerLiteral;
import org.jparsec.examples.java.ast.expression.LambdaExpression;
import org.jparsec.examples.java.ast.expression.MethodCallExpression;
import org.jparsec.examples.java.ast.expression.MethodReference;
import org.jparsec.examples.java.ast.expression.NewArrayExpression;
import org.jparsec.examples.java.ast.expression.NewExpression;
import org.jparsec.examples.java.ast.expression.NullExpression;
import org.jparsec.examples.java.ast.expression.Operator;
import org.jparsec.examples.java.ast.expression.PostfixUnaryExpression;
import org.jparsec.examples.java.ast.expression.PrefixUnaryExpression;
import org.jparsec.examples.java.ast.expression.QualifiedExpression;
import org.jparsec.examples.java.ast.expression.ScientificNumberLiteral;
import org.jparsec.examples.java.ast.expression.StringLiteral;
import org.jparsec.examples.java.ast.expression.SuperExpression;
import org.jparsec.examples.java.ast.expression.ThisExpression;
import org.jparsec.examples.java.ast.statement.ExpressionStatement;
import org.jparsec.examples.java.ast.statement.Statement;

/**
 * Parses java expression.
 * 
 * @author Ben Yu
 */
public final class ExpressionParser {

  static Parser<BinaryOperator<Expression>> conditional(Parser<Expression> consequence) {
    // "? consequence :" can be think of as a right associative infix operator.
    // consequence can be the lazy expression, which is everything
    return consequence.between(term("?"), term(":"))
        .map(cons -> (cond, alt) -> new ConditionalExpression(cond, cons, alt));
  }
  
  static final Parser<Expression> NULL = term("null").retn(NullExpression.instance);
  
  /**
   * {@code (foo)} can be a parenthesized expression, or the prefix of a cast expression,
   * depending on whether there's an expression following.
   */
  static final Parser<Expression> castOrExpression(Parser<Expression> expr) {
    Parser<Expression> explicitCast =
        Parsers.sequence(paren(TypeLiteralParser.TYPE_LITERAL), expr, CastExpression::new);
    return explicitCast.or(paren(expr));
  }
  
  static final Parser<UnaryOperator<Expression>> INSTANCE_OF =
      term("instanceof").next(TypeLiteralParser.TYPE_LITERAL)
          .map(t -> e -> new InstanceOfExpression(e, t));
  
  static final Parser<UnaryOperator<Expression>> QUALIFIED_EXPR =
      term(".").next(Terminals.Identifier.PARSER).map(n -> e -> new QualifiedExpression(e, n));

  static Parser<UnaryOperator<Expression>> subscript(Parser<Expression> expr) {
    return expr.between(term("["), term("]")).map(i -> a -> new ArraySubscriptExpression(a, i));
  }

  static final Parser<UnaryOperator<Expression>> METHOD_REFERENCE = Parsers.sequence(
      term("::"),
      TypeLiteralParser.optionalTypeArgs(TypeLiteralParser.TYPE_LITERAL),
      Terminals.Identifier.PARSER,
      (__, typeParams, name) -> qualifier -> new MethodReference(qualifier, typeParams, name));

  static final Parser<UnaryOperator<Expression>> CONSTRUCTOR_REFERENCE =
      phrase(":: new").retn(ConstructorReference::new);
  
  static Parser<UnaryOperator<Expression>> qualifiedMethodCall(Parser<Expression> arg) {
    return Parsers.sequence(
        term(".").next(TypeLiteralParser.optionalTypeArgs(TypeLiteralParser.TYPE_LITERAL)),
        Terminals.Identifier.PARSER,
        argumentList(arg),
        (t, m, a) -> q -> new MethodCallExpression(q, t, m, a));
  }
  
  static Parser<UnaryOperator<Expression>> qualifiedNew(Parser<Expression> arg, Parser<DefBody> body) {
    return Parsers.sequence(
        phrase(". new").next(TypeLiteralParser.ELEMENT_TYPE_LITERAL),
        argumentList(arg),
        body.optional(),
        (t, a, b) -> q -> new NewExpression(q, t, a, b));
  }
  
  static Parser<Expression> simpleMethodCall(Parser<Expression> arg) {
    return Parsers.sequence(
        Terminals.Identifier.PARSER, argumentList(arg),
        (name, args) ->
            new MethodCallExpression(null, TypeLiteralParser.EMPTY_TYPE_ARGUMENT_LIST, name, args));
  }
  
  // new a class instance
  static Parser<Expression> simpleNewExpression(Parser<Expression> arg, Parser<DefBody> body) {
    return Parsers.sequence(
        term("new").next(TypeLiteralParser.ELEMENT_TYPE_LITERAL),
        argumentList(arg),
        body.optional(),
        (type, args, defBody) -> new NewExpression(null, type, args, defBody));
  }
  
  // new int[5]
  static Parser<Expression> newArrayWithExplicitLength(Parser<Expression> expr) {
    return Parsers.sequence(term("new").next(TypeLiteralParser.TYPE_LITERAL),
        expr.between(term("["), term("]")),
        Parsers.between(term("{"), expr.sepBy(term(",")), term("}")).optional(),
        NewArrayExpression::new);
  }
  
  // new int[] {...}
  static Parser<Expression> newArrayWithoutExplicitLength(Parser<Expression> expr) {
    return Parsers.sequence(
        term("new").next(TypeLiteralParser.ARRAY_TYPE_LITERAL),
        Parsers.between(term("{"), expr.sepBy(term(",")), term("}")),
        (type, values) -> new NewArrayExpression(type.elementType, null, values));
  }
  
  static <T> Parser<T> paren(Parser<T> parser) {
    return parser.between(term("("), term(")"));
  }

  private static Parser<List<Expression>> argumentList(Parser<Expression> arg) {
    return paren(arg.sepBy(term(",")));
  }
  
  static final Parser<Expression> THIS = Terminals.Identifier.PARSER.followedBy(term(".")).many()
      .followedBy(term("this"))
      .map(ThisExpression::new);
  
  static final Parser<Expression> SUPER = term("super").<Expression>retn(new SuperExpression());
  
  static final Parser<Expression> IDENTIFIER = Terminals.Identifier.PARSER.map(Identifier::new);
  
  static final Parser<Expression> CLASS_LITERAL = TypeLiteralParser.TYPE_LITERAL
      .followedBy(phrase(". class"))
      .map(ClassLiteral::new);
  
  static final Parser<Expression> INTEGER_LITERAL =
      Parsers.<Expression>tokenType(IntegerLiteral.class, "integer literal");
  
  static final Parser<Expression> DECIMAL_LITERAL =
      Parsers.<Expression>tokenType(DecimalPointNumberLiteral.class, "decimal number literal");
  
  static final Parser<Expression> STRING_LITERAL = Terminals.StringLiteral.PARSER.map(StringLiteral::new);
  
  static final Parser<Expression> CHAR_LITERAL = Terminals.CharLiteral.PARSER.map(CharLiteral::new);
  
  static final Parser<Expression> BOOLEAN_LITERAL = Parsers.or(
      term("true").<Expression>retn(new BooleanLiteral(true)),
      term("false").<Expression>retn(new BooleanLiteral(false)));
  
  static final Parser<Expression> SCIENTIFIC_LITERAL =
      Parsers.<Expression>tokenType(ScientificNumberLiteral.class, "scientific number literal");

  @SuppressWarnings("unchecked")
  static final Parser<Expression> ATOM = Parsers.or(
        NULL, THIS, SUPER, CLASS_LITERAL, BOOLEAN_LITERAL, CHAR_LITERAL, STRING_LITERAL,
        SCIENTIFIC_LITERAL, INTEGER_LITERAL, DECIMAL_LITERAL, IDENTIFIER);

  static Parser<LambdaExpression> lambdaExpression(
      Parser<Expression> expression, Parser<Statement> stmt) {
    Parser<LambdaExpression.Parameter> typedParam = Parsers.sequence(
        TypeLiteralParser.TYPE_LITERAL, Terminals.Identifier.PARSER,
        LambdaExpression.Parameter::new);
    Parser<LambdaExpression.Parameter> simpleParam = Terminals.Identifier.PARSER.map(LambdaExpression.Parameter::new);
    Parser<LambdaExpression.Parameter> lambdaParam = typedParam.or(simpleParam);
    Parser<List<LambdaExpression.Parameter>> params =
        paren(lambdaParam.sepBy(term(","))).or(lambdaParam.map(Collections::singletonList));
    Parser<Statement> body = StatementParser.blockStatement(stmt).<Statement>cast()
        .or(expression.map(ExpressionStatement::new));
    return Parsers.sequence(params, term("->").next(body), LambdaExpression::new);
  }
 
  static Parser<Expression> expression(
      Parser<Expression> atom, Parser<DefBody> classBody, Parser<Statement> statement) {
    // atom is literal, name, "a.b.c.this", "super".
    Parser.Reference<Expression> ref = Parser.newReference();
    Parser<Expression> lazy = ref.lazy();
    atom = Parsers.or(castOrExpression(lazy), simpleNewExpression(lazy, classBody),
                newArrayWithExplicitLength(lazy), newArrayWithoutExplicitLength(lazy),
                simpleMethodCall(lazy), lambdaExpression(lazy, statement),
                atom);
    Parser<Expression> parser = new OperatorTable<Expression>()
        .postfix(subscript(lazy), 200)
        .postfix(qualifiedMethodCall(lazy), 200)
        .postfix(qualifiedNew(lazy, classBody), 200)
        .postfix(METHOD_REFERENCE,  200)
        .postfix(CONSTRUCTOR_REFERENCE, 20)
        .postfix(QUALIFIED_EXPR, 200)
        .postfix(postfix(Operator.POST_INC), 200)
        .postfix(postfix(Operator.POST_DEC), 200)
        .prefix(prefix(Operator.INC), 190)
        .prefix(prefix(Operator.DEC), 190)
        .prefix(prefix(Operator.POSITIVE), 190)
        .prefix(prefix(Operator.NEGATIVE), 190)
        .prefix(prefix(Operator.INC), 190)
        .prefix(prefix(Operator.DEC), 190)
        .prefix(prefix(Operator.NOT), 190)
        .prefix(prefix(Operator.BITWISE_NOT), 190)
        .infixl(binary(Operator.MUL), 100)
        .infixl(binary(Operator.DIV), 100)
        .infixl(binary(Operator.MOD), 100)
        .infixl(binary(Operator.DIV), 100)
        .infixl(binary(Operator.PLUS), 90)
        .infixl(binary(Operator.MINUS), 90)
        .infixl(binary(Operator.LSHIFT), 80)
        .infixl(binary(Operator.RSHIFT), 80)
        .infixl(binary(Operator.UNSIGNED_RSHIFT), 80)
        .infixl(binary(Operator.LE), 70)
        .infixl(binary(Operator.GE), 70)
        .infixl(binary(Operator.LT), 70)
        .infixl(binary(Operator.GT), 70)
        .postfix(INSTANCE_OF, 65)
        .infixl(binary(Operator.EQ), 60)
        .infixl(binary(Operator.NE), 60)
        .infixl(binary(Operator.BITWISE_AND), 50)
        .infixl(binary(Operator.BITWISE_XOR), 40)
        .infixl(binary(Operator.BITWISE_OR), 30)
        .infixl(binary(Operator.AND), 20)
        .infixl(binary(Operator.OR), 10)
        .infixr(conditional(lazy), 5)
        .infixr(binary(Operator.ASSIGNMENT), 0)
        .infixr(binary(Operator.APLUS), 0)
        .infixr(binary(Operator.AMINUS), 0)
        .infixr(binary(Operator.AMUL), 0)
        .infixr(binary(Operator.ADIV), 0)
        .infixr(binary(Operator.AMOD), 0)
        .infixr(binary(Operator.AAND), 0)
        .infixr(binary(Operator.AOR), 0)
        .infixr(binary(Operator.AXOR), 0)
        .infixr(binary(Operator.ALSHIFT), 0)
        .infixr(binary(Operator.ARSHIFT), 0)
        .infixr(binary(Operator.UNSIGNED_ARSHIFT), 0)
        .build(atom);
    ref.set(parser);
    return parser;
  }
  
  public static Parser<Expression> expression(
      Parser<DefBody> classBody, Parser<Statement> statement) {
    return expression(ATOM, classBody, statement);
  }
  
  public static Parser<Expression> arrayInitializer(Parser<Expression> expr) {
    return expr.sepEndBy(term(",")).between(term("{"), term("}")).map(ArrayInitializer::new);
  }

  static Parser<Expression> arrayInitializerOrRegularExpression(Parser<Expression> expr) {
    return arrayInitializer(expr).or(expr);
  }
  
  private static Parser<BinaryOperator<Expression>> binary(Operator op) {
    return term(op.toString()).retn((l, r) -> new BinaryExpression(l, op, r));
  }
  
  private static Parser<UnaryOperator<Expression>> prefix(Operator op) {
    return term(op.toString()).retn(e -> new PrefixUnaryExpression(op, e));
  }
  
  private static Parser<UnaryOperator<Expression>> postfix(Operator op) {
    return term(op.toString()).retn(e -> new PostfixUnaryExpression(e, op));
  }
}
