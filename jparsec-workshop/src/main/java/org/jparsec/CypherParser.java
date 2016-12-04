/*****************************************************************************
 * Copyright (C) jparsec.org                                                *
 * ------------------------------------------------------------------------- *
 * Licensed under the Apache License, Version 2.0 (the "License");           *
 * you may not use this file except in compliance with the License.          *
 * You may obtain a copy of the License at                                   *
 *                                                                           *
 * http://www.apache.org/licenses/LICENSE-2.0                                *
 *                                                                           *
 * Unless required by applicable law or agreed to in writing, software       *
 * distributed under the License is distributed on an "AS IS" BASIS,         *
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  *
 * See the License for the specific language governing permissions and       *
 * limitations under the License.                                            *
 *****************************************************************************/
package org.jparsec;

import org.codehaus.jparsec.Parser;
import org.codehaus.jparsec.Parsers;
import org.codehaus.jparsec.Scanners;
import static org.codehaus.jparsec.Scanners.isChar;
import org.codehaus.jparsec.functors.Map;
import org.codehaus.jparsec.functors.Map2;
import org.codehaus.jparsec.pattern.CharPredicates;

import java.util.List;

public class CypherParser {

  private static final Parser<Identifier> IDENTIFIER = isChar(CharPredicates.IS_ALPHA_)
      .followedBy(Scanners.many(CharPredicates.IS_ALPHA_NUMERIC_))
      .source().map(new Map<String, Identifier>() {
        @Override
        public Identifier map(String s) {
          return new Identifier(s);
        }
      });

  private static final Parser.Reference<Function> functionsRef = Parser.newReference();

  private static final Parser<List<Expr>> parameter =
      Parsers.or(functionsRef.lazy(), IDENTIFIER).sepBy(isChar(','));

  private static final Parser<Function> functions =
      Parsers.sequence(IDENTIFIER, parameter.between(isChar('('), isChar(')')),
    new Map2<Identifier, List<Expr>, Function>() {

      @Override
      public Function map(Identifier identifier, List<Expr> expr) {
        return new Function(identifier, expr);
      }
    });

  public Expr parse(CharSequence input) {
    functionsRef.set(functions);

    return Parsers.or(functions, IDENTIFIER).parse(input);
  }

}
