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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.codehaus.jparsec.error.ParseErrorDetails;
import org.codehaus.jparsec.error.ParserException;
import org.codehaus.jparsec.internal.annotations.Private;
import org.codehaus.jparsec.internal.util.Lists;

/**
 * Represents the context state during parsing.
 * 
 * @author Ben Yu
 */
abstract class ParseContext {
  
  static final String EOF = "EOF";
  
  final String module;
  final CharSequence source;
  final SourceLocator locator;
  
  /** The current position of the input. Points to the token array for token level. */
  int at;
  
  /** The current logical step. */
  int step;
  
  /** The current parse result. */
  Object result;

  ParserTrace trace = ParserTrace.DISABLED;
  
  enum ErrorType {
    
    /** Default value, no error. */
    NONE(false),
    
    /** When {@link Parser#not()} is called. Signals that something isn't expected. */
    UNEXPECTED(false),
    
    /** When any expected input isn't found. */
    MISSING(true),
    
    /** When {@link Parser#label()} is called. Signals that a logical stuff isn't found. */
    EXPECTED(true),
    
    /** When {@link Parsers#fail(String)} is called. Signals a serious problem. */
    FAILURE(false);
    
    ErrorType(boolean mergeable) {
      this.mergeable = mergeable;
    }
    
    final boolean mergeable;
  }
  
  private ErrorType currentErrorType = ErrorType.NONE;
  private int currentErrorAt;
  private int currentErrorIndex = 0; // TODO: is it necessary to set this to the starting index?
  private final ArrayList<Object> errors = Lists.arrayList();
  private String encountered = null; // for explicitly setting encountered token into ScannerState.
  private TreeNode currentErrorNode = null;
  
  // explicit suppresses error recording if true.
  private boolean errorSuppressed = false;
  
  //caller should not change input after it is passed in.
  ParseContext(CharSequence source, int at, String module, SourceLocator locator) {
    this(source, null, at, module, locator);
  }
  
  ParseContext(
      CharSequence source, Object ret, int at, String module, SourceLocator locator) {
    this.source = source;
    this.result = ret;
    this.step = 0;
    this.at = at;
    this.module = module;
    this.locator = locator;
    this.currentErrorAt = at;
  }
  
  /** Explicitly suppress or de-suppress error recording. */
  final boolean suppressError(boolean value) {
    boolean oldValue = errorSuppressed;
    errorSuppressed = value;
    return oldValue;
  }
  
  /** The physical index of the current most relevant error, {@code 0} if none. */
  final int errorIndex() {
    return currentErrorIndex;
  }

  final void traceCurrentResult() {
    trace.setCurrentResult(result);
  }

  final ParseTree buildParseTree() {
    return toCompletedParseTree(trace.getCurrentNode());
  }

  final ParseTree buildErrorParseTree() {
    return toCompletedParseTree(currentErrorNode);
  }

  private ParseTree toCompletedParseTree(TreeNode node) {
    // The current node is partially done because there was an error.
    // So orphanize it. But at the same time, all ancestor nodes should have their endIndex set to
    // where we are now.
    return (node == null) ? null : node.orphanize().materialize(getIndex()).toParseTree();
  }
  
  /** Only called when rendering the error in {@link ParserException}. */
  final ParseErrorDetails renderError() {
    final int errorIndex = toIndex(currentErrorAt);
    final String encounteredName = getEncountered();
    final ArrayList<String> errorStrings = Lists.arrayList(errors.size());
    for (Object error : errors) {
      errorStrings.add(String.valueOf(error));
    }
    switch (currentErrorType) {
    case UNEXPECTED :
      return new EmptyParseError(errorIndex, encounteredName) {
        @Override public String getUnexpected() {
          return errorStrings.get(0);
        }
      };
    case FAILURE :
      return new EmptyParseError(errorIndex, encounteredName) {
        @Override public String getFailureMessage() {
          return errorStrings.get(0);
        }
      };
    case EXPECTED:
    case MISSING:
      return new EmptyParseError(errorIndex, encounteredName) {
        @Override public List<String> getExpected() {
          return errorStrings;
        }
      };
    default:
      return new EmptyParseError(errorIndex, encounteredName);
    }
  }

