package org.codehaus.jparsec.lambda;

import org.codehaus.jparsec.Parser;
import org.codehaus.jparsec.Scanners;
import org.codehaus.jparsec.functors.Map;
import org.codehaus.jparsec.pattern.CharPredicates;

public class LambdaParser {
  
  private static final Parser<Var> var = Scanners.isChar(CharPredicates.IS_ALPHA).source().map(new Map<String, Var>() {
    @Override
    public Var map(String s) {
      return new Var(s);
    }
  });
  
  public Var parse(String input) {
    return var.parse(input);
  }
  
}
