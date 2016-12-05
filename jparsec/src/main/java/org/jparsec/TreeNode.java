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

import org.jparsec.internal.util.Checks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.jparsec.internal.util.Checks.checkArgument;
import static org.jparsec.internal.util.Checks.checkState;

/**
 * A TreeNode remembers it's parent (which corresponds to a parent parser that syntactically
 * encloses this parter), it's previous node (which is the parser at the same syntactical level
 * and had just <em>succeeded</em> before this parser started). It also keeps all the children.
 * 
 * <p>Once constructed, a node's 'parent' and 'previous' references are immutable.
 * The list of children nodes however can change. When the alternative parsers
 * in an {@code or} parser are attempted one after another, they each generate new child node of
 * the parent node. These "alternative" nodes all point to the same parent and same "previous" node.
 * 
 * <p>When exception is to be thrown, the most relevant error is picked, along with the tree node
 * that was recorded at time of {@link ParseContext#raise}. That tree node is then
 * {@link #freeze frozen} by setting its parent's {@link #latestChild} to this error
 * node's "previous" successful node, and that of its grandparent's to its parent node, all the way
 * up to the root. This essentially freezes and collapse the "multi universes" into a single error
 * state, with all other "potential" error state destroyed and forgotten.
 */
final class TreeNode {

  private final String name;
  private final int beginIndex;
  private final TreeNode parent;
  private final TreeNode previous;
  private int endIndex = 0;
  private Object result = null;
  TreeNode latestChild = null;

  TreeNode(String name, int beginIndex) {
    this.name = name;
    this.beginIndex = beginIndex;
    this.parent = null;
    this.previous = null;
  }

  TreeNode(String name, int beginIndex, TreeNode parent, TreeNode previous) {
    this.name = name;
    this.beginIndex = beginIndex;
    this.parent = parent;
    this.previous = previous;
  }

  void setEndIndex(int index) {
    Checks.checkArgument(index >= beginIndex, "endIndex < beginIndex");
    endIndex = index;
  }

  void setResult(Object result) {
    this.result = result;
  }

  TreeNode parent() {
    Checks.checkState(parent != null, "Root node has no parent");
    return parent;
  }

  TreeNode addChild(String childName, int childIndex) {
    TreeNode child = new TreeNode(childName, childIndex, this, latestChild);
    this.latestChild = child;
    return child;
  }

  /**
   * When this leaf node has errors, it didn't complete and shouldn't be part of the parse tree
   * that is the current partial parse result with all successful matches.
   * In that case, return the parent node, by setting its {@link #latestChild} to {@link #previous}.
   */
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
   * Freezes the current tree node to make it the latest child of its parent
   * (discarding nodes that have been tacked on after it in the same hierarchy level); and
   * recursively apply to all of its ancestors.
   *
   * <p>This is because it's only called at time of error. If an ancestor node has a child node that
   * was added during the process of trying other alternatives and then failed, those paths don't
   * matter. So we should restore the tree back to when this most relevant error happened.
   *
   * <p>Returns the root node, which can then be used to {@link #toParseTree()}.
   */
  TreeNode freeze(int index) {
    TreeNode node = this;
    node.setEndIndex(index);
    while (node.parent != null) {
      node.parent.latestChild = node;
      node = node.parent;
      node.setEndIndex(index);
    }
    return node;
  }

  /** Converts this node into a {@link ParseTree} representation. */
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
