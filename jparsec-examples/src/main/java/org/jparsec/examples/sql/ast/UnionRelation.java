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
 * Models a union relation.
 *  
 * @author Ben Yu
 */
public final class UnionRelation extends ValueObject implements Relation {
  public final Relation left;
  public final boolean all; // true if union all
  public final Relation right;
  
  public UnionRelation(Relation left, boolean all, Relation right) {
    this.left = left;
    this.all = all;
    this.right = right;
  }
}
