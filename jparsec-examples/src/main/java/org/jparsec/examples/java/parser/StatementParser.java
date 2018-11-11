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

import static org.jparsec.Parsers.between;
import static org.jparsec.examples.java.parser.ExpressionParser.paren;
import static org.jparsec.examples.java.parser.TerminalParser.phrase;
import static org.jparsec.examples.java.parser.TerminalParser.term;

import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

import org.jparsec.Parser;
import org.jparsec.Parsers;
import org.jparsec.Terminals;
import org.jparsec.examples.java.ast.expression.Expression;
import org.jparsec.examples.java.ast.statement.Annotation;
import org.jparsec.examples.java.ast.statement.AssertStatement;
import org.jparsec.examples.java.ast.statement.BlockStatement;
import org.jparsec.examples.java.ast.statement.BreakStatement;
import org.jparsec.examples.java.ast.statement.ContinueStatement;
import org.jparsec.examples.java.ast.statement.DoWhileStatement;
import org.jparsec.examples.java.ast.statement.ExpressionListStatement;
import org.jparsec.examples.java.ast.statement.ExpressionStatement;
import org.jparsec.examples.java.ast.statement.ForStatement;
import org.jparsec.examples.java.ast.statement.ForeachStatement;
import org.jparsec.examples.java.ast.statement.IfStatement;
import org.jparsec.examples.java.ast.statement.LabelStatement;
import org.jparsec.examples.java.ast.statement.Modifier;
import org.jparsec.examples.java.ast.statement.NopStatement;
import org.jparsec.examples.java.ast.statement.ParameterDef;
import org.jparsec.examples.java.ast.statement.ReturnStatement;
import org.jparsec.examples.java.ast.statement.Statement;
import org.jparsec.examples.java.ast.statement.SuperCallStatement;
import org.jparsec.examples.java.ast.statement.SwitchStatement;
import org.jparsec.examples.java.ast.statement.SynchronizedBlockStatement;
import org.jparsec.examples.java.ast.statement.SystemModifier;
import org.jparsec.examples.java.ast.statement.ThisCallStatement;
import org.jparsec.examples.java.ast.statement.ThrowStatement;
import org.jparsec.examples.java.ast.statement.TryStatement;
import org.jparsec.examples.java.ast.statement.VarStatement;
import org.jparsec.examples.java.ast.statement.WhileStatement;

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
    Parser<Annotation.Element> element = Parsers.sequence(
        Terminals.Identifier.PARSER.followedBy(term("=")).atomic().optional(),
        ExpressionParser.arrayInitializerOrRegularExpression(expr),
        Annotation.Element::new);
    return Parsers.sequence(
        term("@").next(TypeLiteralParser.ELEMENT_TYPE_LITERAL),
        paren(element.sepBy(term(","))).optional(),
        Annotation::new);
  }
  
  static Parser<Modifier> modifier(Parser<Expression> expr) {
    return Parsers.or(annotation(expr), SYSTEM_MODIFIER);
  }
  
  static final Parser<Statement> NOP = term(";").retn(NopStatement.instance);
  
  static final Parser<UnaryOperator<Statement>> LABEL = Terminals.Identifier.PARSER
      .followedBy(term(":"))
      .atomic()
      .map(name -> stmt -> new LabelStatement(name, stmt));
  
  static final Parser<Statement> BREAK =
      between(term("break"), Terminals.Identifier.PARSER.optional(), term(";"))
          .map(BreakStatement::new);
  
  static final Parser<Statement> CONTINUE =
      between(term("continue"), Terminals.Identifier.PARSER.optional(), term(";"))
          .map(ContinueStatement::new);
  
  static Parser<Statement> returnStatement(Parser<Expression> expr) {
    return between(term("return"), expr.optional(), term(";")).map(ReturnStatement::new);
  }
  
  static Parser<BlockStatement> blockStatement(Parser<Statement> stmt) {
    return between(term("{"), stmt.many(), term("}")).map(BlockStatement::new);
  }
  
  static Parser<Statement> whileStatement(Parser<Expression> expr, Parser<Statement> stmt) {
    return Parsers.sequence(
        between(phrase("while ("), expr, term(")")), stmt,
        WhileStatement::new);
  }
  
  static Parser<Statement> doWhileStatement(Parser<Statement> stmt, Parser<Expression> expr) {
    return Parsers.sequence(
        term("do").next(stmt),
        between(phrase("while ("),  expr, phrase(") ;")),
        DoWhileStatement::new);
  }
  
  static Parser<Statement> ifStatement(Parser<Expression> expr, Parser<Statement> stmt) {
    return Parsers.sequence(
        between(phrase("if ("), expr, term(")")),
        stmt, 
        Parsers.pair(between(phrase("else if ("), expr, term(")")), stmt).many(),
        term("else").next(stmt).optional(),
        IfStatement::new);
  }
  
  static Parser<Statement> switchStatement(Parser<Expression> expr, Parser<Statement> stmt) {
    return Parsers.sequence(
        between(phrase("switch ("), expr, phrase(") {")),
            Parsers.pair(between(term("case"), expr, term(":")), stmt.optional()).many(),
            phrase("default :").next(stmt.optional()).optional().followedBy(term("}")),
            SwitchStatement::new);
  }
  
  static Parser<Statement> foreachStatement(Parser<Expression> expr, Parser<Statement> stmt) {
    return Parsers.sequence(
        phrase("for (").next(TypeLiteralParser.TYPE_LITERAL),
        Terminals.Identifier.PARSER,
        term(":").next(expr),
        term(")").next(stmt),
        ForeachStatement::new);
  }
  
  static Parser<Statement> forStatement(Parser<Expression> expr, Parser<Statement> stmt) {
    return Parsers.sequence(
        phrase("for (").next(Parsers.or(varStatement(expr), expressionList(expr), NOP)),
        expr.optional(),
        between(term(";"), expr.sepBy(term(",")), term(")")),
        stmt,
        ForStatement::new);
  }
  
  static Parser<Statement> thisCall(Parser<Expression> expr) {
    return between(phrase("this ("), expr.sepBy(term(",")), phrase(") ;"))
        .map(ThisCallStatement::new);
  }
  
  static Parser<Statement> superCall(Parser<Expression> expr) {
    return between(phrase("super ("), expr.sepBy(term(",")), phrase(") ;"))
        .map(SuperCallStatement::new);
  }
  
  static Parser<Statement> varStatement(Parser<Expression> expr) {
    Parser<Expression> initializer =
        term("=").next(ExpressionParser.arrayInitializerOrRegularExpression(expr));
    Parser<VarStatement.Var> var = Parsers.sequence(
        Terminals.Identifier.PARSER, initializer.optional(), VarStatement.Var::new);
    return Parsers.sequence(
        modifier(expr).many(),
        TypeLiteralParser.TYPE_LITERAL,
        var.sepBy1(term(",")).followedBy(term(";")),
        VarStatement::new);
  }

  static Parser<Statement> expressionList(Parser<Expression> expr) {
    return expr.sepBy1(term(",")).followedBy(term(";")).map(ExpressionListStatement::new);
  }
  
  static Parser<Statement> synchronizedBlock(Parser<Statement> stmt) {
    return term("synchronized").next(blockStatement(stmt)).map(SynchronizedBlockStatement::new);
  }
  
  static Parser<Statement> assertStatement(Parser<Expression> expr) {
    return Parsers.sequence(
        term("assert").next(expr), term(":").next(expr).optional().followedBy(term(";")),
        AssertStatement::new);
  }
  
  static Parser<Statement> expression(Parser<Expression> expr) {
    return expr.followedBy(term(";")).map(ExpressionStatement::new);
  }
  
  static Parser<ParameterDef> parameter(Parser<Modifier> mod) {
    return Parsers.sequence(
        mod.many(), TypeLiteralParser.TYPE_LITERAL, term("...").succeeds(), Terminals.Identifier.PARSER,
        ParameterDef::new);
  }
  
  static Parser<Statement> tryStatement(Parser<Modifier> mod, Parser<Statement> stmt) {
    Parser<BlockStatement> block = blockStatement(stmt);
    return Parsers.sequence(
        term("try").next(block),
        Parsers.sequence(
            term("catch").next(between(term("("), parameter(mod), term(")"))), block,
            TryStatement.CatchBlock::new).many(),
        term("finally").next(block).optional(),
        TryStatement::new);
  }
  
  static Parser<Statement> throwStatement(Parser<Expression> thrown) {
    return between(term("throw"), thrown, term(";")).map(ThrowStatement::new);
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
}
