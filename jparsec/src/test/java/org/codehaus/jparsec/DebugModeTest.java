package org.codehaus.jparsec;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.List;

import org.codehaus.jparsec.error.ParserException;
import org.codehaus.jparsec.pattern.CharPredicates;
import org.junit.Test;

public class DebugModeTest {

  @Test
  public void emptyParseTreeInParserException() {
    try {
      Scanners.string("hello").parse("", Parser.Mode.DEBUG);
      fail();
    } catch (ParserException e) {
      assertParseTree(rootNode(""), e.getParseTree());
    }
  }

  @Test
  public void nonLabeledParserDoesNotPopulateParseTree() {
    try {
      Scanners.string("hello").source().parse("hello world", Parser.Mode.DEBUG);
      fail();
    } catch (ParserException e) {
      assertParseTree(rootNode("hello"), e.getParseTree());
    }
  }

  @Test
  public void partialMatchDoesNotPopulateParseTree() {
    try {
      Parser<?> parser = Scanners.string("hello ").source().label("hi");
      parser.parse("hello", Parser.Mode.DEBUG);
      fail();
    } catch (ParserException e) {
      assertParseTree(rootNode(""), e.getParseTree());
    }
  }

  @Test
  public void explicitLabelPopulatesParseTree() {
    try {
      Scanners.string("hello").source().label("hi").parse("hello world", Parser.Mode.DEBUG);
      fail();
    } catch (ParserException e) {
      assertParseTree(rootNode("hello", stringNode("hi", "hello")), e.getParseTree());
    }
  }

  @Test
  public void twoChildrenNodesInParseTree() {
    Parser<?> parser = Parsers.sequence(
        Scanners.string("hello").source().label("hi"),
        Scanners.string("world").source().label("you"));
    try {
      parser.parse("helloworld ", Parser.Mode.DEBUG);
      fail();
    } catch (ParserException e) {
      assertParseTree(rootNode("helloworld", stringNode("hi", "hello"), stringNode("you", "world")),
          e.getParseTree());
    }
  }

  @Test
  public void firstChildMatchesInPlusParser() {
    Parser<?> parser = Parsers.plus(
        Scanners.string("hello").source().label("hi"),
        Scanners.string("hello world").source().label("hi you"));
    try {
      parser.label("greeting").parse("helloworld", Parser.Mode.DEBUG);
      fail();
    } catch (ParserException e) {
      assertParseTree(rootNode("hello", stringNode("greeting", "hello", stringNode("hi", "hello"))),
          e.getParseTree());
    }
  }

  @Test
  public void firstChildConsumedInputInPlusParser() {
    Parser<?> parser = Parsers.plus(
        Scanners.string("hello").followedBy(Scanners.isChar(' ')).source().label("hi"),
        Scanners.string("hello").next(Scanners.string("world")).source().label("hi you"));
    try {
      parser.label("greeting").parse("helloworld", Parser.Mode.DEBUG);
      fail();
    } catch (ParserException e) {
      assertParseTree(rootNode("hello", node("greeting", null, "hello")), e.getParseTree());
    }
  }

  @Test
  public void secondChildMatchesInPlusParser() {
    Parser<?> parser = Parsers.plus(
        Scanners.string("hello ").source().label("hi"),
        Scanners.string("hello").next(Scanners.string("world")).source().label("hi you"));
    try {
      parser.label("greeting").parse("helloworldx", Parser.Mode.DEBUG);
      fail();
    } catch (ParserException e) {
      assertParseTree(
          rootNode("helloworld",
              stringNode("greeting", "helloworld", stringNode("hi you", "helloworld"))),
          e.getParseTree());
    }
  }

  @Test
  public void secondChildMismatchesInPlusParser() {
    Parser<?> parser = Parsers.plus(
        Scanners.string("hello ").source().label("hi"),
        Scanners.string("bye").source().label("farewell"));
    try {
      parser.label("greeting").parse("hello", Parser.Mode.DEBUG);
      fail();
    } catch (ParserException e) {
      assertParseTree(rootNode("", node("greeting", null, "")), e.getParseTree());
    }
  }

  @Test
  public void grandChildInOrParser() {
    Parser<?> parser = Parsers.or(
        Scanners.string("hello ").source().label("hi"),
        Scanners.string("hello").next(Scanners.string("world")).source().label("hi you"));
    try {
      parser.label("greeting").parse("helloworldx", Parser.Mode.DEBUG);
      fail();
    } catch (ParserException e) {
      assertParseTree(
          rootNode("helloworld",
              stringNode("greeting", "helloworld", stringNode("hi you", "helloworld"))),
          e.getParseTree());
    }
  }

