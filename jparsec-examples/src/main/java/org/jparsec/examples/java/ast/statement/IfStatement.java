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
package org.jparsec.examples.java.ast.statement;

import java.util.List;

import org.jparsec.examples.common.ValueObject;
import org.jparsec.examples.java.ast.expression.Expression;
import org.jparsec.functors.Pair;

/**
 * Represents an "if" statement.
 * 
 * @author Ben Yu
 */
public final class IfStatement extends ValueObject implements Statement {
  public final Expression condition;
  public final Statement then;
  public final List<Pair<Expression, Statement>> elseifs;
  public final Statement otherwise;
  
  public IfStatement(
      Expression condition, Statement then,
      List<Pair<Expression, Statement>> elseifs, Statement otherwise) {
    this.condition = condition;
    this.then = then;
    this.elseifs = elseifs;
    this.otherwise = otherwise;
  }
  
  @Override public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("if (").append(condition).append(") ").append(then);
    for (Pair<Expression, Statement> elseif : elseifs) {
      builder.append(" else if (").append(elseif.a).append(") ").append(elseif.b);
    }
    if (otherwise != null) {
      builder.append(" else ").append(otherwise);
    }
    return builder.toString();
  }
}
