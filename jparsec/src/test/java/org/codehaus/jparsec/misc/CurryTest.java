package org.codehaus.jparsec.misc;

import static org.codehaus.jparsec.Parsers.constant;
import static org.codehaus.jparsec.Scanners.string;
import static org.junit.Assert.*;

import org.codehaus.jparsec.Parser;
import org.codehaus.jparsec.Parsers;
import org.codehaus.jparsec.error.ParserException;
import org.codehaus.jparsec.functors.Binary;
import org.codehaus.jparsec.functors.Unary;
import org.codehaus.jparsec.util.ObjectTester;
import org.junit.Test;
import static org.codehaus.jparsec.misc.Mapper.skip;
import static org.codehaus.jparsec.Scanners.string;

/**
 * Unit test for {@link Curry}.
 * 
 * @author Ben Yu
 */
public class CurryTest {
  
  static final class Foo {
    final String name;
    final int size;
    
    public Foo(String name, int size) {
      this.name = name;
      this.size = size;
    }
  }
  
  static final class Bar {
    final String name;
    final int size;
    
    public Bar(String name, int size) {
      this.name = name;
      this.size = size;
    }
  }
  
  static final class Baz {
    final byte b;
    final short s;
    final int i;
    final long l;
    final char c;
   
    public Baz(byte b, short s, int i, long l, char c) {
      this.b = b;
      this.s = s;
      this.i = i;
      this.l = l;
      this.c = c;
    }
  }

  @Test
  public void testSequence() {
    Parser<Foo> parser =
        Curry.of(Foo.class).sequence(Parsers.constant("foo"), Parsers.constant(1));
    Foo foo = parser.parse("");
    assertEquals("foo", foo.name);
    assertEquals(1, foo.size);
  }

  @Test
  public void testUnary() {
    Unary<Object> unary = Curry.<Object>of(Foo.class, 1).unary().parse("");
    Foo foo = (Foo) unary.map("foo");
    assertEquals("foo", foo.name);
    assertEquals(1, foo.size);
  }

  @Test
  public void testBinary() {
    Binary<Object> binary = Curry.<Object>of(Foo.class).binary().parse("");
    Foo foo = (Foo) binary.map("foo", 2);
    assertEquals("foo", foo.name);
    assertEquals(2, foo.size);
  }
  
  interface Expr {}
  
  private static final Expr FAKE_EXPR = new Expr() {};
  
  static class PrefixExpr implements Expr {
    final String op;
    final Expr expr;
    
    public PrefixExpr(String op, Expr expr) {
      this.op = op;
      this.expr = expr;
    }
  }
  
  static class PrefixExpr2 implements Expr {
    final String op;
    final int size;
    final Expr expr;
    
    public PrefixExpr2(String op, int size, Expr expr) {
      this.op = op;
      this.size = size;
      this.expr = expr;
    }
  }
  
  static class PostfixExpr implements Expr {
    final Expr expr;
    final String op;
    
    public PostfixExpr(Expr expr, String op) {
      this.expr = expr;
      this.op = op;
    }
  }
  
  static class PostfixExpr2 implements Expr {
    final Expr expr;
    final int size;
    final String op;
    
    public PostfixExpr2(Expr expr, int size, String op) {
      this.expr = expr;
      this.size = size;
      this.op = op;
    }
  }
  
  static class InfixExpr implements Expr {
    final Expr left;
    final String op;
    final Expr right;
    
    public InfixExpr(Expr left, String op, Expr right) {
      this.left = left;
      this.op = op;
      this.right = right;
    }
  }
  
  static class InfixExpr2 implements Expr {
    final Expr left;
    final String op;
    final int size;
    final Expr right;
    
    public InfixExpr2(Expr left, String op, int size, Expr right) {
      this.left = left;
      this.op = op;
      this.size = size;
      this.right = right;
    }
  }

