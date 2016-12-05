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

import java.util.Collections;
import java.util.List;

import org.jparsec.examples.common.Strings;
import org.jparsec.examples.common.ValueObject;
import org.jparsec.examples.java.ast.expression.Expression;

/**
 * Represents a "this(params)" statement.
 * 
 * @author benyu
 */
public final class ThisCallStatement extends ValueObject implements Statement {
  public final List<Expression> args;

  public ThisCallStatement(List<Expression> args) {
    this.args = Collections.unmodifiableList(args);
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder("this(");
    Strings.join(builder, ", ", args);
    builder.append(");");
    return builder.toString();
  }
}
