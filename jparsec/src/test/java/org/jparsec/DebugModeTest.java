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
package org.jparsec;

import org.jparsec.error.ParserException;
import org.jparsec.pattern.CharPredicates;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static java.util.Arrays.asList;
import static org.junit.Assert.*;

public class DebugModeTest {

  @Test
  public void runtimeExceptionPopulatesErrorParseTree() {
    Parser<?> parser = Scanners.string("hello").source().label("word")
        .map(from -> {
            throw new RuntimeException("intentional");
         }).label("throws");
    try {
      parser.parse("hello", Parser.Mode.DEBUG);
      fail();
    } catch (ParserException e) {
      assertParseTree(rootNode("hello", node("throws", null, "hello", stringNode("word", "hello"))),
          e.getParseTree());
    }
  }

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
  public void nonLabeledParserDoesNotPopulateErrorParseTree() {
    try {
      Scanners.string("hello").source().parse("hello world", Parser.Mode.DEBUG);
      fail();
    } catch (ParserException e) {
      assertParseTree(rootNode("hello"), e.getParseTree());
    }
  }

  @Test
  public void nonLabeledParserDoesNotPopulateParseTree() {
    ParseTree tree = Scanners.string("hello").source().parseTree("hello");
    assertParseTree(rootNode("hello"), tree);
  }

  @Test
  public void partialMatchDoesNotPopulateErrorParseTree() {
    try {
      Parser<?> parser = Scanners.string("hello ").source().label("hi");
      parser.parse("hello", Parser.Mode.DEBUG);
      fail();
    } catch (ParserException e) {
      assertParseTree(rootNode(""), e.getParseTree());
    }
  }

  @Test
  public void explicitLabelPopulatesErrorParseTree() {
    try {
      Scanners.string("hello").source().label("hi").parse("hello world", Parser.Mode.DEBUG);
      fail();
    } catch (ParserException e) {
      assertParseTree(rootNode("hello", stringNode("hi", "hello")), e.getParseTree());
    }
  }

  @Test
  public void explicitLabelPopulatesParseTree() {
    ParseTree tree = Scanners.string("hello").source().label("hi").parseTree("hello");
    assertParseTree(rootNode("hello", stringNode("hi", "hello")), tree);
  }

