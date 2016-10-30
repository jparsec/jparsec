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
import static org.codehaus.jparsec.examples.java.parser.StatementParser.modifier;
import static org.codehaus.jparsec.examples.java.parser.TerminalParser.phrase;
import static org.codehaus.jparsec.examples.java.parser.TerminalParser.term;
import static org.codehaus.jparsec.examples.java.parser.TypeLiteralParser.ELEMENT_TYPE_LITERAL;
import static org.codehaus.jparsec.examples.java.parser.TypeLiteralParser.TYPE_LITERAL;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.List;

import org.codehaus.jparsec.Parser;
import org.codehaus.jparsec.Parsers;
import org.codehaus.jparsec.Terminals;
import org.codehaus.jparsec.examples.java.ast.declaration.AnnotationDef;
import org.codehaus.jparsec.examples.java.ast.declaration.ClassDef;
import org.codehaus.jparsec.examples.java.ast.declaration.ClassInitializerDef;
import org.codehaus.jparsec.examples.java.ast.declaration.ConstructorDef;
import org.codehaus.jparsec.examples.java.ast.declaration.Declaration;
import org.codehaus.jparsec.examples.java.ast.declaration.DefBody;
import org.codehaus.jparsec.examples.java.ast.declaration.EnumDef;
import org.codehaus.jparsec.examples.java.ast.declaration.FieldDef;
import org.codehaus.jparsec.examples.java.ast.declaration.Import;
import org.codehaus.jparsec.examples.java.ast.declaration.InterfaceDef;
import org.codehaus.jparsec.examples.java.ast.declaration.Member;
import org.codehaus.jparsec.examples.java.ast.declaration.MethodDef;
import org.codehaus.jparsec.examples.java.ast.declaration.NestedDef;
import org.codehaus.jparsec.examples.java.ast.declaration.Program;
import org.codehaus.jparsec.examples.java.ast.declaration.QualifiedName;
import org.codehaus.jparsec.examples.java.ast.declaration.TypeParameterDef;
import org.codehaus.jparsec.examples.java.ast.expression.Expression;
import org.codehaus.jparsec.examples.java.ast.statement.BlockStatement;
import org.codehaus.jparsec.examples.java.ast.statement.Modifier;
import org.codehaus.jparsec.examples.java.ast.statement.Statement;

/**
 * Parses class, interface, enum, annotation declarations.
 * 
 * @author Ben Yu
 */
public final class DeclarationParser {
  static Parser<DefBody> body(Parser<Member> member) {
    Parser<Member> empty = term(";").retn(null);
    return Parsers.between(term("{"), empty.or(member).many().map(DeclarationParser::removeNulls), term("}"))
        .map(DefBody::new);
  }

  static <T> List<T> removeNulls(List<T> list) {
    for (Iterator<?> it = list.iterator(); it.hasNext();) {
      if (it.next() == null) {
        it.remove();
      }
    }
    return list;
  }
  
  static Parser<Member> fieldDef(Parser<Expression> initializer) {
    return Parsers.sequence(
        modifier(initializer).many(), TYPE_LITERAL, Terminals.Identifier.PARSER,
        term("=").next(ExpressionParser.arrayInitializerOrRegularExpression(initializer))
            .optional(),
        term(";"),
        (modifiers, type, name, value, __) -> new FieldDef(modifiers, type, name ,value));
  }
  
  static final Parser<TypeParameterDef> TYPE_PARAMETER = Parsers.sequence(
      Terminals.Identifier.PARSER, term("extends").next(TypeLiteralParser.TYPE_LITERAL).optional(),
      TypeParameterDef::new);
  
  static final Parser<List<TypeParameterDef>> TYPE_PARAMETERS =
      between(term("<"), TYPE_PARAMETER.sepBy1(term(",")), term(">"));
  
  static Parser<Member> constructorDef(Parser<Modifier> mod, Parser<Statement> stmt) {
    return Parsers.sequence(
        mod.many(), Terminals.Identifier.PARSER,
        term("(").next(StatementParser.parameter(mod).sepBy(term(","))).followedBy(term(")")),
        term("throws").next(ELEMENT_TYPE_LITERAL.sepBy1(term(","))).optional(),
        StatementParser.blockStatement(stmt),
        ConstructorDef::new);
  }
  
  static Parser<Member> methodDef(
      Parser<Modifier> mod, Parser<Expression> defaultValue, Parser<Statement> stmt) {
    return Parsers.sequence(
        mod.many(), TYPE_PARAMETERS.optional(),
        TYPE_LITERAL, Terminals.Identifier.PARSER,
        term("(").next(StatementParser.parameter(mod).sepBy(term(","))).followedBy(term(")")),
        term("throws").next(ELEMENT_TYPE_LITERAL.sepBy1(term(","))).optional(),
        term("default").next(ExpressionParser.arrayInitializerOrRegularExpression(defaultValue))
            .optional(),
        Parsers.or(
            StatementParser.blockStatement(stmt),
            term(";").retn((BlockStatement) null)),
        MethodDef::new);
  }
  
