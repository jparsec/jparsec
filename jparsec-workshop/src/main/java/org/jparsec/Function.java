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
package org.jparsec;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.List;
import java.util.Objects;


/**
 */
public class Function implements Expr {
  private final Identifier name;
  private final List<Expr> arguments;

  public Function(Identifier name) {
    this.name = name;
    this.arguments = Collections.emptyList();
  }

  public Function(Identifier name, Expr argument, Expr... moreArguments) {
    this.name = name;
    this.arguments = Lists.asList(argument, moreArguments);
  }

  public Function(Identifier name, List<Expr> arguments) {
    this.name = name;
    this.arguments = ImmutableList.copyOf(arguments);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, arguments);
  }

  @Override
  public boolean equals(Object obj) {

    if (this == obj) {
      return true;
    }

    if ((obj == null) || (getClass() != obj.getClass())) {
      return false;
    }

    final Function other = (Function) obj;

    return Objects.equals(this.name, other.name) && Objects.equals(this.arguments, other.arguments);
  }

  @Override
  public String toString() {
    return "Function{" + "name=" + name + ", arguments=" + arguments + '}';
  }
}
