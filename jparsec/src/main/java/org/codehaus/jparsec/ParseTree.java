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
 * Represents a parse tree.
 *
 * @author Winter Young
 * @since 3.0
 */
public class ParseTree {
  /**
   * The root node.
   */
  private ParseTreeNode rootNode;

  public ParseTree(ParseTreeNode rootNode) {
    this.rootNode = rootNode;
  }

  /**
   * Format to json.
   */
  public String toJson() {
    StringBuilder sb = new StringBuilder();
    int indentLevel = 0;
    toJson(sb, rootNode, indentLevel, false);
    return sb.toString();
  }

  private void toJson(StringBuilder sb, ParseTreeNode node, int indentLevel, boolean appendDot) {
    line(sb, indentLevel, "{");

    List<ParseTreeNode> children = node.getChildren();
    if (node instanceof ParseTreeNodeStub || children.isEmpty()) {
      line(sb, indentLevel + 1, "matched: " + node.getMatchedString());
    } else {
      line(sb, indentLevel + 1, "name: ", node.getParseTreeNodeName(), ",");
      line(sb, indentLevel + 1, "children: {");
      int i = 0;
      for (ParseTreeNode child : children) {
        toJson(sb, child, indentLevel + 2, i < children.size() - 1);
        i++;
      }
      line(sb, indentLevel + 1, "}");
    }

    if (appendDot) {
      line(sb, indentLevel, "},");
    } else {
      line(sb, indentLevel, "}");
    }
  }

  private void line(StringBuilder sb, int indentLevel, String... parts) {
    for (int i = 0; i < indentLevel; i++) {
      sb.append("   ");
    }
    for (String part : parts) {
      sb.append(part);
    }
    sb.append("\n");
  }

}