  @Test
  public void longerGrandChildMatchesInLongerParser() {
    Parser<?> parser = Parsers.longer(
        Scanners.string("hello ").source().label("hi"),
        Scanners.string("hello").next(Scanners.string("world")).source().label("hi you"));
    try {
      parser.label("greeting").parse("helloworldx", Parser.Mode.DEBUG);
      fail();
    } catch (ParserException e) {
      assertParseTree(
          rootNode("helloworld",
              stringNode("greeting", "helloworld", stringNode("hi you", "helloworld"))),
          e.getParseTree());
    }
  }

  @Test
  public void shorterGrandChildMatchesInLongerParser() {
    Parser<?> parser = Parsers.longer(
        Scanners.string("hello ").source().label("hi"),
        Scanners.string("hello").next(Scanners.string("world")).source().label("hi you"));
    try {
      parser.label("greeting").parse("helloworldx", Parser.Mode.DEBUG);
      fail();
    } catch (ParserException e) {
      assertParseTree(
          rootNode("helloworld",
              stringNode("greeting", "helloworld", stringNode("hi you", "helloworld"))),
          e.getParseTree());
    }
  }

  @Test
  public void bothGrandChildrenMatchInLongerParser() {
    Parser<?> parser = Parsers.longer(
        Scanners.string("hello ").source().label("hi"),
        Scanners.string("hello").next(Scanners.string(" world")).source().label("hi you"));
    try {
      parser.label("greeting").parse("hello world x", Parser.Mode.DEBUG);
      fail();
    } catch (ParserException e) {
      assertParseTree(
          rootNode("hello world",
              stringNode("greeting", "hello world", stringNode("hi you", "hello world"))),
          e.getParseTree());
    }
  }

  @Test
  public void longerGrandChildMatchesInShorterParser() {
    Parser<?> parser = Parsers.shorter(
        Scanners.string("hello ").source().label("hi"),
        Scanners.string("hello").next(Scanners.string("world")).source().label("hi you"));
    try {
      parser.label("greeting").parse("helloworldx", Parser.Mode.DEBUG);
      fail();
    } catch (ParserException e) {
      assertParseTree(
          rootNode("helloworld",
              stringNode("greeting", "helloworld", stringNode("hi you", "helloworld"))),
          e.getParseTree());
    }
  }

  @Test
  public void failedOptionalAttemptDoesNotPopulateParseTree() {
    Parser<?> parser = Scanners.string("hello ").optional().source().label("hi");
    try {
      parser.parse("helloworld", Parser.Mode.DEBUG);
      fail();
    } catch (ParserException e) {
      assertParseTree(rootNode(""), e.getParseTree());
    }
  }

  @Test
  public void shorterGrandChildMatchesInShorterParser() {
    Parser<?> parser = Parsers.shorter(
        Scanners.string("hello ").source().label("hi"),
        Scanners.string("hello").next(Scanners.string("world")).source().label("hi you"));
    try {
      parser.label("greeting").parse("hello world", Parser.Mode.DEBUG);
      fail();
    } catch (ParserException e) {
      assertParseTree(
          rootNode("hello ",
              stringNode("greeting", "hello ", stringNode("hi", "hello "))),
          e.getParseTree());
    }
  }

  @Test
  public void bothGrandChildrenMatchInShorterParser() {
    Parser<?> parser = Parsers.shorter(
        Scanners.string("hello").source().label("hi"),
        Scanners.string("hello").next(Scanners.string("world")).source().label("hi you"));
    try {
      parser.label("greeting").parse("helloworldx", Parser.Mode.DEBUG);
      fail();
    } catch (ParserException e) {
      assertParseTree(
          rootNode("hello",
              stringNode("greeting", "hello", stringNode("hi", "hello"))),
          e.getParseTree());
    }
  }

  @Test
  public void ifElseParserWithTrueBranchFailed() {
    Parser<?> parser = Scanners.isChar('@').label("?")
        .ifelse(Scanners.INTEGER.label("tel"), Scanners.IDENTIFIER.label("name"));
    try {
      parser.label("id").parse("@abc", Parser.Mode.DEBUG);
      fail();
    } catch (ParserException e) {
      assertParseTree(rootNode("@", node("id", null, "@", node("?", null, "@"))), e.getParseTree());
    }
  }

