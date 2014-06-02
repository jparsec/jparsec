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
package org.codehaus.jparsec.examples.java.parser;

import static org.codehaus.jparsec.Parsers.between;
import static org.codehaus.jparsec.examples.java.parser.TerminalParser.phrase;
import static org.codehaus.jparsec.examples.java.parser.TerminalParser.term;

import java.util.List;

import org.codehaus.jparsec.OperatorTable;
import org.codehaus.jparsec.Parser;
import org.codehaus.jparsec.Parsers;
import org.codehaus.jparsec.Terminals;
import org.codehaus.jparsec.examples.java.ast.declaration.DefBody;
import org.codehaus.jparsec.examples.java.ast.expression.ArrayInitializer;
import org.codehaus.jparsec.examples.java.ast.expression.ArraySubscriptExpression;
import org.codehaus.jparsec.examples.java.ast.expression.BinaryExpression;
import org.codehaus.jparsec.examples.java.ast.expression.BooleanLiteral;
import org.codehaus.jparsec.examples.java.ast.expression.CastExpression;
import org.codehaus.jparsec.examples.java.ast.expression.CharLiteral;
import org.codehaus.jparsec.examples.java.ast.expression.ClassLiteral;
import org.codehaus.jparsec.examples.java.ast.expression.ConditionalExpression;
import org.codehaus.jparsec.examples.java.ast.expression.DecimalPointNumberLiteral;
import org.codehaus.jparsec.examples.java.ast.expression.Expression;
import org.codehaus.jparsec.examples.java.ast.expression.Identifier;
import org.codehaus.jparsec.examples.java.ast.expression.InstanceOfExpression;
import org.codehaus.jparsec.examples.java.ast.expression.IntegerLiteral;
import org.codehaus.jparsec.examples.java.ast.expression.MethodCallExpression;
import org.codehaus.jparsec.examples.java.ast.expression.NewArrayExpression;
import org.codehaus.jparsec.examples.java.ast.expression.NewExpression;
import org.codehaus.jparsec.examples.java.ast.expression.NullExpression;
import org.codehaus.jparsec.examples.java.ast.expression.Operator;
import org.codehaus.jparsec.examples.java.ast.expression.PostfixUnaryExpression;
import org.codehaus.jparsec.examples.java.ast.expression.PrefixUnaryExpression;
import org.codehaus.jparsec.examples.java.ast.expression.QualifiedExpression;
import org.codehaus.jparsec.examples.java.ast.expression.ScientificNumberLiteral;
import org.codehaus.jparsec.examples.java.ast.expression.StringLiteral;
import org.codehaus.jparsec.examples.java.ast.expression.SuperExpression;
import org.codehaus.jparsec.examples.java.ast.expression.ThisExpression;
import org.codehaus.jparsec.examples.java.ast.type.ArrayTypeLiteral;
import org.codehaus.jparsec.examples.java.ast.type.TypeLiteral;
import org.codehaus.jparsec.functors.Binary;
import org.codehaus.jparsec.functors.Unary;
import org.codehaus.jparsec.misc.Mapper;

/**
 * Parses java expression.
 * 
 * @author Ben Yu
 */
public final class ExpressionParser {

  static Parser<Binary<Expression>> conditional(Parser<Expression> consequence) {
    // "? consequence :" can be think of as a right associative infix operator.
    // consequence can be the lazy expression, which is everything
    return curry(ConditionalExpression.class).infix(term("?"), consequence, term(":"));
  }
  
  static final Parser<Expression> NULL = term("null").retn(NullExpression.instance);
  
  /**
   * {@code (foo)} can be a parenthesized expression, or the prefix of a cast expression,
   * depending on whether there's an expression following.
   */
  static final Parser<Expression> castOrExpression(Parser<Expression> expr) {
    return curry(CastExpression.class)
        .sequence(term("("), TypeLiteralParser.TYPE_LITERAL, term(")"), expr)
        .or(paren(expr));
  }
  
  static final Parser<Unary<Expression>> INSTANCE_OF = curry(InstanceOfExpression.class)
      .postfix(term("instanceof"), TypeLiteralParser.TYPE_LITERAL);
  
  static final Parser<Unary<Expression>> QUALIFIED_EXPR =
      curry(QualifiedExpression.class).postfix(term("."), Terminals.Identifier.PARSER);

  static Parser<Unary<Expression>> subscript(Parser<Expression> expr) {
    return curry(ArraySubscriptExpression.class).postfix(term("["), expr, term("]"));
  }
  
  static Parser<Unary<Expression>> qualifiedMethodCall(Parser<Expression> arg) {
    return curry(MethodCallExpression.class).postfix(
        term("."), TypeLiteralParser.optionalTypeArgs(TypeLiteralParser.TYPE_LITERAL),
        Terminals.Identifier.PARSER, argumentList(arg));
  }
  
  static Parser<Unary<Expression>> qualifiedNew(Parser<Expression> arg, Parser<DefBody> body) {
    return curry(NewExpression.class).postfix(
        phrase(". new"), TypeLiteralParser.ELEMENT_TYPE_LITERAL,
        argumentList(arg), body.optional());
  }
  
  static Parser<Expression> simpleMethodCall(Parser<Expression> arg) {
    return new Mapper<Expression>() {
      @SuppressWarnings("unused")
      Expression map(String name, List<Expression> args) {
        return new MethodCallExpression(
            null, TypeLiteralParser.EMPTY_TYPE_ARGUMENT_LIST, name, args);
      }
    }.sequence(Terminals.Identifier.PARSER, argumentList(arg));
  }
  
