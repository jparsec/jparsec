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
package org.jparsec.examples.sql.ast;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.jparsec.examples.common.ValueObject;

/**
 * A function call.
 * 
 * @author Ben Yu
 */
public final class FunctionExpression extends ValueObject implements Expression {
  public final QualifiedName function;
  public final List<Expression> args;
  
  public FunctionExpression(QualifiedName function, List<Expression> args) {
    this.function = function;
    this.args = Collections.unmodifiableList(args);
  }
  
  public static FunctionExpression of(QualifiedName function, Expression... args) {
    return new FunctionExpression(function, Arrays.asList(args));
  }
}
