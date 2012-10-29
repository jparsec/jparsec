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
import static org.codehaus.jparsec.examples.java.parser.ExpressionParser.paren;
import static org.codehaus.jparsec.examples.java.parser.TerminalParser.phrase;
import static org.codehaus.jparsec.examples.java.parser.TerminalParser.term;
import static org.codehaus.jparsec.examples.java.parser.TypeLiteralParser.TYPE_LITERAL;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.jparsec.Parser;
import org.codehaus.jparsec.Parsers;
import org.codehaus.jparsec.Terminals;
import org.codehaus.jparsec.examples.java.ast.expression.Expression;
import org.codehaus.jparsec.examples.java.ast.statement.Annotation;
import org.codehaus.jparsec.examples.java.ast.statement.AssertStatement;
import org.codehaus.jparsec.examples.java.ast.statement.BlockStatement;
import org.codehaus.jparsec.examples.java.ast.statement.BreakStatement;
import org.codehaus.jparsec.examples.java.ast.statement.ContinueStatement;
import org.codehaus.jparsec.examples.java.ast.statement.DoWhileStatement;
import org.codehaus.jparsec.examples.java.ast.statement.ExpressionListStatement;
import org.codehaus.jparsec.examples.java.ast.statement.ExpressionStatement;
import org.codehaus.jparsec.examples.java.ast.statement.ForStatement;
import org.codehaus.jparsec.examples.java.ast.statement.ForeachStatement;
import org.codehaus.jparsec.examples.java.ast.statement.IfStatement;
import org.codehaus.jparsec.examples.java.ast.statement.LabelStatement;
import org.codehaus.jparsec.examples.java.ast.statement.Modifier;
import org.codehaus.jparsec.examples.java.ast.statement.NopStatement;
import org.codehaus.jparsec.examples.java.ast.statement.ParameterDef;
import org.codehaus.jparsec.examples.java.ast.statement.ReturnStatement;
import org.codehaus.jparsec.examples.java.ast.statement.Statement;
import org.codehaus.jparsec.examples.java.ast.statement.SuperCallStatement;
import org.codehaus.jparsec.examples.java.ast.statement.SwitchStatement;
import org.codehaus.jparsec.examples.java.ast.statement.SynchronizedBlockStatement;
import org.codehaus.jparsec.examples.java.ast.statement.SystemModifier;
import org.codehaus.jparsec.examples.java.ast.statement.ThisCallStatement;
import org.codehaus.jparsec.examples.java.ast.statement.ThrowStatement;
import org.codehaus.jparsec.examples.java.ast.statement.TryStatement;
import org.codehaus.jparsec.examples.java.ast.statement.VarStatement;
import org.codehaus.jparsec.examples.java.ast.statement.WhileStatement;
import org.codehaus.jparsec.functors.Unary;
import org.codehaus.jparsec.misc.Mapper;

/**
 * Parses a statement.
 * 
 * @author Ben Yu
 */
public final class StatementParser {
  
  static Parser<Modifier> systemModifier(SystemModifier... modifiers) {
    List<Parser<Modifier>> list = new ArrayList<Parser<Modifier>>(modifiers.length);
    for (Modifier modifier : modifiers) {
      list.add(term(modifier.toString()).retn(modifier));
    }
    return Parsers.or(list);
  }
  
  static final Parser<Modifier> SYSTEM_MODIFIER = systemModifier(SystemModifier.values());
  
  static Parser<Annotation> annotation(Parser<Expression> expr) {
    Parser<Annotation.Element> element = Mapper.curry(Annotation.Element.class).sequence(
        Terminals.Identifier.PARSER.followedBy(term("=")).atomic().optional(),
        ExpressionParser.arrayInitializerOrRegularExpression(expr));
    return Mapper.curry(Annotation.class).sequence(
        term("@"), TypeLiteralParser.ELEMENT_TYPE_LITERAL,
        paren(element.sepBy(term(","))).optional());
  }
  
  static Parser<Modifier> modifier(Parser<Expression> expr) {
    return Parsers.or(annotation(expr), SYSTEM_MODIFIER);
  }
  
  static final Parser<Statement> NOP = term(";").retn(NopStatement.instance);
  
  static final Parser<Unary<Statement>> LABEL =
      curry(LabelStatement.class).prefix(Terminals.Identifier.PARSER, term(":")).atomic();
  
  static final Parser<Statement> BREAK = curry(BreakStatement.class)
      .sequence(term("break"), Terminals.Identifier.PARSER.optional(), term(";"));
  
  static final Parser<Statement> CONTINUE = curry(ContinueStatement.class)
      .sequence(term("continue"), Terminals.Identifier.PARSER.optional(), term(";"));
  
  static Parser<Statement> returnStatement(Parser<Expression> expr) {
    return curry(ReturnStatement.class)
        .sequence(term("return"), expr.optional(), term(";"));
  }
  
  static Parser<Statement> blockStatement(Parser<Statement> stmt) {
    return curry(BlockStatement.class).sequence(term("{"), stmt.many(), term("}"));
  }
  
  static Parser<Statement> whileStatement(Parser<Expression> expr, Parser<Statement> stmt) {
    return curry(WhileStatement.class).sequence(
        phrase("while ("), expr, term(")"), stmt);
  }
  
