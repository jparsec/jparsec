/*****************************************************************************
 * Copyright (C) Codehaus.org                                                *
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
package org.codehaus.jparsec.examples.java.parser;

import static org.codehaus.jparsec.Parsers.between;
import static org.codehaus.jparsec.examples.java.parser.TerminalParser.phrase;
import static org.codehaus.jparsec.examples.java.parser.TerminalParser.term;

import java.util.Collections;
import java.util.List;

import org.codehaus.jparsec.Parser;
import org.codehaus.jparsec.Parsers;
import org.codehaus.jparsec.Terminals;
import org.codehaus.jparsec.examples.java.ast.type.ArrayTypeLiteral;
import org.codehaus.jparsec.examples.java.ast.type.LowerBoundWildcard;
import org.codehaus.jparsec.examples.java.ast.type.SimpleTypeLiteral;
import org.codehaus.jparsec.examples.java.ast.type.TypeLiteral;
import org.codehaus.jparsec.examples.java.ast.type.UpperBoundWildcard;
import org.codehaus.jparsec.functors.Maps;
import org.codehaus.jparsec.functors.Unary;
import org.codehaus.jparsec.misc.Mapper;

/**
 * Parses any type literal.
 * 
 * @author Ben Yu
 */
public final class TypeLiteralParser {

  static final List<TypeLiteral> EMPTY_TYPE_ARGUMENT_LIST = Collections.<TypeLiteral>emptyList();

  static final Parser<Unary<TypeLiteral>> ARRAY_OF =
      phrase("[ ]").next(curry(ArrayTypeLiteral.class).unary());
  
  static final Parser<TypeLiteral> ELEMENT_TYPE_LITERAL = TypeLiteralParser.elementTypeLiteral();
  
  // at least one "[]" followed by any number of "[]".
  static final Parser<TypeLiteral> ARRAY_TYPE_LITERAL = curry(ArrayTypeLiteral.class)
      .sequence(ELEMENT_TYPE_LITERAL, phrase("[ ]")).postfix(ARRAY_OF);
  
  // an element type optionally followed by some "[]".
  static final Parser<TypeLiteral> TYPE_LITERAL = ELEMENT_TYPE_LITERAL.postfix(ARRAY_OF);

  static Parser<TypeLiteral> elementTypeLiteral() {
    Parser.Reference<TypeLiteral> ref = Parser.newReference();
    Parser<TypeLiteral> lazy = ref.lazy();
    Parser<TypeLiteral> arg = wildcard(lazy).or(lazy);
    Parser<String> nativeTypeName = TerminalParser.oneOf(
        "byte", "short", "int", "long", "boolean", "char", "float", "double", "void")
        .map(Maps.mapToString());
    Parser<String> typeName = nativeTypeName.or(Terminals.Identifier.PARSER);
    Parser<TypeLiteral> parser = Mapper.<TypeLiteral>curry(SimpleTypeLiteral.class)
        .sequence(typeName.sepBy1(term(".")), TypeLiteralParser.optionalTypeArgs(arg));
    ref.set(parser.postfix(ARRAY_OF));
    return parser;
  }

  static Parser<List<TypeLiteral>> optionalTypeArgs(Parser<TypeLiteral> parser) {
    return between(term("<"), parser.sepBy1(term(",")), term(">"))
        .optional(TypeLiteralParser.EMPTY_TYPE_ARGUMENT_LIST);
  }
  
  static Parser<TypeLiteral> wildcard(Parser<TypeLiteral> type) {
    return Parsers.or(
        curry(UpperBoundWildcard.class).sequence(phrase("? extends"), type),
        curry(LowerBoundWildcard.class).sequence(phrase("? super"), type),
        term("?").retn(new UpperBoundWildcard(null)));
  }
  
  private static Mapper<TypeLiteral> curry(
      Class<? extends TypeLiteral> clazz, Object... curryArgs) {
    return Mapper.curry(clazz, curryArgs);
  }
}
