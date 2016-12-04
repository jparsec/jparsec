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
import org.jparsec.examples.java.ast.type.TypeLiteral;

/**
 * Represents a parameter definition.
 * 
 * @author Ben Yu
 */
public final class ParameterDef extends ValueObject {
  public final List<Modifier> modifiers;
  public final TypeLiteral type;
  public final boolean vararg;
  public final String name;
  
  public ParameterDef(List<Modifier> modifiers, TypeLiteral type, boolean vararg, String name) {
    this.modifiers = modifiers;
    this.type = type;
    this.vararg = vararg;
    this.name = name;
  }

  @Override public String toString() {
    StringBuilder builder = new StringBuilder();
    for (Modifier modifier : modifiers) {
      builder.append(modifier).append(' ');
    }
    builder.append(type);
    if (vararg) builder.append("...");
    builder.append(' ').append(name);
    return builder.toString();
  }
}
