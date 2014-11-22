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

import org.codehaus.jparsec.util.Strings;

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
    if (node instanceof ParseTreeNodeStub) {
      String dot = appendDot ? "," : "";
      line(sb, indentLevel, "\"", escapeJsonStr(node.getMatchedString()), "\"", dot);
    } else {
      line(sb, indentLevel, "{");

      List<ParseTreeNode> children = node.getChildren();
      if (children.isEmpty()) {
        line(sb, indentLevel + 1, "name: \"", escapeJsonStr(node.getParseTreeNodeName()), "\",");
        line(sb, indentLevel + 1, "matched: \"", escapeJsonStr(node.getMatchedString()), "\"");
      } else {
        line(sb, indentLevel + 1, "name: \"", escapeJsonStr(node.getParseTreeNodeName()), "\",");
        line(sb, indentLevel + 1, "children: [");
        int i = 0;
        for (ParseTreeNode child : children) {
          toJson(sb, child, indentLevel + 2, i < children.size() - 1);
          i++;
        }
        line(sb, indentLevel + 1, "]");
      }

      if (appendDot) {
        line(sb, indentLevel, "},");
      } else {
        line(sb, indentLevel, "}");
      }
    }
  }

  private String escapeJsonStr(String str) {
    // In order to deal with east-asian characters correctly,
    // write the conversion code manually.
    // Refer to http://www.json.org/ for the spec.
    str = Strings.replace(str, "\"", "\\\"");
    str = Strings.replace(str, "\\", "\\\\");
    str = Strings.replace(str, "/", "\\/");
    str = Strings.replace(str, "\b", "\\b");
    str = Strings.replace(str, "\f", "\\f");
    str = Strings.replace(str, "\n", "\\n");
    str = Strings.replace(str, "\r", "\\r");
    str = Strings.replace(str, "\t", "\\t");
    return str;
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
