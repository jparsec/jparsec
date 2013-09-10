package org.codehaus.jparsec.lambda;

import org.codehaus.jparsec.Parser;
import org.codehaus.jparsec.Scanners;
import org.codehaus.jparsec.misc.Mapper;
import org.codehaus.jparsec.pattern.CharPredicates;

import static org.codehaus.jparsec.misc.Mapper._;

public class LambdaParser {

  private static final Parser<String> identifier = Scanners.isChar(CharPredicates.IS_ALPHA).many1().source();
  
  private static final Parser<Expr> var = Mapper.curry(Var.class).sequence(identifier).cast();

  private static final Parser<?> L_PAREN = _(Scanners.isChar('('));
  private static final Parser<?> R_PAREN = _(Scanners.isChar(')'));
  private static final Parser<?> WS = _(Scanners.WHITESPACES);
  
  private static final Parser<App> app = Mapper.curry(App.class)
      .sequence(var, WS, var)
      .between(L_PAREN, R_PAREN);

  private static final Parser<Expr> lambda = var.or(app);
  
  public Expr parse(String input) {
    return lambda.parse(input);
  }
  
}