  @Test
  public void testPrefix() {
    Expr result = Curry.<Expr>of(PrefixExpr.class).prefix(constant("x"))
        .parse("").map(FAKE_EXPR);
    PrefixExpr prefix = (PrefixExpr) result;
    assertEquals("x", prefix.op);
    assertSame(FAKE_EXPR, prefix.expr);
  }

  @Test
  public void testPrefix_onlyOneUnskippedOperator() {
    Expr result = Curry.<Expr>of(PrefixExpr.class).prefix(skip(string("foo")), constant("x"))
        .parse("foo").map(FAKE_EXPR);
    PrefixExpr prefix = (PrefixExpr) result;
    assertEquals("x", prefix.op);
    assertSame(FAKE_EXPR, prefix.expr);
  }

  @Test
  public void testPrefix_multiOp() {
    Expr result = Curry.<Expr>of(PrefixExpr2.class).prefix(constant("x"), constant(2))
        .parse("").map(FAKE_EXPR);
    PrefixExpr2 prefix = (PrefixExpr2) result;
    assertEquals("x", prefix.op);
    assertEquals(2, prefix.size);
    assertSame(FAKE_EXPR, prefix.expr);
  }

  @Test
  public void testPostfix() {
    Expr result = Curry.<Expr>of(PostfixExpr.class).postfix(constant("x"))
        .parse("").map(FAKE_EXPR);
    PostfixExpr postfix = (PostfixExpr) result;
    assertEquals("x", postfix.op);
    assertSame(FAKE_EXPR, postfix.expr);
  }

  @Test
  public void testPostfix_onlyOneUnskippedOperator() {
    Expr result = Curry.<Expr>of(PostfixExpr.class).postfix(skip(string("foo")), constant("x"))
        .parse("foo").map(FAKE_EXPR);
    PostfixExpr postfix = (PostfixExpr) result;
    assertEquals("x", postfix.op);
    assertSame(FAKE_EXPR, postfix.expr);
  }

  @Test
  public void testPostfix_multiOp() {
    Expr result = Curry.<Expr>of(PostfixExpr2.class).postfix(constant(1), constant("x"))
        .parse("").map(FAKE_EXPR);
    PostfixExpr2 postfix = (PostfixExpr2) result;
    assertEquals("x", postfix.op);
    assertEquals(1, postfix.size);
    assertSame(FAKE_EXPR, postfix.expr);
  }

  @Test
  public void testInfix() {
    Expr left = FAKE_EXPR;
    Expr right = new Expr() {};
    Expr result = Curry.<Expr>of(InfixExpr.class).infix(constant("x"))
        .parse("").map(left, right);
    InfixExpr infix = (InfixExpr) result;
    assertEquals("x", infix.op);
    assertSame(left, infix.left);
    assertSame(right, infix.right);
  }

  @Test
  public void testInfix_onlyOneUnskippedOperator() {
    Expr left = FAKE_EXPR;
    Expr right = new Expr() {};
    Expr result = Curry.<Expr>of(InfixExpr.class).infix(skip(string("foo")), constant("x"))
        .parse("foo").map(left, right);
    InfixExpr infix = (InfixExpr) result;
    assertEquals("x", infix.op);
    assertSame(left, infix.left);
    assertSame(right, infix.right);
  }

  @Test
  public void testInfix_multiOp() {
    Expr left = FAKE_EXPR;
    Expr right = new Expr() {};
    Expr result = Curry.<Expr>of(InfixExpr2.class).infix(constant("x"), constant(3))
        .parse("").map(left, right);
    InfixExpr2 infix = (InfixExpr2) result;
    assertEquals("x", infix.op);
    assertEquals(3, infix.size);
    assertSame(left, infix.left);
    assertSame(right, infix.right);
  }
  
  static final class ThrowError {
    public ThrowError(String message) {
      throw new AssertionError(message);
    }
  }

  @Test
  public void testSequence_propagatesError() {
    Parser<ThrowError> parser = Curry.of(ThrowError.class).sequence(Parsers.constant("foo"));
    try {
      parser.parse("");
      fail();
    } catch (AssertionError e) {
      assertEquals("foo", e.getMessage());
    }
  }
  