  static Parser<Statement> doWhileStatement(Parser<Statement> stmt, Parser<Expression> expr) {
    return curry(DoWhileStatement.class)
        .sequence(term("do"), stmt, phrase("while ("), expr, phrase(") ;"));
  }
  
  static Parser<Statement> ifStatement(Parser<Expression> expr, Parser<Statement> stmt) {
    return curry(IfStatement.class)
        .sequence(phrase("if ("), expr, term(")"), stmt, 
            Parsers.pair(between(phrase("else if ("), expr, term(")")), stmt).many(),
            term("else").next(stmt).optional());
  }
  
  static Parser<Statement> switchStatement(Parser<Expression> expr, Parser<Statement> stmt) {
    return curry(SwitchStatement.class)
        .sequence(phrase("switch ("), expr, phrase(") {"),
            Parsers.pair(between(term("case"), expr, term(":")), stmt.optional()).many(),
            phrase("default :").next(stmt.optional()).optional(), term("}"));
  }
  
  static Parser<Statement> foreachStatement(Parser<Expression> expr, Parser<Statement> stmt) {
    return curry(ForeachStatement.class).sequence(
        phrase("for ("), TypeLiteralParser.TYPE_LITERAL, Terminals.Identifier.PARSER, term(":"),
        expr, term(")"), stmt);
  }
  
  static Parser<Statement> forStatement(Parser<Expression> expr, Parser<Statement> stmt) {
    return curry(ForStatement.class).sequence(
        phrase("for ("), Parsers.or(varStatement(expr), expressionList(expr), NOP),
        expr.optional(), term(";"), expr.sepBy(term(",")), term(")"),
        stmt);
  }
  
  static Parser<Statement> thisCall(Parser<Expression> expr) {
    return curry(ThisCallStatement.class)
        .sequence(term("this"), term("("), expr.sepBy(term(",")), term(")"), term(";"));
  }
  
  static Parser<Statement> superCall(Parser<Expression> expr) {
    return curry(SuperCallStatement.class)
    .sequence(term("super"), term("("), expr.sepBy(term(",")), term(")"), term(";"));
  }
  
  static Parser<Statement> varStatement(Parser<Expression> expr) {
    Parser<Expression> initializer =
        term("=").next(ExpressionParser.arrayInitializerOrRegularExpression(expr));
    Parser<VarStatement.Var> var = Mapper.curry(VarStatement.Var.class).sequence(
        Terminals.Identifier.PARSER, initializer.optional());
    return curry(VarStatement.class).sequence(
        modifier(expr).many(), TypeLiteralParser.TYPE_LITERAL,
        var.sepBy1(term(",")), term(";"));
  }

  static Parser<Statement> expressionList(Parser<Expression> expr) {
    return curry(ExpressionListStatement.class).sequence(
        expr.sepBy1(term(",")), term(";"));
  }
  
  static Parser<Statement> synchronizedBlock(Parser<Statement> stmt) {
    return curry(SynchronizedBlockStatement.class)
        .sequence(term("synchronized"), blockStatement(stmt));
  }
  
  static Parser<Statement> assertStatement(Parser<Expression> expr) {
    return curry(AssertStatement.class)
        .sequence(term("assert"), expr, term(":").next(expr).optional(), term(";"));
  }
  
  static Parser<Statement> expression(Parser<Expression> expr) {
    return curry(ExpressionStatement.class).sequence(expr, term(";"));
  }
  
  static Parser<ParameterDef> parameter(Parser<Modifier> mod) {
    return Mapper.curry(ParameterDef.class).sequence(
      mod.many(), TYPE_LITERAL, term("...").succeeds(), Terminals.Identifier.PARSER);
  }
  
  static Parser<Statement> tryStatement(Parser<Modifier> mod, Parser<Statement> stmt) {
    Parser<Statement> block = blockStatement(stmt);
    return curry(TryStatement.class).sequence(
        term("try"), block,
        Mapper.curry(TryStatement.CatchBlock.class).sequence(
            term("catch"), term("("), parameter(mod), term(")"), block).many(),
        term("finally").next(block).optional());
  }
  
  static Parser<Statement> throwStatement(Parser<Expression> thrown) {
    return curry(ThrowStatement.class).sequence(term("throw"), thrown, term(";"));
  }
  
  static Parser<Statement> statement(Parser<Expression> expr) {
    Parser.Reference<Statement> ref = Parser.newReference();
    Parser<Statement> lazy = ref.lazy();
    @SuppressWarnings("unchecked")
    Parser<Statement> parser = Parsers.or(
        returnStatement(expr), BREAK, CONTINUE, blockStatement(lazy),
        foreachStatement(expr, lazy), forStatement(expr, lazy),
        whileStatement(expr, lazy), doWhileStatement(lazy, expr),
        ifStatement(expr, lazy), switchStatement(expr, lazy),
        tryStatement(modifier(expr), lazy), throwStatement(expr),
        synchronizedBlock(lazy), assertStatement(expr), varStatement(expr),
        thisCall(expr), superCall(expr),
        expression(expr), NOP).prefix(LABEL).label("statement");
    ref.set(parser);
    return parser;
  }
  
  private static Mapper<Statement> curry(Class<? extends Statement> clazz, Object... curryArgs) {
    return Mapper.curry(clazz, curryArgs);
  }
}
