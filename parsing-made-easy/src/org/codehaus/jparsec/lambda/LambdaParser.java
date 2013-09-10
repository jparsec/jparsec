package org.codehaus.jparsec.lambda;

import org.codehaus.jparsec.Parser;
import org.codehaus.jparsec.Scanners;
import org.codehaus.jparsec.misc.Mapper;
import org.codehaus.jparsec.pattern.CharPredicates;

public class LambdaParser {

  private static final Parser<String> identifier = Scanners.isChar(CharPredicates.IS_ALPHA).many1().source();
  
  private static final Parser<Expr> var = Mapper.curry(Var.class).sequence(identifier).cast();
  
  private static final Parser<App> app = Mapper.curry(App.class).sequence(var,var);

  private static final Parser<Expr> lambda = var.or(app);
  
  public Expr parse(String input) {
    return lambda.parse(input);
  }
  
}
