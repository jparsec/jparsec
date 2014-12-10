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

import org.codehaus.jparsec.Parser;
import org.codehaus.jparsec.Parsers;
import org.codehaus.jparsec.Scanners;
import org.codehaus.jparsec.examples.java.ast.expression.DecimalPointNumberLiteral;
import org.codehaus.jparsec.examples.java.ast.expression.IntegerLiteral;
import org.codehaus.jparsec.examples.java.ast.expression.NumberType;
import org.codehaus.jparsec.examples.java.ast.expression.ScientificNumberLiteral;
import org.codehaus.jparsec.examples.java.ast.expression.IntegerLiteral.Radix;
import org.codehaus.jparsec.misc.Mapper;
import org.codehaus.jparsec.pattern.CharPredicate;
import org.codehaus.jparsec.pattern.Patterns;

/**
 * Lexer specific for the Java language rules.
 * 
 * @author Ben Yu
 */
public final class JavaLexer {
  
  private static final CharPredicate JAVA_IDENTIFIER_START = new CharPredicate() {
    public boolean isChar(char c) {
      return Character.isJavaIdentifierStart(c);
    }
  };
  
  private static final CharPredicate JAVA_IDENTIFIER_PART = new CharPredicate() {
    public boolean isChar(char c) {
      return Character.isJavaIdentifierPart(c);
    }
  };
  
  static final Parser<String> IDENTIFIER = Patterns.isChar(JAVA_IDENTIFIER_START)
      .next(Patterns.isChar(JAVA_IDENTIFIER_PART).many())
      .toScanner("identifier")
      .source();
  
  static final Parser<Void> DECIMAL_POINT_SCANNER =
      Patterns.INTEGER.optional().next(Patterns.FRACTION).toScanner("decimal point number");
  
  static final Parser<DecimalPointNumberLiteral> DECIMAL_POINT_NUMBER = 
      Mapper.curry(DecimalPointNumberLiteral.class).sequence(
          DECIMAL_POINT_SCANNER.source(), numberType(NumberType.DOUBLE));
  
  static final Parser<IntegerLiteral> HEX_INTEGER = new Mapper<IntegerLiteral>() {
    @SuppressWarnings("unused")
    IntegerLiteral map(String text, NumberType type) {
      return new IntegerLiteral(Radix.HEX, text.substring(2), type);
    }
  }.sequence(Scanners.HEX_INTEGER.source(), numberType(NumberType.INT));
  
  static final Parser<IntegerLiteral> OCT_INTEGER = new Mapper<IntegerLiteral>() {
    @SuppressWarnings("unused")
    IntegerLiteral map(String text, NumberType type) {
      return new IntegerLiteral(Radix.OCT, text.length() == 1 ? text : text.substring(1), type);
    }
  }.sequence(JavaScanners.OCT_INTEGER.source(), numberType(NumberType.INT));
  
  static final Parser<IntegerLiteral> DEC_INTEGER =
      Mapper.curry(IntegerLiteral.class, Radix.DEC)
      .sequence(JavaScanners.DEC_INTEGER.source(), numberType(NumberType.INT));
  
  static final Parser<IntegerLiteral> INTEGER = Parsers.or(HEX_INTEGER, OCT_INTEGER, DEC_INTEGER);
  
  static final Parser<ScientificNumberLiteral> SCIENTIFIC_NUMBER_LITERAL =
      Mapper.curry(ScientificNumberLiteral.class)
        .sequence(Scanners.SCIENTIFIC_NOTATION, numberType(NumberType.DOUBLE));
  
  static Parser<NumberType> numberType(NumberType defaultType) {
    return Parsers.or(
        Scanners.among("lL").retn(NumberType.LONG),
        Scanners.among("fF").retn(NumberType.FLOAT),
        Scanners.among("dD").retn(NumberType.DOUBLE),
        Parsers.constant(defaultType)
     );
  }
}
