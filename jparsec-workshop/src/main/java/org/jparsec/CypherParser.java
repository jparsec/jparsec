package org.jparsec;

import org.codehaus.jparsec.Parser;
import org.codehaus.jparsec.Parsers;
import org.codehaus.jparsec.Scanners;
import static org.codehaus.jparsec.Scanners.isChar;
import org.codehaus.jparsec.functors.Map;
import org.codehaus.jparsec.functors.Map2;
import org.codehaus.jparsec.pattern.CharPredicates;

import java.util.List;


/**
 */
public class CypherParser {

  /*
  lexers
   */
  private static final Parser<String> identifier = isChar(CharPredicates.IS_ALPHA_).followedBy(Scanners.many(
      CharPredicates.IS_ALPHA_NUMERIC_))
      .source();

  /*
  parsers
   */
  private static final Parser<Identifier> identifiers = identifier.map(new Map<String, Identifier>() {
      @Override
      public Identifier map(String s) {
        return new Identifier(s);
      }
    });

  private static final Parser.Reference<Function> functionsRef = Parser.newReference();

  private static final Parser<List<Expr>> parameter = Parsers.or(functionsRef.lazy(), identifiers).sepBy(isChar(',')).cast();

  private static final Parser<Function> functions = Parsers.sequence(identifiers, parameter.between(isChar('('), isChar(')')),
    new Map2<Identifier, List<Expr>, Function>() {

      @Override
      public Function map(Identifier identifier, List<Expr> expr) {
        return new Function(identifier, expr);
      }
    });

  public Expr parse(CharSequence input) {
    functionsRef.set(functions);

    return Parsers.or(functions, identifiers).parse(input);
  }

}