  @Test
  public void twoChildrenNodesInErrorParseTree() {
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
  public void twoChildrenNodesInParseTree() {
    Parser<?> parser = Parsers.sequence(
        Scanners.string("hello").source().label("hi"),
        Scanners.string("world").source().label("you"));
    ParseTree tree = parser.parseTree("helloworld");
    assertParseTree(rootNode("helloworld", stringNode("hi", "hello"), stringNode("you", "world")),
        tree);
  }

  @Test
  public void grandChildInOrParserPopulatesErrorParseTree() {
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
  public void grandChildInOrParserPopulatesParseTree() {
    Parser<?> parser = Parsers.or(
        Scanners.string("hello ").source().label("hi"),
        Scanners.string("hello").next(Scanners.string("world")).source().label("hi you"));
    ParseTree tree = parser.label("greeting").parseTree("helloworld");
    assertParseTree(
        rootNode("helloworld",
            stringNode("greeting", "helloworld", stringNode("hi you", "helloworld"))),
        tree);
  }

  @Test
  public void longerGrandChildMatchesInLongerParserPopulatesErrorParseTree() {
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
  public void longerGrandChildMatchesInLongerParserPopulatesParseTree() {
    Parser<?> parser = Parsers.longer(
        Scanners.string("hello ").source().label("hi"),
        Scanners.string("hello").next(Scanners.string("world")).source().label("hi you"));
    ParseTree tree = parser.label("greeting").parseTree("helloworld");
    assertParseTree(
        rootNode("helloworld",
            stringNode("greeting", "helloworld", stringNode("hi you", "helloworld"))),
        tree);
  }

  @Test
  public void shorterGrandChildMatchesInLongerParserPopulatesErrorParseTree() {
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
  public void shorterGrandChildMatchesInLongerParserPopulatesParseTree() {
    Parser<?> parser = Parsers.longer(
        Scanners.string("hello ").source().label("hi"),
        Scanners.string("hello").next(Scanners.string("world")).source().label("hi you"));
    ParseTree tree = parser.label("greeting").parseTree("helloworld");
    assertParseTree(
        rootNode("helloworld",
            stringNode("greeting", "helloworld", stringNode("hi you", "helloworld"))),
        tree);
  }

  @Test
  public void bothGrandChildrenMatchInLongerParserPopulatesErrorParseTree() {
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
  public void bothGrandChildrenMatchInLongerParserPopulatesParseTree() {
    Parser<?> parser = Parsers.longer(
        Scanners.string("hello ").source().label("hi"),
        Scanners.string("hello").next(Scanners.string(" world")).source().label("hi you"));
    ParseTree tree = parser.label("greeting").parseTree("hello world");
    assertParseTree(
        rootNode("hello world",
            stringNode("greeting", "hello world", stringNode("hi you", "hello world"))),
        tree);
  }

  @Test
  public void longerGrandChildMatchesInShorterParserPopulatesErrorParseTree() {
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
  public void longerGrandChildMatchesInShorterParserPopulatesParseTree() {
    Parser<?> parser = Parsers.shorter(
        Scanners.string("hello ").source().label("hi"),
        Scanners.string("hello").next(Scanners.string("world")).source().label("hi you"));
    ParseTree tree = parser.label("greeting").parseTree("helloworld");
    assertParseTree(
        rootNode("helloworld",
            stringNode("greeting", "helloworld", stringNode("hi you", "helloworld"))),
        tree);
  }

  @Test
  public void failedOptionalAttemptDoesNotPopulateErrorParseTree() {
    Parser<?> parser = Scanners.string("hello ").label("hi").asOptional().source();
    try {
      parser.parse("helloworld", Parser.Mode.DEBUG);
      fail();
    } catch (ParserException e) {
      assertParseTree(rootNode(""), e.getParseTree());
    }
  }

  @Test
  public void succeededOptionalAttemptDoesNotPopulateParseTree() {
    Parser<?> parser = Scanners.string("hello").label("hi").optional(null).source();
    ParseTree tree = parser.parseTree("hello");
    assertParseTree(rootNode("hello", node("hi", null, "hello")), tree);
  }

  @Test
  public void succeededAsOptionalAttemptDoesNotPopulateParseTree() {
    Parser<?> parser = Scanners.string("hello").label("hi").source().asOptional();
    ParseTree tree = parser.parseTree("hello");
    assertParseTree(rootNode("hello", node("hi", null, "hello")), tree);
  }

  @Test
  public void shorterGrandChildMatchesInShorterParserPopulatesErrorParseTree() {
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
  public void shorterGrandChildMatchesInShorterParserPopulatesParseTree() {
    Parser<?> parser = Parsers.shorter(
        Scanners.string("hello ").source().label("hi"),
        Scanners.string("hello").next(Scanners.string("world")).source().label("hi you"));
    ParseTree tree = parser.followedBy(Scanners.ANY_CHAR.skipMany()).label("greeting")
        .parseTree("hello world");
    assertParseTree(
        rootNode("hello world",
            node("greeting", "hello ", "hello world", stringNode("hi", "hello "))),
        tree);
  }

  @Test
  public void bothGrandChildrenMatchInShorterParserPopulatesErrorParseTree() {
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
  public void bothGrandChildrenMatchInShorterParserPopulatesParseTree() {
    Parser<?> parser = Parsers.shorter(
        Scanners.string("hello").source().label("hi"),
        Scanners.string("hello").next(Scanners.string("world")).source().label("hi you"));
    ParseTree tree = parser.label("greeting").followedBy(Scanners.ANY_CHAR.skipMany())
        .parseTree("helloworld");
    assertParseTree(
        rootNode("helloworld",
            stringNode("greeting", "hello", stringNode("hi", "hello"))),
        tree);
  }

  @Test
  public void ifElseParserWithTrueBranchFailedPopulatesErrorParseTree() {
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
  public void ifElseParserWithTrueBranchSucceededPopulatesErrorParseTree() {
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
  public void ifElseParserWithTrueBranchSucceededPopulatesParseTree() {
    Parser<?> parser = Scanners.isChar('@').label("?")
        .ifelse(Scanners.INTEGER.label("tel"), Scanners.IDENTIFIER.label("name"));
    ParseTree tree = parser.label("id").parseTree("@123");
    assertParseTree(
        rootNode("@123", node("id", "123", "@123", node("?", null, "@"), stringNode("tel", "123"))),
        tree);
  }

  @Test
  public void ifElseParserWithFalseBranchFailedPopulatesErrorParseTree() {
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
  public void ifElseParserWithFalseBranchSucceededPopulatesErrorParseTree() {
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
  public void ifElseParserWithFalseBranchSucceededPopulatesParseTree() {
    Parser<?> parser = Scanners.isChar('@').label("?")
        .ifelse(Scanners.INTEGER.label("tel"), Scanners.IDENTIFIER.label("name"));
    ParseTree tree = parser.label("id").parseTree("Ben");
    assertParseTree(rootNode("Ben", stringNode("id", "Ben", stringNode("name", "Ben"))), tree);
  }

  @Test
  public void endByProducesEmptyListInErrorParseTree() {
    Parser<?> parser =
        Scanners.isChar(CharPredicates.IS_DIGIT).source().label("d").endBy(Scanners.isChar(';'));
    try {
      parser.label("digits").parse("; ", Parser.Mode.DEBUG);
      fail();
    } catch (ParserException e) {
      assertParseTree(
          rootNode("", node("digits", Arrays.<String>asList(), "")),
          e.getParseTree());
    }
  }

  @Test
  public void endByProducesSingleElementListInErrorParseTree() {
    Parser<?> parser =
        Scanners.isChar(CharPredicates.IS_DIGIT).source().label("d").endBy(Scanners.isChar(';'));
    try {
      parser.label("digits").parse("1; ", Parser.Mode.DEBUG);
      fail();
    } catch (ParserException e) {
      assertParseTree(
          rootNode("1;", node("digits", asList("1"), "1;", stringNode("d", "1"))),
          e.getParseTree());
    }
  }

  @Test
  public void endByProducesTwoElementListInErrorParseTree() {
    Parser<?> parser =
        Scanners.isChar(CharPredicates.IS_DIGIT).source().label("d").endBy(Scanners.isChar(';'));
    try {
      parser.label("digits").parse("1;2; ", Parser.Mode.DEBUG);
      fail();
    } catch (ParserException e) {
      assertParseTree(
          rootNode("1;2;", node("digits", asList("1", "2"), "1;2;",
                  stringNode("d", "1"),
                  stringNode("d", "2").leading(1))),
          e.getParseTree());
    }
  }

  @Test
  public void endByProducesEmptyListInParseTree() {
    Parser<?> parser =
        Scanners.isChar(CharPredicates.IS_DIGIT).source().label("d").endBy(Scanners.isChar(';'));
    ParseTree tree = parser.label("digits").parseTree("");
    assertParseTree(rootNode("", node("digits", Arrays.<String>asList(), "")), tree);
  }

  @Test
  public void endByProducesSingleElementListInParseTree() {
    Parser<?> parser =
        Scanners.isChar(CharPredicates.IS_DIGIT).source().label("d").endBy(Scanners.isChar(';'));
    ParseTree tree = parser.label("digits").parseTree("1;");
    assertParseTree(
        rootNode("1;", node("digits", asList("1"), "1;", stringNode("d", "1"))),
        tree);
  }

  @Test
  public void endByProducesTwoElementListInParseTree() {
    Parser<?> parser =
        Scanners.isChar(CharPredicates.IS_DIGIT).source().label("d").endBy(Scanners.isChar(';'));
    ParseTree tree = parser.label("digits").parseTree("1;2;");
    assertParseTree(
        rootNode("1;2;", node("digits", asList("1", "2"), "1;2;",
                stringNode("d", "1"),
                stringNode("d", "2").leading(1))),
        tree);
  }

  @Test
  public void endBy1ProducesEmptyListInErrorParseTree() {
    Parser<?> parser =
        Scanners.isChar(CharPredicates.IS_DIGIT).source().label("d").endBy1(Scanners.isChar(';'));
    try {
      parser.label("digits").parse("; ", Parser.Mode.DEBUG);
      fail();
    } catch (ParserException e) {
      assertParseTree(
          rootNode("", node("digits", null, "")),
          e.getParseTree());
    }
  }

  @Test
  public void endBy1ProducesSingleElementListInErrorParseTree() {
    Parser<?> parser =
        Scanners.isChar(CharPredicates.IS_DIGIT).source().label("d").endBy1(Scanners.isChar(';'));
    try {
      parser.label("digits").parse("1; ", Parser.Mode.DEBUG);
      fail();
    } catch (ParserException e) {
      assertParseTree(
          rootNode("1;", node("digits", asList("1"), "1;", stringNode("d", "1"))),
          e.getParseTree());
    }
  }

  @Test
  public void endBy1ProducesTwoElementListInErrorParseTree() {
    Parser<?> parser =
        Scanners.isChar(CharPredicates.IS_DIGIT).source().label("d").endBy1(Scanners.isChar(';'));
    try {
      parser.label("digits").parse("1;2; ", Parser.Mode.DEBUG);
      fail();
    } catch (ParserException e) {
      assertParseTree(
          rootNode("1;2;", node("digits", asList("1", "2"), "1;2;",
                  stringNode("d", "1"),
                  stringNode("d", "2").leading(1))),
          e.getParseTree());
    }
  }

  @Test
  public void endBy1ProducesSingleElementListInParseTree() {
    Parser<?> parser =
        Scanners.isChar(CharPredicates.IS_DIGIT).source().label("d").endBy1(Scanners.isChar(';'));
    ParseTree tree = parser.label("digits").parseTree("1;");
    assertParseTree(
        rootNode("1;", node("digits", asList("1"), "1;", stringNode("d", "1"))),
        tree);
  }

  @Test
  public void endBy1ProducesTwoElementListInParseTree() {
    Parser<?> parser =
        Scanners.isChar(CharPredicates.IS_DIGIT).source().label("d").endBy1(Scanners.isChar(';'));
    ParseTree tree = parser.label("digits").parseTree("1;2;");
    assertParseTree(
        rootNode("1;2;", node("digits", asList("1", "2"), "1;2;",
                stringNode("d", "1"),
                stringNode("d", "2").leading(1))),
        tree);
  }

  @Test
  public void sepByProducesEmptyListInErrorParseTree() {
    Parser<?> parser =
        Scanners.isChar(CharPredicates.IS_DIGIT).source().label("d").sepBy(Scanners.isChar(','));
    try {
      parser.label("digits").parse(" ", Parser.Mode.DEBUG);
      fail();
    } catch (ParserException e) {
      assertParseTree(
          rootNode("", node("digits", Arrays.<String>asList(), "")),
          e.getParseTree());
    }
  }

  @Test
  public void sepByProducesSingleElementListInErrorParseTree() {
    Parser<?> parser =
        Scanners.isChar(CharPredicates.IS_DIGIT).source().label("d").sepBy(Scanners.isChar(','));
    try {
      parser.label("digits").parse("1 ", Parser.Mode.DEBUG);
      fail();
    } catch (ParserException e) {
      assertParseTree(
          rootNode("1", node("digits", asList("1"), "1", stringNode("d", "1"))),
          e.getParseTree());
    }
  }

  @Test
  public void sepByProducesTwoElementListInErrorParseTree() {
    Parser<?> parser =
        Scanners.isChar(CharPredicates.IS_DIGIT).source().label("d").sepBy(Scanners.isChar(','));
    try {
      parser.label("digits").parse("1,2 ", Parser.Mode.DEBUG);
      fail();
    } catch (ParserException e) {
      assertParseTree(
          rootNode("1,2", node("digits",
              asList("1", "2"), "1,2",
                  stringNode("d", "1"),
                  stringNode("d", "2").leading(1)
                  )),
          e.getParseTree());
    }
  }

  @Test
  public void sepByProducesEmptyListInParseTree() {
    Parser<?> parser =
        Scanners.isChar(CharPredicates.IS_DIGIT).source().label("d").sepBy(Scanners.isChar(','));
    ParseTree tree = parser.label("digits").parseTree("");
    assertParseTree(
        rootNode("", node("digits", Arrays.<String>asList(), "")),
        tree);
  }

  @Test
  public void sepByProducesSingleElementListInParseTree() {
    Parser<?> parser =
        Scanners.isChar(CharPredicates.IS_DIGIT).source().label("d").sepBy(Scanners.isChar(','));
    ParseTree tree = parser.label("digits").parseTree("1");
    assertParseTree(
        rootNode("1", node("digits", asList("1"), "1", stringNode("d", "1"))),
        tree);
  }

  @Test
  public void sepByProducesTwoElementListInParseTree() {
    Parser<?> parser =
        Scanners.isChar(CharPredicates.IS_DIGIT).source().label("d").sepBy(Scanners.isChar(','));
    ParseTree tree = parser.label("digits").parseTree("1,2");
    assertParseTree(
        rootNode("1,2", node("digits",
            asList("1", "2"), "1,2",
                stringNode("d", "1"),
                stringNode("d", "2").leading(1)
                )),
        tree);
  }

  @Test
  public void sepBy1ProducesEmptyListInErrorParseTree() {
    Parser<?> parser =
        Scanners.isChar(CharPredicates.IS_DIGIT).source().label("d").sepBy1(Scanners.isChar(','));
    try {
      parser.label("digits").parse("", Parser.Mode.DEBUG);
      fail();
    } catch (ParserException e) {
      assertParseTree(
          rootNode("", node("digits", null, "")),
          e.getParseTree());
    }
  }

  @Test
  public void sepBy1ProducesSingleElementListInErrorParseTree() {
    Parser<?> parser =
        Scanners.isChar(CharPredicates.IS_DIGIT).source().label("d").sepBy1(Scanners.isChar(','));
    try {
      parser.label("digits").parse("1 ", Parser.Mode.DEBUG);
      fail();
    } catch (ParserException e) {
      assertParseTree(
          rootNode("1", node("digits", asList("1"), "1", stringNode("d", "1"))),
          e.getParseTree());
    }
  }

  @Test
  public void sepBy1ProducesTwoElementListInErrorParseTree() {
    Parser<?> parser =
        Scanners.isChar(CharPredicates.IS_DIGIT).source().label("d").sepBy1(Scanners.isChar(','));
    try {
      parser.label("digits").parse("1,2 ", Parser.Mode.DEBUG);
      fail();
    } catch (ParserException e) {
      assertParseTree(
          rootNode("1,2", node("digits",
              asList("1", "2"), "1,2",
                  stringNode("d", "1"),
                  stringNode("d", "2").leading(1)
                  )),
          e.getParseTree());
    }
  }

  @Test
  public void sepBy1ProducesSingleElementListInParseTree() {
    Parser<?> parser =
        Scanners.isChar(CharPredicates.IS_DIGIT).source().label("d").sepBy1(Scanners.isChar(','));
    ParseTree tree = parser.label("digits").parseTree("1");
    assertParseTree(
        rootNode("1", node("digits", asList("1"), "1", stringNode("d", "1"))),
        tree);
  }

  @Test
  public void sepBy1ProducesTwoElementListInParseTree() {
    Parser<?> parser =
        Scanners.isChar(CharPredicates.IS_DIGIT).source().label("d").sepBy1(Scanners.isChar(','));
    ParseTree tree = parser.label("digits").parseTree("1,2");
    assertParseTree(
        rootNode("1,2", node("digits",
            asList("1", "2"), "1,2",
                stringNode("d", "1"),
                stringNode("d", "2").leading(1)
                )),
        tree);
  }

  @Test
  public void sepEndBy1ProducesEmptyListInErrorParseTree() {
    Parser<?> parser =
        Scanners.isChar(CharPredicates.IS_DIGIT).source().label("d").sepEndBy1(Scanners.isChar(','));
    try {
      parser.label("digits").parse(" ", Parser.Mode.DEBUG);
      fail();
    } catch (ParserException e) {
      assertParseTree(
          rootNode("", node("digits", null, "")),
          e.getParseTree());
    }
  }

  @Test
  public void sepEndBy1ProducesSingleElementListInErrorParseTree() {
    Parser<?> parser =
        Scanners.isChar(CharPredicates.IS_DIGIT).source().label("d").sepEndBy1(Scanners.isChar(','));
    try {
      parser.label("digits").parse("1, ", Parser.Mode.DEBUG);
      fail();
    } catch (ParserException e) {
      assertParseTree(
          rootNode("1,", node("digits", asList("1"), "1,",
              stringNode("d", "1"))),
          e.getParseTree());
    }
  }

  @Test
  public void sepEndBy1ProducesListInErrorParseTree() {
    Parser<?> parser =
        Scanners.isChar(CharPredicates.IS_DIGIT).source().label("d").sepEndBy1(Scanners.isChar(','));
    try {
      parser.label("digits").parse("1,2,3 ", Parser.Mode.DEBUG);
      fail();
    } catch (ParserException e) {
      assertParseTree(
          rootNode("1,2,3", node("digits", asList("1", "2", "3"), "1,2,3",
              stringNode("d", "1"),
              stringNode("d", "2").leading(1),
              stringNode("d", "3").leading(1))),
          e.getParseTree());
    }
  }

  @Test
  public void sepEndBy1ProducesSingleElementListInParseTree() {
    Parser<?> parser =
        Scanners.isChar(CharPredicates.IS_DIGIT).source().label("d").sepEndBy1(Scanners.isChar(','));
    ParseTree tree = parser.label("digits").parseTree("1,");
    assertParseTree(
        rootNode("1,", node("digits", asList("1"), "1,",
            stringNode("d", "1"))),
        tree);
  }

  @Test
  public void sepEndBy1ProducesListInParseTree() {
    Parser<?> parser =
        Scanners.isChar(CharPredicates.IS_DIGIT).source().label("d").sepEndBy1(Scanners.isChar(','));
    ParseTree tree = parser.label("digits").parseTree("1,2,3");
    assertParseTree(
        rootNode("1,2,3", node("digits", asList("1", "2", "3"), "1,2,3",
            stringNode("d", "1"),
            stringNode("d", "2").leading(1),
            stringNode("d", "3").leading(1))),
        tree);
  }

  @Test
  public void sepEndByProducesEmptyListInErrorParseTree() {
    Parser<?> parser =
        Scanners.isChar(CharPredicates.IS_DIGIT).source().label("d").sepEndBy(Scanners.isChar(','));
    try {
      parser.label("digits").parse(" ", Parser.Mode.DEBUG);
      fail();
    } catch (ParserException e) {
      assertParseTree(
          rootNode("", node("digits", Arrays.<String>asList(), "")),
          e.getParseTree());
    }
  }

  @Test
  public void sepEndByProducesSingleElementListInErrorParseTree() {
    Parser<?> parser =
        Scanners.isChar(CharPredicates.IS_DIGIT).source().label("d").sepEndBy(Scanners.isChar(','));
    try {
      parser.label("digits").parse("1, ", Parser.Mode.DEBUG);
      fail();
    } catch (ParserException e) {
      assertParseTree(
          rootNode("1,", node("digits", asList("1"), "1,",
              stringNode("d", "1"))),
          e.getParseTree());
    }
  }

  @Test
  public void sepEndByProducesListInErrorParseTree() {
    Parser<?> parser =
        Scanners.isChar(CharPredicates.IS_DIGIT).source().label("d").sepEndBy(Scanners.isChar(','));
    try {
      parser.label("digits").parse("1,2,3 ", Parser.Mode.DEBUG);
      fail();
    } catch (ParserException e) {
      assertParseTree(
          rootNode("1,2,3", node("digits", asList("1", "2", "3"), "1,2,3",
              stringNode("d", "1"),
              stringNode("d", "2").leading(1),
              stringNode("d", "3").leading(1))),
          e.getParseTree());
    }
  }

  @Test
  public void sepEndByProducesEmptyListInParseTree() {
    Parser<?> parser =
        Scanners.isChar(CharPredicates.IS_DIGIT).source().label("d").sepEndBy(Scanners.isChar(','));
    ParseTree tree = parser.label("digits").parseTree("");
    assertParseTree(
        rootNode("", node("digits", Arrays.<String>asList(), "")),
        tree);
  }

  @Test
  public void sepEndByProducesSingleElementListInParseTree() {
    Parser<?> parser =
        Scanners.isChar(CharPredicates.IS_DIGIT).source().label("d").sepEndBy(Scanners.isChar(','));
    ParseTree tree = parser.label("digits").parseTree("1,");
    assertParseTree(
        rootNode("1,", node("digits", asList("1"), "1,",
            stringNode("d", "1"))),
        tree);
  }

  @Test
  public void sepEndByProducesListInParseTree() {
    Parser<?> parser =
        Scanners.isChar(CharPredicates.IS_DIGIT).source().label("d").sepEndBy(Scanners.isChar(','));
    ParseTree tree = parser.label("digits").parseTree("1,2,3");
    assertParseTree(
        rootNode("1,2,3", node("digits", asList("1", "2", "3"), "1,2,3",
            stringNode("d", "1"),
            stringNode("d", "2").leading(1),
            stringNode("d", "3").leading(1))),
        tree);
  }

  @Test
  public void manyProducesEmptyListInParseTree() {
    Parser<?> parser =
        Scanners.isChar(CharPredicates.IS_DIGIT).source().label("d").many();
    ParseTree tree = parser.label("digits").parseTree("");
    assertParseTree(rootNode("", node("digits", Arrays.<String>asList(), "")), tree);
  }

  @Test
  public void manyProducesSingleElementListInParseTree() {
    Parser<?> parser =
        Scanners.isChar(CharPredicates.IS_DIGIT).source().label("d").many();
    ParseTree tree = parser.label("digits").parseTree("1");
    assertParseTree(
        rootNode("1", node("digits", Arrays.asList("1"), "1", stringNode("d", "1"))), tree);
  }

  @Test
  public void manyProducesTwoElementsListInParseTree() {
    Parser<?> parser =
        Scanners.isChar(CharPredicates.IS_DIGIT).source().label("d").many();
    ParseTree tree = parser.label("digits").parseTree("12");
    assertParseTree(
        rootNode("12", node("digits", Arrays.asList("1", "2"), "12",
            stringNode("d", "1"), stringNode("d", "2"))),
        tree);
  }

  @Test
  public void many1ProducesSingleElementListInParseTree() {
    Parser<?> parser =
        Scanners.isChar(CharPredicates.IS_DIGIT).source().label("d").many1();
    ParseTree tree = parser.label("digits").parseTree("1");
    assertParseTree(
        rootNode("1", node("digits", Arrays.asList("1"), "1", stringNode("d", "1"))), tree);
  }

  @Test
  public void many1ProducesTwoElementsListInParseTree() {
    Parser<?> parser =
        Scanners.isChar(CharPredicates.IS_DIGIT).source().label("d").many1();
    ParseTree tree = parser.label("digits").parseTree("12");
    assertParseTree(
        rootNode("12", node("digits", Arrays.asList("1", "2"), "12",
            stringNode("d", "1"), stringNode("d", "2"))),
        tree);
  }

  @Test
  public void atMostProducesEmptyListInParseTree() {
    Parser<?> parser =
        Scanners.isChar(CharPredicates.IS_DIGIT).source().label("d").times(0, 2);
    ParseTree tree = parser.label("digits").parseTree("");
    assertParseTree(rootNode("", node("digits", Arrays.<String>asList(), "")), tree);
  }

  @Test
  public void atMostProducesSingleElementListInParseTree() {
    Parser<?> parser =
        Scanners.isChar(CharPredicates.IS_DIGIT).source().label("d").times(0, 2);
    ParseTree tree = parser.label("digits").parseTree("1");
    assertParseTree(
        rootNode("1", node("digits", Arrays.asList("1"), "1", stringNode("d", "1"))), tree);
  }

  @Test
  public void atMostProducesTwoElementsListInParseTree() {
    Parser<?> parser =
        Scanners.isChar(CharPredicates.IS_DIGIT).source().label("d").times(0, 2);
    ParseTree tree = parser.label("digits").parseTree("12");
    assertParseTree(
        rootNode("12", node("digits", Arrays.asList("1", "2"), "12",
            stringNode("d", "1"), stringNode("d", "2"))),
        tree);
  }

  @Test
  public void timesProducesEmptyListInParseTree() {
    Parser<?> parser =
        Scanners.isChar(CharPredicates.IS_DIGIT).source().label("d").times(0);
    ParseTree tree = parser.label("digits").parseTree("");
    assertParseTree(rootNode("", node("digits", Arrays.<String>asList(), "")), tree);
  }

  @Test
  public void timesProducesSingleElementListInParseTree() {
    Parser<?> parser =
        Scanners.isChar(CharPredicates.IS_DIGIT).source().label("d").times(1);
    ParseTree tree = parser.label("digits").parseTree("1");
    assertParseTree(
        rootNode("1", node("digits", Arrays.asList("1"), "1", stringNode("d", "1"))), tree);
  }

  @Test
  public void timesProducesTwoElementsListInParseTree() {
    Parser<?> parser =
        Scanners.isChar(CharPredicates.IS_DIGIT).source().label("d").times(2);
    ParseTree tree = parser.label("digits").parseTree("12");
    assertParseTree(
        rootNode("12", node("digits", Arrays.asList("1", "2"), "12",
            stringNode("d", "1"), stringNode("d", "2"))),
        tree);
  }

  @Test
  public void manyProducesListInErrorParseTree() {
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
  public void many1ProducesListInErrorParseTree() {
    Parser<?> parser = Scanners.isChar(CharPredicates.IS_DIGIT).source().label("d").many1();
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
  public void atLeastProducesListInErrorParseTree() {
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
  public void timesProducesListInErrorParseTree() {
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

  @Test
  public void terminalsPhrasePopulatedInErrorParseTree() {
    Terminals terminals = Terminals.operators("if", "then");
    Parser<?> parser = terminals.phrase("if", "then")
        .from(terminals.tokenizer(), Scanners.WHITESPACES);
    try {
      parser.parse("if then then", Parser.Mode.DEBUG);
      fail();
    } catch (ParserException e) {
      assertParseTree(rootNode("if then ", node("if then", "if then", "if then ")),
          e.getParseTree());
    }
  }

  @Test
  public void tokenLevelLabelPopulatedInErrorParseTree() {
    Terminals terminals = Terminals.operators("if", "then");
    Parser<?> parser = terminals.token("if").retn(true).label("condition")
        .from(terminals.tokenizer().label("token"), Scanners.WHITESPACES);
    try {
      parser.parse("if then", Parser.Mode.DEBUG);
      fail();
    } catch (ParserException e) {
      assertParseTree(rootNode("if ", node("condition", true, "if ")),
          e.getParseTree());
    }
  }

  @Test
  public void unrecognizedCharactersReportedInTokenLevelErrorParseTree() {
    Terminals terminals = Terminals.operators("if", "then");
    Parser<?> parser = terminals.token("if").retn(true).label("condition")
        .from(terminals.tokenizer().label("token"), Scanners.WHITESPACES);
    try {
      parser.parse("if x", Parser.Mode.DEBUG);
      fail();
    } catch (ParserException e) {
      ParseTree root = e.getParseTree();
      assertEquals(0, root.getBeginIndex());
      assertEquals(3, root.getEndIndex());
      assertEquals(1, root.getChildren().size());
      ParseTree child = root.getChildren().get(0);
      assertEquals("token", child.getName());
      assertEquals(0, child.getBeginIndex());
      assertEquals(2, child.getEndIndex());
      assertEquals("if", child.getValue().toString());
    }
  }

  @Test
  public void errorInOuterScanner() {
    Parser<?> parser = Scanners.nestedScanner(
        Scanners.string("ab").label("outer1").next(Scanners.isChar('c').label("outer2"))
            .label("outer"),
        Scanners.ANY_CHAR.label("inner").skipMany());
    try {
      parser.parse("abd", Parser.Mode.DEBUG);
      fail();
    } catch (ParserException e) {
      assertParseTree(rootNode("ab", node("outer", null, "ab", node("outer1", null, "ab"))),
          e.getParseTree());
    }
  }

  @Test
  public void errorInInnerScanner() {
    Parser<?> parser = Scanners.nestedScanner(
        Scanners.string("ab").source().label("outer1")
            .next(Scanners.isChar('c').source().label("outer2"))
            .source()
            .label("outer"),
        Scanners.string("a").source().label("inner1")
            .next(Scanners.string("bd").source().label("inner2"))
            .label("inner")
            .<Void>retn(null));
    try {
      parser.parse("abc", Parser.Mode.DEBUG);
      fail();
    } catch (ParserException e) {
      ParseTree tree = e.getParseTree();
      assertEquals(0, tree.getBeginIndex());
      assertEquals(1, tree.getEndIndex());
      assertEquals(2, tree.getChildren().size());
      assertParseTree(
          stringNode("outer", "abc",
                  stringNode("outer1", "ab"),
                  stringNode("outer2", "c")),
          tree.getChildren().get(0));
      assertParseTree(
          node("inner", null, "a", stringNode("inner1", "a")),
          tree.getChildren().get(1));
    }
  }

  @Test
  public void parseToTreeWithTreePopulatedAtTokenLevel() {
    Terminals terms = Terminals.operators("+");
    Parser<?> expr = Parsers.sequence(
        Terminals.IntegerLiteral.PARSER.label("lhs"),
        terms.token("+").retn("+").label("plus"),
        Terminals.IntegerLiteral.PARSER.label("rhs"));
    Parser<?> tokenizer = Parsers.<Object>or(
        terms.tokenizer().label("op"), Terminals.IntegerLiteral.TOKENIZER.label("num"));
    Parser<?> parser = expr.source().label("expr").from(tokenizer, Scanners.WHITESPACES);
    ParseTree tree = parser.parseTree("1 + 2");
    assertParseTree(
        rootNode("1 + 2",
            stringNode("expr", "1 + 2",
                stringNode("lhs", "1").trailing(1),
                stringNode("plus", "+").trailing(1),
                stringNode("rhs", "2"))),
            tree);
  }

  @Test
  public void parseToTreeWithTreePopulatedAtCharacterLevel() {
    Parser<?> expr = Parsers.sequence(
        Scanners.INTEGER.label("lhs"),
        Scanners.isChar('+').source().label("plus"),
        Scanners.INTEGER.label("rhs"));
    ParseTree tree = expr.source().label("expr").parseTree("1+2");
    assertParseTree(
        rootNode("1+2",
            stringNode("expr", "1+2",
                stringNode("lhs", "1"),
                stringNode("plus", "+"),
                stringNode("rhs", "2"))),
            tree);
  }

  @Test
  public void parseToTreeWithTreePopulatedAtCharacterLevelWithNestedScanner() {
    Parser<?> expr = Parsers.sequence(
        Scanners.INTEGER.label("lhs"),
        Scanners.isChar('+').source().label("plus"),
        Scanners.INTEGER.label("rhs"));
    Parser<?> parser = Scanners.nestedScanner(expr, Scanners.ANY_CHAR.skipMany())
        .source()
        .label("expr");
    assertParseTree(
        rootNode("1+2",
            stringNode("expr", "1+2",
                stringNode("lhs", "1"),
                stringNode("plus", "+"),
                stringNode("rhs", "2"))),
            parser.parseTree("1+2"));
  }

  @Test
  public void parseToTreeWithEmptyTree() {
    Parser<?> expr = Parsers.sequence(
        Scanners.INTEGER,
        Scanners.isChar('+').source(),
        Scanners.INTEGER);
    ParseTree tree = expr.source().parseTree("1+2");
    assertParseTree(rootNode("1+2"), tree);
  }

  @Test
  public void parseToTreeWithCharacterLevelTreeDiscarded() {
    Terminals terms = Terminals.operators("+");
    Parser<?> expr = Parsers.sequence(
        Terminals.IntegerLiteral.PARSER,
        terms.token("+").retn("+"),
        Terminals.IntegerLiteral.PARSER);
    Parser<?> tokenizer = Parsers.<Object>or(
        terms.tokenizer().label("op"), Terminals.IntegerLiteral.TOKENIZER.label("num"));
    Parser<?> parser = expr.source().from(tokenizer, Scanners.WHITESPACES);
    ParseTree tree = parser.parseTree("1 + 2");
    assertParseTree(rootNode("1 + 2"), tree);
  }

  private static void assertParseTree(MatchNode expected, ParseTree actual) {
    assertParseTree(0, expected, actual);
  }

  private static void assertParseTree(int offset, MatchNode expected, ParseTree actual) {
    assertNotNull(actual);
    assertEquals(actual.toString(), expected.name, actual.getName());
    assertEquals("beginIndex of " + actual.toString(), offset + expected.leading,
        actual.getBeginIndex());
    assertEquals("endIndex of " + actual.toString(),
        offset + expected.leading + expected.matched.length() + expected.trailing,
        actual.getEndIndex());
    assertEquals("value of " + actual.toString(), expected.value, actual.getValue());
    assertEquals("children.size() of " + actual.toString(), expected.children.size(),
        actual.getChildren().size());
    for (int i = 0; i < expected.children.size(); i++) {
      MatchNode expectedChild = expected.children.get(i);
      assertParseTree(offset, expectedChild, actual.getChildren().get(i));
      offset += expectedChild.matched.length() + expectedChild.leading + expectedChild.trailing;
    }
  }

  private static MatchNode stringNode(String name, String matched, MatchNode... children) {
    return node(name, matched, matched, children);
  }

  private static MatchNode rootNode(String matched, MatchNode... children) {
    return node("root", null, matched, children);
  }

  private static MatchNode node(String name, Object value, String matched, MatchNode... children) {
    return new MatchNode(name, value, matched, asList(children));
  }

  private static final class MatchNode {
    final String name;
    final String matched;
    final Object value;
    final List<MatchNode> children;
    private int leading = 0;
    private int trailing = 0;

    MatchNode(String name, Object value, String matched, List<MatchNode> children) {
      this.name = name;
      this.matched = matched;
      this.value = value;
      this.children = children;
    }

    MatchNode leading(int offset) {
      this.leading = offset;
      return this;
    }

    MatchNode trailing(int offset) {
      this.trailing = offset;
      return this;
    }
  }
}
