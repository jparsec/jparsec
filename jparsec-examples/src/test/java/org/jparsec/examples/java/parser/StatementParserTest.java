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
import org.jparsec.examples.java.ast.statement.*;
import org.junit.Test;

/**
 * Unit test for {@link StatementParser}.
 * 
 * @author Ben Yu
 */
public class StatementParserTest {
  
  private static final Parser<Statement> SIMPLE_STATEMENT = StatementParser.expression(ExpressionParser.IDENTIFIER);

  @Test
  public void testNop() {
    TerminalParserTest.assertResult(StatementParser.NOP, ";", NopStatement.class, ";");
  }

  @Test
  public void testSystemModifier() {
    Parser<Modifier> parser = StatementParser.SYSTEM_MODIFIER;
    TerminalParserTest.assertParser(parser, "private", SystemModifier.PRIVATE);
    TerminalParserTest.assertParser(parser, "protected", SystemModifier.PROTECTED);
    TerminalParserTest.assertParser(parser, "public", SystemModifier.PUBLIC);
    TerminalParserTest.assertParser(parser, "static", SystemModifier.STATIC);
    TerminalParserTest.assertParser(parser, "transient", SystemModifier.TRANSIENT);
    TerminalParserTest.assertParser(parser, "volatile", SystemModifier.VOLATILE);
    TerminalParserTest.assertParser(parser, "final", SystemModifier.FINAL);
    TerminalParserTest.assertParser(parser, "abstract", SystemModifier.ABSTRACT);
    TerminalParserTest.assertParser(parser, "synchronized", SystemModifier.SYNCHRONIZED);
    TerminalParserTest.assertParser(parser, "native", SystemModifier.NATIVE);
  }

  @Test
  public void testAnnotation() {
    Parser<Annotation> parser = StatementParser.annotation(ExpressionParser.IDENTIFIER);
    TerminalParserTest.assertResult(parser, "@Foo", Annotation.class, "@Foo");
    TerminalParserTest.assertResult(parser, "@org.codehaus.jparsec.Foo",
        Annotation.class, "@org.codehaus.jparsec.Foo");
    TerminalParserTest.assertResult(parser, "@Foo()", Annotation.class, "@Foo()");
    TerminalParserTest.assertResult(parser, "@Foo(foo)", Annotation.class, "@Foo(foo)");
    TerminalParserTest.assertResult(parser, "@Foo(foo=bar)", Annotation.class, "@Foo(foo=bar)");
    TerminalParserTest.assertResult(parser, "@Foo(foo={bar})", Annotation.class, "@Foo(foo={bar})");
    TerminalParserTest.assertResult(parser, "@Foo(foo={bar}, a=b)", Annotation.class, "@Foo(foo={bar}, a=b)");
    TerminalParserTest.assertFailure(parser, "Foo", 1, 1);
    TerminalParserTest.assertFailure(parser, "@Foo({{foo}})", 1, 7);
  }

  @Test
  public void testModifier() {
    Parser<Modifier> parser = StatementParser.modifier(ExpressionParser.IDENTIFIER);
    TerminalParserTest.assertParser(parser, "private", SystemModifier.PRIVATE);
    TerminalParserTest.assertResult(parser, "@Foo(foo)", Annotation.class, "@Foo(foo)");
  }

  @Test
  public void testBreak() {
    Parser<Statement> parser = StatementParser.BREAK;
    TerminalParserTest.assertResult(parser, "break;", BreakStatement.class, "break;");
    TerminalParserTest.assertResult(parser, "break foo;", BreakStatement.class, "break foo;");
  }

  @Test
  public void testContinue() {
    Parser<Statement> parser = StatementParser.CONTINUE;
    TerminalParserTest.assertResult(parser, "continue;", ContinueStatement.class, "continue;");
    TerminalParserTest.assertResult(parser, "continue foo;", ContinueStatement.class, "continue foo;");
  }

  @Test
  public void testReturnStatement() {
    Parser<Statement> parser = StatementParser.returnStatement(ExpressionParser.IDENTIFIER);
    TerminalParserTest.assertResult(parser, "return;", ReturnStatement.class, "return;");
    TerminalParserTest.assertResult(parser, "return foo;", ReturnStatement.class, "return foo;");
  }

