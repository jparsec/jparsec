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
package org.jparsec.examples.java.ast.expression;

/**
 * Defines operators used in Java.
 * 
 * @author Ben Yu
 */
public enum Operator {
  POST_INC("++"), POST_DEC("--"),
  INC("++"), DEC("--"), POSITIVE("+"), NEGATIVE("-"), NOT("!"), BITWISE_NOT("~"),
  MUL("*"), DIV("/"), MOD("%"),
  PLUS("+"), MINUS("-"),
  LSHIFT("<<"), RSHIFT(">>"), UNSIGNED_RSHIFT(">>>"),
  LT("<"), GT(">"), LE("<="), GE(">="), // instanceof has same precedence
  EQ("=="), NE("!="),
  BITWISE_AND("&"),
  BITWISE_XOR("|"),
  BITWISE_OR("^"),
  AND("&&"),
  OR("||"),
  // ?: falls here
  
  // The following have same precedence
  ASSIGNMENT("="), APLUS("+="), AMINUS("-="), AMUL("*="), ADIV("/="), AMOD("%="),
  AAND("&="), AOR("|="), AXOR("^="), ALSHIFT("<<="), ARSHIFT(">>="), UNSIGNED_ARSHIFT(">>>=");
  
  private final String name;
  
  private Operator(final String name) {
    this.name = name;
  }
  
  @Override public String toString() {
    return name;
  }
}
