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

import org.jparsec.examples.common.Strings;
import org.jparsec.examples.common.ValueObject;
import org.jparsec.examples.java.ast.expression.Expression;

/**
 * Represents the traditional for loop.
 * 
 * @author Ben Yu
 */
public final class ForStatement extends ValueObject implements Statement {
  public final Statement initializer;
  public final Expression condition;
  public final List<Expression> incrementer;
  public final Statement statement;
  
  public ForStatement(
      Statement initializer, Expression condition, List<Expression> incrementer,
      Statement statement) {
    this.initializer = initializer;
    this.condition = condition;
    this.incrementer = incrementer;
    this.statement = statement;
  }
  
  @Override public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("for (");
    builder.append(initializer);
    if (condition != null) {
      builder.append(condition);
    }
    builder.append(";");
    Strings.join(builder, ", ", incrementer);
    builder.append(") ").append(statement);
    return builder.toString();
  }
}
