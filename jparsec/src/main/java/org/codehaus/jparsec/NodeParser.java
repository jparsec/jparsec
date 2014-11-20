/*****************************************************************************
 * Copyright (C) Codehaus.org                                                *
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
package org.codehaus.jparsec;

import java.util.List;

/**
 * A parser that implements {@link org.codehaus.jparsec.ParseTreeNode}.
 *
 * @author Winter Young
 * @since 3.0
 */
class NodeParser<T> extends Parser<T> implements ParseTreeNode {
  private Parser<T> parser;
  private String name;

  public NodeParser(Parser<T> parser, String name) {
    this.parser = parser;
    this.name = name;
  }

  @Override
  boolean apply(ParseContext ctxt) {
    return false;
  }

  @Override
  public List<ParseTreeNode> getChildren() {
    return null;
  }

  @Override
  public void addChildren(List<ParseTreeNode> children) {

  }

  @Override
  public Integer getMatchedStart() {
    return null;
  }

  @Override
  public void setMatchedStart(Integer matchedStart) {

  }

  @Override
  public Integer getMatchedEnd() {
    return null;
  }

  @Override
  public void setMatchedEnd(Integer matchedEnd) {

  }

  @Override
  public String getMatchedString() {
    return null;
  }

  @Override
  public void setMatchedString(String matchedString) {

  }
}