  static Parser<Member> initializerDef(Parser<Statement> stmt) {
    return Parsers.sequence(
        term("static").succeeds(), StatementParser.blockStatement(stmt),
        ClassInitializerDef::new);
  }
  
  static Parser<Member> nestedDef(Parser<Declaration> dec) {
    return dec.map(NestedDef::new);
  }
  
  static Parser<Declaration> classDef(Parser<Modifier> mod, Parser<Member> member) {
    return Parsers.sequence(
        mod.many(), term("class").next(Terminals.Identifier.PARSER), TYPE_PARAMETERS.optional(),
        term("extends").next(ELEMENT_TYPE_LITERAL).optional(),
        term("implements").next(ELEMENT_TYPE_LITERAL.sepBy1(term(","))).optional(),
        body(member),
        ClassDef::new);
  }
  
  static Parser<Declaration> interfaceDef(Parser<Modifier> mod, Parser<Member> member) {
    return Parsers.sequence(
        mod.many(), term("interface").next(Terminals.Identifier.PARSER), TYPE_PARAMETERS.optional(),
        term("extends").next(ELEMENT_TYPE_LITERAL.sepBy1(term(","))).optional(),
        body(member),
        InterfaceDef::new);
  }
  
  static Parser<Declaration> annotationDef(Parser<Modifier> mod, Parser<Member> member) {
    return Parsers.sequence(
        mod.many(), phrase("@ interface").next(Terminals.Identifier.PARSER), body(member),
        AnnotationDef::new);
  }
  
  static Parser<Declaration> enumDef(Parser<Expression> expr, Parser<Member> member) {
    Parser<EnumDef.Value> enumValue = Parsers.sequence(
        Terminals.Identifier.PARSER, between(term("("), expr.sepBy(term(",")), term(")"))
            .optional(),
        between(term("{"), member.many(), term("}")).optional(),
        EnumDef.Value::new);
    return Parsers.sequence(
        modifier(expr).many(),
        term("enum").next(Terminals.Identifier.PARSER),
        term("implements").next(ELEMENT_TYPE_LITERAL.sepBy1(term(","))).optional(),
        term("{").next(enumValue.sepBy(term(","))),
        term(";").next(member.many()).optional().followedBy(term("}")),
        EnumDef::new);
  }
  
  static final Parser<QualifiedName> QUALIFIED_NAME =
      Terminals.Identifier.PARSER.sepBy1(term(".")).map(QualifiedName::new);
  
  static final Parser<Import> IMPORT = Parsers.sequence(
      term("import").next(term("static").succeeds()),
      QUALIFIED_NAME, phrase(". *").succeeds().followedBy(term(";")),
      Import::new);
  
  static final Parser<QualifiedName> PACKAGE = between(term("package"), QUALIFIED_NAME, term(";"));
  
  public static Parser<Program> program() {
    Parser.Reference<Member> memberRef = Parser.newReference();
    Parser.Reference<Statement> stmtRef = Parser.newReference();
    Parser<Expression> expr = ExpressionParser.expression(body(memberRef.lazy()), stmtRef.lazy());
    Parser<Statement> stmt = StatementParser.statement(expr);
    stmtRef.set(stmt);
    Parser<Modifier> mod = modifier(expr);
    Parser.Reference<Declaration> decRef = Parser.newReference();
    Parser<Member> member = Parsers.or(
        fieldDef(expr), methodDef(mod, expr, stmt), constructorDef(mod, stmt),
        initializerDef(stmt), nestedDef(decRef.lazy()));
    memberRef.set(member);
    Parser<Declaration> declaration = Parsers.or(
        classDef(mod, member), interfaceDef(mod, member),
        enumDef(expr, member), annotationDef(mod, member));
    decRef.set(declaration);
    return Parsers.sequence(PACKAGE.optional(), IMPORT.many(), declaration.many(), Program::new);
  }
  
  /** Parses any Java source.  */
  public static Program parse(String source) {
    return TerminalParser.parse(program(), source);
  }
  
  /** Parses source code read from {@code url}. */
  public static Program parse(URL url) throws IOException {
    InputStream in = url.openStream();
    try {
      return TerminalParser.parse(
          program(), new InputStreamReader(in, Charset.forName("UTF-8")), url.toString());
    } finally {
      in.close();
    }
  }
}
