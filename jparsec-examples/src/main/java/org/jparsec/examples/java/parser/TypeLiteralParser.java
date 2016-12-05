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
package org.jparsec.examples.java.parser;

import static org.jparsec.Parsers.between;
import static org.jparsec.examples.java.parser.TerminalParser.phrase;
import static org.jparsec.examples.java.parser.TerminalParser.term;

import java.util.Collections;
import java.util.List;
import java.util.function.UnaryOperator;

import org.jparsec.Parser;
import org.jparsec.Parsers;
import org.jparsec.Terminals;
import org.jparsec.examples.java.ast.type.ArrayTypeLiteral;
import org.jparsec.examples.java.ast.type.LowerBoundWildcard;
import org.jparsec.examples.java.ast.type.SimpleTypeLiteral;
import org.jparsec.examples.java.ast.type.TypeLiteral;
import org.jparsec.examples.java.ast.type.UpperBoundWildcard;

/**
 * Parses any type literal.
 * 
 * @author Ben Yu
 */
public final class TypeLiteralParser {

  static final List<TypeLiteral> EMPTY_TYPE_ARGUMENT_LIST = Collections.<TypeLiteral>emptyList();

  static final Parser<UnaryOperator<TypeLiteral>> ARRAY_OF =
      phrase("[ ]").retn(ArrayTypeLiteral::new);
  
  static final Parser<TypeLiteral> ELEMENT_TYPE_LITERAL = TypeLiteralParser.elementTypeLiteral();
  
  // at least one "[]" followed by any number of "[]".
  static final Parser<ArrayTypeLiteral> ARRAY_TYPE_LITERAL =
      ELEMENT_TYPE_LITERAL.followedBy(phrase("[ ]")).postfix(ARRAY_OF).map(ArrayTypeLiteral::new);
  
  // an element type optionally followed by some "[]".
  static final Parser<TypeLiteral> TYPE_LITERAL = ELEMENT_TYPE_LITERAL.postfix(ARRAY_OF);

  static Parser<TypeLiteral> elementTypeLiteral() {
    Parser.Reference<TypeLiteral> ref = Parser.newReference();
    Parser<TypeLiteral> lazy = ref.lazy();
    Parser<TypeLiteral> arg = wildcard(lazy).or(lazy);
    Parser<String> nativeTypeName = TerminalParser.oneOf(
        "byte", "short", "int", "long", "boolean", "char", "float", "double", "void")
        .map(Object::toString);
    Parser<String> typeName = nativeTypeName.or(Terminals.Identifier.PARSER);
    Parser<TypeLiteral> parser = Parsers.sequence(
        typeName.sepBy1(term(".")), TypeLiteralParser.optionalTypeArgs(arg),
        SimpleTypeLiteral::new);
    ref.set(parser.postfix(ARRAY_OF));
    return parser;
  }

  static Parser<List<TypeLiteral>> optionalTypeArgs(Parser<TypeLiteral> parser) {
    return between(term("<"), parser.sepBy1(term(",")), term(">"))
        .optional(TypeLiteralParser.EMPTY_TYPE_ARGUMENT_LIST);
  }
  
  static Parser<TypeLiteral> wildcard(Parser<TypeLiteral> type) {
    return Parsers.or(
        phrase("? extends").next(type).map(UpperBoundWildcard::new),
        phrase("? super").next(type).map(LowerBoundWildcard::new),
        term("?").retn(new UpperBoundWildcard(null)));
  }
}
