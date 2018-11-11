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

import java.util.Collections;
import java.util.List;

import org.jparsec.examples.common.ValueObject;
import org.jparsec.examples.java.ast.expression.Expression;
import org.jparsec.examples.java.ast.statement.Modifier;
import org.jparsec.examples.java.ast.type.TypeLiteral;

/**
 * Represents a field definition.
 * 
 * @author Ben Yu
 */
public final class FieldDef extends ValueObject implements Member {
  public final List<Modifier> modifiers;
  public final TypeLiteral type;
  public final String name;
  public final Expression value;
  
  public FieldDef(List<Modifier> modifiers, TypeLiteral type, String name, Expression value) {
    this.modifiers = Collections.unmodifiableList(modifiers);
    this.type = type;
    this.name = name;
    this.value = value;
  }
  
  @Override public String toString() {
    StringBuilder builder = new StringBuilder();
    for (Modifier modifier : modifiers) {
      builder.append(modifier).append(' ');
    }
    builder.append(type).append(' ').append(name);
    if (value != null) {
      builder.append(" = ").append(value);
    }
    builder.append(';');
    return builder.toString();
  }
}
