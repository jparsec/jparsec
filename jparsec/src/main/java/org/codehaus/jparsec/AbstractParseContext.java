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

import org.codehaus.jparsec.annotations.Private;
import org.codehaus.jparsec.error.ParseErrorDetails;
import org.codehaus.jparsec.error.ParserException;
import org.codehaus.jparsec.util.Lists;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the context state during parsing.
 * 
 * @author Ben Yu
 */
abstract class AbstractParseContext implements ParseContext {

  private final String module;
  private final CharSequence source;
  private final SourceLocator locator;
  
  /** The current position of the input. Points to the token array for token level. */
  private int at;

  /** The current logical step. */
  private int step;

  /** The current parse result. */
  private Object result;

  /**
   * On a failed match, this records the end of the previous successful match.
   * On a successful match, this records the end of the match.
   */
  private int partialMatchedEnd;

  private ErrorType currentErrorType = ErrorType.NONE;
  private int currentErrorAt;
  private int currentErrorIndex = 0; // TODO: is it necessary to set this to the starting index?
  private final ArrayList<Object> errors = Lists.arrayList();
  private String encountered = null; // for explicitly setting encountered token into ScannerState.

  // explicit suppresses error recording if true.
  private boolean errorSuppressed = false;

  @Override
  public int getAt() {
    return at;
  }

  @Override
  public void setAt(int at) {
    this.at = at;
  }

  @Override
  public int getPartialMatchedEnd() {
    return partialMatchedEnd;
  }

  @Override
  public void setPartialMatchedEnd(int partialMatchedEnd) {
    this.partialMatchedEnd = partialMatchedEnd;
  }

  @Override
  public int getStep() {
    return step;
  }

  @Override
  public void setStep(int step) {
    this.step = step;
  }

  @Override
  public Object getResult() {
    return result;
  }

  @Override
  public void setResult(Object result) {
    this.result = result;
  }

  @Override
  public String getModule() {
    return module;
  }

  @Override
  public CharSequence getSource() {
    return source;
  }

  @Override
  public SourceLocator getLocator() {
    return locator;
  }

  /** Explicitly suppress or de-suppress error recording. */
  @Override
  public final boolean suppressError(boolean value) {
    boolean oldValue = errorSuppressed;
    errorSuppressed = value;
    return oldValue;
  }
  
  /** The physical index of the current most relevant error, {@code 0} if none. */
  @Override
  public final int errorIndex() {
    return currentErrorIndex;
  }

  /** The type of the current most relevant error. */
  @Override
  public final ErrorType errorType() {
    return currentErrorType;
  }
  
  /** The current most relevant error. {@code null} if none. */
  @Override
  public final List<Object> errors() {
    return errors;
  }
  
  /** Only called when rendering the error in {@link ParserException}. */
  @Override
  public final ParseErrorDetails renderError() {
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
    case EXPECT:
      return new EmptyParseError(errorIndex, encounteredName) {
        @Override public List<String> getExpected() {
          return errorStrings;
        }
      };
    }
    return new EmptyParseError(errorIndex, encounteredName);
  }

  @Override
  public final String getEncountered() {
    if (encountered != null) {
      return encountered;
    }
    return getInputName(currentErrorAt);
  }
  
  /**
   * Explicitly sets the encountered token,
   * which is from a nested {@link AbstractParseContext} instance.
   */
  @Override
  public final void setEncountered(String encountered) {
    this.encountered = encountered;
  }

  /** Returns the current index in the original source. */
  @Override
  public final int getIndex() {
    return toIndex(getAt());
  }

  @Private final void raise(ErrorType type, Object subject) {
    if (errorSuppressed) return;
    if (getAt() < currentErrorAt) return;
    if (getAt() > currentErrorAt) {
      setErrorState(getAt(), getIndex(), type);
      errors.add(subject);
      return;
    }
    // now error location is same
    if (type.ordinal() < currentErrorType.ordinal()) {
      return;
    }
    if (type.ordinal() > currentErrorType.ordinal()) {
      setErrorState(getAt(), getIndex(), type);
      errors.add(subject);
      return;
    }
    // now even error type is same
    if (type.mergeable) {
      // merge expected error.
      errors.add(subject);
    }
  }

  @Override
  public final void trap() {
    raise(ErrorType.TRAP, null);
  }

  @Override
  public final void fail(String message) {
    raise(ErrorType.FAILURE, message);
  }

  @Override
  public final void expected(Object what) {
    raise(ErrorType.EXPECTED, what);
  }

  @Override
  public final void unexpected(String what) {
    raise(ErrorType.UNEXPECTED, what);
  }

  @Override
  public final void set(int step, int at, Object ret) {
    this.setStep(step);
    this.setAt(at);
    this.setResult(ret);
  }

  @Override
  public final void setErrorState(
      int errorAt, int errorIndex, ErrorType errorType, List<Object> errors) {
    setErrorState(errorAt, errorIndex, errorType);
    this.errors.addAll(errors);
  }

  private void setErrorState(int errorAt, int errorIndex, ErrorType errorType) {
    this.currentErrorIndex = errorIndex;
    this.currentErrorAt = errorAt;
    this.currentErrorType = errorType;
    this.encountered = null;
    this.errors.clear();
  }

  @Override
  public final void setAt(int step, int at) {
    this.setStep(step);
    this.setAt(at);
  }

  @Override
  public final void next() {
    setAt(getAt() + 1);
    setStep(getStep() + 1);
  }

  @Override
  public final void next(int n) {
    setAt(getAt() + n);
    if (n > 0) setStep(getStep() + 1);
  }
  
  //caller should not change input after it is passed in.
  AbstractParseContext(CharSequence source, int at, String module, SourceLocator locator) {
    this(source, null, at, module, locator);
  }
  
  AbstractParseContext(
      CharSequence source, Object ret, int at, String module, SourceLocator locator) {
    this.source = source;
    this.setResult(ret);
    this.setStep(0);
    this.setAt(at);
    this.module = module;
    this.locator = locator;
    this.currentErrorAt = at;
  }

}
