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
 * Represents the "switch case" expression.
 * 
 * @author Ben Yu
 */
public final class SwitchStatement extends ValueObject implements Statement {
  public final Expression condition;
  public final List<Pair<Expression, Statement>> cases;
  public final Statement defaultCase;
  
  public SwitchStatement(
      Expression condition, List<Pair<Expression, Statement>> cases, Statement defaultCase) {
    this.condition = condition;
    this.cases = cases;
    this.defaultCase = defaultCase;
  }
  
  @Override public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("switch (").append(condition).append(") {");
    for (Pair<Expression, Statement> c : cases) {
      builder.append(" case ").append(c.a).append(":");
      if (c.b != null) {
        builder.append(" ").append(c.b);
      }
    }
    if (defaultCase != null) {
      builder.append(" default: ").append(defaultCase);
    }
    builder.append("}");
    return builder.toString();
  }
}