  // new a class instance
  static Parser<Expression> simpleNewExpression(Parser<Expression> arg, Parser<DefBody> body) {
    return new Mapper<Expression>() {
      @SuppressWarnings("unused")
      Expression map(TypeLiteral type, List<Expression> args, DefBody defBody) {
        return new NewExpression(null, type, args, defBody);
      }
    }.sequence(term("new"), TypeLiteralParser.ELEMENT_TYPE_LITERAL,
        argumentList(arg), body.optional());
  }
  
  // new int[5]
  static Parser<Expression> newArrayWithExplicitLength(Parser<Expression> expr) {
    return new Mapper<Expression>() {
      @SuppressWarnings("unused")
      Expression map(TypeLiteral type, Expression length, List<Expression> values) {
        return new NewArrayExpression(type, length, values);
      }
    }.sequence(term("new"), TypeLiteralParser.TYPE_LITERAL,
        term("["), expr, term("]"),
        between(term("{"), expr.sepBy(term(",")), term("}")).optional());
  }
  
  // new int[] {...}
  static Parser<Expression> newArrayWithoutExplicitLength(Parser<Expression> expr) {
    return new Mapper<Expression>() {
      @SuppressWarnings("unused")
      Expression map(ArrayTypeLiteral type, List<Expression> values) {
        return new NewArrayExpression(type.elementType, null, values);
      }
    }.sequence(term("new"), TypeLiteralParser.ARRAY_TYPE_LITERAL,
        term("{"), expr.sepBy(term(",")), term("}"));
  }
  
  static <T> Parser<T> paren(Parser<T> parser) {
    return parser.between(term("("), term(")"));
  }

  private static Parser<List<Expression>> argumentList(Parser<Expression> arg) {
    return paren(arg.sepBy(term(",")));
  }
  
  static final Parser<Expression> THIS = curry(ThisExpression.class).sequence(
      Terminals.Identifier.PARSER.followedBy(term(".")).many(), term("this"));
  
  static final Parser<Expression> SUPER = term("super").<Expression>retn(new SuperExpression());
  
  static final Parser<Expression> IDENTIFIER =
      curry(Identifier.class).sequence(Terminals.Identifier.PARSER);
  
  static final Parser<Expression> CLASS_LITERAL = curry(ClassLiteral.class)
      .sequence(TypeLiteralParser.TYPE_LITERAL, phrase(". class"));
  
  static final Parser<Expression> INTEGER_LITERAL =
      Parsers.<Expression>tokenType(IntegerLiteral.class, "integer literal");
  
  static final Parser<Expression> DECIMAL_LITERAL =
      Parsers.<Expression>tokenType(DecimalPointNumberLiteral.class, "decimal number literal");
  
  static final Parser<Expression> STRING_LITERAL = 
      curry(StringLiteral.class).sequence(Terminals.StringLiteral.PARSER);
  
  static final Parser<Expression> CHAR_LITERAL =
      curry(CharLiteral.class).sequence(Terminals.CharLiteral.PARSER);
  
  static final Parser<Expression> BOOLEAN_LITERAL = Parsers.or(
      term("true").<Expression>retn(new BooleanLiteral(true)),
      term("false").<Expression>retn(new BooleanLiteral(false)));
  
  static final Parser<Expression> SCIENTIFIC_LITERAL =
      Parsers.<Expression>tokenType(ScientificNumberLiteral.class, "scientific number literal");

  @SuppressWarnings("unchecked")
  static final Parser<Expression> ATOM = Parsers.or(
        NULL, THIS, SUPER, CLASS_LITERAL, BOOLEAN_LITERAL, CHAR_LITERAL, STRING_LITERAL,
        SCIENTIFIC_LITERAL, INTEGER_LITERAL, DECIMAL_LITERAL, IDENTIFIER);
  
  static Parser<Expression> expression(Parser<Expression> atom, Parser<DefBody> classBody) {
    // atom is literal, name, "a.b.c.this", "super".
    Parser.Reference<Expression> ref = Parser.newReference();
    Parser<Expression> lazy = ref.lazy();
    atom = Parsers.or(castOrExpression(lazy), simpleNewExpression(lazy, classBody),
                newArrayWithExplicitLength(lazy), newArrayWithoutExplicitLength(lazy),
                simpleMethodCall(lazy), atom);
    Parser<Expression> parser = new OperatorTable<Expression>()
        .postfix(subscript(lazy), 200)
        .postfix(qualifiedMethodCall(lazy), 200)
        .postfix(qualifiedNew(lazy, classBody), 200)
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
  
  public static Parser<Expression> expression(Parser<DefBody> classBody) {
    return expression(ATOM, classBody);
  }
  
  public static Parser<Expression> arrayInitializer(Parser<Expression> expr) {
    return curry(ArrayInitializer.class)
        .sequence(term("{"), expr.sepEndBy(term(",")), term("}"));
  }

  static Parser<Expression> arrayInitializerOrRegularExpression(Parser<Expression> expr) {
    return arrayInitializer(expr).or(expr);
  }
  
  private static Parser<Binary<Expression>> binary(Operator op) {
    return term(op.toString()).next(curry(BinaryExpression.class, op).binary());
  }
  
  private static Parser<Unary<Expression>> prefix(Operator op) {
    return term(op.toString()).next(curry(PrefixUnaryExpression.class, op).unary());
  }
  
  private static Parser<Unary<Expression>> postfix(Operator op) {
    return term(op.toString()).next(curry(PostfixUnaryExpression.class, op).unary());
  }
  
  static Mapper<Expression> curry(Class<? extends Expression> clazz, Object... args) {
    return Mapper.curry(clazz, args);
  }
}
