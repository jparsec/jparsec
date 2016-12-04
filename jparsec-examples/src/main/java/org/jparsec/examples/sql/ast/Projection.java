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

import org.jparsec.examples.common.ValueObject;

/**
 * Represents a single projection in a select statement, it can be either an expression, a wildcard
 * or an expression with an alias.
 * 
 * @author Ben Yu
 */
public final class Projection extends ValueObject {
  public final Expression expression;
  public final String alias;
  
  public Projection(Expression expression, String alias) {
    this.expression = expression;
    this.alias = alias;
  }
}