  private String getEncountered() {
    if (encountered != null) {
      return encountered;
    }
    return getInputName(currentErrorAt);
  }
  
  /** Returns the string representation of the current input (character or token). */
  abstract String getInputName(int pos);

  abstract boolean isEof();
  
  /** Returns the current index in the original source. */
  final int getIndex() {
    return toIndex(at);
  }
  
  /** Returns the current token. Only applicable to token level parser. */
  abstract Token getToken();
  
  /** Peeks the current character. Only applicable to character level parser. */
  abstract char peekChar();
  
  /** Translates the logical position to physical index in the original source. */
  abstract int toIndex(int pos);
  
  @Private final void raise(ErrorType type, Object subject) {
    if (errorSuppressed) return;
    if (at < currentErrorAt) return;
    if (at > currentErrorAt) {
      setErrorState(at, getIndex(), type);
      errors.add(subject);
      return;
    }
    // now error location is same
    if (type.ordinal() < currentErrorType.ordinal()) {
      return;
    }
    if (type.ordinal() > currentErrorType.ordinal()) {
      setErrorState(at, getIndex(), type);
      errors.add(subject);
      return;
    }
    // now even error type is same
    if (type.mergeable) {
      // merge expected error.
      errors.add(subject);
    }
  }
  
  final void fail(String message) {
    raise(ErrorType.FAILURE, message);
  }
  
  final void missing(Object what) {
    raise(ErrorType.MISSING, what);
  }
  
  final void expected(Object what) {
    raise(ErrorType.EXPECTED, what);
  }
  
  final void unexpected(String what) {
    raise(ErrorType.UNEXPECTED, what);
  }

  final boolean repeat(Parser<?> parser, int n) {
    for (int i = 0; i < n; i++) {
      if (!parser.apply(this)) return false;
    }
    return true;
  }

  final <T> boolean repeat(
      Parser<? extends T> parser, int n, Collection<T> collection) {
    for (int i = 0; i < n; i++) {
      if (!parser.apply(this)) return false;
      collection.add(parser.getReturn(this));
    }
    return true;
  }

  final boolean applyNested(Parser<?> parser, ParseContext nestedState) {
    if (parser.apply(nestedState))  {
      set(nestedState.step, at, nestedState.result);
      return true;
    }
    // index on token level is the "at" on character level
    set(step, nestedState.getIndex(), null);
    
    // always copy error because there could be false alarms in the character level.
    // For example, a "or" parser nested in a "many" failed in one of its branches.
    copyErrorFrom(nestedState);
    return false;
  }

  final boolean stillThere(int wasAt, int originalStep) {
    if (step == originalStep) {
      // logical step didn't change, so logically we are still there, undo any physical offset
      setAt(originalStep, wasAt);
      return true;
    }
    return false;
  }
  
  final void set(int step, int at, Object ret, TreeNode latestChild) {
    set(step, at, ret);
    trace.setLatestChild(latestChild);
  }
  
  final void set(int step, int at, Object ret) {
    this.step = step;
    this.at = at;
    this.result = ret;
  }
  
  final void setAt(int step, int at) {
    this.step = step;
    this.at = at;
  }
  
  final void next() {
    at ++;
    step ++;
  }
  
  final void next(int n) {
    at += n;
    if (n > 0) step++;
  }
  
  private void setErrorState(
      int errorAt, int errorIndex, ErrorType errorType, List<Object> errors) {
    setErrorState(errorAt, errorIndex, errorType);
    this.errors.addAll(errors);
  }

  private void setErrorState(int errorAt, int errorIndex, ErrorType errorType) {
    this.currentErrorIndex = errorIndex;
    this.currentErrorAt = errorAt;
    this.currentErrorType = errorType;
    this.currentErrorNode = trace.getCurrentNode();
    this.encountered = null;
    this.errors.clear();
  }

  private void copyErrorFrom(ParseContext that) {
    int errorIndex = that.errorIndex();
    setErrorState(errorIndex, errorIndex, that.currentErrorType, that.errors);
    if (!that.isEof()) {
      this.encountered = that.getEncountered();
    }
    currentErrorNode = that.currentErrorNode;
  }

  /** Reads the characters as input. Only applicable to character level parsers. */
  abstract CharSequence characters();
}