package org.codehaus.jparsec.lambda;

import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

public class LambdaParserTest {

  private final LambdaParser parser = new LambdaParser();

  @Test
  public void canParseSingleVariable() throws Exception {
    assertThat(parser.parse("x")).isEqualTo(new Var("x"));
    assertThat(parser.parse("y")).isEqualTo(new Var("y"));
    assertThat(parser.parse("foo")).isEqualTo(new Var("foo"));
  }

  @Test
  public void canParseApplication() throws Exception {
    assertThat(parser.parse("(foo bar)")).isEqualTo(new App(new Var("foo"),new Var("bar")));
  }
}
