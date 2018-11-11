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
 * An expression of binary operator.
 * 
 * @author Ben Yu
 */
public final class BinaryExpression extends ValueObject implements Expression {
  public final Expression left;
  public final Expression right;
  public final Op operator;
  
  public BinaryExpression(Expression left, Op op, Expression right) {
    this.left = left;
    this.operator = op;
    this.right = right;
  }
}
