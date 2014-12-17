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

import static org.codehaus.jparsec.internal.util.Checks.checkState;

import org.codehaus.jparsec.error.ParserException;

/**
 * Parser state for scanner.
 * 
 * @author Ben Yu
 */
final class ScannerState extends ParseContext {
  private final int end;
  
  ScannerState(CharSequence source) {
    this(null, source, 0, new SourceLocator(source));
  }
  
  ScannerState(String module, CharSequence source, int from, SourceLocator locator) {
    super(source, from, module, locator);
    this.end = source.length();
  }
  
  /**
   * @param module the current module name for error reporting
   * @param source the source string
   * @param from from where do we start to scan?
   * @param end till where do we stop scanning? (exclusive)
   * @param locator the locator for mapping index to line and column number
   * @param originalResult the original result value
   */
  ScannerState(String module, CharSequence source, int from, int end,
      SourceLocator locator, Object originalResult) {
    super(source, originalResult, from, module, locator);
    this.end = end;
  }
  
  @Override char peekChar() {
    return source.charAt(at);
  }
  
  @Override boolean isEof() {
    return end == at;
  }
  
  @Override int toIndex(int pos) {
    return pos;
  }
  
  @Override String getInputName(int pos) {
    if (pos >= end) return EOF;
    return Character.toString(source.charAt(pos));
  }
  
  @Override CharSequence characters() {
    return source;
  }

  @Override Token getToken() {
    throw new IllegalStateException("Parser not on token level");
  }

  ScannerState enableTrace(final String rootName) {
    this.trace = new ParserTrace() {
        private TreeNode current = new TreeNode(rootName, getIndex());
    
        @Override public void push(String name) {
          TreeNode newChild = new TreeNode(name, getIndex());
          current.addChild(newChild);
          this.current = newChild;
        }
        @Override public void pop() {
          current.setEndIndex(getIndex());
          this.current = current.parent();
        }
        @Override public TreeNode getCurrentNode() {
          return current;
        }
        @Override public TreeNode getLatestChild() {
          return current.latestChild;
        }
        @Override public void setLatestChild(TreeNode latest) {
          checkState(latest == null || latest.parent() == current,
              "Trying to set a child node not owned by the parent node");
          current.latestChild = latest;
        }
        @Override public void setCurrentResult(Object result) {
          current.setResult(result);
        }
      };
    return this;
  }

  final <T> T run(Parser<T> parser) {
    if (!applyWithExceptionWrapped(parser)) {
      @SuppressWarnings("deprecation")
      ParserException exception =  new ParserException(
          renderError(), module, locator.locate(errorIndex()));
      exception.setParseTree(buildErrorParseTree());
      throw exception;
    }
    return parser.getReturn(this);
  }

  private boolean applyWithExceptionWrapped(Parser<?> parser) {
    try {
      return parser.apply(this);
    } catch (RuntimeException e) {
      if (e instanceof ParserException) throw (ParserException) e;
      @SuppressWarnings("deprecation")
      ParserException wrapper =
          new ParserException(e, null, module, locator.locate(getIndex()));
      // Use the successful parse tree because we are interrupted abruptly by an exception
      // So no need to take the "farthest error path".
      wrapper.setParseTree(buildParseTree());
      throw wrapper;
    }
  }
}
