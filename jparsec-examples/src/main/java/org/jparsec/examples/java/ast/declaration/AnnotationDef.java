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

import org.jparsec.examples.common.ValueObject;
import org.jparsec.examples.java.ast.statement.Modifier;

/**
 * Represents an annotation definition.
 * 
 * @author Ben Yu
 */
public final class AnnotationDef extends ValueObject implements Declaration {
  public final List<Modifier> modifiers;
  public final String name;
  public final DefBody body;
  
  public AnnotationDef(
      List<Modifier> modifiers, String name, DefBody body) {
    this.modifiers = modifiers;
    this.name = name;
    this.body = body;
  }
  
  @Override public String toString() {
    StringBuilder builder = new StringBuilder();
    for (Modifier modifier : modifiers) {
      builder.append(modifier).append(' ');
    }
    builder.append("@interface ").append(name).append(' ').append(body);
    return builder.toString();
  }
}
