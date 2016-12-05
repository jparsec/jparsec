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

import java.util.Collections;
import java.util.List;

import org.jparsec.examples.common.Strings;
import org.jparsec.examples.common.ValueObject;
import org.jparsec.examples.java.ast.type.TypeLiteral;

/**
 * Represents expressions like {@code obj.f(...)}.
 * 
 * @author Ben Yu
 */
public final class MethodCallExpression extends ValueObject implements Expression {
  public final Expression target;
  public final List<TypeLiteral> typeParameters;
  public final String method;
  public final List<Expression> arguments;
  
  public MethodCallExpression(
      Expression target, List<TypeLiteral> typeParameters,
      String method, List<Expression> arguments) {
    this.target = target;
    this.typeParameters = Collections.unmodifiableList(typeParameters);
    this.method = method;
    this.arguments = Collections.unmodifiableList(arguments);
  }
  
  @Override public String toString() {
    return (target == null ? "" : target + ".")
        + (typeParameters.isEmpty() ? "" : "<" + Strings.join(", ", typeParameters) + ">")
        + method + "(" + Strings.join(", ", arguments) + ")";
  }
}
