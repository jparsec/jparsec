package org.codehaus.jparsec.lambda;

import org.codehaus.jparsec.Parser;
import org.codehaus.jparsec.Scanners;
import org.codehaus.jparsec.Terminals;
import org.codehaus.jparsec.misc.Mapper;

import static org.codehaus.jparsec.Parsers.or;
import static org.codehaus.jparsec.misc.Mapper._;

public class LambdaParser {

  // scanners
  private static final Parser<String> identifier = Terminals.Identifier.PARSER;

  private static final Terminals TERMS = Terminals.caseSensitive(new String[]{"(", ")"}, new String[0]);

  private static final Parser<?> L_PAREN = token("(");
  private static final Parser<?> R_PAREN = token(")");

  // parsers
  private static final Parser.Reference<Expr> lambdaRef = Parser.newReference();

  private static final Parser<Var> var = Mapper.curry(Var.class).sequence(identifier);

  private static final Parser<App> app = Mapper.curry(App.class)
      .sequence(lambdaRef.lazy(), lambdaRef.lazy())
      .between(L_PAREN, R_PAREN);

  private static final Parser<Expr> lambda = or(var,app);

  private static Parser<?> token(String name) {
    return _(TERMS.token(name));
  }

  static {
    lambdaRef.set(lambda);
  }

  public Expr parse(String input) {
    return lambda.from(TERMS.tokenizer(),Scanners.WHITESPACES.optional()).parse(input);
  }

}
