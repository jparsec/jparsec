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

import java.util.Collections;
import java.util.List;

import org.jparsec.examples.common.ValueObject;


/**
 * Models the select statement.
 * 
 * @author Ben Yu
 */
public final class Select extends ValueObject implements Relation {
  public final boolean distinct;
  public final List<Projection> projections;
  public final List<Relation> from;
  public final Expression where;
  public final GroupBy groupBy;
  public final OrderBy orderBy;
  
  public Select(
      boolean distinct, List<Projection> projections, List<Relation> from, Expression where,
      GroupBy groupBy, OrderBy orderBy) {
    this.distinct = distinct;
    this.projections = Collections.unmodifiableList(projections);
    this.from = Collections.unmodifiableList(from);
    this.where = where;
    this.groupBy = groupBy;
    this.orderBy = orderBy;
  }
}
