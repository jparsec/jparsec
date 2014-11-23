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

import org.codehaus.jparsec.error.ParseErrorDetails;
import org.codehaus.jparsec.util.Lists;

import java.util.List;

/**
 * A {@link org.codehaus.jparsec.ParseContext} that are used when constructing a parse tree.
 *
 * @author Winter Young
 */
class ParseTreeContext implements ParseContext {
  /**
   * The delegated parse context.
   */
  private ParseContext ctxt;
  /**
   * The current parse tree node.
   */
  private ParseTreeNode parseTreeNode;

  public ParseTreeContext(ParseContext ctxt) {
    this.ctxt = ctxt;
  }

  public ParseContext getCtxt() {
    return ctxt;
  }

  public void setCtxt(ParseContext ctxt) {
    this.ctxt = ctxt;
  }

  public ParseTree createParseTree() {
    bottomUpWalkParseTree(getParseTreeNode());
    topDownWalkParseTree(getParseTreeNode(), true);
    return new ParseTree(getParseTreeNode());
  }

  private Integer bottomUpWalkParseTree(ParseTreeNode node) {
    List<ParseTreeNode> children = node.getChildren();
    Integer end = node.getMatchedEnd();
    if (!children.isEmpty()) {
      for (ParseTreeNode child : children) {
        Integer childEnd = bottomUpWalkParseTree(child);
        if (end == null || end < childEnd) {
          end = childEnd;
        }
      }
      node.setMatchedEnd(end);
    }
    return end;
  }

  private void topDownWalkParseTree(ParseTreeNode node, boolean root) {
    List<ParseTreeNode> children = node.getChildren();
    fillStubsToChildren(node, children);
    if (root) {
      fillStubToRoot(node, children);
    }

    if (children.isEmpty()) {
      fillNodeString(node);
    } else {
      for (ParseTreeNode child : children) {
        topDownWalkParseTree(child, false);
      }
    }
  }

  private void fillStubToRoot(ParseTreeNode node, List<ParseTreeNode> children) {
    Integer end = node.getMatchedEnd();
    if (getPartialMatchedEnd() > end) {
      fillStubToRoot(node, end);
    } else if (getPartialMatchedEnd() == end && children.size() > 0) {
      ParseTreeNode lastChild = children.get(children.size() - 1);
      end = lastChild.getMatchedEnd();
      if (getPartialMatchedEnd() > end) {
        fillStubToRoot(node, end);
      }
    }
  }

  private void fillStubToRoot(ParseTreeNode node, Integer end) {
    ParseTreeNodeStub stub = new ParseTreeNodeStub();
    stub.setMatchedStart(end);
    stub.setMatchedEnd(getPartialMatchedEnd());
    node.addChild(stub);
  }

  private void fillNodeString(ParseTreeNode node) {
    Integer start = node.getMatchedStart();
    Integer end = node.getMatchedEnd();

    if (end > start) {
      String str = getSource().subSequence(start, end).toString();
      node.setMatchedString(str);
    }
  }

  private void fillStubsToChildren(ParseTreeNode node, List<ParseTreeNode> children) {
    List<ParseTreeNode> newChildren = Lists.arrayList();

    Integer start = node.getMatchedStart();
    for (ParseTreeNode child : children) {
      Integer childStart = child.getMatchedStart();

      if (childStart > start) {
        ParseTreeNodeStub stub = new ParseTreeNodeStub();
        stub.setMatchedStart(start);
        stub.setMatchedEnd(childStart);
        newChildren.add(stub);
      }
      newChildren.add(child);

      start = child.getMatchedEnd();
    }

    if (getPartialMatchedEnd() > start
        && getPartialMatchedEnd() < node.getMatchedEnd()
        && !(node instanceof ParseTreeNodeStub)) {
      ParseTreeNodeStub stub = new ParseTreeNodeStub();
      stub.setMatchedStart(start);
      stub.setMatchedEnd(getPartialMatchedEnd());
      newChildren.add(stub);
    }

    children.clear();
    children.addAll(newChildren);
  }

  @Override
  public void setAt(int at) {
    if (at < getAt()) {
      // when backtrack happens remove the last child.
      ParseTreeNode node = getParseTreeNode();
      node.removeLastChild();
    }

    ctxt.setAt(at);
  }

  private boolean between(int index, Integer start, Integer end) {
    return start != null
        && start <= index
        && (end == null || end > index);
  }

  @Override
  public int getAt() {
    return ctxt.getAt();
  }

  @Override
  public int getPartialMatchedEnd() {
    return ctxt.getPartialMatchedEnd();
  }

  @Override
  public void setPartialMatchedEnd(int partialMatchedEnd) {
    ctxt.setPartialMatchedEnd(partialMatchedEnd);
  }

  @Override
  public int getStep() {
    return ctxt.getStep();
  }

  @Override
  public void setStep(int step) {
    ctxt.setStep(step);
  }

  @Override
  public Object getResult() {
    return ctxt.getResult();
  }

  @Override
  public void setResult(Object result) {
    ctxt.setResult(result);
  }

  public ParseTreeNode getParseTreeNode() {
    return parseTreeNode;
  }

  public void setParseTreeNode(ParseTreeNode parseTreeNode) {
    this.parseTreeNode = parseTreeNode;
  }

  @Override
  public String getModule() {
    return ctxt.getModule();
  }

  @Override
  public CharSequence getSource() {
    return ctxt.getSource();
  }

  @Override
  public SourceLocator getLocator() {
    return ctxt.getLocator();
  }

  @Override
  public boolean suppressError(boolean value) {
    return ctxt.suppressError(value);
  }

  @Override
  public int errorIndex() {
    return ctxt.errorIndex();
  }

  @Override
  public ErrorType errorType() {
    return ctxt.errorType();
  }

  @Override
  public List<Object> errors() {
    return ctxt.errors();
  }

  @Override
  public ParseErrorDetails renderError() {
    return ctxt.renderError();
  }

  @Override
  public String getEncountered() {
    return ctxt.getEncountered();
  }

  @Override
  public void setEncountered(String encountered) {
    ctxt.setEncountered(encountered);
  }

  @Override
  public String getInputName(int pos) {
    return ctxt.getInputName(pos);
  }

  @Override
  public boolean isEof() {
    return ctxt.isEof();
  }

  @Override
  public int getIndex() {
    return ctxt.getIndex();
  }

  @Override
  public Token getToken() {
    return ctxt.getToken();
  }

  @Override
  public char peekChar() {
    return ctxt.peekChar();
  }

  @Override
  public int toIndex(int pos) {
    return ctxt.toIndex(pos);
  }

  @Override
  public void trap() {
    ctxt.trap();
  }

  @Override
  public void fail(String message) {
    ctxt.fail(message);
  }

  @Override
  public void expected(Object what) {
    ctxt.expected(what);
  }

  @Override
  public void unexpected(String what) {
    ctxt.unexpected(what);
  }

  @Override
  public void set(int step, int at, Object ret) {
    this.setStep(step);
    this.setAt(at);
    this.setResult(ret);
  }

  @Override
  public void setErrorState(int errorAt, int errorIndex, ErrorType errorType, List<Object> errors) {
    ctxt.setErrorState(errorAt, errorIndex, errorType, errors);
  }

  @Override
  public void setAt(int step, int at) {
    this.setStep(step);
    this.setAt(at);
  }

  @Override
  public void next() {
    ctxt.next();
  }

  @Override
  public void next(int n) {
    ctxt.next(n);
  }

  @Override
  public CharSequence characters() {
    return ctxt.characters();
  }
}
