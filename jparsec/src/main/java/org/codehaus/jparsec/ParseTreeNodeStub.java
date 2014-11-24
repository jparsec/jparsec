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

import org.codehaus.jparsec.util.ImmutableList;

/**
 * This stub is used when some characters were matched successfully, but no parser decorated with
 * {@code node()} method was found, then an instance of this class is created to fill the place where a node
 * is required.
 *
 * @author Winter Young
 * @since 3.0
 */
class ParseTreeNodeStub implements ParseTreeNode {
  private Integer matchedStart;
  private Integer matchedEnd;
  private String matchedString;

  @Override
  public String getParseTreeNodeName() {
    return "STUB";
  }

  @Override
  public ImmutableList<ParseTreeNode> getChildren() {
    return ImmutableList.empty();
  }

  @Override
  public ImmutableList<ParseTreeNode> getReverseChildren() {
    return ImmutableList.empty();
  }

  @Override
  public boolean hasChildren() {
    return false;
  }

  @Override
  public void addChild(ParseTreeNode child) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void removeLastChild() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void resetChildren(ImmutableList<ParseTreeNode> children) {
    if (!children.isEmpty()) {
      throw new IllegalArgumentException();
    }
  }

  @Override
  public Integer getMatchedStart() {
    return matchedStart;
  }

  @Override
  public void setMatchedStart(Integer matchedStart) {
    this.matchedStart = matchedStart;
  }

  @Override
  public Integer getMatchedEnd() {
    return matchedEnd;
  }

  @Override
  public void setMatchedEnd(Integer matchedEnd) {
    this.matchedEnd = matchedEnd;
  }

  @Override
  public String getMatchedString() {
    return matchedString;
  }

  @Override
  public void setMatchedString(String matchedString) {
    this.matchedString = matchedString;
  }

  @Override
  public String toString() {
    return "ParseTreeNodeStub{" +
        "matchedStart=" + matchedStart +
        ", matchedEnd=" + matchedEnd +
        ", matchedString='" + matchedString + '\'' +
        '}';
  }
}
