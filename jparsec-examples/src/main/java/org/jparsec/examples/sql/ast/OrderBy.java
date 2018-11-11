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
 * The "order by" clause.
 * 
 * @author Ben Yu
 */
public final class OrderBy extends ValueObject {
  
  public static final class Item extends ValueObject {
    public final Expression by;
    public final boolean ascending;
    
    public Item(Expression by, boolean ascending) {
      this.by = by;
      this.ascending = ascending;
    }
  }
  
  public final List<Item> items;
  
  public OrderBy(List<Item> items) {
    this.items = Collections.unmodifiableList(items);
  }
}
