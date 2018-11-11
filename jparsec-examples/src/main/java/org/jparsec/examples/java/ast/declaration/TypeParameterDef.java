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

import org.jparsec.examples.common.ValueObject;
import org.jparsec.examples.java.ast.type.TypeLiteral;

/**
 * Represents a type parameter in a type or method definition.
 * 
 * @author Ben Yu
 */
public final class TypeParameterDef extends ValueObject {
  
  public final String name;
  public final TypeLiteral bound;
  
  public TypeParameterDef(String name, TypeLiteral bound) {
    this.name = name;
    this.bound = bound;
  }
  
  @Override public String toString() {
    return name + (bound == null ? "" : " extends " + bound);
  }
}