  @Test
  public void ifElseParserWithTrueBranchSucceeded() {
    Parser<?> parser = Scanners.isChar('@').label("?")
        .ifelse(Scanners.INTEGER.label("tel"), Scanners.IDENTIFIER.label("name"));
    try {
      parser.label("id").parse("@123x", Parser.Mode.DEBUG);
      fail();
    } catch (ParserException e) {
      assertParseTree(
          rootNode("@123", node("id", "123", "@123", node("?", null, "@"), stringNode("tel", "123"))),
          e.getParseTree());
    }
  }

  @Test
  public void ifElseParserWithFalseBranchFailed() {
    Parser<?> parser = Scanners.isChar('@').label("?")
        .ifelse(Scanners.INTEGER.label("tel"), Scanners.IDENTIFIER.label("name"));
    try {
      parser.label("id").parse("123", Parser.Mode.DEBUG);
      fail();
    } catch (ParserException e) {
      assertParseTree(rootNode("", node("id", null, "")), e.getParseTree());
    }
  }

  @Test
  public void ifElseParserWithFalseBranchSucceeded() {
    Parser<?> parser = Scanners.isChar('@').label("?")
        .ifelse(Scanners.INTEGER.label("tel"), Scanners.IDENTIFIER.label("name"));
    try {
      parser.label("id").parse("Ben ", Parser.Mode.DEBUG);
      fail();
    } catch (ParserException e) {
      assertParseTree(rootNode("Ben", stringNode("id", "Ben", stringNode("name", "Ben"))),
          e.getParseTree());
    }
  }

  @Test
  public void manyProducesListInParseTree() {
    Parser<?> parser = Scanners.isChar(CharPredicates.IS_DIGIT).source().label("d").many();
    try {
      parser.label("digits").parse("123 ", Parser.Mode.DEBUG);
      fail();
    } catch (ParserException e) {
      assertParseTree(
          rootNode("123", node("digits", asList("1", "2", "3"), "123",
              stringNode("d", "1"),
              stringNode("d", "2"),
              stringNode("d", "3"))),
          e.getParseTree());
    }
  }

  @Test
  public void atLeastProducesListInParseTree() {
    Parser<?> parser = Scanners.isChar(CharPredicates.IS_DIGIT).source().label("d").atLeast(1);
    try {
      parser.label("digits").parse("123 ", Parser.Mode.DEBUG);
      fail();
    } catch (ParserException e) {
      assertParseTree(
          rootNode("123", node("digits", asList("1", "2", "3"), "123",
              stringNode("d", "1"),
              stringNode("d", "2"),
              stringNode("d", "3"))),
          e.getParseTree());
    }
  }

  @Test
  public void timesProducesListInParseTree() {
    Parser<?> parser = Scanners.isChar(CharPredicates.IS_DIGIT).source().label("d").times(0, 2);
    try {
      parser.label("digits").parse("123", Parser.Mode.DEBUG);
      fail();
    } catch (ParserException e) {
      assertParseTree(
          rootNode("12", node("digits", asList("1", "2"), "12",
              stringNode("d", "1"),
              stringNode("d", "2"))),
          e.getParseTree());
    }
  }

  private static void assertParseTree(MatchNode expected, ParseTree actual) {
    assertParseTree(0, expected, actual);
  }

  private static void assertParseTree(int offset, MatchNode expected, ParseTree actual) {
    assertEquals(actual.toString(), expected.name, actual.getName());
    assertEquals(actual.toString(), offset, actual.getBeginIndex());
    assertEquals(actual.toString(),
        offset + expected.skipped + expected.matched.length(), actual.getEndIndex());
    assertEquals(actual.toString(), expected.value, actual.getValue());
    assertEquals(actual.toString(), expected.children.size(), actual.getChildren().size());
    for (int i = 0; i < expected.children.size(); i++) {
      MatchNode expectedChild = expected.children.get(i);
      assertParseTree(offset, expectedChild, actual.getChildren().get(i));
      offset += expectedChild.skipped + expectedChild.matched.length();
    }
  }

  private static MatchNode stringNode(String name, String matched, MatchNode... children) {
    return node(name, matched, matched, children);
  }

  private static MatchNode rootNode(String matched, MatchNode... children) {
    return node("root", null, matched, children);
  }

  private static MatchNode node(String name, Object value, String matched, MatchNode... children) {
    return new MatchNode(0, name, value, matched, asList(children));
  }

  private static final class MatchNode {
    final int skipped;
    final String name;
    final String matched;
    final Object value;
    final List<MatchNode> children;

    MatchNode(int skipped, String name, Object value, String matched, List<MatchNode> children) {
      this.skipped = skipped;
      this.name = name;
      this.matched = matched;
      this.value = value;
      this.children = children;
    }
  }
}
