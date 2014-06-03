package org.codehaus.jparsec.examples.java.parser;

import static org.codehaus.jparsec.examples.java.parser.ExpressionParser.IDENTIFIER;
import static org.codehaus.jparsec.examples.java.parser.StatementParser.SYSTEM_MODIFIER;
import static org.codehaus.jparsec.examples.java.parser.TerminalParserTest.assertFailure;
import static org.codehaus.jparsec.examples.java.parser.TerminalParserTest.assertResult;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.codehaus.jparsec.Parser;
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
import org.codehaus.jparsec.examples.java.ast.declaration.Program;
import org.codehaus.jparsec.examples.java.ast.declaration.QualifiedName;
import org.codehaus.jparsec.examples.java.ast.declaration.TypeParameterDef;
import org.junit.Test;

/**
 * Unit test for {@link DeclarationParser}.
 * 
 * @author Ben Yu
 */
public class DeclarationParserTest {
  
  private static final Parser<Member> FIELD = DeclarationParser.fieldDef(IDENTIFIER);

  @Test
  public void testRemoveNulls() {
    List<?> list = new ArrayList<String>(Arrays.asList("a", "b", null, "1", "2", null));
    DeclarationParser.removeNulls(list);
    assertEquals(Arrays.asList("a", "b", "1", "2"), list);
  }

  @Test
  public void testFieldDef() {
    assertResult(FIELD, "int f;", FieldDef.class, "int f;");
    assertResult(FIELD, "static final int f;", FieldDef.class, "static final int f;");
    assertResult(FIELD, "int f = foo;", FieldDef.class, "int f = foo;");
    assertResult(FIELD, "int[] a = {foo};", FieldDef.class, "int[] a = {foo};");
  }

  @Test
  public void testBody() {
    Parser<DefBody> parser = DeclarationParser.body(FIELD);
    assertResult(parser, "{}", DefBody.class, "{}");
    assertResult(parser, "{int f;}", DefBody.class, "{int f;}");
    assertResult(parser, "{int f=foo; int g;}", DefBody.class, "{int f = foo; int g;}");
    assertResult(parser, "{;int f=foo;;; int g;;}", DefBody.class, "{int f = foo; int g;}");
  }

  @Test
  public void testTypeParameter() {
    Parser<TypeParameterDef> parser = DeclarationParser.TYPE_PARAMETER;
    assertResult(parser, "T", TypeParameterDef.class, "T");
    assertResult(parser, "T extends F", TypeParameterDef.class, "T extends F");
    assertResult(parser, "T extends Enum<T>", TypeParameterDef.class, "T extends Enum<T>");
    assertResult(parser, "T extends Enum<?>", TypeParameterDef.class, "T extends Enum<?>");
    assertFailure(parser, "T extends ?", 1, 11, "? encountered.");
  }

  @Test
  public void testMethodDef() {
    Parser<Member> parser = DeclarationParser.methodDef(
        SYSTEM_MODIFIER, IDENTIFIER, StatementParser.BREAK);
    assertResult(parser, "public static void f();", MethodDef.class, "public static void f();");
    assertResult(parser, "String f() default foo;", MethodDef.class, "String f() default foo;");
    assertResult(parser, "void f() throws E;",
        MethodDef.class, "void f() throws E;");
    assertResult(parser, "void f() throws E, F<T>;",
        MethodDef.class, "void f() throws E, F<T>;");
    assertFailure(parser, "void f() throws", 1, 16);
    assertFailure(parser, "void f() throws E[];", 1, 18);
    assertResult(parser, "void f() {}", MethodDef.class, "void f() {}");
    assertResult(parser, "void f() {break; break;}",
        MethodDef.class, "void f() {break; break;}");
    assertResult(parser, "void f(int i) {}",
        MethodDef.class, "void f(int i) {}");
    assertResult(parser, "void f(final int i, List<Foo> l) {}",
        MethodDef.class, "void f(final int i, List<Foo> l) {}");
    assertResult(parser, "<K, V extends K> void f(int i) {}",
        MethodDef.class, "<K, V extends K> void f(int i) {}");
  }

