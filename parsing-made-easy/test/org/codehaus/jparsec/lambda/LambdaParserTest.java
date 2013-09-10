package org.codehaus.jparsec.lambda;

import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

public class LambdaParserTest {

  @Test
  public void canParseASingleVariable() throws Exception {
    LambdaParser parser = new LambdaParser();
    
    assertThat(parser.parse("x")).isEqualTo(new Var("x"));
  }
}
