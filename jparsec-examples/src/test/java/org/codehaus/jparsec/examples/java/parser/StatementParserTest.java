package org.codehaus.jparsec.examples.java.parser;

import static org.codehaus.jparsec.examples.java.parser.ExpressionParser.IDENTIFIER;
import static org.codehaus.jparsec.examples.java.parser.StatementParser.NOP;
import static org.codehaus.jparsec.examples.java.parser.TerminalParserTest.assertFailure;
import static org.codehaus.jparsec.examples.java.parser.TerminalParserTest.assertParser;
import static org.codehaus.jparsec.examples.java.parser.TerminalParserTest.assertResult;

import org.codehaus.jparsec.Parser;
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
import org.junit.Test;

/**
 * Unit test for {@link StatementParser}.
 * 
 * @author Ben Yu
 */
public class StatementParserTest {
  
  private static final Parser<Statement> SIMPLE_STATEMENT = StatementParser.expression(IDENTIFIER);

  @Test
  public void testNop() {
    assertResult(NOP, ";", NopStatement.class, ";");
  }

  @Test
  public void testSystemModifier() {
    Parser<Modifier> parser = StatementParser.SYSTEM_MODIFIER;
    assertParser(parser, "private", SystemModifier.PRIVATE);
    assertParser(parser, "protected", SystemModifier.PROTECTED);
    assertParser(parser, "public", SystemModifier.PUBLIC);
    assertParser(parser, "static", SystemModifier.STATIC);
    assertParser(parser, "transient", SystemModifier.TRANSIENT);
    assertParser(parser, "volatile", SystemModifier.VOLATILE);
    assertParser(parser, "final", SystemModifier.FINAL);
    assertParser(parser, "abstract", SystemModifier.ABSTRACT);
    assertParser(parser, "synchronized", SystemModifier.SYNCHRONIZED);
    assertParser(parser, "native", SystemModifier.NATIVE);
  }

  @Test
  public void testAnnotation() {
    Parser<Annotation> parser = StatementParser.annotation(IDENTIFIER);
    assertResult(parser, "@Foo", Annotation.class, "@Foo");
    assertResult(parser, "@org.codehaus.jparsec.Foo",
        Annotation.class, "@org.codehaus.jparsec.Foo");
    assertResult(parser, "@Foo()", Annotation.class, "@Foo()");
    assertResult(parser, "@Foo(foo)", Annotation.class, "@Foo(foo)");
    assertResult(parser, "@Foo(foo=bar)", Annotation.class, "@Foo(foo=bar)");
    assertResult(parser, "@Foo(foo={bar})", Annotation.class, "@Foo(foo={bar})");
    assertResult(parser, "@Foo(foo={bar}, a=b)", Annotation.class, "@Foo(foo={bar}, a=b)");
    assertFailure(parser, "Foo", 1, 1);
    assertFailure(parser, "@Foo({{foo}})", 1, 7);
  }

  @Test
  public void testModifier() {
    Parser<Modifier> parser = StatementParser.modifier(IDENTIFIER);
    assertParser(parser, "private", SystemModifier.PRIVATE);
    assertResult(parser, "@Foo(foo)", Annotation.class, "@Foo(foo)");
  }

  @Test
  public void testBreak() {
    Parser<Statement> parser = StatementParser.BREAK;
    assertResult(parser, "break;", BreakStatement.class, "break;");
    assertResult(parser, "break foo;", BreakStatement.class, "break foo;");
  }

  @Test
  public void testContinue() {
    Parser<Statement> parser = StatementParser.CONTINUE;
    assertResult(parser, "continue;", ContinueStatement.class, "continue;");
    assertResult(parser, "continue foo;", ContinueStatement.class, "continue foo;");
  }

  @Test
  public void testReturnStatement() {
    Parser<Statement> parser = StatementParser.returnStatement(IDENTIFIER);
    assertResult(parser, "return;", ReturnStatement.class, "return;");
    assertResult(parser, "return foo;", ReturnStatement.class, "return foo;");
  }

  @Test
  public void testBlockStatement() {
    Parser<Statement> parser = StatementParser.blockStatement(NOP);
    assertResult(parser, "{}", BlockStatement.class, "{}");
    assertResult(parser, "{;}", BlockStatement.class, "{;}");
    assertResult(parser, "{;;}", BlockStatement.class, "{; ;}");
  }

  @Test
  public void testSynchronizedBlockStatement() {
    Parser<Statement> parser = StatementParser.synchronizedBlock(SIMPLE_STATEMENT);
    assertResult(parser, "synchronized {foo;}",
        SynchronizedBlockStatement.class, "synchronized {foo;}");
  }

  @Test
  public void testWhileStatement() {
    Parser<Statement> parser = StatementParser.whileStatement(IDENTIFIER, SIMPLE_STATEMENT);
    assertResult(parser, "while(foo) bar;", WhileStatement.class, "while (foo) bar;");
  }