  @Test
  public void testConstructorDef() {
    Parser<Member> parser = DeclarationParser.constructorDef(
        SYSTEM_MODIFIER, StatementParser.BREAK);
    assertResult(parser, "public Foo(){}", ConstructorDef.class, "public Foo() {}");
    assertResult(parser, "Foo() throws E{break;}", ConstructorDef.class, "Foo() throws E {break;}");
    assertResult(parser, "Foo(int i) {}", ConstructorDef.class, "Foo(int i) {}");
    assertResult(parser, "Foo(final int i, List<Foo> l) {}",
        ConstructorDef.class, "Foo(final int i, List<Foo> l) {}");
  }

  @Test
  public void testInitializerDef() {
    Parser<Member> parser = DeclarationParser.initializerDef(StatementParser.BREAK);
    assertResult(parser, "static {}", ClassInitializerDef.class, "static {}");
    assertResult(parser, "static {break;}", ClassInitializerDef.class, "static {break;}");
    assertResult(parser, " {}", ClassInitializerDef.class, "{}");
    assertResult(parser, " {break;}", ClassInitializerDef.class, "{break;}");
  }

  @Test
  public void testClassDef() {
    Parser<Declaration> parser = DeclarationParser.classDef(SYSTEM_MODIFIER, FIELD);
    assertResult(parser, "public final class Foo {}", ClassDef.class, "public final class Foo {}");
    assertResult(parser, "final class Foo<T> {}", ClassDef.class, "final class Foo<T> {}");
    assertResult(parser, "final class Foo<T extends Foo<T>, K> {}",
        ClassDef.class, "final class Foo<T extends Foo<T>, K> {}");
    assertResult(parser, "final class Foo<T extends Foo<T>> extends ArrayList<?> {}",
        ClassDef.class, "final class Foo<T extends Foo<T>> extends ArrayList<?> {}");
    assertResult(parser, "final class Foo<T extends Foo<T>> implements List<?> {}",
        ClassDef.class, "final class Foo<T extends Foo<T>> implements List<?> {}");
    assertResult(parser, "final class Foo<T extends Foo<T>> implements List<?>, Iterable<T> {}",
        ClassDef.class, "final class Foo<T extends Foo<T>> implements List<?>, Iterable<T> {}");
    assertResult(parser, "final class Foo<T extends Foo<T>> {public static final String S = foo;}",
        ClassDef.class, "final class Foo<T extends Foo<T>> {public static final String S = foo;}");
    assertResult(parser, "final class Foo<T extends Foo<T>> {int i; int j;}",
        ClassDef.class, "final class Foo<T extends Foo<T>> {int i; int j;}");
  }

  @Test
  public void testInterfaceDef() {
    Parser<Declaration> parser = DeclarationParser.interfaceDef(SYSTEM_MODIFIER, FIELD);
    assertResult(parser, "public native interface Foo {}",
        InterfaceDef.class, "public native interface Foo {}");
    assertResult(parser, "interface Foo<T> {}", InterfaceDef.class, "interface Foo<T> {}");
    assertResult(parser, "interface Foo<T extends Foo<T>, K> {}",
        InterfaceDef.class, "interface Foo<T extends Foo<T>, K> {}");
    assertResult(parser, "interface Foo<T extends Foo<T>> extends List<?> {}",
        InterfaceDef.class, "interface Foo<T extends Foo<T>> extends List<?> {}");
    assertFailure(parser, "interface Foo implements List {}", 1, 15, "implements encountered.");
    assertResult(parser, "interface Foo<T extends Foo<T>> extends List<?>, Iterable<T> {}",
        InterfaceDef.class, "interface Foo<T extends Foo<T>> extends List<?>, Iterable<T> {}");
    assertResult(parser, "interface Foo<T extends Foo<T>> {public static String S = foo;}",
        InterfaceDef.class, "interface Foo<T extends Foo<T>> {public static String S = foo;}");
    assertResult(parser, "interface Foo<T extends Foo<T>> {int i; int j;}",
        InterfaceDef.class, "interface Foo<T extends Foo<T>> {int i; int j;}");
  }

  @Test
  public void testAnnotationDef() {
    Parser<Declaration> parser =
        DeclarationParser.annotationDef(StatementParser.modifier(IDENTIFIER), FIELD);
    assertResult(parser, "@interface Foo{}", AnnotationDef.class, "@interface Foo {}");
    assertResult(parser, "@Target({METHOD, FIELD}) @RetentionPolicy(RUNTIME) @interface Foo{}",
        AnnotationDef.class, "@Target({METHOD, FIELD}) @RetentionPolicy(RUNTIME) @interface Foo {}");
    assertResult(parser, "@interface Foo{int i;int j;}",
        AnnotationDef.class, "@interface Foo {int i; int j;}");
  }

