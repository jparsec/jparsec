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
import org.jparsec.examples.java.ast.declaration.*;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.jparsec.examples.java.parser.ExpressionParser.IDENTIFIER;
import static org.jparsec.examples.java.parser.TerminalParserTest.assertFailure;
import static org.junit.Assert.assertEquals;

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
    TerminalParserTest.assertResult(FIELD, "int f;", FieldDef.class, "int f;");
    TerminalParserTest.assertResult(FIELD, "static final int f;", FieldDef.class, "static final int f;");
    TerminalParserTest.assertResult(FIELD, "int f = foo;", FieldDef.class, "int f = foo;");
    TerminalParserTest.assertResult(FIELD, "int[] a = {foo};", FieldDef.class, "int[] a = {foo};");
  }

  @Test
  public void testBody() {
    Parser<DefBody> parser = DeclarationParser.body(FIELD);
    TerminalParserTest.assertResult(parser, "{}", DefBody.class, "{}");
    TerminalParserTest.assertResult(parser, "{int f;}", DefBody.class, "{int f;}");
    TerminalParserTest.assertResult(parser, "{int f=foo; int g;}", DefBody.class, "{int f = foo; int g;}");
    TerminalParserTest.assertResult(parser, "{;int f=foo;;; int g;;}", DefBody.class, "{int f = foo; int g;}");
  }

  @Test
  public void testTypeParameter() {
    Parser<TypeParameterDef> parser = DeclarationParser.TYPE_PARAMETER;
    TerminalParserTest.assertResult(parser, "T", TypeParameterDef.class, "T");
    TerminalParserTest.assertResult(parser, "T extends F", TypeParameterDef.class, "T extends F");
    TerminalParserTest.assertResult(parser, "T extends Enum<T>", TypeParameterDef.class, "T extends Enum<T>");
    TerminalParserTest.assertResult(parser, "T extends Enum<?>", TypeParameterDef.class, "T extends Enum<?>");
    TerminalParserTest.assertFailure(parser, "T extends ?", 1, 11, "? encountered.");
  }

  @Test
  public void testMethodDef() {
    Parser<Member> parser = DeclarationParser.methodDef(
        StatementParser.SYSTEM_MODIFIER, IDENTIFIER, StatementParser.BREAK);
    TerminalParserTest.assertResult(parser, "public static void f();", MethodDef.class, "public static void f();");
    TerminalParserTest.assertResult(parser, "String f() default foo;", MethodDef.class, "String f() default foo;");
    TerminalParserTest.assertResult(parser, "void f() throws E;",
        MethodDef.class, "void f() throws E;");
    TerminalParserTest.assertResult(parser, "void f() throws E, F<T>;",
        MethodDef.class, "void f() throws E, F<T>;");
    TerminalParserTest.assertFailure(parser, "void f() throws", 1, 16);
    TerminalParserTest.assertFailure(parser, "void f() throws E[];", 1, 18);
    TerminalParserTest.assertResult(parser, "void f() {}", MethodDef.class, "void f() {}");
    TerminalParserTest.assertResult(parser, "void f() {break; break;}",
        MethodDef.class, "void f() {break; break;}");
    TerminalParserTest.assertResult(parser, "void f(int i) {}",
        MethodDef.class, "void f(int i) {}");
    TerminalParserTest.assertResult(parser, "void f(final int i, List<Foo> l) {}",
        MethodDef.class, "void f(final int i, List<Foo> l) {}");
    TerminalParserTest.assertResult(parser, "<K, V extends K> void f(int i) {}",
        MethodDef.class, "<K, V extends K> void f(int i) {}");
  }

  @Test
  public void testConstructorDef() {
    Parser<Member> parser = DeclarationParser.constructorDef(
        StatementParser.SYSTEM_MODIFIER, StatementParser.BREAK);
    TerminalParserTest.assertResult(parser, "public Foo(){}", ConstructorDef.class, "public Foo() {}");
    TerminalParserTest.assertResult(parser, "Foo() throws E{break;}", ConstructorDef.class, "Foo() throws E {break;}");
    TerminalParserTest.assertResult(parser, "Foo(int i) {}", ConstructorDef.class, "Foo(int i) {}");
    TerminalParserTest.assertResult(parser, "Foo(final int i, List<Foo> l) {}",
        ConstructorDef.class, "Foo(final int i, List<Foo> l) {}");
  }

  @Test
  public void testInitializerDef() {
    Parser<Member> parser = DeclarationParser.initializerDef(StatementParser.BREAK);
    TerminalParserTest.assertResult(parser, "static {}", ClassInitializerDef.class, "static {}");
    TerminalParserTest.assertResult(parser, "static {break;}", ClassInitializerDef.class, "static {break;}");
    TerminalParserTest.assertResult(parser, " {}", ClassInitializerDef.class, "{}");
    TerminalParserTest.assertResult(parser, " {break;}", ClassInitializerDef.class, "{break;}");
  }

  @Test
  public void testClassDef() {
    Parser<Declaration> parser = DeclarationParser.classDef(StatementParser.SYSTEM_MODIFIER, FIELD);
    TerminalParserTest.assertResult(parser, "public final class Foo {}", ClassDef.class, "public final class Foo {}");
    TerminalParserTest.assertResult(parser, "final class Foo<T> {}", ClassDef.class, "final class Foo<T> {}");
    TerminalParserTest.assertResult(parser, "final class Foo<T extends Foo<T>, K> {}",
        ClassDef.class, "final class Foo<T extends Foo<T>, K> {}");
    TerminalParserTest.assertResult(parser, "final class Foo<T extends Foo<T>> extends ArrayList<?> {}",
        ClassDef.class, "final class Foo<T extends Foo<T>> extends ArrayList<?> {}");
    TerminalParserTest.assertResult(parser, "final class Foo<T extends Foo<T>> implements List<?> {}",
        ClassDef.class, "final class Foo<T extends Foo<T>> implements List<?> {}");
    TerminalParserTest.assertResult(parser, "final class Foo<T extends Foo<T>> implements List<?>, Iterable<T> {}",
        ClassDef.class, "final class Foo<T extends Foo<T>> implements List<?>, Iterable<T> {}");
    TerminalParserTest.assertResult(parser, "final class Foo<T extends Foo<T>> {public static final String S = foo;}",
        ClassDef.class, "final class Foo<T extends Foo<T>> {public static final String S = foo;}");
    TerminalParserTest.assertResult(parser, "final class Foo<T extends Foo<T>> {int i; int j;}",
        ClassDef.class, "final class Foo<T extends Foo<T>> {int i; int j;}");
  }

  @Test
  public void testInterfaceDef() {
    Parser<Declaration> parser = DeclarationParser.interfaceDef(StatementParser.SYSTEM_MODIFIER, FIELD);
    TerminalParserTest.assertResult(parser, "public native interface Foo {}",
        InterfaceDef.class, "public native interface Foo {}");
    TerminalParserTest.assertResult(parser, "interface Foo<T> {}", InterfaceDef.class, "interface Foo<T> {}");
    TerminalParserTest.assertResult(parser, "interface Foo<T extends Foo<T>, K> {}",
        InterfaceDef.class, "interface Foo<T extends Foo<T>, K> {}");
    TerminalParserTest.assertResult(parser, "interface Foo<T extends Foo<T>> extends List<?> {}",
        InterfaceDef.class, "interface Foo<T extends Foo<T>> extends List<?> {}");
    TerminalParserTest.assertFailure(parser, "interface Foo implements List {}", 1, 15, "implements encountered.");
    TerminalParserTest.assertResult(parser, "interface Foo<T extends Foo<T>> extends List<?>, Iterable<T> {}",
        InterfaceDef.class, "interface Foo<T extends Foo<T>> extends List<?>, Iterable<T> {}");
    TerminalParserTest.assertResult(parser, "interface Foo<T extends Foo<T>> {public static String S = foo;}",
        InterfaceDef.class, "interface Foo<T extends Foo<T>> {public static String S = foo;}");
    TerminalParserTest.assertResult(parser, "interface Foo<T extends Foo<T>> {int i; int j;}",
        InterfaceDef.class, "interface Foo<T extends Foo<T>> {int i; int j;}");
  }

  @Test
  public void testAnnotationDef() {
    Parser<Declaration> parser =
        DeclarationParser.annotationDef(StatementParser.modifier(IDENTIFIER), FIELD);
    TerminalParserTest.assertResult(parser, "@interface Foo{}", AnnotationDef.class, "@interface Foo {}");
    TerminalParserTest.assertResult(parser, "@Target({METHOD, FIELD}) @RetentionPolicy(RUNTIME) @interface Foo{}",
        AnnotationDef.class, "@Target({METHOD, FIELD}) @RetentionPolicy(RUNTIME) @interface Foo {}");
    TerminalParserTest.assertResult(parser, "@interface Foo{int i;int j;}",
        AnnotationDef.class, "@interface Foo {int i; int j;}");
  }

  @Test
  public void testEnumDef() {
    Parser<Declaration> parser = DeclarationParser.enumDef(IDENTIFIER, FIELD);
    TerminalParserTest.assertResult(parser, "enum Foo {}", EnumDef.class, "enum Foo {}");
    TerminalParserTest.assertResult(parser, "enum Foo {FOO{int x;}}", EnumDef.class, "enum Foo {FOO {int x;}}");
    TerminalParserTest.assertResult(parser, "@For(Test) enum Foo {}", EnumDef.class, "@For(Test) enum Foo {}");
    TerminalParserTest.assertResult(parser, "enum Foo implements Comparable<Foo>, Serializable {}",
        EnumDef.class, "enum Foo implements Comparable<Foo>, Serializable {}");
    TerminalParserTest.assertResult(parser, "enum Foo {ONE, TWO(two); int i; int j;}",
        EnumDef.class, "enum Foo {ONE, TWO(two); int i; int j;}");
    TerminalParserTest.assertResult(parser, "enum Foo {ONE, TWO}", EnumDef.class, "enum Foo {ONE, TWO}");
  }

  @Test
  public void testQualifiedName() {
    Parser<QualifiedName> parser = DeclarationParser.QUALIFIED_NAME;
    TerminalParserTest.assertResult(parser, "foo.bar", QualifiedName.class, "foo.bar");
    TerminalParserTest.assertResult(parser, "foo", QualifiedName.class, "foo");
  }

  @Test
  public void testPackage() {
    Parser<QualifiedName> parser = DeclarationParser.PACKAGE;
    TerminalParserTest.assertResult(parser, "package foo.bar;", QualifiedName.class, "foo.bar");
    TerminalParserTest.assertResult(parser, "package foo;", QualifiedName.class, "foo");
  }

  @Test
  public void testImport() {
    Parser<Import> parser = DeclarationParser.IMPORT;
    TerminalParserTest.assertResult(parser, "import foo;", Import.class, "import foo;");
    TerminalParserTest.assertResult(parser, "import foo.bar;", Import.class, "import foo.bar;");
    TerminalParserTest.assertResult(parser, "import foo.bar.*;", Import.class, "import foo.bar.*;");
    TerminalParserTest.assertResult(parser, "import static foo;", Import.class, "import static foo;");
    TerminalParserTest.assertResult(parser, "import static foo.*;", Import.class, "import static foo.*;");
  }

  @Test
  public void testProgram() {
    Parser<Program> parser = DeclarationParser.program();
    TerminalParserTest.assertResult(parser, "package foo; import foo.bar.*; class Foo {int[] a = {1}; Foo(){}}",
        Program.class, "package foo; import foo.bar.*; class Foo {int[] a = {1}; Foo() {}}");
    TerminalParserTest.assertResult(parser, "class Foo {{} static {}}",
        Program.class, "class Foo {{} static {}}");
    TerminalParserTest.assertResult(parser, "package foo; import foo.bar.*; enum Foo {}",
        Program.class, "package foo; import foo.bar.*; enum Foo {}");
    TerminalParserTest.assertResult(parser, "enum Foo {;static {1;} static {2;} {3;} {4;}}",
        Program.class, "enum Foo {; static {1;} static {2;} {3;} {4;}}");
    TerminalParserTest.assertResult(parser, "package foo; import foo.bar.*; interface Foo {int i = 1;}",
        Program.class, "package foo; import foo.bar.*; interface Foo {int i = 1;}");
    TerminalParserTest.assertResult(parser, "package foo; import foo.bar.*; @interface Foo {int[] value() default {1};}",
        Program.class, "package foo; import foo.bar.*; @interface Foo {int[] value() default {1};}");
    TerminalParserTest.assertResult(parser, "import foo.bar.*; class Foo<T> implements Bar {} interface Bar {}",
        Program.class, "import foo.bar.*; class Foo<T> implements Bar {} interface Bar {}");
    TerminalParserTest.assertResult(parser, "class Foo {class Bar {}}",
        Program.class, "class Foo {class Bar {}}");
    TerminalParserTest.assertResult(parser, "class Foo {private static final class Bar {}}",
        Program.class, "class Foo {private static final class Bar {}}");
    TerminalParserTest.assertResult(parser, "class Foo {enum Bar {B}}",
        Program.class, "class Foo {enum Bar {B}}");
    TerminalParserTest.assertResult(parser, "class Foo {@interface Bar {;;}}",
        Program.class, "class Foo {@interface Bar {}}");
  }
}
