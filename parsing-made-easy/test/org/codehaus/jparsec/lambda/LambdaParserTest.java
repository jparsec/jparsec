package org.codehaus.jparsec.lambda;

import org.junit.Test;

import static org.codehaus.jparsec.lambda.App.app;
import static org.codehaus.jparsec.lambda.Var.var;
import static org.fest.assertions.Assertions.assertThat;

public class LambdaParserTest {

  private final LambdaParser parser = new LambdaParser();

  @Test
  public void canParseSingleVariable() throws Exception {
    assertThat(parser.parse("x")).isEqualTo(var("x"));
    assertThat(parser.parse("y")).isEqualTo(var("y"));
    assertThat(parser.parse("foo")).isEqualTo(var("foo"));
  }

  @Test
  public void canParseApplication() throws Exception {
    assertThat(parser.parse("(foo bar)")).isEqualTo(app(var("foo"), var("bar")));
    assertThat(parser.parse("(foo (bar baz))")).isEqualTo(app(var("foo"), app(var("bar"), var("baz"))));
    assertThat(parser.parse("((bar baz) foo)")).isEqualTo(app(app(var("bar"), var("baz")), var("foo")));
  }
}
