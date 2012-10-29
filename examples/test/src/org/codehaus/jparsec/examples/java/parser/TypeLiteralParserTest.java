package org.codehaus.jparsec.examples.java.parser;

import static org.codehaus.jparsec.examples.java.parser.TerminalParserTest.assertFailure;
import static org.codehaus.jparsec.examples.java.parser.TerminalParserTest.assertResult;

import java.util.Arrays;

import junit.framework.TestCase;

import org.codehaus.jparsec.Parser;
import org.codehaus.jparsec.examples.java.ast.type.ArrayTypeLiteral;
import org.codehaus.jparsec.examples.java.ast.type.SimpleTypeLiteral;
import org.codehaus.jparsec.examples.java.ast.type.TypeLiteral;

/**
 * Unit test for [@link TypeLiteralParser}.
 * 
 * @author Ben Yu
 */
public class TypeLiteralParserTest extends TestCase {
  
  public void testArrayOf() {
    SimpleTypeLiteral intType =
        new SimpleTypeLiteral(Arrays.asList("int"), TypeLiteralParser.EMPTY_TYPE_ARGUMENT_LIST);
    TerminalParserTest.assertToString(ArrayTypeLiteral.class, "int[]",
        TerminalParser.parse(TypeLiteralParser.ARRAY_OF, "[]").map(intType));
  }
  
  public void testTypeLiteral() {
    Parser<TypeLiteral> parser = TypeLiteralParser.TYPE_LITERAL;
    assertResult(parser, "int", SimpleTypeLiteral.class, "int");
    assertResult(parser, "a.b.c", SimpleTypeLiteral.class, "a.b.c");
    assertResult(parser, "java.util.Map<K, V>", SimpleTypeLiteral.class, "java.util.Map<K, V>");
    assertResult(parser, "Pair<A, Pair<A,B>>", SimpleTypeLiteral.class, "Pair<A, Pair<A, B>>");
    assertResult(parser, "Pair<?, ?>", SimpleTypeLiteral.class, "Pair<?, ?>");
    assertResult(parser, "List<? extends List<?>>",
        SimpleTypeLiteral.class, "List<? extends List<?>>");
    assertFailure(parser, "?", 1, 1);
    assertFailure(parser, "List<? extends ?>", 1, 16);
    assertResult(parser, "Pair<? extends A, ? super B>",
        SimpleTypeLiteral.class, "Pair<? extends A, ? super B>");
    assertResult(parser, "int[]", ArrayTypeLiteral.class, "int[]");
    assertResult(parser, "Pair<A, Pair<A,B>>[]", ArrayTypeLiteral.class, "Pair<A, Pair<A, B>>[]");
    assertResult(parser, "int[][]", ArrayTypeLiteral.class, "int[][]");
  }
  
  public void testElementTypeLiteral() {
    Parser<TypeLiteral> parser = TypeLiteralParser.ELEMENT_TYPE_LITERAL;
    assertResult(parser, "int", SimpleTypeLiteral.class, "int");
    assertFailure(parser, "int[]", 1, 4);
  }
  
  public void testArrayTypeLiteral() {
    Parser<TypeLiteral> parser = TypeLiteralParser.ARRAY_TYPE_LITERAL;
    assertResult(parser, "int[]", ArrayTypeLiteral.class, "int[]");
    assertFailure(parser, "int", 1, 4);
  }
}
