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
import java.util.Iterator;
import java.util.List;

import org.jparsec.examples.common.ValueObject;

/**
 * A qualified name like "a.b.c".
 * @author Ben Yu
 */
public final class QualifiedName extends ValueObject implements Iterable<String> {
  public final List<String> names;

  public QualifiedName(List<String> names) {
    this.names = Collections.unmodifiableList(names);
  }
  
  public static QualifiedName of(String... names) {
    return new QualifiedName(Arrays.asList(names));
  }

  @Override public Iterator<String> iterator() {
    return names.iterator();
  }
}