  @Test
  public void testBlockStatement() {
    Parser<BlockStatement> parser = StatementParser.blockStatement(StatementParser.NOP);
    TerminalParserTest.assertResult(parser, "{}", BlockStatement.class, "{}");
    TerminalParserTest.assertResult(parser, "{;}", BlockStatement.class, "{;}");
    TerminalParserTest.assertResult(parser, "{;;}", BlockStatement.class, "{; ;}");
  }

  @Test
  public void testSynchronizedBlockStatement() {
    Parser<Statement> parser = StatementParser.synchronizedBlock(SIMPLE_STATEMENT);
    TerminalParserTest.assertResult(parser, "synchronized {foo;}",
        SynchronizedBlockStatement.class, "synchronized {foo;}");
  }

  @Test
  public void testWhileStatement() {
    Parser<Statement> parser = StatementParser.whileStatement(ExpressionParser.IDENTIFIER, SIMPLE_STATEMENT);
    TerminalParserTest.assertResult(parser, "while(foo) bar;", WhileStatement.class, "while (foo) bar;");
  }

  @Test
  public void testDoWhileStatement() {
    Parser<Statement> parser = StatementParser.doWhileStatement(SIMPLE_STATEMENT, ExpressionParser.IDENTIFIER);
    TerminalParserTest.assertResult(parser, "do bar;while(foo);", DoWhileStatement.class, "do bar; while (foo);");
  }

  @Test
  public void testIfStatement() {
    Parser<Statement> parser = StatementParser.ifStatement(ExpressionParser.IDENTIFIER, SIMPLE_STATEMENT);
    TerminalParserTest.assertResult(parser, "if (foo) bar;", IfStatement.class, "if (foo) bar;");
    TerminalParserTest.assertResult(parser, "if (foo) bar; else baz;", IfStatement.class, "if (foo) bar; else baz;");
    TerminalParserTest.assertResult(parser, "if (foo) bar; else if(baz) baz;",
        IfStatement.class, "if (foo) bar; else if (baz) baz;");
  }

  @Test
  public void testSwitchStatement() {
    Parser<Statement> parser = StatementParser.switchStatement(ExpressionParser.IDENTIFIER, SIMPLE_STATEMENT);
    TerminalParserTest.assertResult(parser, "switch (foo) {}", SwitchStatement.class, "switch (foo) {}");
    TerminalParserTest.assertResult(parser, "switch (foo) { default:}", SwitchStatement.class, "switch (foo) {}");
    TerminalParserTest.assertResult(parser, "switch (foo) { case foo:}",
        SwitchStatement.class, "switch (foo) { case foo:}");
    TerminalParserTest.assertResult(parser, "switch (foo) { case foo: case bar: baz;}",
        SwitchStatement.class, "switch (foo) { case foo: case bar: baz;}");
    TerminalParserTest.assertResult(parser, "switch (foo) { case foo: case bar: baz; default: x;}",
        SwitchStatement.class, "switch (foo) { case foo: case bar: baz; default: x;}");
  }

  @Test
  public void testForeachStatement() {
    Parser<Statement> parser = StatementParser.foreachStatement(ExpressionParser.IDENTIFIER, SIMPLE_STATEMENT);
    TerminalParserTest.assertResult(parser, "for(Foo foo : foos) bar;",
        ForeachStatement.class, "for (Foo foo : foos) bar;");
  }

  @Test
  public void testVarStatement() {
    Parser<Statement> parser = StatementParser.varStatement(ExpressionParser.IDENTIFIER);
    TerminalParserTest.assertResult(parser, "int i;", VarStatement.class, "int i;");
    TerminalParserTest.assertResult(parser, "int i=n;", VarStatement.class, "int i = n;");
    TerminalParserTest.assertResult(parser, "final int i=n;", VarStatement.class, "final int i = n;");
    TerminalParserTest.assertResult(parser, "final int[] a1={}, a2, a3={m, n};",
        VarStatement.class, "final int[] a1 = {}, a2, a3 = {m, n};");
  }

