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
 * Models a join.
 * 
 * @author Ben Yu
 */
public final class JoinRelation extends ValueObject implements Relation {
  public final Relation left;
  public final Relation right;
  public final JoinType joinType;
  public final Expression condition;
  
  public JoinRelation(Relation left, JoinType joinType, Relation right, Expression condition) {
    this.left = left;
    this.right = right;
    this.joinType = joinType;
    this.condition = condition;
  }
}