  @Test
  public void testDoWhileStatement() {
    Parser<Statement> parser = StatementParser.doWhileStatement(SIMPLE_STATEMENT, IDENTIFIER);
    assertResult(parser, "do bar;while(foo);", DoWhileStatement.class, "do bar; while (foo);");
  }

  @Test
  public void testIfStatement() {
    Parser<Statement> parser = StatementParser.ifStatement(IDENTIFIER, SIMPLE_STATEMENT);
    assertResult(parser, "if (foo) bar;", IfStatement.class, "if (foo) bar;");
    assertResult(parser, "if (foo) bar; else baz;", IfStatement.class, "if (foo) bar; else baz;");
    assertResult(parser, "if (foo) bar; else if(baz) baz;",
        IfStatement.class, "if (foo) bar; else if (baz) baz;");
  }

  @Test
  public void testSwitchStatement() {
    Parser<Statement> parser = StatementParser.switchStatement(IDENTIFIER, SIMPLE_STATEMENT);
    assertResult(parser, "switch (foo) {}", SwitchStatement.class, "switch (foo) {}");
    assertResult(parser, "switch (foo) { default:}", SwitchStatement.class, "switch (foo) {}");
    assertResult(parser, "switch (foo) { case foo:}",
        SwitchStatement.class, "switch (foo) { case foo:}");
    assertResult(parser, "switch (foo) { case foo: case bar: baz;}",
        SwitchStatement.class, "switch (foo) { case foo: case bar: baz;}");
    assertResult(parser, "switch (foo) { case foo: case bar: baz; default: x;}",
        SwitchStatement.class, "switch (foo) { case foo: case bar: baz; default: x;}");
  }

  @Test
  public void testForeachStatement() {
    Parser<Statement> parser = StatementParser.foreachStatement(IDENTIFIER, SIMPLE_STATEMENT);
    assertResult(parser, "for(Foo foo : foos) bar;",
        ForeachStatement.class, "for (Foo foo : foos) bar;");
  }

  @Test
  public void testVarStatement() {
    Parser<Statement> parser = StatementParser.varStatement(IDENTIFIER);
    assertResult(parser, "int i;", VarStatement.class, "int i;");
    assertResult(parser, "int i=n;", VarStatement.class, "int i = n;");
    assertResult(parser, "final int i=n;", VarStatement.class, "final int i = n;");
    assertResult(parser, "final int[] a1={}, a2, a3={m, n};",
        VarStatement.class, "final int[] a1 = {}, a2, a3 = {m, n};");
  }

  @Test
  public void testForStatement() {
    Parser<Statement> parser = StatementParser.forStatement(IDENTIFIER, SIMPLE_STATEMENT);
    assertResult(parser, "for(;;)foo;", ForStatement.class, "for (;;) foo;");
    assertResult(parser, "for(int i=m;;)foo;", ForStatement.class, "for (int i = m;;) foo;");
    assertResult(parser, "for(init;;)foo;", ForStatement.class, "for (init;;) foo;");
    assertResult(parser, "for(init1,init2;;)foo;", ForStatement.class, "for (init1, init2;;) foo;");
    assertResult(parser, "for(;cond;)foo;", ForStatement.class, "for (;cond;) foo;");
    assertResult(parser, "for(;;a)foo;", ForStatement.class, "for (;;a) foo;");
    assertResult(parser, "for(;;a,b)foo;", ForStatement.class, "for (;;a, b) foo;");
    assertResult(parser, "for(int i=m, j=n;cond;a,b)foo;",
        ForStatement.class, "for (int i = m, j = n;cond;a, b) foo;");
  }

  @Test
  public void testExpression() {
    assertResult(SIMPLE_STATEMENT, "foo;", ExpressionStatement.class, "foo;");
  }

  @Test
  public void testExpressionList() {
    Parser<Statement> parser = StatementParser.expressionList(IDENTIFIER);
    assertResult(parser, "foo;", ExpressionListStatement.class, "foo;");
    assertResult(parser, "foo,bar;", ExpressionListStatement.class, "foo, bar;");
  }

  @Test
  public void testAssertStatement() {
    Parser<Statement> parser = StatementParser.assertStatement(IDENTIFIER);
    assertResult(parser, "assert foo;", AssertStatement.class, "assert foo;");
    assertResult(parser, "assert foo : bar;", AssertStatement.class, "assert foo : bar;");
  }

  @Test
  public void testParameter() {
    Parser<ParameterDef> parser = StatementParser.parameter(StatementParser.SYSTEM_MODIFIER);
    assertResult(parser, "int f", ParameterDef.class, "int f");
    assertResult(parser, "final int[] f", ParameterDef.class, "final int[] f");
    assertResult(parser, "static final int[] f", ParameterDef.class, "static final int[] f");
    assertResult(parser, "final int[]... f", ParameterDef.class, "final int[]... f");
    assertResult(parser, "final List<Integer>... f",
        ParameterDef.class, "final List<Integer>... f");
    assertResult(parser, "final int... f", ParameterDef.class, "final int... f");
  }

