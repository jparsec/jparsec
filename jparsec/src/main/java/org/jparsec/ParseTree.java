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

import org.jparsec.internal.util.Strings;

import java.util.Collections;
import java.util.List;

/**
 * Represents the syntactical structure of the input being parsed.
 *
 * @since 2.3
 */
public final class ParseTree {
  private final String name;
  private final int beginIndex;
  private final int endIndex;
  private final Object value;
  private final List<ParseTree> children;

  ParseTree(
      String name,
      int beginIndex,
      int endIndex,
      Object value,
      List<ParseTree> children) {
    this.name = name;
    this.beginIndex = beginIndex;
    this.endIndex = endIndex;
    this.value = value;
    this.children = Collections.unmodifiableList(children);
  }

  /** Returns the node name, which is specified in {@link Parser#label}. */
  public String getName() {
    return name;
  }

  /** Returns the index in source where this node starts. */
  public int getBeginIndex() {
    return beginIndex;
  }

  /** Returns the index in source where this node ends. */
  public int getEndIndex() {
    return endIndex;
  }

  /** Returns the parsed value of this node, or {@code null} if it's a failed node. */
  public Object getValue() {
    return value;
  }

  /**
   * Returns the immutable list of child nodes that correspond to {@link Parser#label labeled}
   * parsers syntactically enclosed inside parent parser.
   */
  public List<ParseTree> getChildren() {
    return children;
  }

  @Override public String toString() {
    StringBuilder builder = new StringBuilder(name).append(": ");
    if (children.isEmpty()){
      builder.append(value);
    } else {
      builder.append("{\n");
      Strings.join(builder, ",\n", children);
      builder.append("\n}");
    }
    return builder.toString();
  }
}
