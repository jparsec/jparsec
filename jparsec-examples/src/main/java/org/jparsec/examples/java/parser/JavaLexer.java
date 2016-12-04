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

import org.jparsec.Parser;
import org.jparsec.Parsers;
import org.jparsec.Scanners;
import org.jparsec.examples.java.ast.expression.DecimalPointNumberLiteral;
import org.jparsec.examples.java.ast.expression.IntegerLiteral;
import org.jparsec.examples.java.ast.expression.NumberType;
import org.jparsec.examples.java.ast.expression.ScientificNumberLiteral;
import org.jparsec.pattern.Patterns;

/**
 * Lexer specific for the Java language rules.
 * 
 * @author Ben Yu
 */
public final class JavaLexer {
  
  static final Parser<String> IDENTIFIER = Patterns.isChar(Character::isJavaIdentifierStart)
      .next(Patterns.isChar(Character::isJavaIdentifierPart).many())
      .toScanner("identifier")
      .source();
  
  static final Parser<Void> DECIMAL_POINT_SCANNER =
      Patterns.INTEGER.optional().next(Patterns.FRACTION).toScanner("decimal point number");
  
  static final Parser<DecimalPointNumberLiteral> DECIMAL_POINT_NUMBER = Parsers.sequence(
      DECIMAL_POINT_SCANNER.source(), numberType(NumberType.DOUBLE),
      DecimalPointNumberLiteral::new);
  
  static final Parser<IntegerLiteral> HEX_INTEGER = Parsers.sequence(
      Scanners.HEX_INTEGER.source(), numberType(NumberType.INT),
      (text, type) -> new IntegerLiteral(IntegerLiteral.Radix.HEX, text.substring(2), type));
  
  static final Parser<IntegerLiteral> OCT_INTEGER = Parsers.sequence(
      JavaScanners.OCT_INTEGER.source(), numberType(NumberType.INT),
      (text, type) -> new IntegerLiteral(IntegerLiteral.Radix.OCT, text.length() == 1 ? text : text.substring(1), type));
  
  static final Parser<IntegerLiteral> DEC_INTEGER = Parsers.sequence(
      JavaScanners.DEC_INTEGER.source(), numberType(NumberType.INT),
      (n, t) -> new IntegerLiteral(IntegerLiteral.Radix.DEC, n, t));
  
  static final Parser<IntegerLiteral> INTEGER = Parsers.or(HEX_INTEGER, OCT_INTEGER, DEC_INTEGER);
  
  static final Parser<ScientificNumberLiteral> SCIENTIFIC_NUMBER_LITERAL = Parsers.sequence(
      Scanners.SCIENTIFIC_NOTATION, numberType(NumberType.DOUBLE),
          ScientificNumberLiteral::new);
  
  static Parser<NumberType> numberType(NumberType defaultType) {
    return Parsers.or(
        Scanners.among("lL").retn(NumberType.LONG),
        Scanners.among("fF").retn(NumberType.FLOAT),
        Scanners.among("dD").retn(NumberType.DOUBLE),
        Parsers.constant(defaultType)
     );
  }
}
