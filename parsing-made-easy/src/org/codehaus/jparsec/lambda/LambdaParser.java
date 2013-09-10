package org.codehaus.jparsec.lambda;

import org.codehaus.jparsec.Parser;
import org.codehaus.jparsec.Scanners;
import org.codehaus.jparsec.misc.Mapper;

import static org.codehaus.jparsec.misc.Mapper._;

public class LambdaParser {

  // scanners
  private static final Parser<String> identifier = Scanners.IDENTIFIER;

  private static final Parser<?> L_PAREN = token("(");
  private static final Parser<?> R_PAREN = token(")");
  private static final Parser<?> WS = _(Scanners.WHITESPACES);
  private static final Parser<?> WS_OPT = _(Scanners.WHITESPACES.optional());

  // parsers
  private static final Parser.Reference<Expr> lambdaRef = Parser.newReference();

  private static final Parser<Expr> var = Mapper.curry(Var.class).sequence(identifier).cast();

  private static final Parser<App> app = Mapper.curry(App.class)
      .sequence(WS_OPT, lambdaRef.lazy(), WS_OPT, lambdaRef.lazy(), WS_OPT)
      .between(L_PAREN, R_PAREN).between(WS_OPT, WS_OPT);

  private static final Parser<Expr> lambda = var.or(app);

  private static Parser<?> token(String name) {
    return _(Scanners.string(name));
  }

  static {
    lambdaRef.set(lambda);
  }

  public Expr parse(String input) {
    return lambda.parse(input);
  }

}