  @Test
  public void testForStatement() {
    Parser<Statement> parser = StatementParser.forStatement(ExpressionParser.IDENTIFIER, SIMPLE_STATEMENT);
    TerminalParserTest.assertResult(parser, "for(;;)foo;", ForStatement.class, "for (;;) foo;");
    TerminalParserTest.assertResult(parser, "for(int i=m;;)foo;", ForStatement.class, "for (int i = m;;) foo;");
    TerminalParserTest.assertResult(parser, "for(init;;)foo;", ForStatement.class, "for (init;;) foo;");
    TerminalParserTest.assertResult(parser, "for(init1,init2;;)foo;", ForStatement.class, "for (init1, init2;;) foo;");
    TerminalParserTest.assertResult(parser, "for(;cond;)foo;", ForStatement.class, "for (;cond;) foo;");
    TerminalParserTest.assertResult(parser, "for(;;a)foo;", ForStatement.class, "for (;;a) foo;");
    TerminalParserTest.assertResult(parser, "for(;;a,b)foo;", ForStatement.class, "for (;;a, b) foo;");
    TerminalParserTest.assertResult(parser, "for(int i=m, j=n;cond;a,b)foo;",
        ForStatement.class, "for (int i = m, j = n;cond;a, b) foo;");
  }

  @Test
  public void testExpression() {
    TerminalParserTest.assertResult(SIMPLE_STATEMENT, "foo;", ExpressionStatement.class, "foo;");
  }

  @Test
  public void testExpressionList() {
    Parser<Statement> parser = StatementParser.expressionList(ExpressionParser.IDENTIFIER);
    TerminalParserTest.assertResult(parser, "foo;", ExpressionListStatement.class, "foo;");
    TerminalParserTest.assertResult(parser, "foo,bar;", ExpressionListStatement.class, "foo, bar;");
  }

  @Test
  public void testAssertStatement() {
    Parser<Statement> parser = StatementParser.assertStatement(ExpressionParser.IDENTIFIER);
    TerminalParserTest.assertResult(parser, "assert foo;", AssertStatement.class, "assert foo;");
    TerminalParserTest.assertResult(parser, "assert foo : bar;", AssertStatement.class, "assert foo : bar;");
  }

  @Test
  public void testParameter() {
    Parser<ParameterDef> parser = StatementParser.parameter(StatementParser.SYSTEM_MODIFIER);
    TerminalParserTest.assertResult(parser, "int f", ParameterDef.class, "int f");
    TerminalParserTest.assertResult(parser, "final int[] f", ParameterDef.class, "final int[] f");
    TerminalParserTest.assertResult(parser, "static final int[] f", ParameterDef.class, "static final int[] f");
    TerminalParserTest.assertResult(parser, "final int[]... f", ParameterDef.class, "final int[]... f");
    TerminalParserTest.assertResult(parser, "final List<Integer>... f",
        ParameterDef.class, "final List<Integer>... f");
    TerminalParserTest.assertResult(parser, "final int... f", ParameterDef.class, "final int... f");
  }

  @Test
  public void testTryStatement() {
    Parser<Statement> parser =
        StatementParser.tryStatement(StatementParser.SYSTEM_MODIFIER, SIMPLE_STATEMENT);
    TerminalParserTest.assertResult(parser, "try {foo;} catch(E e) {bar;}",
        TryStatement.class, "try {foo;} catch (E e) {bar;}");
    TerminalParserTest.assertResult(parser, "try {foo;} catch(E e) {bar;}catch(E2 e) {baz;}",
        TryStatement.class, "try {foo;} catch (E e) {bar;} catch (E2 e) {baz;}");
    TerminalParserTest.assertResult(parser, "try {foo;} finally{bar;}",
        TryStatement.class, "try {foo;} finally {bar;}");
    TerminalParserTest.assertResult(parser, "try {foo;} catch(E e){bar;}finally{bar;}",
        TryStatement.class, "try {foo;} catch (E e) {bar;} finally {bar;}");
    TerminalParserTest.assertResult(parser, "try {foo;} catch(E e){bar;}catch(E e2){bar2;}finally{bar;}",
        TryStatement.class, "try {foo;} catch (E e) {bar;} catch (E e2) {bar2;} finally {bar;}");
    TerminalParserTest.assertFailure(parser, "try{foo;}catch(E e1, E e2){bar;}", 1, 20);
  }

  @Test
  public void testThrowStatement() {
    Parser<Statement> parser = StatementParser.throwStatement(ExpressionParser.IDENTIFIER);
    TerminalParserTest.assertResult(parser, "throw foo;", ThrowStatement.class, "throw foo;");
  }

