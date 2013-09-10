package org.codehaus.jparsec.lambda;

import org.codehaus.jparsec.Parser;
import org.codehaus.jparsec.Scanners;
import org.codehaus.jparsec.misc.Mapper;
import org.codehaus.jparsec.pattern.CharPredicates;

public class LambdaParser {

  private static final Parser<String> identifier = Scanners.isChar(CharPredicates.IS_ALPHA).many1().source();
  
  private static final Parser<Var> var = Mapper.curry(Var.class).sequence(identifier);

  public Var parse(String input) {
    return var.parse(input);
  }
  
}