  static final class ThrowUncheckedException {
    public ThrowUncheckedException(String message) {
      throw new IllegalArgumentException(message);
    }
  }

  @Test
  public void testSequence_propagatesUncheckedException() {
    Parser<ThrowUncheckedException> parser =
        Curry.of(ThrowUncheckedException.class).sequence(Parsers.constant("foo"));
    try {
      parser.parse("");
      fail();
    } catch (ParserException e) {
      assertEquals("foo", e.getCause().getMessage());
    }
  }
  
  static final class ThrowCheckedException {
    public ThrowCheckedException(String message) throws Exception {
      throw new Exception(message);
    }
  }

  @Test
  public void testSequence_propagatesCheckedException() {
    Parser<ThrowCheckedException> parser =
        Curry.of(ThrowCheckedException.class).sequence(Parsers.constant("foo"));
    try {
      parser.parse("");
      fail();
    } catch (ParserException e) {
      assertEquals("foo", e.getCause().getCause().getMessage());
    }
  }

  @Test
  public void testToString() {
    assertEquals(Foo.class.getName(), Curry.of(Foo.class).toString());
  }

  @Test
  public void testName() {
    assertEquals(Foo.class.getName(), Curry.of(Foo.class).name());
  }

  @Test
  public void testEquals() {
    ObjectTester.assertEqual(Curry.of(Foo.class), Curry.of(Foo.class));
    ObjectTester.assertEqual(Curry.of(Foo.class, "foo"), Curry.of(Foo.class, "foo"));
    ObjectTester.assertNotEqual(Curry.of(Foo.class, "foo"),
        Curry.of(Foo.class, "bar"), Curry.of(Bar.class, "foo"));
  }
  
  static abstract class AbstractBar {
    public AbstractBar() {}
  }

  @Test
  public void testAbstractClass() {
    try {
      Curry.of(AbstractBar.class);
      fail();
    } catch (IllegalArgumentException e) {
      assertEquals("Cannot curry abstract class: " + AbstractBar.class.getName(), e.getMessage());
    }
  }
  
  static class NoPublicConstructor {
    NoPublicConstructor() {}
  }

  @Test
  public void testNoPublicConstructor() {
    try {
      Curry.of(NoPublicConstructor.class);
      fail();
    } catch (IllegalArgumentException e) {
      assertTrue(e.getMessage(),
          e.getMessage().contains("public constructor in " + NoPublicConstructor.class.getName()));
    }
  }
  
  static class AmbiguousConstructor {
    public AmbiguousConstructor(@SuppressWarnings("unused") int i) {}
    public AmbiguousConstructor(@SuppressWarnings("unused") String n) {}
  }

  @Test
  public void testAmbiguousConstructor() {
    try {
      Curry.of(AmbiguousConstructor.class);
      fail();
    } catch (IllegalArgumentException e) {
      assertTrue(e.getMessage(),
          e.getMessage().contains("public constructor in " + AmbiguousConstructor.class.getName()));
    }
  }
  
  static class VarargConstructor {
    public VarargConstructor(@SuppressWarnings("unused") String... names) {}
  }

  @Test
  public void testVarargConstructor() {
    try {
      Curry.of(VarargConstructor.class);
      fail();
    } catch (IllegalArgumentException e) {
      assertTrue(e.getMessage(),
          e.getMessage().contains("Cannot curry for constructor with varargs"));
    }
  }

  @Test
  public void testTooManyCurryArgs() {
    try {
      Curry.of(Foo.class, "foo", 1, 2);
      fail();
    } catch (IllegalArgumentException e) {}
  }

  @Test
  public void testCurryArgTypeMismatch() {
    try {
      Curry.of(Foo.class, 1L);
      fail();
    } catch (IllegalArgumentException e) {
      assertTrue(e.getMessage(), e.getMessage().contains("Long"));
    }
  }

