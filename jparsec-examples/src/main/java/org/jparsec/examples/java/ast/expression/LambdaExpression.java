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

import org.jparsec.examples.common.Strings;
import org.jparsec.examples.common.ValueObject;
import org.jparsec.examples.java.ast.statement.Statement;
import org.jparsec.examples.java.ast.type.TypeLiteral;

import java.util.List;
import java.util.Optional;

/**
 * Represents lambda expressions.
 * 
 * @author Ben Yu
 */
public class LambdaExpression extends ValueObject implements Expression {

  public final List<Parameter> parameters;
  public final Statement body;

  public LambdaExpression(List<Parameter> parameters, Statement body) {
    this.parameters = parameters;
    this.body = body;
  }

  public static final class Parameter extends ValueObject {
    public final Optional<TypeLiteral> type;
    public final String name;
 
    public Parameter(TypeLiteral type, String name) {
      this.type = Optional.of(type);
      this.name = name;
    }
 
    public Parameter(String name) {
      this.type = Optional.empty();
      this.name = name;
    }

    @Override public String toString() {
      return type.map(t -> t + " ").orElse("") + name;
    }
  }

  @Override public String toString() {
    return "(" + Strings.join(", ", parameters) + ") -> " + body;
  }
}
