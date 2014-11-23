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
 * Represents a parse tree node.
 *
 * @author Winter Young
 * @since 3.0
 */
interface ParseTreeNode {
  /**
   * Get the name.
   */
  String getParseTreeNodeName();

  /**
   * Get sub nodes.
   */
  List<ParseTreeNode> getChildren();

  /**
   * Add a sub node.
   */
  void addChild(ParseTreeNode child);

  /**
   * Remove the last child.
   */
  void removeLastChild();

  /**
   * On a successful match, this is the start index (inclusive). Default is null, which means no match.
   */
  Integer getMatchedStart();

  /**
   * On a successful match, this is the start index (inclusive). Default is null, which means no match.
   */
  void setMatchedStart(Integer matchedStart);

  /**
   * On a successful match, this is the end index (exclusive). Default is null, which means no match.
   */
  Integer getMatchedEnd();

  /**
   * On a successful match, this is the end index (exclusive). Default is null, which means no match.
   */
  void setMatchedEnd(Integer matchedEnd);

  /**
   * On a successful match, this is the matched string. Default is null, which means no match.
   */
  String getMatchedString();

  /**
   * On a successful match, this is the matched string. Default is null, which means no match.
   */
  void setMatchedString(String matchedString);

  /**
   * Set the parent parse tree node. null parent indicates this is a top level node.
   * This method is used when {@link org.codehaus.jparsec.ParseTreeNodeParser} was constructing
   * a parse tree.
   */
  void setParentParseTreeNode(ParseTreeNode parentParseTreeNode);

  /**
   * Get the parent parse tree node. null parent indicates this is a top level node.
   * This method is used when {@link org.codehaus.jparsec.ParseTreeNodeParser} was constructing
   * a parse tree.
   */
  ParseTreeNode getParent();
}