  @Test
  public void testAmbiguousCurryArg() {
    try {
      Curry.of(Foo.class, 1, 2);
      fail();
    } catch (IllegalArgumentException e) {
      assertTrue(e.getMessage(), e.getMessage().contains("int"));
    }
  }

  @Test
  public void testWrongArgumentType() {
    try {
      Curry.<Object>of(Foo.class).asBinary().map("foo", 2L);
      fail();
    } catch (IllegalArgumentException e) {
      assertTrue(e.getMessage(), e.getMessage().contains("int"));
    }
  }

  @Test
  public void testAsUnary_wrongParamNumber() {
    Curry<Foo> curry = Curry.of(Foo.class);
    try {
      curry.asUnary();
      fail();
    } catch (IllegalArgumentException e) {}
  }

  @Test
  public void testAsBinary_wrongParamNumber() {
    Curry<Foo> curry = Curry.of(Foo.class, 1);
    try {
      curry.asBinary();
      fail();
    } catch (IllegalArgumentException e) {}
  }

  @Test
  public void testPrefix_wrongParamNumber() {
    Curry<Foo> curry = Curry.of(Foo.class, 1);
    try {
      curry.prefix(Parsers.constant(2));
      fail();
    } catch (IllegalArgumentException e) {
      assertTrue(e.getMessage(), e.getMessage().contains("2 parameters expected"));
      assertTrue(e.getMessage(), e.getMessage().contains("3 will be provided"));
    }
  }

  @Test
  public void testPrefix_multiOp_wrongParamNumber() {
    Curry<Foo> curry = Curry.of(Foo.class);
    try {
      curry.prefix(Parsers.constant(1), Parsers.constant(2));
      fail();
    } catch (IllegalArgumentException e) {
      assertTrue(e.getMessage(), e.getMessage().contains("2 parameters expected"));
      assertTrue(e.getMessage(), e.getMessage().contains("3 will be provided"));
    }
  }

  @Test
  public void testPostfix_wrongParamNumber() {
    Curry<Foo> curry = Curry.of(Foo.class, 1);
    try {
      curry.postfix(Parsers.constant(2));
      fail();
    } catch (IllegalArgumentException e) {
      assertTrue(e.getMessage(), e.getMessage().contains("2 parameters expected"));
      assertTrue(e.getMessage(), e.getMessage().contains("3 will be provided"));
    }
  }

  @Test
  public void testPostfix_multiOp_wrongParamNumber() {
    Curry<Foo> curry = Curry.of(Foo.class);
    try {
      curry.postfix(Parsers.constant(1), Parsers.constant(2));
      fail();
    } catch (IllegalArgumentException e) {
      assertTrue(e.getMessage(), e.getMessage().contains("2 parameters expected"));
      assertTrue(e.getMessage(), e.getMessage().contains("3 will be provided"));
    }
  }

  @Test
  public void testInfix_wrongParamNumber() {
    Curry<Foo> curry = Curry.of(Foo.class);
    try {
      curry.infix(Parsers.constant(2));
      fail();
    } catch (IllegalArgumentException e) {
      assertTrue(e.getMessage(), e.getMessage().contains("2 parameters expected"));
      assertTrue(e.getMessage(), e.getMessage().contains("3 will be provided"));
    }
  }

  @Test
  public void testInfix_multiOp_wrongParamNumber() {
    Curry<Foo> curry = Curry.of(Foo.class);
    try {
      curry.infix(Parsers.constant(1), Parsers.constant(2));
      fail();
    } catch (IllegalArgumentException e) {
      assertTrue(e.getMessage(), e.getMessage().contains("2 parameters expected"));
      assertTrue(e.getMessage(), e.getMessage().contains("4 will be provided"));
    }
  }

  @Test
  public void testInvoke_wrongParameterNumber() throws Throwable {
    try {
      Curry.of(Foo.class, 1).invoke(new Object[] {"foo", "bar"});
      fail();
    } catch (IllegalArgumentException e) {
      assertTrue(e.getMessage(), e.getMessage().contains("1 parameters expected, 2 provided: "));
    }
  }

}
