package org.codehaus.jparsec;

import static org.codehaus.jparsec.internal.util.Checks.checkArgument;
import static org.codehaus.jparsec.internal.util.Checks.checkState;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final class TreeNode {

  private final String name;
  private final int beginIndex;
  private TreeNode parent;
  private TreeNode previous;
  private int endIndex = 0;
  private Object result = null;
  TreeNode latestChild = null;

  TreeNode(String name, int beginIndex) {
    this.name = name;
    this.beginIndex = beginIndex;
  }

  void setEndIndex(int index) {
    checkArgument(index >= beginIndex, "endIndex < beginIndex");
    endIndex = index;
  }

  void setResult(Object result) {
    this.result = result;
  }

  TreeNode parent() {
    checkState(parent != null, "Root node has no parent");
    return parent;
  }

  void addChild(TreeNode child) {
    child.previous = latestChild;
    child.parent = this;
    this.latestChild = child;
  }

  TreeNode materialize() {
    TreeNode node = this;
    while (node.parent != null) {
      node.parent.latestChild = node;
      node = node.parent;
    }
    return node;
  }

  ParseTree toParseTree() {
    List<ParseTree> children = new ArrayList<ParseTree>();
    for (TreeNode child = latestChild; child != null; child = child.previous) {
      children.add(child.toParseTree());
    }
    Collections.reverse(children);
    return new ParseTree(name, beginIndex, endIndex, result, children);
  }
}
