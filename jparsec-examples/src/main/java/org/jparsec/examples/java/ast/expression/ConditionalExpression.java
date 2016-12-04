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

import org.jparsec.examples.common.ValueObject;

/**
 * Represents "?:" expression.
 * 
 * @author Ben Yu
 */
public final class ConditionalExpression extends ValueObject implements Expression {
  public final Expression condition;
  public final Expression consequence;
  public final Expression alternative;
  
  public ConditionalExpression(Expression condition, Expression consequence, Expression alternative) {
    this.condition = condition;
    this.consequence = consequence;
    this.alternative = alternative;
  }
  
  @Override public String toString() {
    return "(" + condition + " ? " + consequence + " : " + alternative + ")";
  }
}
