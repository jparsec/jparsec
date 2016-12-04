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
 * An expression like "expr in (select ...)".
 * 
 * @author Ben Yu
 */
public final class BinaryRelationalExpression extends ValueObject implements Expression {
  public final Expression expression;
  public final Op operator;
  public final Relation relation;
  
  public BinaryRelationalExpression(Expression expression, Op operator, Relation relation) {
    this.expression = expression;
    this.operator = operator;
    this.relation = relation;
  }
}
