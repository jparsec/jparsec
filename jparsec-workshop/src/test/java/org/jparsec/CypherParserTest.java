package org.jparsec;

import static org.assertj.core.api.Assertions.assertThat;

import org.codehaus.jparsec.error.ParserException;

import org.junit.Test;


public class CypherParserTest {

  private final CypherParser parser = new CypherParser();

  @Test
  public void parsesIdentifier() throws Exception {
    assertThat(parser.parse("joe")).isEqualTo(new Identifier("joe"));
    assertThat(parser.parse("joe67_")).isEqualTo(new Identifier("joe67_"));
  }

  @Test(expected = ParserException.class)
  public void rejectsIdentifierStartingWithANumber() throws Exception {
    parser.parse("67joe");
  }


  @Test
  public void parsesFunctionCall() throws Exception {
    assertThat(parser.parse("foo()")).isEqualTo(new Function(new Identifier("foo")));
    assertThat(parser.parse("foo(bar)")).isEqualTo(new Function(new Identifier("foo"), new Identifier("bar")));
    assertThat(parser.parse("foo(bar,baz,qix)")).isEqualTo(new Function(new Identifier("foo"), new Identifier("bar"),
        new Identifier("baz"), new Identifier("qix")));
    assertThat(parser.parse("foo(bar,baz(qix))")).isEqualTo(new Function(new Identifier("foo"), new Identifier("bar"),
        new Function(new Identifier("baz"), new Identifier("qix"))));
  }

}
