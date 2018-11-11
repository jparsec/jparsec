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
package org.jparsec.examples.java.ast.declaration;

import java.util.List;

import org.jparsec.examples.common.Strings;
import org.jparsec.examples.common.ValueObject;
import org.jparsec.examples.java.ast.expression.Expression;
import org.jparsec.examples.java.ast.statement.BlockStatement;
import org.jparsec.examples.java.ast.statement.Modifier;
import org.jparsec.examples.java.ast.statement.ParameterDef;
import org.jparsec.examples.java.ast.type.TypeLiteral;

/**
 * Represents a method definition.
 * 
 * @author Ben Yu
 */
public final class MethodDef extends ValueObject implements Member {
  
  public final List<Modifier> modifiers;
  public final List<TypeParameterDef> typeParameters;
  public final TypeLiteral returnType;
  public final String name;
  public final List<ParameterDef> parameters;
  public final List<TypeLiteral> exceptions;
  public final Expression defaultValue;
  public final BlockStatement body;
  
  public MethodDef(List<Modifier> modifiers, List<TypeParameterDef> typeParameters,
      TypeLiteral returnType, String name, List<ParameterDef> parameters,
      List<TypeLiteral> exceptions, Expression defaultValue, BlockStatement body) {
    this.modifiers = modifiers;
    this.typeParameters = typeParameters;
    this.returnType = returnType;
    this.name = name;
    this.parameters = parameters;
    this.exceptions = exceptions;
    this.defaultValue = defaultValue;
    this.body = body;
  }

  @Override public String toString() {
    StringBuilder builder = new StringBuilder();
    for (Modifier modifier : modifiers) {
      builder.append(modifier).append(' ');
    }
    if (typeParameters != null) {
      builder.append('<');
      Strings.join(builder, ", ", typeParameters);
      builder.append("> ");
    }
    builder.append(returnType).append(' ').append(name).append('(');
    Strings.join(builder, ", ", parameters);
    builder.append(')');
    if (exceptions != null) {
      builder.append(" throws ");
      Strings.join(builder, ", ", exceptions);
    }
    if (defaultValue != null) {
      builder.append(" default ").append(defaultValue);
    }
    if (body == null) {
      builder.append(';');
    } else {
      builder.append(' ').append(body);
    }
    return builder.toString();
  }
}