  @Test
  public void testTryStatement() {
    Parser<Statement> parser =
        StatementParser.tryStatement(StatementParser.SYSTEM_MODIFIER, SIMPLE_STATEMENT);
    assertResult(parser, "try {foo;} catch(E e) {bar;}",
        TryStatement.class, "try {foo;} catch (E e) {bar;}");
    assertResult(parser, "try {foo;} catch(E e) {bar;}catch(E2 e) {baz;}",
        TryStatement.class, "try {foo;} catch (E e) {bar;} catch (E2 e) {baz;}");
    assertResult(parser, "try {foo;} finally{bar;}",
        TryStatement.class, "try {foo;} finally {bar;}");
    assertResult(parser, "try {foo;} catch(E e){bar;}finally{bar;}",
        TryStatement.class, "try {foo;} catch (E e) {bar;} finally {bar;}");
    assertResult(parser, "try {foo;} catch(E e){bar;}catch(E e2){bar2;}finally{bar;}",
        TryStatement.class, "try {foo;} catch (E e) {bar;} catch (E e2) {bar2;} finally {bar;}");
    assertFailure(parser, "try{foo;}catch(E e1, E e2){bar;}", 1, 20);
  }

  @Test
  public void testThrowStatement() {
    Parser<Statement> parser = StatementParser.throwStatement(IDENTIFIER);
    assertResult(parser, "throw foo;", ThrowStatement.class, "throw foo;");
  }

  @Test
  public void testThisCallStatement() {
    Parser<Statement> parser = StatementParser.thisCall(IDENTIFIER);
    assertResult(parser, "this();", ThisCallStatement.class, "this();");
    assertResult(parser, "this(foo);", ThisCallStatement.class, "this(foo);");
    assertResult(parser, "this(foo,bar);", ThisCallStatement.class, "this(foo, bar);");
  }

  @Test
  public void testSuperCallStatement() {
    Parser<Statement> parser = StatementParser.superCall(IDENTIFIER);
    assertResult(parser, "super();", SuperCallStatement.class, "super();");
    assertResult(parser, "super(foo);", SuperCallStatement.class, "super(foo);");
    assertResult(parser, "super(foo,bar);", SuperCallStatement.class, "super(foo, bar);");
  }
  
  // Makes sure the parts are correctly aggregated to create the statement parser.
  @Test
  public void testStatement() {
    Parser<Statement> parser = StatementParser.statement(IDENTIFIER);
    assertResult(parser, ";", NopStatement.class, ";");
    assertResult(parser, "foo: bar;", LabelStatement.class, "foo: bar;");
    assertResult(parser, "foo: bar: baz;", LabelStatement.class, "foo: bar: baz;");
    assertResult(parser, "break foo;", BreakStatement.class, "break foo;");
    assertResult(parser, "continue foo;", ContinueStatement.class, "continue foo;");
    assertResult(parser, "return foo;", ReturnStatement.class, "return foo;");
    assertResult(parser, "foo;", ExpressionStatement.class, "foo;");
    assertResult(parser, "{foo;}", BlockStatement.class, "{foo;}");
    assertResult(parser, "synchronized {foo;}",
        SynchronizedBlockStatement.class, "synchronized {foo;}");
    assertResult(parser, "while(foo) bar;", WhileStatement.class, "while (foo) bar;");
    assertResult(parser, "do bar;while(foo);", DoWhileStatement.class, "do bar; while (foo);");
    assertResult(parser, "if (foo) bar; else if(baz) baz;",
        IfStatement.class, "if (foo) bar; else if (baz) baz;");
    assertResult(parser, "switch (foo) { case foo: case bar: baz; default: x;}",
        SwitchStatement.class, "switch (foo) { case foo: case bar: baz; default: x;}");
    assertResult(parser, "for(Foo<Bar>[][] foo : foos) bar;",
        ForeachStatement.class, "for (Foo<Bar>[][] foo : foos) bar;");
    assertResult(parser, "for(int i=m, j=n;cond;a,b)foo;",
        ForStatement.class, "for (int i = m, j = n;cond;a, b) foo;");
    assertResult(parser, "for(init1,init2;;)foo;", ForStatement.class, "for (init1, init2;;) foo;");
    assertResult(parser, "final int[] a1={}, a2, a3={m, n};",
        VarStatement.class, "final int[] a1 = {}, a2, a3 = {m, n};");
    assertResult(parser, "assert foo : bar;", AssertStatement.class, "assert foo : bar;");
    assertResult(parser, "try {foo;} catch(E e){bar;}catch(E e2){bar2;}finally{bar;}",
        TryStatement.class, "try {foo;} catch (E e) {bar;} catch (E e2) {bar2;} finally {bar;}");
    assertResult(parser, "throw foo;", ThrowStatement.class, "throw foo;");
    assertResult(parser, "this(foo, bar);", ThisCallStatement.class, "this(foo, bar);");
    assertResult(parser, "super(foo, bar);", SuperCallStatement.class, "super(foo, bar);");
  }
}
