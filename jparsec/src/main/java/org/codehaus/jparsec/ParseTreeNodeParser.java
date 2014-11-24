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
 * A parser that implements {@link org.codehaus.jparsec.ParseTreeNode}.
 *
 * @author Winter Young
 * @since 3.0
 */
class ParseTreeNodeParser<T> extends Parser<T> implements ParseTreeNode {
  private Parser<T> parser;
  private String name;
  private ImmutableList<ParseTreeNode> reverseChildren = ImmutableList.empty();
  private Integer matchedStart;
  private Integer matchedEnd;
  private String matchedString;

  public ParseTreeNodeParser(Parser<T> parser, String name) {
    this.parser = parser;
    this.name = name;
  }

  @Override
  boolean apply(ParseContext ctxt) {
    if (ctxt instanceof ParseTreeContext) {
      ParseTreeContext parseTreeContext = (ParseTreeContext) ctxt;
      return apply(parseTreeContext);
    } else {
      return parser.run(ctxt);
    }
  }

  private boolean apply(ParseTreeContext ctxt) {
    ParseTreeNode originalNode = ctxt.getParseTreeNode();
    ParseTreeNode currentNode = createCleanCopy(this);

    if (originalNode != null) {
      originalNode.addChild(currentNode);
    }
    ctxt.setParseTreeNode(currentNode);

    currentNode.setMatchedStart(ctxt.getIndex());
    boolean ok = parser.run(ctxt);
    if (ok) {
      currentNode.setMatchedEnd(ctxt.getIndex());
    } else if (currentNode.getReverseChildren().isEmpty()) {
      Integer end = currentNode.getMatchedStart();
      if (end == null || end < ctxt.getPartialMatchedEnd()) {
        end = ctxt.getPartialMatchedEnd();
      }
      currentNode.setMatchedEnd(end);
    }

    if (originalNode == null) {
      ctxt.setParseTreeNode(currentNode);
    } else {
      ctxt.setParseTreeNode(originalNode);
    }
    return ok;
  }

  @Override
  public String getParseTreeNodeName() {
    return name;
  }

  @Override
  public ImmutableList<ParseTreeNode> getChildren() {
    return reverseChildren.reverse();
  }

  @Override
  public ImmutableList<ParseTreeNode> getReverseChildren() {
    return reverseChildren;
  }

  @Override
  public boolean hasChildren() {
    return reverseChildren.isEmpty();
  }

  @Override
  public void addChild(ParseTreeNode child) {
    reverseChildren = reverseChildren.insert(child);
  }

  @Override
  public void removeLastChild() {
    reverseChildren = reverseChildren.tail();
  }

  @Override
  public void resetChildren(ImmutableList<ParseTreeNode> children) {
    this.reverseChildren = children;
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

  Parser<T> getParser() {
    return parser;
  }

  @Override
  public String toString() {
    return "ParseTreeNodeParser{" +
        "name='" + name + '\'' +
        ", matchedStart=" + matchedStart +
        ", matchedEnd=" + matchedEnd +
        ", matchedString='" + matchedString + '\'' +
        '}';
  }

  /**
   * Creates a clean copy with only the underlying parser and name.
   */
  private static <T> ParseTreeNodeParser<T> createCleanCopy(ParseTreeNodeParser<T> nodeParser) {
    return new ParseTreeNodeParser<T>(nodeParser.getParser(), nodeParser.getParseTreeNodeName());
  }
}