  @Test
  public void testEnumDef() {
    Parser<Declaration> parser = DeclarationParser.enumDef(IDENTIFIER, FIELD);
    assertResult(parser, "enum Foo {}", EnumDef.class, "enum Foo {}");
    assertResult(parser, "enum Foo {FOO{int x;}}", EnumDef.class, "enum Foo {FOO {int x;}}");
    assertResult(parser, "@For(Test) enum Foo {}", EnumDef.class, "@For(Test) enum Foo {}");
    assertResult(parser, "enum Foo implements Comparable<Foo>, Serializable {}",
        EnumDef.class, "enum Foo implements Comparable<Foo>, Serializable {}");
    assertResult(parser, "enum Foo {ONE, TWO(two); int i; int j;}",
        EnumDef.class, "enum Foo {ONE, TWO(two); int i; int j;}");
    assertResult(parser, "enum Foo {ONE, TWO}", EnumDef.class, "enum Foo {ONE, TWO}");
  }

  @Test
  public void testQualifiedName() {
    Parser<QualifiedName> parser = DeclarationParser.QUALIFIED_NAME;
    assertResult(parser, "foo.bar", QualifiedName.class, "foo.bar");
    assertResult(parser, "foo", QualifiedName.class, "foo");
  }

  @Test
  public void testPackage() {
    Parser<QualifiedName> parser = DeclarationParser.PACKAGE;
    assertResult(parser, "package foo.bar;", QualifiedName.class, "foo.bar");
    assertResult(parser, "package foo;", QualifiedName.class, "foo");
  }

  @Test
  public void testImport() {
    Parser<Import> parser = DeclarationParser.IMPORT;
    assertResult(parser, "import foo;", Import.class, "import foo;");
    assertResult(parser, "import foo.bar;", Import.class, "import foo.bar;");
    assertResult(parser, "import foo.bar.*;", Import.class, "import foo.bar.*;");
    assertResult(parser, "import static foo;", Import.class, "import static foo;");
    assertResult(parser, "import static foo.*;", Import.class, "import static foo.*;");
  }

  @Test
  public void testProgram() {
    Parser<Program> parser = DeclarationParser.program();
    assertResult(parser, "package foo; import foo.bar.*; class Foo {int[] a = {1}; Foo(){}}",
        Program.class, "package foo; import foo.bar.*; class Foo {int[] a = {1}; Foo() {}}");
    assertResult(parser, "class Foo {{} static {}}",
        Program.class, "class Foo {{} static {}}");
    assertResult(parser, "package foo; import foo.bar.*; enum Foo {}",
        Program.class, "package foo; import foo.bar.*; enum Foo {}");
    assertResult(parser, "enum Foo {;static {1;} static {2;} {3;} {4;}}",
        Program.class, "enum Foo {; static {1;} static {2;} {3;} {4;}}");
    assertResult(parser, "package foo; import foo.bar.*; interface Foo {int i = 1;}",
        Program.class, "package foo; import foo.bar.*; interface Foo {int i = 1;}");
    assertResult(parser, "package foo; import foo.bar.*; @interface Foo {int[] value() default {1};}",
        Program.class, "package foo; import foo.bar.*; @interface Foo {int[] value() default {1};}");
    assertResult(parser, "import foo.bar.*; class Foo<T> implements Bar {} interface Bar {}",
        Program.class, "import foo.bar.*; class Foo<T> implements Bar {} interface Bar {}");
    assertResult(parser, "class Foo {class Bar {}}",
        Program.class, "class Foo {class Bar {}}");
    assertResult(parser, "class Foo {private static final class Bar {}}",
        Program.class, "class Foo {private static final class Bar {}}");
    assertResult(parser, "class Foo {enum Bar {B}}",
        Program.class, "class Foo {enum Bar {B}}");
    assertResult(parser, "class Foo {@interface Bar {;;}}",
        Program.class, "class Foo {@interface Bar {}}");
  }
}
