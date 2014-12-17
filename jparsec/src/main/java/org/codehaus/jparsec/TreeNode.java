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

  TreeNode orphanize() {
    if (parent == null) {
      // Root node is provided free, without an explicit asNode() call.
      // So there isn't a partially completed node.
      return this;
    }
    parent.latestChild = previous;
    return parent;
  }

  /**
   * Materializes the current tree node to make it the latest child of its parent
   * (discarding nodes that have been tacked on after it in the same hierarchy level); and
   * recursively apply to all of its ancestors.
   *
   * <p>This is because it's only called at time of error. If an ancestor node has a child node that
   * was added during the process of trying other alternatives and then failed, those paths don't
   * matter. So we should restore the tree back to when this most relevant error happened.
   *
   * <p>Returns the root node, which can then be used to {@link #toParseTree()}.
   */
  TreeNode materialize(int index) {
    TreeNode node = this;
    node.setEndIndex(index);
    while (node.parent != null) {
      node.parent.latestChild = node;
      node = node.parent;
      node.setEndIndex(index);
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

  @Override public String toString() {
    return name;
  }
}
