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

package org.jparsec.examples.bnf.parser;

import java.util.List;

import org.jparsec.Parser;
import org.jparsec.Parsers;
import org.jparsec.Terminals;
import org.jparsec.examples.bnf.ast.AltRule;
import org.jparsec.examples.bnf.ast.LiteralRule;
import org.jparsec.examples.bnf.ast.Rule;
import org.jparsec.examples.bnf.ast.RuleDef;
import org.jparsec.examples.bnf.ast.RuleReference;
import org.jparsec.examples.bnf.ast.SequentialRule;

/**
 * Parser for bnf rules.
 * 
 * @author benyu
 */
public final class RuleParser {
  
  static final Parser<Rule> LITERAL = Terminals.StringLiteral.PARSER.map(LiteralRule::new);
  
  static final Parser<Rule> IDENT = Terminals.Identifier.PARSER.notFollowedBy(TerminalParser.term("::="))
      .map(RuleReference::new);
  
  static Parser<RuleDef> RULE_DEF = Parsers.sequence(
      Terminals.Identifier.PARSER, TerminalParser.term("::="), rule(), (name, __, r) -> new RuleDef(name, r));
  
  public static Parser<List<RuleDef>> RULE_DEFS = RULE_DEF.many();
  
  static Parser<Rule> rule() {
    Parser.Reference<Rule> ref = Parser.newReference();
    Parser<Rule> atom = Parsers.or(LITERAL, IDENT, unit(ref.lazy()));
    Parser<Rule> parser = alternative(sequential(atom));
    ref.set(parser);
    return parser;
  }

  static Parser<Rule> unit(Parser<Rule> rule) {
    return Parsers.or(
        rule.between(TerminalParser.term("("), TerminalParser.term(")")),
        rule.between(TerminalParser.INDENTATION.indent(), TerminalParser.INDENTATION.outdent()));
  }
  
  static Parser<Rule> sequential(Parser<Rule> rule) {
    return rule.many1().map(list -> list.size() == 1 ? list.get(0) : new SequentialRule(list));
  }

  static Parser<Rule> alternative(Parser<Rule> rule) {
    return rule.sepBy1(TerminalParser.term("|")).map(list -> list.size() == 1 ? list.get(0) : new AltRule(list));
  }
}
