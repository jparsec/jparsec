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

import org.jparsec.examples.common.ValueObject;

/**
 * Represents "obj.field", "SomeType.staticField", "SomeType.SomeNestedType"
 * or "org.codehaus.jparsec" kind of qualified expressions. (The latter isn't really an expression,
 * but at parse time we have no way to tell).
 * 
 * @author Ben Yu
 */
public final class QualifiedExpression extends ValueObject implements Expression {
  public final Expression qualifier;
  public final String name;
  
  public QualifiedExpression(Expression qualifier, String name) {
    this.qualifier = qualifier;
    this.name = name;
  }
  
  @Override public String toString() {
    return "(" + qualifier + "." + name + ")";
  }
}
