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
import org.jparsec.examples.java.ast.statement.Modifier;
import org.jparsec.examples.java.ast.type.TypeLiteral;

/**
 * Represents an enum definition.
 * 
 * @author Ben Yu
 */
public final class EnumDef extends ValueObject implements Declaration {
  
  /** Represents an enum value. */
  public static final class Value {
    public final String name;
    public final List<Expression> arguments;
    public final List<Member> body;
    
    public Value(String name, List<Expression> arguments, List<Member> body) {
      this.name = name;
      this.arguments = arguments;
      this.body = body;
    }
    
    @Override public String toString() {
      return name + (arguments == null ? "" : "(" + Strings.join(", ", arguments) + ")")
        + (body == null ? "" : " {" + Strings.join(" ", body) +"}") ;
    }
  }
  
  public final List<Modifier> modifiers;
  public final String name;
  public final List<TypeLiteral> interfaces;
  public final List<Value> values;
  public final List<Member> members;
  
  public EnumDef(List<Modifier> modifiers, String name, List<TypeLiteral> interfaces,
      List<Value> values, List<Member> members) {
    this.modifiers = modifiers;
    this.name = name;
    this.interfaces = interfaces;
    this.values = values;
    this.members = members;
  }
  
  @Override public String toString() {
    StringBuilder builder = new StringBuilder();
    for (Modifier modifier : modifiers) {
      builder.append(modifier).append(' ');
    }
    builder.append("enum ").append(name);
    if (interfaces != null) {
      builder.append(" implements ");
      Strings.join(builder, ", ", interfaces);
    }
    builder.append(" {");
    Strings.join(builder, ", ", values);
    if (members != null) {
      builder.append("; ");
      Strings.join(builder, " ", members);
    }
    builder.append("}");
    return builder.toString();
  }
}