  @Test
  public void testThisCallStatement() {
    Parser<Statement> parser = StatementParser.thisCall(ExpressionParser.IDENTIFIER);
    TerminalParserTest.assertResult(parser, "this();", ThisCallStatement.class, "this();");
    TerminalParserTest.assertResult(parser, "this(foo);", ThisCallStatement.class, "this(foo);");
    TerminalParserTest.assertResult(parser, "this(foo,bar);", ThisCallStatement.class, "this(foo, bar);");
  }

  @Test
  public void testSuperCallStatement() {
    Parser<Statement> parser = StatementParser.superCall(ExpressionParser.IDENTIFIER);
    TerminalParserTest.assertResult(parser, "super();", SuperCallStatement.class, "super();");
    TerminalParserTest.assertResult(parser, "super(foo);", SuperCallStatement.class, "super(foo);");
    TerminalParserTest.assertResult(parser, "super(foo,bar);", SuperCallStatement.class, "super(foo, bar);");
  }
  
  // Makes sure the parts are correctly aggregated to create the statement parser.
  @Test
  public void testStatement() {
    Parser<Statement> parser = StatementParser.statement(ExpressionParser.IDENTIFIER);
    TerminalParserTest.assertResult(parser, ";", NopStatement.class, ";");
    TerminalParserTest.assertResult(parser, "foo: bar;", LabelStatement.class, "foo: bar;");
    TerminalParserTest.assertResult(parser, "foo: bar: baz;", LabelStatement.class, "foo: bar: baz;");
    TerminalParserTest.assertResult(parser, "break foo;", BreakStatement.class, "break foo;");
    TerminalParserTest.assertResult(parser, "continue foo;", ContinueStatement.class, "continue foo;");
    TerminalParserTest.assertResult(parser, "return foo;", ReturnStatement.class, "return foo;");
    TerminalParserTest.assertResult(parser, "foo;", ExpressionStatement.class, "foo;");
    TerminalParserTest.assertResult(parser, "{foo;}", BlockStatement.class, "{foo;}");
    TerminalParserTest.assertResult(parser, "synchronized {foo;}",
        SynchronizedBlockStatement.class, "synchronized {foo;}");
    TerminalParserTest.assertResult(parser, "while(foo) bar;", WhileStatement.class, "while (foo) bar;");
    TerminalParserTest.assertResult(parser, "do bar;while(foo);", DoWhileStatement.class, "do bar; while (foo);");
    TerminalParserTest.assertResult(parser, "if (foo) bar; else if(baz) baz;",
        IfStatement.class, "if (foo) bar; else if (baz) baz;");
    TerminalParserTest.assertResult(parser, "switch (foo) { case foo: case bar: baz; default: x;}",
        SwitchStatement.class, "switch (foo) { case foo: case bar: baz; default: x;}");
    TerminalParserTest.assertResult(parser, "for(Foo<Bar>[][] foo : foos) bar;",
        ForeachStatement.class, "for (Foo<Bar>[][] foo : foos) bar;");
    TerminalParserTest.assertResult(parser, "for(int i=m, j=n;cond;a,b)foo;",
        ForStatement.class, "for (int i = m, j = n;cond;a, b) foo;");
    TerminalParserTest.assertResult(parser, "for(init1,init2;;)foo;", ForStatement.class, "for (init1, init2;;) foo;");
    TerminalParserTest.assertResult(parser, "final int[] a1={}, a2, a3={m, n};",
        VarStatement.class, "final int[] a1 = {}, a2, a3 = {m, n};");
    TerminalParserTest.assertResult(parser, "assert foo : bar;", AssertStatement.class, "assert foo : bar;");
    TerminalParserTest.assertResult(parser, "try {foo;} catch(E e){bar;}catch(E e2){bar2;}finally{bar;}",
        TryStatement.class, "try {foo;} catch (E e) {bar;} catch (E e2) {bar2;} finally {bar;}");
    TerminalParserTest.assertResult(parser, "throw foo;", ThrowStatement.class, "throw foo;");
    TerminalParserTest.assertResult(parser, "this(foo, bar);", ThisCallStatement.class, "this(foo, bar);");
    TerminalParserTest.assertResult(parser, "super(foo, bar);", SuperCallStatement.class, "super(foo, bar);");
  }
}
