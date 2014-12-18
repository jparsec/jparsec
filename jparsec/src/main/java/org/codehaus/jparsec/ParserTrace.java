package org.codehaus.jparsec;

/** Allows tracing of parsing progress during error condition, to ease debugging. */
interface ParserTrace {

  static final ParserTrace DISABLED = new ParserTrace() {
    @Override public void push(String name) {}
    @Override public void pop() {}
    @Override public TreeNode getCurrentNode() { return null; }
    @Override public void setCurrentResult(Object result) {}
    @Override public TreeNode getLatestChild() { return null; }
    @Override public void setLatestChild(TreeNode node) {}
    @Override public void startFresh(ParseContext context) {}
    @Override public void setStateAs(ParserTrace that) {}
  };

  /**
   * Upon applying a parser with {@link Parser#label}, the label name is used to create a new
   * child node under the current node. The new child node is set to be the current node.
   */
  void push(String name);

  /** When a parser finishes, the current node is popped so we are back to the parent parser. */
  void pop();

  /** Returns the current node, that is being parsed (not necessarily finished). */
  TreeNode getCurrentNode();

  /** Whenever a labeled parser succeeds, it calls this method to set its result in the trace. */
  void setCurrentResult(Object result);

  /**
   * Called by branching parsers, to save the current state of tree, before trying parsers that
   * could modify the tree state.
   */
  TreeNode getLatestChild();

  /**
   * Called by branching parsers, like or(), to reset the current child node, before the next
   * alternative parser is attempted.
   */
  void setLatestChild(TreeNode node);

  /** Called when tokenizer passes on to token-level parser. */
  void startFresh(ParseContext context);

  /**
   * Set the enclosing parser's tree state into the nested parser's state. Called for both nested
   * token-level parser and nested scanner.
   */
  void setStateAs(ParserTrace that);
}
